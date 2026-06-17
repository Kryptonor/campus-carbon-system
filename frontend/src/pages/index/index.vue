<template>
  <view class="home-page">
    <!-- Top Header -->
    <view class="home-header">
      <view class="header-left">
        <view class="avatar-wrap" @tap="goProfile">
          <text class="avatar-text">{{ avatarText }}</text>
        </view>
        <view class="user-greeting">
          <text class="greeting-text">{{ greeting }}</text>
          <text class="user-name">{{ userStore.userName }}</text>
        </view>
      </view>
      <view class="header-right">
        <view class="rank-tag" @tap="goLeaderboard">
          <text class="rank-label">🏅 学院排名</text>
          <text class="rank-value">
            {{ appStore.dailySummary.collegeRank }}/{{ appStore.dailySummary.collegeTotal }}
          </text>
        </view>
      </view>
    </view>

    <scroll-view
      class="page-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      :show-scrollbar="false"
    >
      <!-- Points Card -->
      <d-points-card
        :points="appStore.dailySummary.totalPoints"
        :today-points="appStore.dailySummary.todayPoints"
        @tap="goPointsCenter"
      />

      <!-- Carbon Card -->
      <d-carbon-card :summary="appStore.dailySummary" />

      <!-- Quick Actions -->
      <d-action-entry
        :actions="appStore.quickActions"
        @action="handleAction"
        @more="goGreenActions"
      />

      <!-- Carbon Trend Chart -->
      <d-carbon-chart
        :trend-data="appStore.carbonTrend"
        :weekly-total="appStore.dailySummary.weeklyCarbon"
        :weekly-change="appStore.dailySummary.weeklyChange"
        @period-change="handlePeriodChange"
      />

      <!-- Data Viz Entry -->
      <view class="data-viz-entry card" @tap="goDataViz">
        <view class="viz-left">
          <text class="viz-icon">📊</text>
          <view class="viz-text">
            <text class="viz-title">数据分析</text>
            <text class="viz-desc">碳足迹构成 · 校园对比 · 绿色行为评估</text>
          </view>
        </view>
        <text class="viz-arrow">→</text>
      </view>

      <!-- Blockchain Status -->
      <view class="blockchain-bar card" @tap="goBlockchainInfo">
        <view class="bc-bar-left">
          <text class="bc-bar-icon">⛓️</text>
          <view class="bc-bar-text">
            <text class="bc-bar-title">链上积分存证</text>
            <text class="bc-bar-desc">FISCO BCOS 区块链保障积分安全透明</text>
          </view>
        </view>
        <text class="bc-bar-arrow">→</text>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import dPointsCard from '@/components/dashboard/points-card.vue'
import dCarbonCard from '@/components/dashboard/carbon-card.vue'
import dActionEntry from '@/components/dashboard/action-entry.vue'
import dCarbonChart from '@/components/dashboard/carbon-chart.vue'

const userStore = useUserStore()
const appStore = useAppStore()
const refreshing = ref(false)

const avatarText = computed(() => {
  const name = userStore.userName
  return name ? name.charAt(0) : '学'
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 9) return '早上好，'
  if (h < 12) return '上午好，'
  if (h < 14) return '中午好，'
  if (h < 18) return '下午好，'
  return '晚上好，'
})

onShow(() => loadData())

async function loadData() {
  await Promise.all([appStore.fetchDashboardData(), userStore.fetchProfile()])
}

async function onRefresh() {
  refreshing.value = true
  await loadData()
  refreshing.value = false
}

function handleAction(action) {
  uni.switchTab({ url: '/pages/green-actions/list' })
}

function handlePeriodChange(period) {
  appStore.fetchCarbonTrend(period)
}

function goProfile() {
  uni.navigateTo({ url: '/pages/profile/profile' })
}

function goLeaderboard() {
  uni.switchTab({ url: '/pages/leaderboard/index' })
}

function goPointsCenter() {
  uni.switchTab({ url: '/pages/points/index' })
}

function goGreenActions() {
  uni.switchTab({ url: '/pages/green-actions/list' })
}

function goDataViz() {
  uni.navigateTo({ url: '/pages/data-viz/index' })
}

function goBlockchainInfo() {
  uni.navigateTo({ url: '/pages/blockchain/index' })
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: $bg-color;
  display: flex;
  flex-direction: column;
}

.home-header {
  padding: $space-md $space-lg;
  padding-top: calc(var(--status-bar-height, 44px) + $space-md);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(180deg, #E8F5E9 0%, $bg-color 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: $space-sm;
}

.avatar-wrap {
  width: 72rpx;
  height: 72rpx;
  background: $bg-gradient;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: $text-white;
  font-size: $font-lg;
  font-weight: 600;
}

.greeting-text {
  font-size: $font-sm;
  color: $text-secondary;
}

.user-name {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
  display: block;
}

.rank-tag {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-xs $space-sm;
  background: $bg-white;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
}

.rank-label {
  font-size: 20rpx;
  color: $text-secondary;
}

.rank-value {
  font-size: $font-md;
  font-weight: 700;
  color: $accent-warm;
}

.page-scroll {
  flex: 1;
  padding: 0 $space-md;
  padding-top: $space-sm;
}

.data-viz-entry {
  margin-top: $space-md;
  padding: $space-md $space-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.viz-left {
  display: flex;
  align-items: center;
  gap: $space-sm;
}

.viz-icon {
  font-size: 40rpx;
}

.viz-text {
  display: flex;
  flex-direction: column;
}

.viz-title {
  font-size: $font-md;
  font-weight: 600;
  color: $text-primary;
}

.viz-desc {
  font-size: $font-xs;
  color: $text-light;
}

.viz-arrow {
  font-size: $font-lg;
  color: $text-light;
}

.blockchain-bar {
  margin-top: $space-md;
  padding: $space-md $space-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bc-bar-left {
  display: flex;
  align-items: center;
  gap: $space-sm;
}

.bc-bar-icon {
  font-size: 36rpx;
}

.bc-bar-text {
  display: flex;
  flex-direction: column;
}

.bc-bar-title {
  font-size: $font-md;
  font-weight: 600;
  color: $text-primary;
}

.bc-bar-desc {
  font-size: $font-xs;
  color: $text-light;
}

.bc-bar-arrow {
  font-size: $font-lg;
  color: $text-light;
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom, 30rpx) + 30rpx);
}
</style>
