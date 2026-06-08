package com.carbon.dto;

public record PointsBalanceResponse(
    long total,
    long onChain,
    long offChain,
    long todayEarned
) {}
