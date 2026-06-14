package com.carbon.controller;

import com.carbon.blockchain.service.BlockchainService;
import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.dao.UserRepository;
import com.carbon.dto.ApiResponse;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 区块链信息控制器 — 为前端 /api/blockchain/* 提供链上数据展示。
 */
@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final UserRepository userRepository;
    private final BehaviorRecordRepository behaviorRecordRepository;

    private static final Map<String, String> BEHAVIOR_ICONS = Map.ofEntries(
        Map.entry("walk", "🚶"), Map.entry("bike", "🚲"), Map.entry("bus", "🚌"),
        Map.entry("recycle", "♻️"), Map.entry("oldGoods", "📦"), Map.entry("savePower", "💡"),
        Map.entry("noPlastic", "🥤"), Map.entry("plantTree", "🌳"), Map.entry("clean_plate", "🍽️"),
        Map.entry("vegan", "🥬"), Map.entry("stairs", "🪜")
    );
    private static final Map<String, String> BEHAVIOR_NAMES = Map.ofEntries(
        Map.entry("walk", "步行出行"), Map.entry("bike", "骑行出行"), Map.entry("bus", "公交出行"),
        Map.entry("recycle", "垃圾分类"), Map.entry("oldGoods", "旧物回收"), Map.entry("savePower", "节约用电"),
        Map.entry("noPlastic", "拒绝一次性塑料"), Map.entry("plantTree", "植树护绿"), Map.entry("clean_plate", "光盘行动"),
        Map.entry("vegan", "绿色素食"), Map.entry("stairs", "走楼梯")
    );

    public BlockchainController(BlockchainService blockchainService,
                                UserRepository userRepository,
                                BehaviorRecordRepository behaviorRecordRepository) {
        this.blockchainService = blockchainService;
        this.userRepository = userRepository;
        this.behaviorRecordRepository = behaviorRecordRepository;
    }

    @GetMapping("/points-balance")
    public ApiResponse<Map<String, Object>> getPointsBalance(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "address", required = false) String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        long onChain = 0;
        try {
            onChain = blockchainService.getOnChainBalance(user.getStudentNo());
        } catch (Exception ignored) {}

        long total = user.getPointsBalance();
        long offChain = Math.max(0, total - onChain);

        Map<String, Object> result = new HashMap<>();
        result.put("onChainBalance", onChain);
        result.put("offChainBalance", offChain);
        result.put("totalBalance", total);
        return ApiResponse.ok(result);
    }

    @GetMapping("/contract-info")
    public ApiResponse<Map<String, Object>> getContractInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("networkStatus", "运行中");
        info.put("chainName", "FISCO BCOS 校园联盟链");
        info.put("pointsToken", "0x0948ca17438bcc79e9d384df7995027da8442cc9");
        info.put("actionLedger", "0x2b6eb6dae8980fe51e23e415d9e3a74b5a551552");
        info.put("blockHeight", "659+");
        info.put("txCount", "—");
        return ApiResponse.ok(info);
    }

    @GetMapping("/transactions")
    public ApiResponse<List<Map<String, Object>>> getTransactions(
            @RequestAttribute("userId") Long userId) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<BehaviorRecord> records = behaviorRecordRepository.findByUserId(userId);
        records.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        List<Map<String, Object>> txs = new ArrayList<>();
        for (BehaviorRecord r : records) {
            if (r.getTxHash() == null) continue;
            Map<String, Object> tx = new HashMap<>();
            tx.put("txHash", r.getTxHash());
            tx.put("type", "奖励发放");
            tx.put("amount", r.getPoints());
            tx.put("timestamp", r.getCreatedAt().format(dtf));
            txs.add(tx);
        }
        return ApiResponse.ok(txs);
    }

    @GetMapping("/action-records")
    public ApiResponse<List<Map<String, Object>>> getActionRecords(
            @RequestAttribute("userId") Long userId) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<BehaviorRecord> records = behaviorRecordRepository.findByUserId(userId);
        records.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        List<Map<String, Object>> actions = new ArrayList<>();
        for (BehaviorRecord r : records) {
            if (r.getTxHash() == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", "ar_" + r.getId());
            item.put("icon", BEHAVIOR_ICONS.getOrDefault(r.getBehaviorType(), "🌿"));
            item.put("actionName", BEHAVIOR_NAMES.getOrDefault(r.getBehaviorType(), r.getBehaviorType()));
            item.put("date", r.getCreatedAt().format(dtf));
            item.put("txHash", r.getTxHash());
            actions.add(item);
        }
        return ApiResponse.ok(actions);
    }
}
