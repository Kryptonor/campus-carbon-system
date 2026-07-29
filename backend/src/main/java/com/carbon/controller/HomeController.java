package com.carbon.controller;

import com.carbon.dto.ApiResponse;
import com.carbon.dto.DailyTrendStats;
import com.carbon.dto.HomeDailySummary;
import com.carbon.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    private final StatsService statsService;

    public HomeController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/daily-summary")
    public ApiResponse<HomeDailySummary> getDailySummary(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(statsService.getHomeDailySummary(userId));
    }

    @GetMapping("/carbon-trend")
    public ApiResponse<List<DailyTrendStats>> getCarbonTrend(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "period", defaultValue = "week") String period
    ) {
        return ApiResponse.ok(statsService.getUserCarbonTrend(userId, period));
    }

    @GetMapping("/quick-actions")
    public ApiResponse<List<java.util.Map<String, Object>>> getQuickActions() {
        List<java.util.Map<String, Object>> actions = List.of(
            java.util.Map.of("id", "walk", "name", "步行出行", "icon", "walk", "points", 10, "color", "#4CAF50"),
            java.util.Map.of("id", "bike", "name", "骑行出行", "icon", "bike", "points", 15, "color", "#29B6F6"),
            java.util.Map.of("id", "bus", "name", "公交出行", "icon", "bus", "points", 20, "color", "#FF9800"),
            java.util.Map.of("id", "recycle", "name", "垃圾分类", "icon", "recycle", "points", 5, "color", "#8BC34A"),
            java.util.Map.of("id", "oldGoods", "name", "旧物回收", "icon", "goods", "points", 30, "color", "#FFB300"),
            java.util.Map.of("id", "savePower", "name", "节约用电", "icon", "power", "points", 8, "color", "#00BCD4"),
            java.util.Map.of("id", "noPlastic", "name", "拒绝一次性塑料", "icon", "plastic", "points", 12, "color", "#E91E63"),
            java.util.Map.of("id", "plantTree", "name", "植树护绿", "icon", "tree", "points", 50, "color", "#2E7D32")
        );
        return ApiResponse.ok(actions);
    }
}
