package com.carbon.blockchain.service;

import com.carbon.blockchain.config.BlockchainProperties;
import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.dao.ExchangeRecordRepository;
import com.carbon.dao.UserRepository;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.ExchangeRecord;
import com.carbon.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 异步上链队列 + 失败补录定时任务。
 *
 * <h3>为什么需要异步</h3>
 * <ol>
 *   <li>区块链交易确认需要秒级时间，不能让用户等</li>
 *   <li>解耦链上写入与业务请求响应</li>
 *   <li>失败时自动重试，保证最终一致性</li>
 * </ol>
 *
 * <h3>工作机制</h3>
 * <ol>
 *   <li>业务调用 {@link #submitBehavior(BehaviorRecord, String)} 提交异步任务</li>
 *   <li>上链成功后，自动回填 txHash 到 MySQL</li>
 *   <li>上链失败 → txHash 保持 null</li>
 *   <li>定时任务每 30 秒扫描 txHash=null 的记录进行补录</li>
 * </ol>
 */
@Component
@EnableAsync
@EnableScheduling
public class BlockchainTxQueue {

    private static final Logger log = LoggerFactory.getLogger(BlockchainTxQueue.class);

    private final BlockchainService blockchainService;
    private final BlockchainProperties properties;
    private final BehaviorRecordRepository behaviorRecordRepository;
    private final ExchangeRecordRepository exchangeRecordRepository;
    private final UserRepository userRepository;

    public BlockchainTxQueue(BlockchainService blockchainService,
                             BlockchainProperties properties,
                             BehaviorRecordRepository behaviorRecordRepository,
                             ExchangeRecordRepository exchangeRecordRepository,
                             UserRepository userRepository) {
        this.blockchainService = blockchainService;
        this.properties = properties;
        this.behaviorRecordRepository = behaviorRecordRepository;
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.userRepository = userRepository;
    }

    // ==================== 异步提交 ====================

    /**
     * 异步提交行为记录上链。
     *
     * @param behavior 已落库的行为记录
     */
    @Async
    public void submitBehavior(BehaviorRecord behavior) {
        if (!properties.isEnabled()) {
            log.info("Blockchain disabled, skipping behavior record: id={}", behavior.getId());
            return;
        }
        try {
            String studentNo = resolveStudentNo(behavior.getUserId());
            String txHash = blockchainService.recordBehavior(behavior, studentNo);
            // 回填 txHash
            behavior.setTxHash(txHash);
            behaviorRecordRepository.save(behavior);
            log.info("Behavior on-chain success: recordId={}, txHash={}", behavior.getId(), txHash);
        } catch (Exception e) {
            log.error("Behavior on-chain failed (will retry): recordId={}, error={}",
                    behavior.getId(), e.getMessage());
        }
    }

    /**
     * 异步提交兑换记录上链。
     *
     * @param exchange 已落库的兑换记录
     */
    @Async
    public void submitExchange(ExchangeRecord exchange) {
        if (!properties.isEnabled()) {
            log.info("Blockchain disabled, skipping exchange record: id={}", exchange.getId());
            return;
        }
        try {
            String studentNo = resolveStudentNo(exchange.getUserId());
            String txHash = blockchainService.recordExchange(exchange, studentNo);
            // 回填 txHash
            exchange.setTxHash(txHash);
            exchangeRecordRepository.save(exchange);
            log.info("Exchange on-chain success: recordId={}, txHash={}", exchange.getId(), txHash);
        } catch (Exception e) {
            log.error("Exchange on-chain failed (will retry): recordId={}, error={}",
                    exchange.getId(), e.getMessage());
        }
    }

    // ==================== 定时补录 ====================

    /**
     * 每 30 秒扫描一次未上链的行为记录，尝试补录。
     */
    @Scheduled(fixedDelay = 30_000)
    public void retryBehaviorRecords() {
        if (!properties.isEnabled()) {
            return;
        }
        List<BehaviorRecord> unconfirmed = behaviorRecordRepository.findByTxHashIsNull();
        if (unconfirmed.isEmpty()) {
            return;
        }
        log.info("Retrying {} unconfirmed behavior records...", unconfirmed.size());
        for (BehaviorRecord record : unconfirmed) {
            try {
                String studentNo = resolveStudentNo(record.getUserId());
                String txHash = blockchainService.recordBehavior(record, studentNo);
                record.setTxHash(txHash);
                behaviorRecordRepository.save(record);
                log.info("Retry success: behavior recordId={}, txHash={}", record.getId(), txHash);
            } catch (Exception e) {
                log.warn("Retry failed: behavior recordId={}, error={}", record.getId(), e.getMessage());
            }
        }
    }

    /**
     * 每 45 秒扫描一次未上链的兑换记录，尝试补录。
     */
    @Scheduled(fixedDelay = 45_000)
    public void retryExchangeRecords() {
        if (!properties.isEnabled()) {
            return;
        }
        List<ExchangeRecord> unconfirmed = exchangeRecordRepository.findByTxHashIsNull();
        if (unconfirmed.isEmpty()) {
            return;
        }
        log.info("Retrying {} unconfirmed exchange records...", unconfirmed.size());
        for (ExchangeRecord record : unconfirmed) {
            try {
                String studentNo = resolveStudentNo(record.getUserId());
                String txHash = blockchainService.recordExchange(record, studentNo);
                record.setTxHash(txHash);
                exchangeRecordRepository.save(record);
                log.info("Retry success: exchange recordId={}, txHash={}", record.getId(), txHash);
            } catch (Exception e) {
                log.warn("Retry failed: exchange recordId={}, error={}", record.getId(), e.getMessage());
            }
        }
    }

    // ==================== 辅助方法 ====================

    private String resolveStudentNo(Long userId) {
        return userRepository.findById(userId)
                .map(User::getStudentNo)
                .orElse("UNKNOWN");
    }
}
