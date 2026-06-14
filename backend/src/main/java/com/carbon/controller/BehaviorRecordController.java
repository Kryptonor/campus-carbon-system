package com.carbon.controller;

import com.carbon.entity.BehaviorRecord;
import com.carbon.service.BehaviorRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.carbon.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class BehaviorRecordController {
    private final BehaviorRecordService behaviorRecordService;

    private static final Map<String, String> CATEGORY_NAMES = Map.ofEntries(
        Map.entry("walk", "出行"), Map.entry("bike", "出行"), Map.entry("bus", "出行"),
        Map.entry("recycle", "废弃物"), Map.entry("oldGoods", "废弃物"),
        Map.entry("savePower", "用电"), Map.entry("noPlastic", "购物"),
        Map.entry("plantTree", "出行"), Map.entry("clean_plate", "餐饮"),
        Map.entry("vegan", "餐饮"), Map.entry("stairs", "出行")
    );
    private static final Map<String, String> CATEGORY_ICONS = Map.ofEntries(
        Map.entry("walk", "🚶"), Map.entry("bike", "🚲"), Map.entry("bus", "🚌"),
        Map.entry("recycle", "♻️"), Map.entry("oldGoods", "📦"), Map.entry("savePower", "💡"),
        Map.entry("noPlastic", "🛍️"), Map.entry("plantTree", "🌳"), Map.entry("clean_plate", "🍽️"),
        Map.entry("vegan", "🥬"), Map.entry("stairs", "🪜")
    );

    public BehaviorRecordController(BehaviorRecordService behaviorRecordService) {
        this.behaviorRecordService = behaviorRecordService;
    }

    // 原有: /api/behaviors
    @GetMapping("/api/behaviors")
    public ApiResponse<Page<BehaviorRecord>> list(
            @RequestParam(value = "userId", required = false) Long userId,
            Pageable pageable
    ) {
        return ApiResponse.ok(behaviorRecordService.list(userId, pageable));
    }

    // 前端兼容: /api/record/list
    @GetMapping("/api/record/list")
    public ApiResponse<Map<String, Object>> recordList(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "50") int pageSize) {

        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BehaviorRecord> pageData = behaviorRecordService.list(userId, pageable);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<Map<String, Object>> records = new ArrayList<>();
        for (BehaviorRecord r : pageData.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("category", CATEGORY_NAMES.getOrDefault(r.getBehaviorType(), r.getBehaviorType()));
            item.put("categoryIcon", CATEGORY_ICONS.getOrDefault(r.getBehaviorType(), "📝"));
            item.put("notes", r.getAiLabel() != null ? r.getAiLabel() : "");
            item.put("date", r.getCreatedAt().format(dtf));
            item.put("carbonAmount", String.format("%.2f", r.getPoints() * 0.1));
            item.put("quantity", "1次");
            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageData.getTotalElements());
        return ApiResponse.ok(result);
    }
}
