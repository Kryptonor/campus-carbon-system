package com.carbon.controller;

import com.carbon.dto.CreateUserRequest;
import com.carbon.entity.User;
import com.carbon.service.UserService;
import jakarta.validation.Valid;
import com.carbon.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/users")
    public ApiResponse<User> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @GetMapping("/api/users/{id}")
    public ApiResponse<User> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @GetMapping("/api/users")
    public ApiResponse<List<User>> list(@org.springframework.web.bind.annotation.RequestParam(required = false) String keyword) {
        return ApiResponse.ok(userService.list(keyword));
    }

    @org.springframework.web.bind.annotation.PutMapping("/api/users/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @Valid @RequestBody com.carbon.dto.UpdateUserRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/api/users/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    // 新增前端期望的 profile 路由，支持通过当前 Token 解析出来的 userId 自动获取/更新个人资料
    @GetMapping("/api/user/profile")
    public ApiResponse<User> getProfile(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(userService.getById(userId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/api/user/profile")
    public ApiResponse<User> updateProfile(@RequestAttribute("userId") Long userId, @Valid @RequestBody com.carbon.dto.UpdateUserRequest request) {
        return ApiResponse.ok(userService.update(userId, request));
    }
}
