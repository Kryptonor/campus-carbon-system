package com.carbon.dto;

import java.math.BigDecimal;

public record AiVerifyResponse(
        Long recordId,
        String decision,
        String label,
        BigDecimal score,
        double threshold,
        long points
) {
}
