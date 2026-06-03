package com.carbon.blockchain.exception;

/**
 * 区块链操作异常 — 上链失败、合约调用异常等场景的统一异常。
 */
public class BlockchainException extends RuntimeException {

    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}
