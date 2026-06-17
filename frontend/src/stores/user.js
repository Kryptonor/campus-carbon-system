/**
 * 用户状态管理 — Pinia Store
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { STORAGE_KEYS } from '@/utils/constants'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // ===== State =====
  const token = ref('')
  const userInfo = ref(null)
  const isLoggedIn = ref(false)

  // ===== Getters =====
  const userName = computed(() => userInfo.value?.name || '同学')
  const studentId = computed(() => userInfo.value?.studentId || '')
  const department = computed(() => userInfo.value?.department || '')
  const points = computed(() => userInfo.value?.points || 0)
  const walletAddress = computed(() => userInfo.value?.walletAddress || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // ===== Actions =====

  // 应用启动时检查登录状态
  function checkLogin() {
    const savedToken = uni.getStorageSync(STORAGE_KEYS.token)
    const savedUserInfo = uni.getStorageSync(STORAGE_KEYS.userInfo)
    if (savedToken && savedUserInfo) {
      token.value = savedToken
      userInfo.value = savedUserInfo
      isLoggedIn.value = true
    }
  }

  // 学号密码登录
  async function login(studentId, password) {
    const res = await userApi.login(studentId, password)
    if (res.code === 200) {
      token.value = res.data.token
      userInfo.value = res.data.userInfo
      isLoggedIn.value = true
      persistLogin(res.data)
      return { success: true }
    }
    return { success: false, message: res.message || '登录失败' }
  }

  // 微信授权登录
  async function wechatLogin() {
    try {
      const [err, loginRes] = await uni.login({ provider: 'weixin' })
      if (err) return { success: false, message: '微信授权失败' }
      const res = await userApi.wechatLogin(loginRes.code)
      if (res.code === 200) {
        token.value = res.data.token
        userInfo.value = res.data.userInfo
        isLoggedIn.value = true
        persistLogin(res.data)
        return { success: true }
      }
      return { success: false, message: res.message || '登录失败' }
    } catch (e) {
      return { success: false, message: '微信授权登录失败' }
    }
  }

  // 注册
  async function register(data) {
    const res = await userApi.register(data)
    if (res.code === 200) {
      return { success: true }
    }
    return { success: false, message: res.message || '注册失败' }
  }

  // 获取用户资料
  async function fetchProfile() {
    const res = await userApi.getProfile()
    if (res.code === 200) {
      userInfo.value = res.data
      uni.setStorageSync(STORAGE_KEYS.userInfo, res.data)
    }
  }

  // 更新用户资料
  async function updateProfile(data) {
    const res = await userApi.updateProfile(data)
    if (res.code === 200) {
      userInfo.value = { ...userInfo.value, ...data }
      uni.setStorageSync(STORAGE_KEYS.userInfo, userInfo.value)
      return { success: true }
    }
    return { success: false, message: res.message || '更新失败' }
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    uni.removeStorageSync(STORAGE_KEYS.token)
    uni.removeStorageSync(STORAGE_KEYS.userInfo)
    uni.reLaunch({ url: '/pages/login/login' })
  }

  // 持久化登录信息
  function persistLogin(data) {
    uni.setStorageSync(STORAGE_KEYS.token, data.token)
    uni.setStorageSync(STORAGE_KEYS.userInfo, data.userInfo)
    if (data.refreshToken) {
      uni.setStorageSync(STORAGE_KEYS.refreshToken, data.refreshToken)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userName,
    studentId,
    department,
    points,
    walletAddress,
    avatar,
    checkLogin,
    login,
    wechatLogin,
    register,
    fetchProfile,
    updateProfile,
    logout,
  }
})
