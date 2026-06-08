package com.carbon.dto;

public record CheckinRequest(
    String actionId,
    String notes,
    String date
) {}
