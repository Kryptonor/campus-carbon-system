package com.carbon.dto;

import com.carbon.entity.User;

public record LoginResponse(
    String token,
    String refreshToken,
    long expiresIn,
    User userInfo
) {}
