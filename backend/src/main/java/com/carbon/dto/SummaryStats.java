package com.carbon.dto;

public record SummaryStats(
    double totalCarbonReduction,
    long totalPoints,
    long participantCount
) {}
