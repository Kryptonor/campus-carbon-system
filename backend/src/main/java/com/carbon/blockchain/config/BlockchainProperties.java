package com.carbon.blockchain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 区块链配置属性 — 映射 application.yml 中 carbon.blockchain 段。
 */
@Component
@ConfigurationProperties(prefix = "carbon.blockchain")
public class BlockchainProperties {

    /** 是否启用区块链（开发阶段可设为 false 跳过所有上链操作） */
    private boolean enabled = true;

    /** 是否异步上链（true: 不阻塞业务响应, false: 同步等交易回执） */
    private boolean asyncEnabled = true;

    /** 上链失败重试次数 */
    private int maxRetry = 3;

    /** 合约地址配置 */
    private Contracts contracts = new Contracts();

    // --- getters / setters ---

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public Contracts getContracts() {
        return contracts;
    }

    public void setContracts(Contracts contracts) {
        this.contracts = contracts;
    }

    // --- inner classes ---

    public static class Contracts {
        private String carbonPoints;
        private String auditTrail;

        public String getCarbonPoints() {
            return carbonPoints;
        }

        public void setCarbonPoints(String carbonPoints) {
            this.carbonPoints = carbonPoints;
        }

        public String getAuditTrail() {
            return auditTrail;
        }

        public void setAuditTrail(String auditTrail) {
            this.auditTrail = auditTrail;
        }
    }
}
