package com.carbon.dto;

public record ApiResponse<T>(int code, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) { 
        return new ApiResponse<>(200, "操作成功", data); 
    }
    
    public static <T> ApiResponse<T> ok(String msg, T data) { 
        return new ApiResponse<>(200, msg, data); 
    }
    
    public static <T> ApiResponse<T> error(int code, String message) { 
        return new ApiResponse<>(code, message, null); 
    }
}
