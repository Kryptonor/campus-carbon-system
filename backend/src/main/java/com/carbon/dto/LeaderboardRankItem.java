package com.carbon.dto;

public record LeaderboardRankItem(
    int rank,
    String name,
    String department,
    long score,
    double carbonReduced
) {}
