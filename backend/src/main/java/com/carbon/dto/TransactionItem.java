package com.carbon.dto;

public record TransactionItem(
    String id,
    String type, // "income" 或 "expense"
    String desc,
    long points,
    String date,
    String txHash
) {}
