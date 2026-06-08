package com.carbon.controller;

import com.carbon.dto.BehaviorTypeStats;
import com.carbon.dto.DailyTrendStats;
import com.carbon.dto.LeaderboardEntry;
import com.carbon.dto.SummaryStats;
import com.carbon.service.StatsService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SummaryStats> getSummary() {
        return ResponseEntity.ok(statsService.getSummary());
    }

    @GetMapping("/behavior-type")
    public ResponseEntity<List<BehaviorTypeStats>> getBehaviorTypeStats() {
        return ResponseEntity.ok(statsService.getBehaviorTypeStats());
    }

    @GetMapping("/trend")
    public ResponseEntity<List<DailyTrendStats>> getDailyTrendStats(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(statsService.getDailyTrendStats(days));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getLeaderboard(limit));
    }
}
