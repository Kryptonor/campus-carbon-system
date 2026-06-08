package com.carbon.service;

import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.dao.UserRepository;
import com.carbon.dto.BehaviorTypeStats;
import com.carbon.dto.DailyTrendStats;
import com.carbon.dto.LeaderboardEntry;
import com.carbon.dto.SummaryStats;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
        // 按照积分余额排行
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
}
