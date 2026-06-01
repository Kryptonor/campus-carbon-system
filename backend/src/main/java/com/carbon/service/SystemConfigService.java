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
        refreshCache();
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
