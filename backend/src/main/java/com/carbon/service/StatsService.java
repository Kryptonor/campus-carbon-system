package com.carbon.service;

import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.dao.UserRepository;
import com.carbon.dto.BehaviorTypeStats;
import com.carbon.dto.DailyTrendStats;
import com.carbon.dto.LeaderboardEntry;
import com.carbon.dto.SummaryStats;
import com.carbon.dto.HomeDailySummary;
import com.carbon.dto.LeaderboardResponse;
import com.carbon.dto.LeaderboardRankItem;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final BehaviorRecordRepository behaviorRecordRepository;
    private final UserRepository userRepository;
    private final SystemConfigService systemConfigService;

    public StatsService(BehaviorRecordRepository behaviorRecordRepository,
                        UserRepository userRepository,
                        SystemConfigService systemConfigService) {
        this.behaviorRecordRepository = behaviorRecordRepository;
        this.userRepository = userRepository;
        this.systemConfigService = systemConfigService;
    }

    private double getExchangeRate() {
        return systemConfigService.getDouble("carbon.exchange.rate", 0.10);
    }

    public SummaryStats getSummary() {
        List<BehaviorRecord> records = behaviorRecordRepository.findByDecision("PASS");
        long totalPoints = records.stream().mapToLong(BehaviorRecord::getPoints).sum();
        double totalCarbonReduction = totalPoints * getExchangeRate();
        long participantCount = records.size();

        return new SummaryStats(totalCarbonReduction, totalPoints, participantCount);
    }

    public List<BehaviorTypeStats> getBehaviorTypeStats() {
        List<BehaviorRecord> records = behaviorRecordRepository.findByDecision("PASS");
        double rate = getExchangeRate();

        Map<String, List<BehaviorRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(BehaviorRecord::getBehaviorType));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String type = entry.getKey();
                    long count = entry.getValue().size();
                    long points = entry.getValue().stream().mapToLong(BehaviorRecord::getPoints).sum();
                    double carbon = points * rate;
                    return new BehaviorTypeStats(type, count, points, carbon);
                })
                .sorted(Comparator.comparingLong(BehaviorTypeStats::totalPoints).reversed())
                .collect(Collectors.toList());
    }

    public List<DailyTrendStats> getDailyTrendStats(int days) {
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<BehaviorRecord> records = behaviorRecordRepository.findByDecisionAndCreatedAtAfter("PASS", startDateTime);
        double rate = getExchangeRate();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 按日期（yyyy-MM-dd）分组
        Map<String, List<BehaviorRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().format(dtf)));

        List<DailyTrendStats> trend = new ArrayList<>();
        // 保证填充最近几天，即使当天没有数据也显示 0
        for (int i = days - 1; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).format(dtf);
            List<BehaviorRecord> dayRecords = grouped.getOrDefault(dateStr, Collections.emptyList());
            long count = dayRecords.size();
            long points = dayRecords.stream().mapToLong(BehaviorRecord::getPoints).sum();
            double carbon = points * rate;
            trend.add(new DailyTrendStats(dateStr, count, points, carbon));
        }

        return trend;
    }

    public List<LeaderboardEntry> getLeaderboard(int limit) {
        double rate = getExchangeRate();
        // 按积分余额排行，排除管理员
        List<User> topUsers = userRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "pointsBalance"))
        ).getContent();

        return topUsers.stream()
                .map(u -> new LeaderboardEntry(
                        u.getId(),
                        u.getStudentNo(),
                        u.getName(),
                        u.getAvatarUrl(),
                        u.getPointsBalance(),
                        u.getPointsBalance() * rate
                ))
                .collect(Collectors.toList());
    }

    public LeaderboardResponse getLeaderboardList(Long userId, String type, String scope, int page, int pageSize) {
        double rate = getExchangeRate();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if ("class".equalsIgnoreCase(scope)) {
            // 班级人均排行，排除管理员
            List<User> allUsers = userRepository.findAll();
            // 按班级分组，过滤掉班级为空的用户
            Map<String, List<User>> classGroup = allUsers.stream()
                    .filter(u -> u.getClassName() != null && !u.getClassName().isBlank())
                    .collect(Collectors.groupingBy(User::getClassName));

            List<LeaderboardRankItem> classItems = new ArrayList<>();
            for (Map.Entry<String, List<User>> entry : classGroup.entrySet()) {
                String className = entry.getKey();
                List<User> members = entry.getValue();
                long totalPoints = members.stream().mapToLong(User::getPointsBalance).sum();
                long avgScore = totalPoints / members.size();
                double avgCarbon = (totalPoints * rate) / members.size();
                // 用班级里任意一个成员的学院作为展示学院
                String dept = members.isEmpty() ? "" : members.get(0).getDepartment();
                classItems.add(new LeaderboardRankItem(0, className, dept, avgScore, avgCarbon));
            }

            // 按照平均分降序排序
            classItems.sort((c1, c2) -> Long.compare(c2.score(), c1.score()));

            // 赋 Rank 名次
            for (int i = 0; i < classItems.size(); i++) {
                LeaderboardRankItem item = classItems.get(i);
                classItems.set(i, new LeaderboardRankItem(i + 1, item.name(), item.department(), item.score(), item.carbonReduced()));
            }

            // 计算当前用户的班级名次和分
            int myRank = 0;
            long myScore = 0;
            String myClassName = currentUser.getClassName();
            if (myClassName != null && !myClassName.isBlank()) {
                for (LeaderboardRankItem item : classItems) {
                    if (item.name().equalsIgnoreCase(myClassName)) {
                        myRank = item.rank();
                        myScore = item.score();
                        break;
                    }
                }
            }

            // 内存分页
            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, classItems.size());
            List<LeaderboardRankItem> paged = Collections.emptyList();
            if (fromIndex < classItems.size()) {
                paged = classItems.subList(fromIndex, toIndex);
            }

            return new LeaderboardResponse(paged, myRank, myScore);
        } else {
            // 全校个人排行
            List<User> allUsers = userRepository.findAll();
            allUsers.sort((u1, u2) -> Long.compare(u2.getPointsBalance(), u1.getPointsBalance()));

            List<LeaderboardRankItem> items = new ArrayList<>();
            for (int i = 0; i < allUsers.size(); i++) {
                User u = allUsers.get(i);
                items.add(new LeaderboardRankItem(
                        i + 1,
                        u.getName(),
                        u.getDepartment(),
                        u.getPointsBalance(),
                        u.getPointsBalance() * rate
                ));
            }

            int myRank = 0;
            long myScore = currentUser.getPointsBalance();
            for (LeaderboardRankItem item : items) {
                if (item.name().equalsIgnoreCase(currentUser.getName()) && item.department().equalsIgnoreCase(currentUser.getDepartment())) {
                    myRank = item.rank();
                    break;
                }
            }
            if (myRank == 0) {
                // 后备安全匹配方式
                for (int i = 0; i < allUsers.size(); i++) {
                    if (allUsers.get(i).getId().equals(userId)) {
                        myRank = i + 1;
                        break;
                    }
                }
            }

            // 分页
            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, items.size());
            List<LeaderboardRankItem> paged = Collections.emptyList();
            if (fromIndex < items.size()) {
                paged = items.subList(fromIndex, toIndex);
            }

            return new LeaderboardResponse(paged, myRank, myScore);
        }
    }

    public HomeDailySummary getHomeDailySummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        double rate = getExchangeRate();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayEnd.minusDays(1);

        // 今日碳和积分
        List<BehaviorRecord> todayRecords = behaviorRecordRepository.findByUserIdAndDecisionAndCreatedAtBetween(userId, "PASS", todayStart, todayEnd);
        long todayPoints = todayRecords.stream().mapToLong(BehaviorRecord::getPoints).sum();
        double todayCarbon = todayPoints * rate;

        // 昨日碳
        List<BehaviorRecord> yesterdayRecords = behaviorRecordRepository.findByUserIdAndDecisionAndCreatedAtBetween(userId, "PASS", yesterdayStart, yesterdayEnd);
        long yesterdayPoints = yesterdayRecords.stream().mapToLong(BehaviorRecord::getPoints).sum();
        double yesterdayCarbon = yesterdayPoints * rate;

        double carbonChange = todayCarbon - yesterdayCarbon;

        // 本周数据（从周一开始到现在）
        LocalDateTime startOfWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<BehaviorRecord> weeklyRecords = behaviorRecordRepository.findByUserIdAndDecisionAndCreatedAtBetween(userId, "PASS", startOfWeek, todayEnd);
        long weeklyPoints = weeklyRecords.stream().mapToLong(BehaviorRecord::getPoints).sum();
        double weeklyCarbon = weeklyPoints * rate;

        // 上周同期数据（用于变化量）
        LocalDateTime startOfLastWeek = startOfWeek.minusWeeks(1);
        LocalDateTime endOfLastWeekSameDay = now.minusWeeks(1);
        List<BehaviorRecord> lastWeeklyRecords = behaviorRecordRepository.findByUserIdAndDecisionAndCreatedAtBetween(userId, "PASS", startOfLastWeek, endOfLastWeekSameDay);
        long lastWeeklyPoints = lastWeeklyRecords.stream().mapToLong(BehaviorRecord::getPoints).sum();
        double lastWeeklyCarbon = lastWeeklyPoints * rate;

        double weeklyChange = weeklyCarbon - lastWeeklyCarbon;

        // 目标进度
        double goalCarbon = 50.0; // 默认碳减排目标
        double goalProgress = Math.min(100.0, (weeklyCarbon / goalCarbon) * 100);

        // 学院排名（排除管理员）
        String dept = user.getDepartment();
        long collegeRank = 1;
        long collegeTotal = 1;
        if (dept != null && !dept.isBlank()) {
            List<User> collegeUsers = userRepository.findByDepartment(dept);
            // 按照 pointsBalance 降序
            collegeUsers.sort((u1, u2) -> Long.compare(u2.getPointsBalance(), u1.getPointsBalance()));
            collegeTotal = collegeUsers.size();
            for (int i = 0; i < collegeUsers.size(); i++) {
                if (collegeUsers.get(i).getId().equals(userId)) {
                    collegeRank = i + 1;
                    break;
                }
            }
        } else {
            // 如果部门为空，按全局排名算
            List<User> allUsers = userRepository.findAll();
            allUsers.sort((u1, u2) -> Long.compare(u2.getPointsBalance(), u1.getPointsBalance()));
            collegeTotal = allUsers.size();
            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).getId().equals(userId)) {
                    collegeRank = i + 1;
                    break;
                }
            }
        }

        return new HomeDailySummary(
                todayCarbon,
                yesterdayCarbon,
                carbonChange,
                todayPoints,
                user.getPointsBalance(),
                weeklyCarbon,
                weeklyChange,
                goalCarbon,
                goalProgress,
                collegeRank,
                collegeTotal
        );
    }

    /**
     * 班级人均排行 — 返回前端兼容格式。
     * 前端期望字段: className, department, perCapitaCarbon, perCapitaPoints, studentCount
     */
    public Map<String, Object> getClassLeaderboard(Long userId, int page, int pageSize) {
        double rate = getExchangeRate();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        List<User> allUsers = userRepository.findAll();
        Map<String, List<User>> classGroup = allUsers.stream()
                .filter(u -> u.getClassName() != null && !u.getClassName().isBlank())
                .collect(Collectors.groupingBy(User::getClassName));

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, List<User>> entry : classGroup.entrySet()) {
            String className = entry.getKey();
            List<User> members = entry.getValue();
            long totalPoints = members.stream().mapToLong(User::getPointsBalance).sum();
            int studentCount = members.size();
            long perCapitaPoints = totalPoints / studentCount;
            double perCapitaCarbon = (totalPoints * rate) / studentCount;
            String dept = members.get(0).getDepartment();

            Map<String, Object> item = new HashMap<>();
            item.put("className", className);
            item.put("department", dept != null ? dept : "");
            item.put("perCapitaCarbon", Math.round(perCapitaCarbon * 10.0) / 10.0);
            item.put("perCapitaPoints", perCapitaPoints);
            item.put("studentCount", studentCount);
            items.add(item);
        }

        // 按人均积分降序
        items.sort((a, b) -> Long.compare(
                (Long) b.get("perCapitaPoints"), (Long) a.get("perCapitaPoints")));

        // 赋排名
        for (int i = 0; i < items.size(); i++) {
            items.get(i).put("rank", i + 1);
        }

        // 计算当前用户的班级排名
        int myRank = 0;
        long myScore = 0;
        String myClassName = currentUser.getClassName();
        if (myClassName != null && !myClassName.isBlank()) {
            for (Map<String, Object> item : items) {
                if (item.get("className").toString().equalsIgnoreCase(myClassName)) {
                    myRank = (int) item.get("rank");
                    myScore = (long) item.get("perCapitaPoints");
                    break;
                }
            }
        }

        // 内存分页
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        List<Map<String, Object>> paged = new ArrayList<>();
        if (fromIndex < items.size()) {
            paged = items.subList(fromIndex, toIndex);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ranks", paged);
        result.put("myRank", myRank);
        result.put("myScore", myScore);
        return result;
    }

    public List<DailyTrendStats> getUserCarbonTrend(Long userId, String period) {
        int days = "month".equalsIgnoreCase(period) ? 30 : 7;
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<BehaviorRecord> records = behaviorRecordRepository.findByUserIdAndDecisionAndCreatedAtBetween(
                userId, "PASS", startDateTime, LocalDateTime.now()
        );
        double rate = getExchangeRate();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, List<BehaviorRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().format(dtf)));

        List<DailyTrendStats> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).format(dtf);
            List<BehaviorRecord> dayRecords = grouped.getOrDefault(dateStr, Collections.emptyList());
            long count = dayRecords.size();
            long points = dayRecords.stream().mapToLong(BehaviorRecord::getPoints).sum();
            double carbon = points * rate;
            trend.add(new DailyTrendStats(dateStr, count, points, carbon));
        }
        return trend;
    }
}
