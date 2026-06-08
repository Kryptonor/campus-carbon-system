package com.carbon.dto;

public record LeaderboardEntry(
    Long userId,
    String studentNo,
    String name,
    String avatarUrl,
    long pointsBalance,
    double estimatedCarbonReduction
) {}
