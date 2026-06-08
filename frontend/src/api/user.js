/**
 * 用户相关 API — 对接后端 /api/auth/* 和 /api/user/*
 */
import { request } from './request'

export const userApi = {
  login(studentId, password) {
    return request({
      url: '/auth/login',
      method: 'POST',
      data: { studentId, password },
    })
  },

  wechatLogin(code) {
    return request({
      url: '/auth/wechat-login',
      method: 'POST',
      data: { code },
    })
  },

  register(data) {
    return request({
      url: '/auth/register',
      method: 'POST',
      data,
    })
  },

  getProfile() {
    return request({
      url: '/user/profile',
      method: 'GET',
    })
  },

  updateProfile(data) {
    return request({
      url: '/user/profile',
      method: 'PUT',
      data,
    })
  },
}
