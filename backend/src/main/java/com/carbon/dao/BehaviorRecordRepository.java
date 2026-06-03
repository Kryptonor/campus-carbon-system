package com.carbon.dao;

import com.carbon.entity.BehaviorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BehaviorRecordRepository extends JpaRepository<BehaviorRecord, Long> {
	long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
	Page<BehaviorRecord> findByUserId(Long userId, Pageable pageable);

	/** 查询所有未上链的行为记录（txHash 为 null），供定时补录任务使用 */
	List<BehaviorRecord> findByTxHashIsNull();
}
