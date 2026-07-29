package com.carbon.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String studentNo,
        @NotBlank String name,
        String phone,
        String avatarUrl
) {
}
