/**
 * 绿色行动 API — 对接后端 /api/actions/*
 */
import { request } from './request'

export const actionsApi = {
  getActionList() {
    return request({
      url: '/actions/list',
      method: 'GET',
    })
  },

  checkin(data) {
    return request({
      url: '/actions/checkin',
      method: 'POST',
      data,
    })
  },

  getMyHistory(page = 1, pageSize = 10) {
    return request({
      url: '/actions/my-history',
      method: 'GET',
      data: { page, pageSize },
    })
  },
}
