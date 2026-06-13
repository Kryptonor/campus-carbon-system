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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
        Map.of("id", "walk", "name", "步行出行", "icon", "🚶", "color", "#4CAF50"),
        Map.of("id", "bike", "name", "骑行出行", "icon", "🚲", "color", "#29B6F6"),
        Map.of("id", "bus", "name", "公交出行", "icon", "🚌", "color", "#FF9800"),
        Map.of("id", "recycle", "name", "垃圾分类", "icon", "♻️", "color", "#8BC34A"),
        Map.of("id", "oldGoods", "name", "旧物回收", "icon", "📦", "color", "#FFB300"),
        Map.of("id", "savePower", "name", "节约用电", "icon", "💡", "color", "#00BCD4"),
        Map.of("id", "noPlastic", "name", "拒绝一次性塑料", "icon", "🥤", "color", "#E91E63"),
        Map.of("id", "plantTree", "name", "植树护绿", "icon", "🌳", "color", "#2E7D32"),
        Map.of("id", "clean_plate", "name", "光盘行动", "icon", "🍽️", "color", "#FF5722"),
        Map.of("id", "vegan", "name", "绿色素食", "icon", "🥬", "color", "#4CAF50"),
        Map.of("id", "stairs", "name", "走楼梯", "icon", "🪜", "color", "#607D8B")
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
            long maxTimes = 5;
            long remainingTimes = Math.max(0, maxTimes - count);
            long totalCheckins = behaviorRecordRepository.countByBehaviorType(bType);

            java.util.HashMap<String, Object> item = new java.util.HashMap<>(template);
            item.put("points", points);
            item.put("remainingTimes", remainingTimes);
            item.put("totalCheckins", totalCheckins);
            item.put("dailyLimit", maxTimes);
            item.put("description", template.getOrDefault("name", ""));
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
    public ApiResponse<Map<String, Object>> getMyHistory(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BehaviorRecord> pageData = behaviorRecordService.list(userId, pageable);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<Map<String, Object>> records = new ArrayList<>();
        for (BehaviorRecord r : pageData.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "h_" + r.getId());
            item.put("actionId", r.getBehaviorType());
            item.put("actionName", BEHAVIOR_ICONS_TEXT.getOrDefault(r.getBehaviorType(), r.getBehaviorType()));
            item.put("icon", BEHAVIOR_ICONS.getOrDefault(r.getBehaviorType(), "🌿"));
            item.put("date", r.getCreatedAt().format(dtf));
            item.put("points", r.getPoints());
            item.put("status", "PASS".equals(r.getDecision()) ? "passed" : "pending");
            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageData.getTotalElements());
        return ApiResponse.ok(result);
    }

    private static final Map<String, String> BEHAVIOR_ICONS = Map.ofEntries(
        Map.entry("walk", "🚶"), Map.entry("bike", "🚲"), Map.entry("bus", "🚌"),
        Map.entry("recycle", "♻️"), Map.entry("oldGoods", "📦"), Map.entry("savePower", "💡"),
        Map.entry("noPlastic", "🥤"), Map.entry("plantTree", "🌳"), Map.entry("clean_plate", "🍽️"),
        Map.entry("vegan", "🥬"), Map.entry("stairs", "🪜")
    );
    private static final Map<String, String> BEHAVIOR_ICONS_TEXT = Map.ofEntries(
        Map.entry("walk", "步行出行"), Map.entry("bike", "骑行出行"), Map.entry("bus", "公交出行"),
        Map.entry("recycle", "垃圾分类"), Map.entry("oldGoods", "旧物回收"), Map.entry("savePower", "节约用电"),
        Map.entry("noPlastic", "拒绝一次性塑料"), Map.entry("plantTree", "植树护绿"), Map.entry("clean_plate", "光盘行动"),
        Map.entry("vegan", "绿色素食"), Map.entry("stairs", "走楼梯")
    );
}
