package com.carbon.controller;

import com.carbon.dto.ApiResponse;
import com.carbon.dto.LeaderboardResponse;
import com.carbon.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {
    private final StatsService statsService;

    public LeaderboardController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/list")
    public ApiResponse<?> getList(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "type", defaultValue = "points") String type,
            @RequestParam(value = "scope", defaultValue = "all") String scope,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        // 前端传 type=class 等同于 scope=class
        String effectiveScope = "class".equalsIgnoreCase(type) ? "class" : scope;

        if ("class".equalsIgnoreCase(effectiveScope)) {
            return ApiResponse.ok(statsService.getClassLeaderboard(userId, page, pageSize));
        }
        return ApiResponse.ok(statsService.getLeaderboardList(userId, type, effectiveScope, page, pageSize));
    }
}
