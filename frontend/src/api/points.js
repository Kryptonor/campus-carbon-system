/**
 * 积分中心 API — 对接后端 /api/points/*
 */
import { request } from './request'

export const pointsApi = {
  getBalance() {
    return request({
      url: '/points/balance',
      method: 'GET',
    })
  },

  getTransactions(page = 1, pageSize = 20, type = 'all') {
    return request({
      url: '/points/transactions',
      method: 'GET',
      data: { page, pageSize, type },
    })
  },

  getRewards() {
    return request({
      url: '/points/rewards',
      method: 'GET',
    })
  },

  redeem(rewardId) {
    return request({
      url: '/points/redeem',
      method: 'POST',
      data: { rewardId },
    })
  },

  getRedeemHistory(page = 1, pageSize = 10) {
    return request({
      url: '/points/redeem-history',
      method: 'GET',
      data: { page, pageSize },
    })
  },
}
