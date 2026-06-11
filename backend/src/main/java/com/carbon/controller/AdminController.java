package com.carbon.controller;

import com.carbon.blockchain.config.BlockchainProperties;
import com.carbon.blockchain.service.BlockchainTxQueue;
import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.dao.ExchangeRecordRepository;
import com.carbon.dao.UserRepository;
import com.carbon.dto.AdminStatsResponse;
import com.carbon.dto.ApiResponse;
import com.carbon.dto.AuditItem;
import com.carbon.dto.AuditReviewRequest;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.User;
import com.carbon.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final BehaviorRecordRepository behaviorRecordRepository;
    private final ExchangeRecordRepository exchangeRecordRepository;
    private final StatsService statsService;
    private final BlockchainTxQueue blockchainTxQueue;
    private final BlockchainProperties blockchainProperties;

    public AdminController(UserRepository userRepository,
                           BehaviorRecordRepository behaviorRecordRepository,
                           ExchangeRecordRepository exchangeRecordRepository,
                           StatsService statsService,
                           BlockchainTxQueue blockchainTxQueue,
                           BlockchainProperties blockchainProperties) {
        this.userRepository = userRepository;
        this.behaviorRecordRepository = behaviorRecordRepository;
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.statsService = statsService;
        this.blockchainTxQueue = blockchainTxQueue;
        this.blockchainProperties = blockchainProperties;
    }

    private ApiResponse<?> checkAdmin(String role) {
        if (role == null || !"ADMIN".equals(role)) {
            return ApiResponse.error(403, "无管理员权限");
        }
        return null;
    }

    @GetMapping("/stats")
    public ApiResponse<AdminStatsResponse> getStats(@RequestAttribute(value = "role", required = false) String role) {
        ApiResponse<?> err = checkAdmin(role);
        if (err != null) return ApiResponse.error(403, "无管理员权限");
        long totalUsers = userRepository.count();

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        long activeToday = behaviorRecordRepository.findByDecisionAndCreatedAtAfter("PASS", todayStart)
                .stream().map(BehaviorRecord::getUserId).distinct().count();

        long totalCheckins = behaviorRecordRepository.count();
        long pendingAudits = behaviorRecordRepository.findAll().stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getDecision()))
                .count();

        double totalCarbonReduced = statsService.getSummary().totalCarbonReduction();
        long totalPointsIssued = statsService.getSummary().totalPoints();

        long onChainBehaviors = behaviorRecordRepository.findAll().stream()
                .filter(b -> b.getTxHash() != null)
                .count();
        long onChainExchanges = exchangeRecordRepository.findAll().stream()
                .filter(e -> e.getTxHash() != null)
                .count();
        long onChainTxCount = onChainBehaviors + onChainExchanges;

        return ApiResponse.ok(new AdminStatsResponse(
            totalUsers,
            activeToday,
            totalCheckins,
            pendingAudits,
            totalCarbonReduced,
            totalPointsIssued,
            onChainTxCount
        ));
    }

    @PostMapping("/audit-review")
    public ApiResponse<BehaviorRecord> auditReview(@RequestBody AuditReviewRequest request,
                                                    @RequestAttribute(value = "role", required = false) String role) {
        ApiResponse<?> err = checkAdmin(role);
        if (err != null) return ApiResponse.error(403, "无管理员权限");
        BehaviorRecord record = behaviorRecordRepository.findById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("打卡记录不存在"));

        if (!"PENDING".equalsIgnoreCase(record.getDecision())) {
            return ApiResponse.error(400, "该记录已完成审核，无法重复审核");
        }

        record.setDecision(request.approved() ? "PASS" : "REJECT");
        record.setStatus("COMPLETED");
        record = behaviorRecordRepository.save(record);

        if (request.approved()) {
            if (blockchainProperties.isEnabled()) {
                blockchainTxQueue.submitBehavior(record);
            } else {
                User user = userRepository.findById(record.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
                user.setPointsBalance(user.getPointsBalance() + record.getPoints());
                userRepository.save(user);
            }
        }

        return ApiResponse.ok("审核成功", record);
    }

    @GetMapping("/audit-list")
    public ApiResponse<List<AuditItem>> getAuditList(@RequestAttribute(value = "role", required = false) String role) {
        ApiResponse<?> err = checkAdmin(role);
        if (err != null) return ApiResponse.error(403, "无管理员权限");

        List<BehaviorRecord> pending = behaviorRecordRepository.findByDecision("PENDING");
        List<AuditItem> items = pending.stream().map(b -> {
            User u = userRepository.findById(b.getUserId()).orElse(null);
            return new AuditItem(
                b.getId(),
                b.getUserId(),
                u != null ? u.getName() : "未知",
                u != null ? u.getStudentNo() : "",
                b.getBehaviorType(),
                behaviorLabel(b.getBehaviorType()),
                b.getImageUrl(),
                b.getAiScore(),
                b.getAiLabel(),
                b.getDecision(),
                b.getPoints(),
                b.getCreatedAt(),
                ""
            );
        }).collect(java.util.stream.Collectors.toList());

        return ApiResponse.ok(items);
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> getUsers(@RequestAttribute(value = "role", required = false) String role) {
        ApiResponse<?> err = checkAdmin(role);
        if (err != null) return ApiResponse.error(403, "无管理员权限");

        return ApiResponse.ok(userRepository.findAll());
    }

    private String behaviorLabel(String type) {
        if (type == null) return "未知";
        switch (type) {
            case "walk": return "步行出行";
            case "bike": return "骑行出行";
            case "bus": return "公交出行";
            case "recycle": return "垃圾分类";
            case "oldGoods": return "旧物回收";
            case "savePower": return "节约用电";
            case "noPlastic": return "拒绝一次性塑料";
            case "plantTree": return "植树护绿";
            case "clean_plate": return "光盘行动";
            case "vegan": return "绿色素食";
            case "stairs": return "走楼梯";
            default: return type;
        }
    }
}
