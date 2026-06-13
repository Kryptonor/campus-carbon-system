/**
 * 排行榜 API — 对接后端 /api/leaderboard/*
 */
import { request } from './request'

export const leaderboardApi = {
  getRankList(type = 'carbon', scope = 'all', page = 1, pageSize = 20) {
    return request({
      url: '/leaderboard/list',
      method: 'GET',
      data: { type, scope, page, pageSize },
    })
  },
}
