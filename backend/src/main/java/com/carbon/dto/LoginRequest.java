package com.carbon.dto;

public record LoginRequest(String studentNo, String studentId, String password) {
    public String getUsername() {
        return studentNo != null && !studentNo.isBlank() ? studentNo : studentId;
    }
}
