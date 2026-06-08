/**
 * FISCO BCOS 区块链服务层
 * 通过后端 API 代理与区块链交互（Spring Boot + Web3j）
 */
import { API_BASE_URL, CONTRACT_ADDRESS } from './constants'
import { request } from '@/api/request'

export const blockchainService = {
  /**
   * 查询链上积分余额
   */
  async getOnChainBalance(address) {
    return request({
      url: '/blockchain/points-balance',
      method: 'GET',
      data: { address },
    })
  },

  /**
   * 查询最近链上交易记录
   */
  async getRecentTransactions(address, page = 1, pageSize = 10) {
    return request({
      url: '/blockchain/transactions',
      method: 'GET',
      data: { address, page, pageSize },
    })
  },

  /**
   * 查询碳减排行为上链记录
   */
  async getActionRecords(address, page = 1, pageSize = 10) {
    return request({
      url: '/blockchain/action-records',
      method: 'GET',
      data: { address, page, pageSize },
    })
  },

  /**
   * 获取链上合约信息
   */
  async getContractInfo() {
    return request({
      url: '/blockchain/contract-info',
      method: 'GET',
    })
  },
}

/**
 * 直接与区块链交互的工具函数（当后端未就绪时的备选方案）
 * 使用 FISCO BCOS 的 webauthn 签名方式
 * 注意：微信小程序环境需要特殊的 Web3 适配方案
 */

// 格式化链上地址显示
export function formatAddress(address) {
  if (!address || address.length < 12) return address
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}

// 格式化交易哈希显示
export function formatTxHash(hash) {
  if (!hash || hash.length < 16) return hash
  return `${hash.slice(0, 8)}...${hash.slice(-6)}`
}

// 将积分转换为链上最小单位（假设 18 位精度）
export function toTokenDecimals(amount, decimals = 18) {
  return Math.floor(amount * Math.pow(10, decimals)).toString()
}

// 将链上最小单位转换回积分
export function fromTokenDecimals(amount, decimals = 18) {
  return Number(amount) / Math.pow(10, decimals)
}
