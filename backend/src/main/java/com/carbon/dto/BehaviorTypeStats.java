package com.carbon.dto;

public record BehaviorTypeStats(
    String behaviorType,
    long count,
    long totalPoints,
    double carbonReduction
) {}
