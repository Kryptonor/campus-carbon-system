package com.carbon.dto;

public record HomeDailySummary(
    double todayCarbon,
    double yesterdayCarbon,
    double carbonChange,
    long todayPoints,
    long totalPoints,
    double weeklyCarbon,
    double weeklyChange,
    double goalCarbon,
    double goalProgress,
    long collegeRank,
    long collegeTotal
) {}
