package com.carbon.controller;

import com.carbon.dto.ApiResponse;
import com.carbon.dto.BehaviorTypeStats;
import com.carbon.dto.DailyTrendStats;
import com.carbon.dto.SummaryStats;
import com.carbon.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ApiResponse<SummaryStats> getSummary() {
        return ApiResponse.ok(statsService.getSummary());
    }

    @GetMapping("/behavior-type")
    public ApiResponse<List<BehaviorTypeStats>> getBehaviorTypeStats() {
        return ApiResponse.ok(statsService.getBehaviorTypeStats());
    }

    @GetMapping("/trend")
    public ApiResponse<List<DailyTrendStats>> getDailyTrendStats(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(statsService.getDailyTrendStats(days));
    }
}
