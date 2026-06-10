package com.carbon.service;

import com.carbon.blockchain.config.BlockchainProperties;
import com.carbon.blockchain.service.BlockchainTxQueue;
import com.carbon.config.CarbonProperties;
import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.dao.UserRepository;
import com.carbon.dto.AiVerifyResponse;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Set;

import com.carbon.blockchain.service.BlockchainService;

@Service
public class AiVerifyService {
    private final BaiduAiClient baiduAiClient;
    private final BehaviorRecordRepository behaviorRecordRepository;
    private final SystemConfigService systemConfigService;
    private final CarbonProperties carbonProperties;
    private final BehaviorRuleService behaviorRuleService;
    private final UserRepository userRepository;
    private final BlockchainTxQueue blockchainTxQueue;
    private final BlockchainService blockchainService;
    private final BlockchainProperties blockchainProperties;

    public AiVerifyService(BaiduAiClient baiduAiClient,
                           BehaviorRecordRepository behaviorRecordRepository,
                           SystemConfigService systemConfigService,
                           CarbonProperties carbonProperties,
                           BehaviorRuleService behaviorRuleService,
                           UserRepository userRepository,
                           BlockchainTxQueue blockchainTxQueue,
                           BlockchainService blockchainService,
                           BlockchainProperties blockchainProperties) {
        this.baiduAiClient = baiduAiClient;
        this.behaviorRecordRepository = behaviorRecordRepository;
        this.systemConfigService = systemConfigService;
        this.carbonProperties = carbonProperties;
        this.behaviorRuleService = behaviorRuleService;
        this.userRepository = userRepository;
        this.blockchainTxQueue = blockchainTxQueue;
        this.blockchainService = blockchainService;
        this.blockchainProperties = blockchainProperties;
    }

    public AiVerifyResponse verify(Long userId, String behaviorType, MultipartFile file, String imageUrl) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image file is required");
        }
        if (!behaviorRuleService.isAllowedBehavior(behaviorType)) {
            throw new IllegalArgumentException("behavior type not allowed");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("user not found");
        }
        validateContentType(file);
        enforceDailyLimit(userId);

        byte[] bytes = readBytes(file);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String imageHash = sha256Hex(bytes);

        // 重复图片检验（本地和链上双重校验拦截，防刷分且避免浪费 AI 识别额度）
        if (behaviorRecordRepository.existsByImageHashAndDecision(imageHash, "PASS") 
                || blockchainService.isImageHashExists(imageHash)) {
            throw new IllegalArgumentException("duplicate image submission");
        }

        BaiduAiClient.AiLabelScore aiResult = baiduAiClient.classify(base64);

        double threshold = systemConfigService.getDouble(
            "ai_threshold",
            carbonProperties.getAi().getConfidenceThreshold()
        );
        boolean thresholdPass = aiResult.score() >= threshold;
        boolean labelMatch = behaviorRuleService.matchesLabel(behaviorType, aiResult.label());
        boolean pass = thresholdPass && labelMatch;
        String decision = pass ? "PASS" : "REJECT";
        String status = pass ? "PENDING" : "REJECTED";
        long points = pass ? behaviorRuleService.getPoints(behaviorType) : 0L;

        // 基于可信 AI 识别与审计链路的行为记录落库
        BehaviorRecord record = new BehaviorRecord();
        record.setUserId(userId);
        record.setBehaviorType(behaviorType);
        record.setImageUrl(imageUrl == null ? "" : imageUrl);
        record.setImageHash(imageHash);
        record.setPoints(points);
        record.setAiLabel(aiResult.label());
        record.setAiScore(BigDecimal.valueOf(aiResult.score()));
        record.setDecision(decision);
        record.setStatus(status);
        BehaviorRecord saved = behaviorRecordRepository.save(record);

        // 异步上链存证 + 积分铸造（不阻塞当前请求响应）
        if (blockchainProperties.isEnabled()) {
            blockchainTxQueue.submitBehavior(saved);
        } else {
            // 如果链不启用，直接在本地加分，并置状态为 COMPLETED
            if (pass) {
                saved.setStatus("COMPLETED");
                behaviorRecordRepository.save(saved);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("user not found"));
                user.setPointsBalance(user.getPointsBalance() + points);
                userRepository.save(user);
            }
        }

        return new AiVerifyResponse(saved.getId(), decision, aiResult.label(), BigDecimal.valueOf(aiResult.score()), threshold, points);
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("image content type is required");
        }
        Set<String> allowed = Set.of("image/jpeg", "image/png", "image/webp");
        if (!allowed.contains(contentType)) {
            throw new IllegalArgumentException("unsupported image type");
        }
    }

    private void enforceDailyLimit(Long userId) {
        long dailyLimit = systemConfigService.getLong("daily_limit", 3);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        long count = behaviorRecordRepository.countByUserIdAndCreatedAtBetween(userId, start, end);
        if (count >= dailyLimit) {
            throw new IllegalArgumentException("daily limit reached");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception ex) {
            throw new IllegalStateException("failed to read image", ex);
        }
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("failed to hash image", ex);
        }
    }
}
