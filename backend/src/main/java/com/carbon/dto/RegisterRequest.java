package com.carbon.dto;

public record RegisterRequest(
    String studentId, // 映射到后端的 studentNo
    String name,
    String password,
    String department,
    String className,
    String grade
) {}
