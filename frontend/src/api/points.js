/**
 * 积分中心 API — 对接后端 /api/points/*
 */
import { request } from './request'
import { useUserStore } from '@/stores/user'

function getUserId() {
  const store = useUserStore()
  return store.userInfo?.id || 0
}

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
      url: '/products',
      method: 'GET',
    })
  },

  redeem(productId) {
    return request({
      url: '/exchanges',
      method: 'POST',
      data: { userId: getUserId(), productId, amount: 1 },
    })
  },

  getRedeemHistory(page = 1, pageSize = 10) {
    return request({
      url: '/exchanges',
      method: 'GET',
      data: { userId: getUserId(), page: page - 1, size: pageSize },
    })
  },
}
