package com.carbon.dto;

import jakarta.validation.constraints.NotBlank;

public record RedeemRequest(@NotBlank String redeemCode) {
}
