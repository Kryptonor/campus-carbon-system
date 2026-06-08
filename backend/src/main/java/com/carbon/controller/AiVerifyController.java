package com.carbon.controller;

import com.carbon.dto.AiVerifyResponse;
import com.carbon.service.AiVerifyService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.carbon.dto.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AiVerifyController {
    private final AiVerifyService aiVerifyService;

    public AiVerifyController(AiVerifyService aiVerifyService) {
        this.aiVerifyService = aiVerifyService;
    }

    @PostMapping(value = {"/verify", "/analyze"})
    public ApiResponse<AiVerifyResponse> verify(
            @RequestParam("userId") @NotNull Long userId,
            @RequestParam("behaviorType") @NotBlank String behaviorType,
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam(value = "imageUrl", required = false) String imageUrl
    ) {
        AiVerifyResponse response = aiVerifyService.verify(userId, behaviorType, file, imageUrl);
        return ApiResponse.ok(response);
    }
}
