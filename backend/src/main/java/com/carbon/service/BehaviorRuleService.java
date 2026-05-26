package com.carbon.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BehaviorRuleService {
    private final Set<String> allowedBehaviors = Set.of("clean_plate", "recycle");
    private final Set<String> cleanPlateKeywords = Set.of(
        "clean plate",
        "empty plate",
        "plate",
        "dish",
        "leftovers",
        "no waste"
    );
    private final Set<String> recycleKeywords = Set.of(
        "recycle",
        "recycling",
        "trash",
        "garbage",
        "waste",
        "bottle",
        "paper",
        "can",
        "plastic"
    );
    private final SystemConfigService systemConfigService;

    public BehaviorRuleService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    public boolean isAllowedBehavior(String behaviorType) {
        return behaviorType != null && allowedBehaviors.contains(behaviorType);
    }

    public long getPoints(String behaviorType) {
        if ("clean_plate".equals(behaviorType)) {
            return systemConfigService.getLong("points.clean_plate", 10);
        }
        if ("recycle".equals(behaviorType)) {
            return systemConfigService.getLong("points.recycle", 5);
        }
        return 0L;
    }

    public boolean matchesLabel(String behaviorType, String label) {
        if (label == null || label.isBlank()) {
            return false;
        }
        if (!isAscii(label)) {
            return true;
        }
        String normalized = label.toLowerCase();
        if ("clean_plate".equals(behaviorType)) {
            return containsAny(normalized, cleanPlateKeywords);
        }
        if ("recycle".equals(behaviorType)) {
            return containsAny(normalized, recycleKeywords);
        }
        return false;
    }

    private boolean containsAny(String label, Set<String> keywords) {
        for (String keyword : keywords) {
            if (label.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAscii(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }
}
