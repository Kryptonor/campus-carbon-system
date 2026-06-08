package com.carbon.dto;

import java.util.List;

public record LeaderboardResponse(
    List<LeaderboardRankItem> ranks,
    int myRank,
    long myScore
) {}
