/**
 * 首页仪表盘相关 API — 对接后端 /api/home/*
 */
import { request } from './request'

export const homeApi = {
  getDailySummary() {
    return request({
      url: '/home/daily-summary',
      method: 'GET',
    })
  },

  getQuickActions() {
    return request({
      url: '/home/quick-actions',
      method: 'GET',
    })
  },

  getCarbonTrend(period = 'week') {
    return request({
      url: '/home/carbon-trend',
      method: 'GET',
      data: { period },
    })
  },
}
