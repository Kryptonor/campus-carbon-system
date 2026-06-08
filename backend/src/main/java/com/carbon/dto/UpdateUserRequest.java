package com.carbon.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String name,
        String phone,
        String avatarUrl,
        Long pointsBalance
) {
}
