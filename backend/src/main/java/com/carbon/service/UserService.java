package com.carbon.service;

import com.carbon.dao.UserRepository;
import com.carbon.dto.CreateUserRequest;
import com.carbon.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(CreateUserRequest request) {
        if (userRepository.existsByStudentNo(request.studentNo())) {
            throw new IllegalArgumentException("studentNo already exists");
        }
        User user = new User();
        user.setStudentNo(request.studentNo());
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setAvatarUrl(request.avatarUrl());
        return userRepository.save(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    public List<User> list(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.findByNameContainingOrStudentNoContainingOrPhoneContaining(keyword, keyword, keyword);
    }

    public User update(Long id, com.carbon.dto.UpdateUserRequest request) {
        User user = getById(id);
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setAvatarUrl(request.avatarUrl());
        if (request.pointsBalance() != null) {
            user.setPointsBalance(request.pointsBalance());
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        User user = getById(id);
        userRepository.delete(user);
    }
}
