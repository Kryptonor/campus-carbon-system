package com.carbon.blockchain.config;

import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 区块链 SDK 初始化配置。
 *
 * <p>Spring Boot 启动时自动完成三件事:
 * <ol>
 *   <li>加载 config-example.toml → 初始化 BcosSDK</li>
 *   <li>获取群组 Client（用于 RPC 通信）</li>
 *   <li>创建 AssembleTransactionProcessor（用于合约调用，基于 ABI/BIN 文件）</li>
 * </ol>
 *
 * <p>前置条件: resources/conf/ 下已放置 SDK 证书（ca.crt / sdk.crt / sdk.key），
 * resources/abi/ 和 resources/bin/ 下已放置合约 ABI 和 BIN 文件。
 */
@Configuration
public class BlockchainConfig {

    private static final Logger log = LoggerFactory.getLogger(BlockchainConfig.class);

    @Bean(destroyMethod = "stopAll")
    public BcosSDK bcosSDK() throws IOException {
        // config-example.toml 需放在 classpath 根目录（resources/）
        // 由于 FISCO BCOS SDK 加载配置需要文件系统路径，这里做 classpath → 实际路径转换
        String configPath = resolveClasspath("config-example.toml");
        log.info("Initializing BcosSDK from: {}", configPath);
        return new BcosSDK(configPath);
    }

    @Bean
    public Client client(BcosSDK bcosSDK) {
        // group0 是建链时的默认群组
        Client client = bcosSDK.getClient("group0");
        if (client == null) {
            // FISCO BCOS 3.x 可能使用数字形式
            client = bcosSDK.getClient(0);
        }
        if (client == null) {
            throw new IllegalStateException("Failed to get FISCO BCOS client — check config-example.toml peers and group");
        }
        log.info("Connected to FISCO BCOS group, block number: {}", client.getBlockNumber().getBlockNumber());
        return client;
    }

    @Bean
    public CryptoKeyPair credential(Client client) {
        CryptoKeyPair keyPair = client.getCryptoSuite().getCryptoKeyPair();
        log.info("Blockchain admin address: {}", keyPair.getAddress());
        return keyPair;
    }

    @Bean
    public AssembleTransactionProcessor transactionProcessor(
            Client client,
            CryptoKeyPair credential) throws IOException {

        String abiPath = resolveClasspath("abi/");
        String binPath = resolveClasspath("bin/");

        // 确保 ABI/BIN 目录存在
        Path abiDir = Paths.get(abiPath);
        Path binDir = Paths.get(binPath);
        if (!Files.exists(abiDir)) {
            Files.createDirectories(abiDir);
            log.warn("ABI directory created but empty: {}", abiDir.toAbsolutePath());
        }
        if (!Files.exists(binDir)) {
            Files.createDirectories(binDir);
            log.warn("BIN directory created but empty: {}", binDir.toAbsolutePath());
        }

        log.info("AssembleTransactionProcessor initialized with abi={}, bin={}", abiPath, binPath);
        return TransactionProcessorFactory.createAssembleTransactionProcessor(
                client, credential, abiPath, binPath);
    }

    // ============ 辅助方法 ============

    /**
     * 将 classpath 相对路径解析为实际文件系统路径。
     * 开发阶段直接指向 src/main/resources/ 下的文件。
     */
    private String resolveClasspath(String resource) throws IOException {
        // 优先尝试 classpath
        java.net.URL url = getClass().getClassLoader().getResource(resource);
        if (url != null) {
            return Paths.get(url.toURI()).toString();
        }
        // 回退: 相对于 working directory 的 src/main/resources/
        Path fallback = Paths.get("backend", "src", "main", "resources", resource);
        if (Files.exists(fallback)) {
            return fallback.toAbsolutePath().toString();
        }
        // 再回退: 直接相对于 working directory
        Path direct = Paths.get("src", "main", "resources", resource);
        if (Files.exists(direct)) {
            return direct.toAbsolutePath().toString();
        }
        throw new IOException("Cannot resolve classpath resource: " + resource
                + " — ensure config-example.toml, abi/, bin/ exist under resources/");
    }
}
