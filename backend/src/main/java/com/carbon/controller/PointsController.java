package com.carbon.controller;

import com.carbon.blockchain.service.BlockchainService;
import com.carbon.blockchain.service.BlockchainTxQueue;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/points")
public class PointsController {
    private final UserRepository userRepository;
    private final BehaviorRecordRepository behaviorRecordRepository;
    private final ExchangeRecordRepository exchangeRecordRepository;
    private final ProductRepository productRepository;
    private final BlockchainService blockchainService;
    private final BlockchainTxQueue blockchainTxQueue;

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

    /** 根据商品名称关键词返回对应 emoji，用于 imageUrl 缺失或失效时的兜底 */
    private static String emojiForProduct(String name) {
        if (name == null) return "🎁";
        String n = name.toLowerCase();
        if (n.contains("帆布袋") || n.contains("袋") || n.contains("bag") || n.contains("tote")) return "🛍️";
        if (n.contains("咖啡") || n.contains("coffee") || (n.contains("券") && n.contains("饮"))) return "☕";
        if (n.contains("吸管") || n.contains("straw")) return "🥤";
        if (n.contains("笔记本") || n.contains("文具") || n.contains("notebook")) return "📓";
        if (n.contains("盆栽") || n.contains("绿植") || n.contains("多肉") || n.contains("plant") || n.contains("succulent")) return "🪴";
        if (n.contains("图书馆") || n.contains("选座") || n.contains("library")) return "📚";
        if (n.contains("代金券") || n.contains("食堂") || n.contains("voucher") || n.contains("coupon")) return "🎫";
        if (n.contains("水壶") || n.contains("水杯") || n.contains("杯子") || n.contains("bottle") || n.contains("cup") || n.contains("tumbler")) return "🏺";
        if (n.contains("环保") || n.contains("eco")) return "🌱";
        if (n.contains("运动") || n.contains("sport")) return "⚽";
        return "🎁";
    }

    /** 将商品英文名映射为中文名 */
    private static String toChineseProductName(String name) {
        if (name == null) return "环保商品";
        String lower = name.toLowerCase().trim();
        return switch (lower) {
            case "eco bag" -> "环保帆布袋";
            case "coffee coupon" -> "校园咖啡券";
            case "straw set" -> "不锈钢吸管套装";
            case "notebook" -> "校园文创笔记本";
            case "plant pot" -> "多肉植物盆栽";
            case "library pass" -> "图书馆优先选座卡";
            case "canteen voucher" -> "校园食堂代金券5元";
            case "sport bottle" -> "运动水壶";
            default -> name;
        };
    }

    public PointsController(UserRepository userRepository,
                            BehaviorRecordRepository behaviorRecordRepository,
                            ExchangeRecordRepository exchangeRecordRepository,
                            ProductRepository productRepository,
                            BlockchainService blockchainService,
                            BlockchainTxQueue blockchainTxQueue) {
        this.userRepository = userRepository;
        this.behaviorRecordRepository = behaviorRecordRepository;
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.productRepository = productRepository;
        this.blockchainService = blockchainService;
        this.blockchainTxQueue = blockchainTxQueue;
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

    // ==================== 兑换商城 ====================

    /**
     * 获取可兑换的奖品列表（即上架商品）。
     * 前端期望字段: id, image(emoji), name, description, pointsCost, stock
     */
    @GetMapping("/rewards")
    public ApiResponse<List<Map<String, Object>>> getRewards() {
        List<Product> products = productRepository.findAll();
        List<Map<String, Object>> rewards = products.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("image", p.getImageUrl() != null && !p.getImageUrl().isBlank() && !p.getImageUrl().startsWith("http") ? p.getImageUrl() : emojiForProduct(p.getName()));
                    m.put("name", toChineseProductName(p.getName()));
                    m.put("description", p.getDescription() != null ? p.getDescription() : "");
                    m.put("pointsCost", p.getPricePoints());
                    m.put("stock", p.getStock());
                    return m;
                })
                .collect(Collectors.toList());
        return ApiResponse.ok(rewards);
    }

    /**
     * 积分兑换奖品。
     * 请求体: { "rewardId": 1 }
     */
    @PostMapping("/redeem")
    @Transactional
    public ApiResponse<Map<String, Object>> redeem(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("rewardId").toString());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));

        if (product.getStock() <= 0) {
            throw new IllegalArgumentException("商品库存不足");
        }
        if (user.getPointsBalance() < product.getPricePoints()) {
            throw new IllegalArgumentException("积分不足");
        }

        // 扣减积分与库存
        user.setPointsBalance(user.getPointsBalance() - product.getPricePoints());
        product.setStock(product.getStock() - 1);
        userRepository.save(user);
        productRepository.save(product);

        // 创建兑换记录
        ExchangeRecord record = new ExchangeRecord();
        record.setUserId(user.getId());
        record.setProductId(product.getId());
        record.setAmount(1);
        record.setTotalPoints(product.getPricePoints());
        record.setStatus(0);
        record.setRedeemCode(UUID.randomUUID().toString().replace("-", ""));
        record.setRedeemStatus(0);
        ExchangeRecord saved = exchangeRecordRepository.save(record);

        // 异步上链
        blockchainTxQueue.submitExchange(saved);

        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("redeemCode", saved.getRedeemCode());
        return ApiResponse.ok("兑换成功", result);
    }

    /**
     * 获取用户兑换记录。
     * 前端期望字段: image, rewardName, redeemDate, pointsCost, code
     */
    @GetMapping("/redeem-history")
    public ApiResponse<Map<String, Object>> getRedeemHistory(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<ExchangeRecord> allRecords = exchangeRecordRepository.findByUserId(userId);
        // 按时间倒序
        allRecords.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> orders = allRecords.stream()
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", e.getId());
                    m.put("image", "🎁");
                    m.put("rewardName", "");
                    m.put("redeemDate", e.getCreatedAt().format(dtf));
                    m.put("pointsCost", e.getTotalPoints());
                    m.put("code", e.getRedeemCode());
                    // 补全商品名称和图标
                    try {
                        Product p = productRepository.findById(e.getProductId()).orElse(null);
                        if (p != null) {
                            m.put("rewardName", toChineseProductName(p.getName()));
                            m.put("image", p.getImageUrl() != null && !p.getImageUrl().isBlank() && !p.getImageUrl().startsWith("http") ? p.getImageUrl() : emojiForProduct(p.getName()));
                        }
                    } catch (Exception ignore) {}
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("total", allRecords.size());
        return ApiResponse.ok(result);
    }
}
