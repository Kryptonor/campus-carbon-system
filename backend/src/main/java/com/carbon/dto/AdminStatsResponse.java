package com.carbon.dto;

public record AdminStatsResponse(
    long totalUsers,
    long activeToday,
    long totalCheckins,
    long pendingAudits,
    double totalCarbonReduced,
    long totalPointsIssued,
    long onChainTxCount
) {}
