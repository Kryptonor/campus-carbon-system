package com.carbon.blockchain.service;

import com.carbon.blockchain.config.BlockchainProperties;
import com.carbon.blockchain.exception.BlockchainException;
import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.ExchangeRecord;
import org.fisco.bcos.sdk.v3.model.TransactionResponse;
import org.fisco.bcos.sdk.v3.model.callback.CallResponse;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

/**
 * 区块链服务实现 — 通过 ABI/BIN 文件调用 FISCO BCOS 合约。
 *
 * <p>三份合约的职责分工:
 * <ul>
 *   <li><b>CarbonPoints</b> — 积分铸造 (mint) 和消耗 (burn)</li>
 *   <li><b>AuditTrail</b> — 行为存证 (recordBehavior) 和兑换存证 (recordExchange)</li>
 * </ul>
 *
 * <p>上链模式: 异步提交 + 失败补录（由 {@link BlockchainTxQueue} 负责）。
 */
@Service
public class BlockchainServiceImpl implements BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainServiceImpl.class);

    /** FISCO BCOS 3.x AssembleTransactionProcessor 使用的合约名（不含 .sol 后缀） */
    private static final String CONTRACT_CARBON_POINTS = "CarbonPoints";
    private static final String CONTRACT_AUDIT_TRAIL = "AuditTrail";

    private final AssembleTransactionProcessor processor;
    private final BlockchainProperties properties;

    public BlockchainServiceImpl(AssembleTransactionProcessor processor,
                                 BlockchainProperties properties) {
        this.processor = processor;
        this.properties = properties;
    }

    // ==================== 行为上链 ====================

    @Override
    public String recordBehavior(BehaviorRecord behavior, String studentNo) {
        String cpAddress = properties.getContracts().getCarbonPoints();
        String atAddress = properties.getContracts().getAuditTrail();

        // 1. 存证上链 — AuditTrail.recordBehavior(...)
        List<Object> auditParams = buildBehaviorParams(behavior, studentNo);
        TransactionResponse auditTx = processor.sendTransactionAndGetResponseByContractLoader(
                CONTRACT_AUDIT_TRAIL, atAddress, "recordBehavior", auditParams);
        checkTxResponse(auditTx, "recordBehavior");

        String txHash = auditTx.getTransactionReceipt().getTransactionHash();
        log.info("Behavior recorded on chain: recordId={}, txHash={}", behavior.getId(), txHash);

        // 2. 如果 AI 判定通过，铸造积分 — CarbonPoints.mint(...)
        if ("PASS".equals(behavior.getDecision()) && behavior.getPoints() > 0) {
            List<Object> mintParams = Arrays.asList(
                    studentNo,
                    BigInteger.valueOf(behavior.getPoints()),
                    String.valueOf(behavior.getId())
            );
            TransactionResponse mintTx = processor.sendTransactionAndGetResponseByContractLoader(
                    CONTRACT_CARBON_POINTS, cpAddress, "mint", mintParams);
            checkTxResponse(mintTx, "mint");
            log.info("Points minted on chain: studentNo={}, amount={}, mintTxHash={}",
                    studentNo, behavior.getPoints(),
                    mintTx.getTransactionReceipt().getTransactionHash());
        }

        return txHash;
    }

    // ==================== 兑换上链 ====================

    @Override
    public String recordExchange(ExchangeRecord exchange, String studentNo) {
        String cpAddress = properties.getContracts().getCarbonPoints();
        String atAddress = properties.getContracts().getAuditTrail();

        // 1. 先消耗积分 — CarbonPoints.burn(...)
        List<Object> burnParams = Arrays.asList(
                studentNo,
                BigInteger.valueOf(exchange.getTotalPoints()),
                String.valueOf(exchange.getId())
        );
        TransactionResponse burnTx = processor.sendTransactionAndGetResponseByContractLoader(
                CONTRACT_CARBON_POINTS, cpAddress, "burn", burnParams);
        checkTxResponse(burnTx, "burn");

        // 2. 存证上链 — AuditTrail.recordExchange(...)
        byte[] redeemCodeHashBytes = sha256(exchange.getRedeemCode());
        byte[] redeemCodeHash32 = Arrays.copyOf(redeemCodeHashBytes, 32);

        List<Object> auditParams = new ArrayList<>();
        auditParams.add(BigInteger.valueOf(exchange.getId()));
        auditParams.add(studentNo);
        auditParams.add(BigInteger.valueOf(exchange.getProductId()));
        auditParams.add(BigInteger.valueOf(exchange.getAmount()));
        auditParams.add(BigInteger.valueOf(exchange.getTotalPoints()));
        auditParams.add(redeemCodeHash32);

        TransactionResponse auditTx = processor.sendTransactionAndGetResponseByContractLoader(
                CONTRACT_AUDIT_TRAIL, atAddress, "recordExchange", auditParams);
        checkTxResponse(auditTx, "recordExchange");

        String txHash = auditTx.getTransactionReceipt().getTransactionHash();
        log.info("Exchange recorded on chain: recordId={}, txHash={}", exchange.getId(), txHash);
        return txHash;
    }

    // ==================== 查询 ====================

    @Override
    public long getOnChainBalance(String studentNo) {
        try {
            String cpAddress = properties.getContracts().getCarbonPoints();
            List<Object> params = new ArrayList<>();
            params.add(studentNo);

            CallResponse response = processor.sendCallByContractLoader(
                    CONTRACT_CARBON_POINTS, cpAddress, "balanceOf", params);
            List<Object> values = response.getValues();
            if (values != null && !values.isEmpty()) {
                Object val = values.get(0);
                if (val instanceof BigInteger bi) {
                    return bi.longValue();
                }
                if (val instanceof Number num) {
                    return num.longValue();
                }
            }
            return 0L;
        } catch (Exception e) {
            log.error("Failed to query on-chain balance for {}", studentNo, e);
            return 0L;
        }
    }

    @Override
    public boolean isImageHashExists(String imageHashHex) {
        try {
            String atAddress = properties.getContracts().getAuditTrail();
            byte[] hashBytes = HexFormat.of().parseHex(imageHashHex);
            List<Object> params = new ArrayList<>();
            params.add(hashBytes);

            CallResponse response = processor.sendCallByContractLoader(
                    CONTRACT_AUDIT_TRAIL, atAddress, "isImageHashExists", params);
            List<Object> values = response.getValues();
            if (values != null && !values.isEmpty() && values.get(0) instanceof Boolean b) {
                return b;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to check image hash on chain: {}", imageHashHex, e);
            return false;
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 构造 AuditTrail.recordBehavior 的参数列表。
     * Solidity 签名:
     * recordBehavior(uint256, string, string, bytes32, string, uint8, uint256, string)
     */
    private List<Object> buildBehaviorParams(BehaviorRecord b, String studentNo) {
        // imageHash: 把 hex 字符串转成 32 字节的 byte[]
        byte[] hashBytes = HexFormat.of().parseHex(b.getImageHash());

        // aiScore: BigDecimal 0.xx → 整数 0-100
        int scoreInt = b.getAiScore().multiply(new java.math.BigDecimal(100)).intValue();

        return Arrays.asList(
                BigInteger.valueOf(b.getId()),
                studentNo,
                b.getBehaviorType(),
                hashBytes,
                b.getAiLabel() != null ? b.getAiLabel() : "",
                BigInteger.valueOf(scoreInt),
                BigInteger.valueOf(b.getPoints()),
                b.getDecision()
        );
    }

    /**
     * 校验交易回执是否成功。
     */
    private void checkTxResponse(TransactionResponse response, String action) {
        if (response == null || response.getTransactionReceipt() == null) {
            throw new BlockchainException(action + " returned null response");
        }
        String msg = response.getTransactionReceipt().getMessage();
        if (msg != null && !msg.isEmpty() && !"Success".equals(msg)) {
            throw new BlockchainException(action + " failed: " + msg);
        }
    }

    private byte[] sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
