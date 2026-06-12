package com.carbon.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BehaviorRuleService {
    private final Set<String> allowedBehaviors = Set.of(
        "clean_plate", "recycle", "walk", "bike", "bus",
        "oldGoods", "savePower", "noPlastic", "plantTree", "vegan", "stairs"
    );
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
        if ("walk".equals(behaviorType)) {
            return systemConfigService.getLong("points.walk", 10);
        }
        if ("bike".equals(behaviorType)) {
            return systemConfigService.getLong("points.bike", 15);
        }
        if ("bus".equals(behaviorType)) {
            return systemConfigService.getLong("points.bus", 20);
        }
        if ("oldGoods".equals(behaviorType)) {
            return systemConfigService.getLong("points.oldGoods", 30);
        }
        if ("savePower".equals(behaviorType)) {
            return systemConfigService.getLong("points.savePower", 8);
        }
        if ("noPlastic".equals(behaviorType)) {
            return systemConfigService.getLong("points.noPlastic", 12);
        }
        if ("plantTree".equals(behaviorType)) {
            return systemConfigService.getLong("points.plantTree", 50);
        }
        if ("vegan".equals(behaviorType)) {
            return systemConfigService.getLong("points.vegan", 10);
        }
        if ("stairs".equals(behaviorType)) {
            return systemConfigService.getLong("points.stairs", 6);
        }
        return 0L;
    }

    public boolean matchesLabel(String behaviorType, String label) {
        if (label == null || label.isBlank()) {
            return false;
        }
        String normalized = label.toLowerCase();
        if ("clean_plate".equals(behaviorType)) {
            return containsAny(normalized, cleanPlateKeywords) || containsAny(normalized, Set.of(
                "光盘", "空盘", "餐盘", "吃完", "剩饭", "剩菜", "光盘行动", "吃光", "干净", "盘子", "碗", "饭碗", "空碗", "洗碗"
            ));
        }
        if ("recycle".equals(behaviorType)) {
            return containsAny(normalized, recycleKeywords) || containsAny(normalized, Set.of(
                "垃圾", "回收", "分类", "塑料瓶", "废纸", "易拉罐", "垃圾桶", "纸箱", "纸盒", "玻璃瓶", "可回收", "饮料瓶", "金属"
            ));
        }
        if ("walk".equals(behaviorType)) {
            return containsAny(normalized, Set.of("walk", "walking", "pedestrian", "footpath", "走路", "步行", "散步", "行人", "人行道"));
        }
        if ("bike".equals(behaviorType)) {
            return containsAny(normalized, Set.of("bike", "bicycle", "cycling", "ride", "自行车", "骑行", "单车", "骑车"));
        }
        if ("bus".equals(behaviorType)) {
            return containsAny(normalized, Set.of("bus", "subway", "metro", "transit", "公交", "巴士", "地铁", "公共交通"));
        }
        if ("oldGoods".equals(behaviorType)) {
            return containsAny(normalized, Set.of("secondhand", "used", "recycle", "old", "旧物", "二手", "回收", "旧货"));
        }
        if ("savePower".equals(behaviorType)) {
            return containsAny(normalized, Set.of("switch", "off", "light", "power", "energy", "开关", "关闭", "节电", "电源", "灯"));
        }
        if ("noPlastic".equals(behaviorType)) {
            return containsAny(normalized, Set.of("bag", "cloth", "cup", "no plastic", "环保袋", "布袋", "自带杯", "无塑料", "纸袋"));
        }
        if ("plantTree".equals(behaviorType)) {
            return containsAny(normalized, Set.of("tree", "plant", "garden", "green", "树", "植树", "植物", "绿化", "盆栽"));
        }
        if ("vegan".equals(behaviorType)) {
            return containsAny(normalized, Set.of(
                "vegan", "vegetable", "salad", "fruit", "vegetarian", 
                "蔬菜", "水果", "沙拉", "素食", "健康餐", "素菜", 
                "西兰花", "黄瓜", "番茄", "西红柿", "生菜", "胡萝卜", "白菜", "青菜", "菠菜", "芹菜", "茄子", "土豆", "马铃薯", "玉米", "红薯", 
                "豆腐", "豆浆", "豆制品", "苹果", "香蕉", "橙子", "橘子", "西瓜", "草莓", "葡萄", "梨", "桃", "芒果", "柠檬", "坚果", "燕麦"
            ));
        }
        if ("stairs".equals(behaviorType)) {
            return containsAny(normalized, Set.of(
                "stair", "stairs", "staircase", "step", "楼梯", "台阶", "走楼梯", "阶梯", "通道", "消防通道"
            ));
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
