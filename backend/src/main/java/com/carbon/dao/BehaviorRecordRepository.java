package com.carbon.dao;

import com.carbon.entity.BehaviorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface BehaviorRecordRepository extends JpaRepository<BehaviorRecord, Long> {
	long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
	Page<BehaviorRecord> findByUserId(Long userId, Pageable pageable);
}
