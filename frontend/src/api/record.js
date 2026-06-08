/**
 * 碳足迹记录 API — 对接后端 /api/record/* 和 /api/ai/*
 */
import { request } from './request'

export const recordApi = {
  getList(page = 1, pageSize = 10, type = 'all') {
    return request({
      url: '/record/list',
      method: 'GET',
      data: { page, pageSize, type },
    })
  },

  getDetail(id) {
    return request({
      url: '/record/detail',
      method: 'GET',
      data: { id },
    })
  },

  create(data) {
    return request({
      url: '/record/create',
      method: 'POST',
      data,
    })
  },

  deleteRecord(id) {
    return request({
      url: '/record/delete',
      method: 'POST',
      data: { id },
    })
  },

  /**
   * AI 图片分析 — 拍照上传后调用 AI 服务识别碳足迹
   * 后端路由到 /api/ai/analyze → Python FastAPI
   */
  aiAnalyze(photoUrl) {
    return request({
      url: '/ai/analyze',
      method: 'POST',
      data: { photo: photoUrl },
      timeout: 30000,
    })
  },
}
