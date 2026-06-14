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
   * 后端路由到 /api/ai/analyze → Spring Boot Multipart 上传
   */
  aiAnalyze(photoPath, userId, behaviorType) {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('ccs_token')
      // 兼容本地 H5 相对路径上传
      const host = window ? window.location.origin : 'http://localhost:5173'
      const API_BASE_URL = '/api'
      const uploadUrl = API_BASE_URL.startsWith('http')
        ? API_BASE_URL + '/ai/analyze'
        : host + API_BASE_URL + '/ai/analyze'

      uni.uploadFile({
        url: uploadUrl,
        filePath: photoPath,
        name: 'file',
        header: {
          Authorization: token ? `Bearer ${token}` : '',
        },
        formData: {
          userId: String(userId || 0),
          behaviorType: behaviorType || 'recycle',
        },
        timeout: 30000,
        success: (uploadRes) => {
          try {
            const res = JSON.parse(uploadRes.data)
            resolve(res)
          } catch (e) {
            reject(new Error('响应解析失败'))
          }
        },
        fail: (err) => {
          reject(new Error(err.errMsg || '上传失败'))
        },
      })
    })
  },
}
