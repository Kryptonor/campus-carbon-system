package com.carbon.dao;

import com.carbon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNo(String studentNo);
    boolean existsByStudentNo(String studentNo);
    List<User> findByNameContainingOrStudentNoContainingOrPhoneContaining(String name, String studentNo, String phone);
    List<User> findByDepartment(String department);
    List<User> findByClassName(String className);
}
