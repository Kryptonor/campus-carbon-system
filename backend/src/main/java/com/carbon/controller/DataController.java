package com.carbon.controller;

import com.carbon.dto.ApiResponse;
import com.carbon.dto.BehaviorTypeStats;
import com.carbon.dto.CampusCompareResponse;
import com.carbon.dto.HomeDailySummary;
import com.carbon.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataController {
    private final StatsService statsService;

    public DataController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/carbon-breakdown")
    public ApiResponse<List<java.util.Map<String, Object>>> getCarbonBreakdown() {
        List<BehaviorTypeStats> stats = statsService.getBehaviorTypeStats();
        // 前端期望字段: name, value(百分比), color
        java.util.Map<String, String> colors = java.util.Map.ofEntries(
            java.util.Map.entry("walk", "#4CAF50"), java.util.Map.entry("bike", "#29B6F6"),
            java.util.Map.entry("bus", "#FF9800"), java.util.Map.entry("recycle", "#8BC34A"),
            java.util.Map.entry("oldGoods", "#FFB300"), java.util.Map.entry("savePower", "#00BCD4"),
            java.util.Map.entry("noPlastic", "#E91E63"), java.util.Map.entry("plantTree", "#2E7D32"),
            java.util.Map.entry("clean_plate", "#FF5722"), java.util.Map.entry("vegan", "#4CAF50"),
            java.util.Map.entry("stairs", "#607D8B")
        );
        java.util.Map<String, String> names = java.util.Map.ofEntries(
            java.util.Map.entry("walk", "步行出行"), java.util.Map.entry("bike", "骑行出行"),
            java.util.Map.entry("bus", "公交出行"), java.util.Map.entry("recycle", "垃圾分类"),
            java.util.Map.entry("oldGoods", "旧物回收"), java.util.Map.entry("savePower", "节约用电"),
            java.util.Map.entry("noPlastic", "拒绝一次性塑料"), java.util.Map.entry("plantTree", "植树护绿"),
            java.util.Map.entry("clean_plate", "光盘行动"), java.util.Map.entry("vegan", "绿色素食"),
            java.util.Map.entry("stairs", "走楼梯")
        );

        double totalCarbon = stats.stream().mapToDouble(BehaviorTypeStats::carbonReduction).sum();
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (BehaviorTypeStats s : stats) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("name", names.getOrDefault(s.behaviorType(), s.behaviorType()));
            item.put("value", totalCarbon > 0 ? Math.round(s.carbonReduction() / totalCarbon * 1000.0) / 10.0 : 0);
            item.put("color", colors.getOrDefault(s.behaviorType(), "#9E9E9E"));
            result.add(item);
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/campus-compare")
    public ApiResponse<CampusCompareResponse> getCampusCompare(@RequestAttribute("userId") Long userId) {
        HomeDailySummary summary = statsService.getHomeDailySummary(userId);
        double myWeeklyCarbon = summary.weeklyCarbon();

        // 校园每周人均碳排放
        double campusAverage = 12.5; // 预设全校人均周碳排
        try {
            double totalCarbon = statsService.getSummary().totalCarbonReduction();
            long totalUsers = statsService.getLeaderboard(100).size(); // 取出排行榜的大致用户数，或者全局总用户数
            if (totalUsers > 0) {
                // 做一个合理的加权周平均
                campusAverage = totalCarbon / totalUsers;
            }
        } catch (Exception e) {
            campusAverage = 12.5;
        }

        return ApiResponse.ok(new CampusCompareResponse(myWeeklyCarbon, campusAverage));
    }
}
