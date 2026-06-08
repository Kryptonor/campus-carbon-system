package com.carbon.controller;

import com.carbon.blockchain.service.BlockchainService;
import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.dao.ExchangeRecordRepository;
import com.carbon.dao.ProductRepository;
import com.carbon.dao.UserRepository;
import com.carbon.dto.ApiResponse;
import com.carbon.dto.PointsBalanceResponse;
import com.carbon.dto.TransactionItem;
import com.carbon.dto.TransactionsResponse;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.ExchangeRecord;
import com.carbon.entity.Product;
import com.carbon.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointsController {
    private final UserRepository userRepository;
    private final BehaviorRecordRepository behaviorRecordRepository;
    private final ExchangeRecordRepository exchangeRecordRepository;
    private final ProductRepository productRepository;
    private final BlockchainService blockchainService;

    private static final Map<String, String> BEHAVIOR_NAMES = Map.ofEntries(
        Map.entry("walk", "步行出行"),
        Map.entry("bike", "骑行出行"),
        Map.entry("bus", "公交出行"),
        Map.entry("recycle", "垃圾分类"),
        Map.entry("oldGoods", "旧物回收"),
        Map.entry("savePower", "节约用电"),
        Map.entry("noPlastic", "拒绝一次性塑料"),
        Map.entry("plantTree", "植树护绿"),
        Map.entry("clean_plate", "光盘行动"),
        Map.entry("vegan", "绿色素食"),
        Map.entry("stairs", "走楼梯")
    );

    public PointsController(UserRepository userRepository,
                            BehaviorRecordRepository behaviorRecordRepository,
                            ExchangeRecordRepository exchangeRecordRepository,
                            ProductRepository productRepository,
                            BlockchainService blockchainService) {
        this.userRepository = userRepository;
        this.behaviorRecordRepository = behaviorRecordRepository;
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.productRepository = productRepository;
        this.blockchainService = blockchainService;
    }

    @GetMapping("/balance")
    public ApiResponse<PointsBalanceResponse> getBalance(@RequestAttribute("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        long total = user.getPointsBalance();
        long onChain = 0;
        try {
            if (user.getStudentNo() != null && !user.getStudentNo().isBlank()) {
                onChain = blockchainService.getOnChainBalance(user.getStudentNo());
            }
        } catch (Exception e) {
            // 区块链查询失败时进行优雅降级，视为和总分相等
            onChain = total;
        }

        long offChain = total - onChain;
        if (offChain < 0) {
            offChain = 0;
        }

        // 计算今日打卡获得的积分数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        List<BehaviorRecord> todayRecords = behaviorRecordRepository.findByUserIdAndDecisionAndCreatedAtBetween(
                userId, "PASS", todayStart, todayEnd
        );
        long todayEarned = todayRecords.stream().mapToLong(BehaviorRecord::getPoints).sum();

        return ApiResponse.ok(new PointsBalanceResponse(total, onChain, offChain, todayEarned));
    }

    @GetMapping("/transactions")
    public ApiResponse<TransactionsResponse> getTransactions(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "type", defaultValue = "all") String type
    ) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<TransactionItem> items = new ArrayList<>();

        // 1. 获取打卡通过的行为记录（如果是 income 或者 all）
        if ("all".equalsIgnoreCase(type) || "income".equalsIgnoreCase(type)) {
            List<BehaviorRecord> behaviors = behaviorRecordRepository.findByUserId(userId);
            for (BehaviorRecord b : behaviors) {
                if ("PASS".equalsIgnoreCase(b.getDecision())) {
                    String actionName = BEHAVIOR_NAMES.getOrDefault(b.getBehaviorType(), b.getBehaviorType());
                    String desc = actionName + "打卡";
                    items.add(new TransactionItem(
                        "b_" + b.getId(),
                        "income",
                        desc,
                        b.getPoints(),
                        b.getCreatedAt().format(dtf),
                        b.getTxHash()
                    ));
                }
            }
        }

        // 2. 获取商品兑换记录（如果是 expense 或者 all）
        if ("all".equalsIgnoreCase(type) || "expense".equalsIgnoreCase(type)) {
            List<ExchangeRecord> exchanges = exchangeRecordRepository.findByUserId(userId);
            for (ExchangeRecord e : exchanges) {
                String desc = "兑换商品";
                try {
                    Product product = productRepository.findById(e.getProductId()).orElse(null);
                    if (product != null) {
                        desc += ": " + product.getName();
                    } else {
                        desc += " [ID: " + e.getProductId() + "]";
                    }
                } catch (Exception ex) {
                    desc += " [ID: " + e.getProductId() + "]";
                }
                items.add(new TransactionItem(
                    "e_" + e.getId(),
                    "expense",
                    desc,
                    -e.getTotalPoints(), // 支出返回负数
                    e.getCreatedAt().format(dtf),
                    e.getTxHash()
                ));
            }
        }

        // 按照时间从新到老（降序）排序
        items.sort((t1, t2) -> t2.date().compareTo(t1.date()));

        long total = items.size();

        // 内存分页
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        List<TransactionItem> paged = Collections.emptyList();
        if (fromIndex < items.size()) {
            paged = items.subList(fromIndex, toIndex);
        }

        return ApiResponse.ok(new TransactionsResponse(paged, total));
    }
}
