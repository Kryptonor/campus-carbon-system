package com.carbon.dto;

import java.util.List;

public record TransactionsResponse(
    List<TransactionItem> transactions,
    long total
) {}
