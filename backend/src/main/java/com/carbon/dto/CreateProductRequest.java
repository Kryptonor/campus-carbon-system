package com.carbon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @Min(0) Long pricePoints,
        @NotNull @Min(0) Integer stock,
        String imageUrl
) {
}
