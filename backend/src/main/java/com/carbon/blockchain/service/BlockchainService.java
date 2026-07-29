package com.carbon.blockchain.service;

import com.carbon.entity.BehaviorRecord;
import com.carbon.entity.ExchangeRecord;

/**
 * 区块链服务接口 — 对上链操作进行统一抽象。
 *
 * <p>设计目的:
 * <ol>
 *   <li>隔离后端业务与底层区块链 SDK</li>
 *   <li>支持未来切换链平台时只需替换实现</li>
 *   <li>统一处理异步/同步、重试、异常降级</li>
 * </ol>
 */
public interface BlockchainService {

    /**
     * 将行为记录上链存证（含积分铸造）。
     *
     * @param behavior  已落库的行为记录
     * @param studentNo 学生学号
     * @return 交易哈希
     */
    String recordBehavior(BehaviorRecord behavior, String studentNo);

    /**
     * 将兑换记录上链存证（含积分消耗）。
     *
     * @param exchange  已落库的兑换记录
     * @param studentNo 学生学号
     * @return 交易哈希
     */
    String recordExchange(ExchangeRecord exchange, String studentNo);

    /**
     * 查询链上积分余额（用于对账）。
     *
     * @param studentNo 学生学号
     * @return 链上积分余额
     */
    long getOnChainBalance(String studentNo);

    /**
     * 检查图片哈希是否已在链上存证。
     *
     * @param imageHashHex 图片 SHA-256 的十六进制字符串
     * @return true 表示图片已存在
     */
    boolean isImageHashExists(String imageHashHex);
}
