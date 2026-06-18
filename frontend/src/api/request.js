/**
 * 统一请求封装 — 基于 uni.request
 * 对接后端 Spring Boot REST API
 */
import { STORAGE_KEYS, API_BASE_URL } from '@/utils/constants'

// 请求拦截器队列
const requestInterceptors = []
const responseInterceptors = []
const errorInterceptors = []

// Mock 开关：后端已就绪，设为 false 进行真实联调
const USE_MOCK = false

// ====== Mock 数据 ======
import {
  mockDashboardData, createMockUserData, createLoginResponse,
  createMockRecords, mockGreenActions, createMyCheckinHistory,
  createMockTransactions, mockRewards, createRedeemHistory,
  createMockRankList, createMockClassRankList, mockAiAnalyze,
  mockCarbonBreakdown, mockCampusCompare, mockRadarData,
} from './mock'

// 可变 Mock 状态——模拟后端数据库，会话内有效
let currentUser = createMockUserData()
let mockRecords = createMockRecords()
let mockCheckins = createMyCheckinHistory()
let mockTransactions = createMockTransactions()
let mockRedeemHistory = createRedeemHistory()

const BASE_URL = API_BASE_URL

/**
 * 添加请求拦截器
 */
export function addRequestInterceptor(fn) {
  requestInterceptors.push(fn)
}

/**
 * 添加响应拦截器
 */
export function addResponseInterceptor(fn) {
  responseInterceptors.push(fn)
}

/**
 * 添加错误拦截器
 */
export function addErrorInterceptor(fn) {
  errorInterceptors.push(fn)
}

/**
 * 核心请求方法
 */
export function request(config) {
  return new Promise((resolve, reject) => {
    // Mock 模式
    if (USE_MOCK) {
      handleMock(config, resolve, reject)
      return
    }

    // 获取 token
    const token = uni.getStorageSync(STORAGE_KEYS.token)

    // 执行请求拦截器
    let finalConfig = { ...config }
    requestInterceptors.forEach((fn) => {
      finalConfig = fn(finalConfig)
    })

    uni.request({
      url: BASE_URL + finalConfig.url,
      method: finalConfig.method || 'GET',
      data: finalConfig.data || {},
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        ...finalConfig.header,
      },
      timeout: finalConfig.timeout || 15000,
      success: (res) => {
        if (res.statusCode === 200) {
          let result = res.data
          responseInterceptors.forEach((fn) => {
            result = fn(result)
          })
          resolve(result)
        } else if (res.statusCode === 401) {
          // token 过期，清除登录状态
          console.warn('Authentication failed: 401 Unauthorized', res)
          uni.removeStorageSync(STORAGE_KEYS.token)
          uni.removeStorageSync(STORAGE_KEYS.userInfo)
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期，请重新登录'))
        } else if (res.data && res.data.code === 401) {
          // 兼容后端返回 200，但 body.code 为 401 的情况
          console.warn('Authentication failed: Body code 401', res.data)
          uni.removeStorageSync(STORAGE_KEYS.token)
          uni.removeStorageSync(STORAGE_KEYS.userInfo)
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期，请重新登录'))
        } else {
          const err = new Error(res.data?.message || '请求失败')
          errorInterceptors.forEach((fn) => fn(err))
          reject(err)
        }
      },
      fail: (err) => {
        const error = new Error(err.errMsg || '网络异常，请检查网络连接')
        errorInterceptors.forEach((fn) => fn(error))
        uni.showToast({ title: error.message, icon: 'none' })
        reject(error)
      },
    })
  })
}

// ====== Mock 处理器 ======
function handleMock(config, resolve) {
  setTimeout(() => {
    const { url, method, data } = config

    // 登录
    if (url === '/auth/login' && method === 'POST') {
      if (data.studentId && data.password) {
        resolve(createLoginResponse(currentUser))
      } else {
        resolve({ code: 401, message: '学号或密码错误' })
      }
      return
    }

    // 微信登录
    if (url === '/auth/wechat-login' && method === 'POST') {
      resolve(createLoginResponse(currentUser))
      return
    }

    // 注册
    if (url === '/auth/register' && method === 'POST') {
      resolve({ code: 200, message: '注册成功，请登录', data: null })
      return
    }

    // 用户资料
    if (url === '/user/profile' && method === 'GET') {
      resolve({ code: 200, data: { ...currentUser } })
      return
    }

    // 更新资料
    if (url === '/user/profile' && method === 'PUT') {
      Object.assign(currentUser, data)
      resolve({ code: 200, message: '更新成功', data: { ...currentUser } })
      return
    }

    // 首页每日摘要
    if (url === '/home/daily-summary' && method === 'GET') {
      resolve({
        code: 200,
        data: {
          ...mockDashboardData.dailySummary,
          totalPoints: currentUser.points,
        },
      })
      return
    }

    // 首页快捷操作
    if (url === '/home/quick-actions' && method === 'GET') {
      resolve({ code: 200, data: [...mockDashboardData.quickActions] })
      return
    }

    // 碳足迹趋势
    if (url === '/home/carbon-trend' && method === 'GET') {
      resolve({ code: 200, data: [...mockDashboardData.carbonTrend] })
      return
    }

    // 区块链查询
    if (url === '/blockchain/points-balance' && method === 'GET') {
      resolve({
        code: 200,
        data: {
          onChainBalance: currentUser.points,
          offChainBalance: 120,
          totalBalance: currentUser.points + 120,
        },
      })
      return
    }

    if (url === '/blockchain/transactions' && method === 'GET') {
      resolve({
        code: 200,
        data: [
          { txHash: '0x7a3b...8f2c', type: '积分发放', amount: 50, timestamp: Date.now() - 3600000 },
          { txHash: '0x2d1f...a9b4', type: '旧物回收', amount: 30, timestamp: Date.now() - 7200000 },
          { txHash: '0x9e4c...1d7a', type: '垃圾分类', amount: 5, timestamp: Date.now() - 10800000 },
        ],
      })
      return
    }

    if (url === '/blockchain/contract-info' && method === 'GET') {
      resolve({
        code: 200,
        data: {
          networkStatus: '运行中',
          chainName: 'FISCO BCOS 校园联盟链',
          pointsToken: '0x3a9c2b8f1e4d7a6c5b3f8e2d1a0b9c7d5e4f3a2b',
          actionLedger: '0x8f7e6d5c4b3a2918f7e6d5c4b3a2918f7e6d5c',
          blockHeight: 184632 + Math.floor(Date.now() / 600000),
          txCount: 12583 + Math.floor(Date.now() / 300000),
        },
      })
      return
    }

    if (url === '/blockchain/action-records' && method === 'GET') {
      resolve({
        code: 200,
        data: [
          { id: 'ar1', icon: '🚲', actionName: '骑行通勤', date: '2026-06-02', txHash: '0x1a2b...3c4d' },
          { id: 'ar2', icon: '🗑️', actionName: '垃圾分类投放', date: '2026-06-01', txHash: '0x5e6f...7g8h' },
          { id: 'ar3', icon: '💡', actionName: '随手关灯', date: '2026-05-31', txHash: '0x9i0j...1k2l' },
          { id: 'ar4', icon: '♻️', actionName: '旧物回收', date: '2026-05-30', txHash: '0x3m4n...5o6p' },
          { id: 'ar5', icon: '🥬', actionName: '素食日打卡', date: '2026-05-29', txHash: '0x7q8r...9s0t' },
        ],
      })
      return
    }

    // ====== 碳足迹记录 ======
    if (url === '/record/list' && method === 'GET') {
      resolve({ code: 200, data: { records: [...mockRecords], total: mockRecords.length } })
      return
    }

    if (url === '/record/detail' && method === 'GET') {
      const record = mockRecords.find((r) => r.id === data.id)
      resolve(record ? { code: 200, data: record } : { code: 404, message: '记录不存在' })
      return
    }

    if (url === '/record/create' && method === 'POST') {
      const newRecord = {
        id: 'r' + Date.now(),
        category: data.category || '其他',
        categoryIcon: data.categoryIcon || '📝',
        carbonAmount: data.carbonAmount || 0,
        quantity: data.quantity || '1',
        photo: data.photo || '',
        notes: data.notes || '',
        aiResult: data.aiResult || null,
        date: new Date().toISOString().slice(0, 16).replace('T', ' '),
        createdAt: Date.now(),
      }
      mockRecords.unshift(newRecord)
      currentUser.carbonTotal += newRecord.carbonAmount
      resolve({ code: 200, message: '记录创建成功', data: newRecord })
      return
    }

    if (url === '/record/delete' && method === 'POST') {
      mockRecords = mockRecords.filter((r) => r.id !== data.id)
      resolve({ code: 200, message: '删除成功' })
      return
    }

    // ====== AI 分析（拍照审核） ======
    if (url === '/ai/analyze' && method === 'POST') {
      // 模拟 1.5s AI 分析延迟
      setTimeout(() => {
        const result = mockAiAnalyze()
        resolve({ code: 200, data: result })
      }, 1500)
      return
    }

    // ====== 绿色行动 ======
    if (url === '/actions/list' && method === 'GET') {
      resolve({ code: 200, data: [...mockGreenActions] })
      return
    }

    if (url === '/actions/checkin' && method === 'POST') {
      const action = mockGreenActions.find((a) => a.id === data.actionId)
      if (!action) { resolve({ code: 404, message: '行动不存在' }); return }
      const checkin = {
        id: 'c' + Date.now(),
        actionId: action.id,
        actionName: action.name,
        icon: action.icon,
        points: action.points,
        date: data.date || new Date().toISOString().slice(0, 16).replace('T', ' '),
      }
      mockCheckins.unshift(checkin)
      currentUser.points += action.points
      currentUser.carbonReduced += +(action.points * 0.05).toFixed(1)
      resolve({ code: 200, message: `打卡成功！+${action.points}积分`, data: checkin })
      return
    }

    if (url === '/actions/my-history' && method === 'GET') {
      resolve({ code: 200, data: { records: [...mockCheckins], total: mockCheckins.length } })
      return
    }

    // ====== 积分中心 ======
    if (url === '/points/balance' && method === 'GET') {
      resolve({
        code: 200,
        data: {
          total: currentUser.points,
          onChain: currentUser.points,
          offChain: 120,
          todayEarned: 35,
        },
      })
      return
    }

    if (url === '/points/transactions' && method === 'GET') {
      resolve({ code: 200, data: { transactions: [...mockTransactions], total: mockTransactions.length } })
      return
    }

    if (url === '/points/rewards' && method === 'GET') {
      resolve({ code: 200, data: [...mockRewards] })
      return
    }

    if (url === '/points/redeem' && method === 'POST') {
      const reward = mockRewards.find((r) => r.id === data.rewardId)
      if (!reward) { resolve({ code: 404, message: '商品不存在' }); return }
      if (currentUser.points < reward.pointsCost) { resolve({ code: 400, message: '积分不足' }); return }
      if (reward.stock <= 0) { resolve({ code: 400, message: '库存不足' }); return }

      currentUser.points -= reward.pointsCost
      reward.stock -= 1
      const order = {
        id: 'rd' + Date.now(),
        rewardName: reward.name,
        image: reward.image,
        pointsCost: reward.pointsCost,
        redeemDate: new Date().toISOString().slice(0, 16).replace('T', ' '),
        status: 'completed',
        code: 'GFT' + Date.now(),
      }
      mockRedeemHistory.unshift(order)
      mockTransactions.unshift({
        id: 't' + Date.now(),
        type: 'expense',
        desc: `兑换：${reward.name}`,
        points: -reward.pointsCost,
        date: order.redeemDate,
        txHash: '0x' + Math.random().toString(16).slice(2, 10) + '...' + Math.random().toString(16).slice(2, 6),
      })
      resolve({ code: 200, message: '兑换成功！', data: order })
      return
    }

    if (url === '/points/redeem-history' && method === 'GET') {
      resolve({ code: 200, data: { orders: [...mockRedeemHistory], total: mockRedeemHistory.length } })
      return
    }

    // ====== 排行榜 ======
    if (url === '/leaderboard/list' && method === 'GET') {
      if (data.type === 'class') {
        const classRanks = createMockClassRankList()
        resolve({ code: 200, data: { ranks: classRanks } })
      } else {
        const rankList = createMockRankList(data.type || 'carbon')
        // 将当前用户插入到正确的排名位置，保持数据一致性
        const myRank = 12
        const myEntry = {
          rank: myRank,
          name: currentUser.name,
          avatar: currentUser.name.charAt(0),
          department: currentUser.department,
          score: currentUser.points,
          carbonReduced: currentUser.carbonReduced,
        }
        // 删除原有该排名位置的同学，插入当前用户
        const filtered = rankList.filter((r) => r.rank !== myRank)
        filtered.push(myEntry)
        filtered.sort((a, b) => a.rank - b.rank)
        resolve({
          code: 200,
          data: {
            ranks: filtered,
            myRank: myRank,
            myScore: currentUser.points,
            myCarbonReduced: currentUser.carbonReduced,
          },
        })
      }
      return
    }

    // ====== 数据可视化 ======
    if (url === '/data/carbon-breakdown' && method === 'GET') {
      resolve({ code: 200, data: [...mockCarbonBreakdown] })
      return
    }

    if (url === '/data/campus-compare' && method === 'GET') {
      resolve({ code: 200, data: [...mockCampusCompare] })
      return
    }

    if (url === '/data/radar' && method === 'GET') {
      resolve({ code: 200, data: [...mockRadarData] })
      return
    }

    // 默认
    resolve({ code: 404, message: `Mock: 未匹配接口 ${method} ${url}` })
  }, 300)
}
