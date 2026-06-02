/**
 * 全局应用状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { homeApi } from '@/api/home'
import { blockchainService } from '@/utils/web3'

export const useAppStore = defineStore('app', () => {
  // ===== State =====
  const dailySummary = ref({
    todayCarbon: 0,
    yesterdayCarbon: 0,
    carbonChange: 0,
    todayPoints: 0,
    totalPoints: 0,
    weeklyCarbon: 0,
    weeklyChange: 0,
    goalCarbon: 150,
    goalProgress: 0,
    collegeRank: 0,
    collegeTotal: 0,
  })
  const quickActions = ref([])
  const carbonTrend = ref([])
  const blockchainInfo = ref(null)
  const loading = ref(false)

  // ===== Actions =====

  // 获取首页仪表盘数据
  async function fetchDashboardData() {
    loading.value = true
    try {
      const [summaryRes, actionsRes, trendRes] = await Promise.all([
        homeApi.getDailySummary(),
        homeApi.getQuickActions(),
        homeApi.getCarbonTrend('week'),
      ])
      if (summaryRes.code === 200) dailySummary.value = summaryRes.data
      if (actionsRes.code === 200) quickActions.value = actionsRes.data
      if (trendRes.code === 200) carbonTrend.value = trendRes.data
    } catch (e) {
      console.error('获取首页数据失败:', e)
    } finally {
      loading.value = false
    }
  }

  // 获取区块链信息
  async function fetchBlockchainInfo() {
    try {
      const res = await blockchainService.getContractInfo()
      if (res.code === 200) blockchainInfo.value = res.data
    } catch (e) {
      console.error('获取区块链信息失败:', e)
    }
  }

  return {
    dailySummary,
    quickActions,
    carbonTrend,
    blockchainInfo,
    loading,
    fetchDashboardData,
    fetchBlockchainInfo,
  }
})
