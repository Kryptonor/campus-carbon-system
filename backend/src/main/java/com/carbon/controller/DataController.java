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
    public ApiResponse<List<BehaviorTypeStats>> getCarbonBreakdown() {
        return ApiResponse.ok(statsService.getBehaviorTypeStats());
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
