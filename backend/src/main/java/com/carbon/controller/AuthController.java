package com.carbon.controller;

import com.carbon.config.JwtUtils;
import com.carbon.dao.UserRepository;
import com.carbon.dto.ApiResponse;
import com.carbon.dto.LoginRequest;
import com.carbon.dto.LoginResponse;
import com.carbon.dto.RegisterRequest;
import com.carbon.entity.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public AuthController(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        if (username == null || request.password() == null) {
            return ApiResponse.error(400, "学号或密码不能为空");
        }

        Optional<User> userOpt = userRepository.findByStudentNo(username);
        if (userOpt.isEmpty()) {
            return ApiResponse.error(400, "用户不存在");
        }

        User user = userOpt.get();
        // 兼容原先没有设置密码时或者初始状态的情况。如果数据库中的密码为空且输入密码是特定内容，或者使用 BCrypt 校验
        boolean passwordMatch = false;
        try {
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                // 如果原密码为空白，可以允许直接登录或初始验证（这里我们强制要求只要有密码哈希就校验，无则允许注册或报错，符合一般的标准）
                passwordMatch = request.password().isEmpty();
            } else {
                passwordMatch = BCrypt.checkpw(request.password(), user.getPassword());
            }
        } catch (Exception e) {
            passwordMatch = false;
        }

        if (!passwordMatch) {
            return ApiResponse.error(400, "密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getStudentNo());
        LoginResponse response = new LoginResponse(
            token,
            token, // 用 token 作为 refreshToken
            jwtUtils.getExpirationTime() / 1000,
            user
        );

        return ApiResponse.ok("登录成功", response);
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        if (request.studentId() == null || request.studentId().isBlank()) {
            return ApiResponse.error(400, "学号不能为空");
        }
        if (request.password() == null || request.password().isBlank()) {
            return ApiResponse.error(400, "密码不能为空");
        }
        if (request.name() == null || request.name().isBlank()) {
            return ApiResponse.error(400, "姓名不能为空");
        }

        if (userRepository.existsByStudentNo(request.studentId())) {
            return ApiResponse.error(400, "该学号已被注册");
        }

        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());

        User user = new User();
        user.setStudentNo(request.studentId());
        user.setName(request.name());
        user.setPassword(hashedPassword);
        user.setDepartment(request.department() != null ? request.department() : "");
        user.setClassName(request.className() != null ? request.className() : "");
        user.setGrade(request.grade() != null ? request.grade() : "");
        user.setPointsBalance(0L);

        userRepository.save(user);
        return ApiResponse.ok("注册成功", null);
    }
}
