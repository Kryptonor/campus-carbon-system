package com.carbon.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuditItem(
    Long id,
    Long userId,
    String studentName,
    String studentId,
    String behaviorType,
    String actionName,
    String imageUrl,
    BigDecimal aiConfidence,
    String aiLabel,
    String decision,
    Long points,
    LocalDateTime date,
    String category
) {}
