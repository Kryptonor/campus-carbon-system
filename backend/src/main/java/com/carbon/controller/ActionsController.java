package com.carbon.controller;

import com.carbon.blockchain.config.BlockchainProperties;
import com.carbon.blockchain.service.BlockchainTxQueue;
import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.dao.UserRepository;
import com.carbon.dto.ApiResponse;
import com.carbon.dto.CheckinRequest;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.User;
import com.carbon.service.BehaviorRecordService;
import com.carbon.service.BehaviorRuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actions")
public class ActionsController {
    private final UserRepository userRepository;
    private final BehaviorRecordRepository behaviorRecordRepository;
    private final BehaviorRuleService behaviorRuleService;
    private final BehaviorRecordService behaviorRecordService;
    private final BlockchainTxQueue blockchainTxQueue;
    private final BlockchainProperties blockchainProperties;

    private static final List<Map<String, Object>> BEHAVIOR_TEMPLATES = List.of(
        Map.of("id", "walk", "name", "步行出行", "icon", "walk", "color", "#4CAF50"),
        Map.of("id", "bike", "name", "骑行出行", "icon", "bike", "color", "#29B6F6"),
        Map.of("id", "bus", "name", "公交出行", "icon", "bus", "color", "#FF9800"),
        Map.of("id", "recycle", "name", "垃圾分类", "icon", "recycle", "color", "#8BC34A"),
        Map.of("id", "oldGoods", "name", "旧物回收", "icon", "goods", "color", "#FFB300"),
        Map.of("id", "savePower", "name", "节约用电", "icon", "power", "color", "#00BCD4"),
        Map.of("id", "noPlastic", "name", "拒绝一次性塑料", "icon", "plastic", "color", "#E91E63"),
        Map.of("id", "plantTree", "name", "植树护绿", "icon", "tree", "color", "#2E7D32"),
        Map.of("id", "clean_plate", "name", "光盘行动", "icon", "plate", "color", "#FF5722"),
        Map.of("id", "vegan", "name", "绿色素食", "icon", "vegan", "color", "#4CAF50"),
        Map.of("id", "stairs", "name", "走楼梯", "icon", "stairs", "color", "#607D8B")
    );

    public ActionsController(UserRepository userRepository,
                             BehaviorRecordRepository behaviorRecordRepository,
                             BehaviorRuleService behaviorRuleService,
                             BehaviorRecordService behaviorRecordService,
                             BlockchainTxQueue blockchainTxQueue,
                             BlockchainProperties blockchainProperties) {
        this.userRepository = userRepository;
        this.behaviorRecordRepository = behaviorRecordRepository;
        this.behaviorRuleService = behaviorRuleService;
        this.behaviorRecordService = behaviorRecordService;
        this.blockchainTxQueue = blockchainTxQueue;
        this.blockchainProperties = blockchainProperties;
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> getList(@RequestAttribute("userId") Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> template : BEHAVIOR_TEMPLATES) {
            String bType = (String) template.get("id");
            long points = behaviorRuleService.getPoints(bType);
            long count = behaviorRecordRepository.countByUserIdAndBehaviorTypeAndCreatedAtBetween(userId, bType, todayStart, todayEnd);
            long maxTimes = 5; // 每日最大打卡 5 次
            long remainingTimes = Math.max(0, maxTimes - count);

            java.util.HashMap<String, Object> item = new java.util.HashMap<>(template);
            item.put("points", points);
            item.put("remainingTimes", remainingTimes);
            result.add(item);
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/checkin")
    public ApiResponse<BehaviorRecord> checkin(
            @RequestAttribute("userId") Long userId,
            @RequestBody CheckinRequest request
    ) {
        String actionId = request.actionId();
        if (actionId == null || !behaviorRuleService.isAllowedBehavior(actionId)) {
            return ApiResponse.error(400, "非法的打卡行为类型");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 校验每日次数上限
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        long count = behaviorRecordRepository.countByUserIdAndBehaviorTypeAndCreatedAtBetween(userId, actionId, todayStart, todayEnd);
        if (count >= 5) {
            return ApiResponse.error(400, "今日该打卡行为次数已达上限");
        }

        long points = behaviorRuleService.getPoints(actionId);

        BehaviorRecord behavior = new BehaviorRecord();
        behavior.setUserId(userId);
        behavior.setBehaviorType(actionId);
        behavior.setPoints(points);
        behavior.setDecision("PASS");
        behavior.setStatus("PENDING");
        behavior.setImageHash("checkin-" + System.currentTimeMillis());
        behavior.setImageUrl("");
        behavior.setAiLabel("");

        behavior = behaviorRecordRepository.save(behavior);

        // 如果启用区块链，走异步上链（异步上链中包含了加分逻辑）
        if (blockchainProperties.isEnabled()) {
            blockchainTxQueue.submitBehavior(behavior);
        } else {
            // 如果链不启用，直接在本地加分，并置为 COMPLETED
            behavior.setStatus("COMPLETED");
            behaviorRecordRepository.save(behavior);

            user.setPointsBalance(user.getPointsBalance() + points);
            userRepository.save(user);
        }

        return ApiResponse.ok("打卡成功", behavior);
    }

    @GetMapping("/my-history")
    public ApiResponse<Page<BehaviorRecord>> getMyHistory(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(behaviorRecordService.list(userId, pageable));
    }
}
