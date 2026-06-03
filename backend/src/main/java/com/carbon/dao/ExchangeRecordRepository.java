package com.carbon.dao;

import com.carbon.entity.ExchangeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ExchangeRecordRepository extends JpaRepository<ExchangeRecord, Long> {
	Page<ExchangeRecord> findByUserId(Long userId, Pageable pageable);
	Optional<ExchangeRecord> findByRedeemCode(String redeemCode);

	/** 查询所有未上链的兑换记录（txHash 为 null），供定时补录任务使用 */
	List<ExchangeRecord> findByTxHashIsNull();
}
