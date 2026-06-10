package com.carbon.service;

import com.carbon.dao.SystemConfigRepository;
import com.carbon.entity.SystemConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SystemConfigService {
    private final SystemConfigRepository systemConfigRepository;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public SystemConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    @PostConstruct
    public void loadAll() {
        initDefaultConfigsIfNeeded();
        refreshCache();
    }

    private void initDefaultConfigsIfNeeded() {
        try {
            long count = systemConfigRepository.count();
            if (count == 0) {
                java.util.List<SystemConfig> defaults = java.util.List.of(
                    createConfig("ai_threshold", "0.80", "AI confidence threshold for green action verification"),
                    createConfig("daily_limit", "3", "Daily check-in limit per user"),
                    createConfig("points.clean_plate", "10", "Points for clean plate action"),
                    createConfig("points.recycle", "5", "Points for recycle action"),
                    createConfig("points.walk", "10", "Points for walk action"),
                    createConfig("points.bike", "15", "Points for bike action"),
                    createConfig("points.bus", "20", "Points for bus action"),
                    createConfig("points.oldGoods", "30", "Points for oldGoods action"),
                    createConfig("points.savePower", "8", "Points for savePower action"),
                    createConfig("points.noPlastic", "12", "Points for noPlastic action"),
                    createConfig("points.plantTree", "50", "Points for plantTree action"),
                    createConfig("points.vegan", "10", "Points for vegan action"),
                    createConfig("points.stairs", "6", "Points for stairs action"),
                    createConfig("carbon.exchange.rate", "0.10", "Carbon point to carbon reduction rate")
                );
                systemConfigRepository.saveAll(defaults);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize default system configs: " + e.getMessage());
        }
    }

    private SystemConfig createConfig(String key, String value, String desc) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigDesc(desc);
        config.setUpdatedAt(java.time.LocalDateTime.now());
        return config;
    }

    public void refreshCache() {
        cache.clear();
        for (SystemConfig config : systemConfigRepository.findAll()) {
            cache.put(config.getConfigKey(), config.getConfigValue());
        }
    }

    public String getString(String key, String defaultValue) {
        String value = cache.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public double getDouble(String key, double defaultValue) {
        String value = cache.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = cache.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
