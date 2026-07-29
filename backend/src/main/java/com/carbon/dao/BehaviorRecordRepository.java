package com.carbon.dao;

import com.carbon.entity.BehaviorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BehaviorRecordRepository extends JpaRepository<BehaviorRecord, Long> {
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    long countByUserIdAndBehaviorTypeAndCreatedAtBetween(Long userId, String behaviorType, LocalDateTime start, LocalDateTime end);
    Page<BehaviorRecord> findByUserId(Long userId, Pageable pageable);
    List<BehaviorRecord> findByUserId(Long userId);

    List<BehaviorRecord> findByTxHashIsNull();

    boolean existsByImageHashAndDecision(String imageHash, String decision);

    List<BehaviorRecord> findByDecision(String decision);

    List<BehaviorRecord> findByDecisionAndCreatedAtAfter(String decision, LocalDateTime dateTime);

    List<BehaviorRecord> findByUserIdAndDecisionAndCreatedAtBetween(Long userId, String decision, LocalDateTime start, LocalDateTime end);

    long countByUserIdAndDecisionAndCreatedAtBetween(Long userId, String decision, LocalDateTime start, LocalDateTime end);

    long countByBehaviorType(String behaviorType);
}
