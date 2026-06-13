package com.carbon.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class BehaviorRuleService {
    private final Set<String> allowedBehaviors = Set.of(
        "clean_plate", "recycle", "walk", "bike", "bus",
        "oldGoods", "savePower", "noPlastic", "plantTree", "vegan", "stairs"
    );

    // ---- 百度 AI root 分类 → 行为类型 映射（治本方案） ----
    // root 是百度内建固定分类体系，不会随图片变化。西兰花/花椰菜/甘蓝的 root 都是"植物-蔬菜"
    private static final Map<String, Set<String>> ROOT_CATEGORY_RULES = Map.ofEntries(
        Map.entry("vegan",        Set.of("蔬菜", "水果", "食材", "食物", "餐饮", "烹调", "素食", "农产品")),
        Map.entry("clean_plate",  Set.of("餐具", "餐桌", "盘子", "碗", "碟", "餐饮", "食堂")),
        Map.entry("bike",         Set.of("自行车", "单车", "骑行")),
        Map.entry("bus",          Set.of("公交", "巴士", "地铁", "火车", "高铁", "客车", "交通")),
        Map.entry("plantTree",    Set.of("树", "植物", "花", "草", "叶", "森林", "自然")),
        Map.entry("stairs",       Set.of("楼梯", "台阶", "阶梯")),
        Map.entry("recycle",      Set.of("垃圾", "回收", "废品", "废物")),
        Map.entry("savePower",    Set.of("灯", "电器", "电源", "开关", "插座")),
        Map.entry("noPlastic",    Set.of("袋", "环保", "购物", "容器")),
        Map.entry("walk",         Set.of("行人", "道路", "街道", "人行", "户外")),
        Map.entry("oldGoods",     Set.of("二手", "旧", "闲置", "捐赠"))
    );

    // ---- 关键词兜底（root 为空或不匹配时使用） ----
    // 存储百度 AI 经常返回的「具体物体名」，覆盖 root 分类缺失或异常的情况
    private static final Map<String, Set<String>> FALLBACK_KEYWORDS = Map.ofEntries(
        Map.entry("vegan", Set.of(
            "vegan","vegetable","salad","fruit","vegetarian","organic","produce","greens",
            "蔬菜","水果","沙拉","素食","素菜","健康餐","蔬食","果蔬","炒菜","凉拌",
            "西兰花","芦笋","青菜","白菜","菠菜","生菜","西红柿","番茄","黄瓜","胡萝卜",
            "土豆","豆腐","米饭","苹果","香蕉","橙子","葡萄","草莓","花椰菜","芹菜",
            "青椒","玉米","蘑菇","南瓜","茄子","豆角","豆芽","辣椒","洋葱","大蒜"
        )),
        Map.entry("clean_plate", Set.of(
            "clean plate","empty plate","plate","dish","leftovers","no waste","meal",
            "光盘","空盘","餐盘","吃完","剩饭","剩菜","光盘行动","盘子","碗","餐具","餐桌","食堂","饭菜","打包"
        )),
        Map.entry("recycle", Set.of(
            "recycle","recycling","trash","garbage","waste","bottle","paper","can","plastic","bin",
            "垃圾","回收","分类","塑料瓶","废纸","易拉罐","垃圾桶","纸箱","玻璃瓶","瓶子"
        )),
        Map.entry("walk", Set.of(
            "walk","walking","pedestrian","running","jogging","hiking",
            "走路","步行","散步","行人","跑步","慢跑","徒步","健走"
        )),
        Map.entry("bike", Set.of(
            "bike","bicycle","cycling","ride","bicyclist","cyclist",
            "自行车","骑行","单车","骑车","脚踏车","共享单车"
        )),
        Map.entry("bus", Set.of(
            "bus","subway","metro","transit","train","station",
            "公交","巴士","地铁","公共交通","公交车","公共汽车","火车","高铁","大巴"
        )),
        Map.entry("oldGoods", Set.of(
            "secondhand","used","old","reuse","donation","thrift",
            "旧物","二手","回收","旧货","闲置","捐赠","旧书","旧衣服"
        )),
        Map.entry("savePower", Set.of(
            "switch off","light","power","energy","electricity","lamp","bulb","turn off",
            "开关","关闭","节电","电源","灯","插座","电器","节能","电灯","灯泡","关灯","省电"
        )),
        Map.entry("noPlastic", Set.of(
            "bag","cloth","cup","no plastic","reusable","eco bag","paper bag","tumbler",
            "环保袋","布袋","自带杯","无塑料","纸袋","环保","购物袋","帆布袋","保温杯"
        )),
        Map.entry("plantTree", Set.of(
            "tree","plant","garden","green","planting","seedling","forest","leaf","flower","grass",
            "树","植树","植物","绿化","盆栽","花草","叶子","森林","种树","树木","树苗","花","草"
        )),
        Map.entry("stairs", Set.of(
            "stair","stairs","staircase","step","stairway","floor","climb",
            "楼梯","台阶","走楼梯","阶梯","楼层","爬楼","上楼","下楼","楼道"
        ))
    );

    private final SystemConfigService systemConfigService;

    public BehaviorRuleService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    public boolean isAllowedBehavior(String behaviorType) {
        return behaviorType != null && allowedBehaviors.contains(behaviorType);
    }

    public long getPoints(String behaviorType) {
        if ("clean_plate".equals(behaviorType)) return systemConfigService.getLong("points.clean_plate", 10);
        if ("recycle".equals(behaviorType))    return systemConfigService.getLong("points.recycle", 5);
        if ("walk".equals(behaviorType))       return systemConfigService.getLong("points.walk", 10);
        if ("bike".equals(behaviorType))       return systemConfigService.getLong("points.bike", 15);
        if ("bus".equals(behaviorType))        return systemConfigService.getLong("points.bus", 20);
        if ("oldGoods".equals(behaviorType))   return systemConfigService.getLong("points.oldGoods", 30);
        if ("savePower".equals(behaviorType))  return systemConfigService.getLong("points.savePower", 8);
        if ("noPlastic".equals(behaviorType))  return systemConfigService.getLong("points.noPlastic", 12);
        if ("plantTree".equals(behaviorType))  return systemConfigService.getLong("points.plantTree", 50);
        if ("vegan".equals(behaviorType))      return systemConfigService.getLong("points.vegan", 10);
        if ("stairs".equals(behaviorType))     return systemConfigService.getLong("points.stairs", 6);
        return 0L;
    }

    /**
     * 主匹配方法：先用 root 分类匹配，再用 keyword 兜底。
     * @param rootCategory 百度 AI 返回的 root 字段（如 "植物-蔬菜"、"交通工具-自行车"）
     * @param label        百度 AI 返回的 keyword 字段（如 "西兰花"、"自行车"）
     */
    public boolean matches(String behaviorType, String rootCategory, String label) {
        if (behaviorType == null) return false;

        // 优先用百度 root 分类匹配（固定分类体系，覆盖广）
        if (rootCategory != null && !rootCategory.isBlank()) {
            if (matchesRootCategory(behaviorType, rootCategory)) {
                return true;
            }
        }

        // root 为空或不匹配时，回退到 keyword 关键词匹配
        if (label != null && !label.isBlank()) {
            return matchesLabel(behaviorType, label);
        }

        return false;
    }

    /**
     * 基于百度 AI root 分类层级的匹配（治本方案）。
     * root 是百度内建固定分类，如 "植物-蔬菜"、"交通工具-自行车"
     */
    public boolean matchesRootCategory(String behaviorType, String rootCategory) {
        Set<String> keywords = ROOT_CATEGORY_RULES.get(behaviorType);
        if (keywords == null || rootCategory == null) return false;
        String normalized = rootCategory.toLowerCase();
        for (String keyword : keywords) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 基于 AI label/keyword 的关键词匹配（兜底方案）。
     * 当 root 分类不可用或不匹配时使用。
     */
    public boolean matchesLabel(String behaviorType, String label) {
        if (label == null || label.isBlank()) {
            return false;
        }
        String normalized = label.toLowerCase().trim();
        Set<String> keywords = FALLBACK_KEYWORDS.get(behaviorType);
        if (keywords == null) return false;
        return containsAny(normalized, keywords);
    }

    private boolean containsAny(String label, Set<String> keywords) {
        for (String keyword : keywords) {
            if (label.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
