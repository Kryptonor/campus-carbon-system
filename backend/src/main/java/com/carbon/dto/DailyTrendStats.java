package com.carbon.dto;

public record DailyTrendStats(
    String date,
    long count,
    long totalPoints,
    double carbonReduction
) {}
