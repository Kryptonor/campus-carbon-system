<template>
  <view class="page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <!-- Stats Grid -->
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-icon">👥</text>
          <text class="stat-value">{{ stats.totalUsers }}</text>
          <text class="stat-label">注册用户</text>
          <text class="stat-sub">今日活跃 {{ stats.activeToday }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">✅</text>
          <text class="stat-value">{{ stats.totalCheckins }}</text>
          <text class="stat-label">总打卡数</text>
          <text class="stat-sub warn">待审核 {{ stats.pendingAudits }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">🌱</text>
          <text class="stat-value">{{ stats.totalCarbonReduced }}kg</text>
          <text class="stat-label">累计减排</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">🏆</text>
          <text class="stat-value">{{ stats.totalPointsIssued }}</text>
          <text class="stat-label">累计发放积分</text>
        </view>
      </view>

      <!-- Quick Nav -->
      <view class="nav-grid">
        <view class="nav-card" @tap="goAudit">
          <text class="nav-icon">📝</text>
          <text class="nav-title">审核管理</text>
          <text class="nav-desc">{{ stats.pendingAudits }} 条待审核</text>
          <text class="nav-arrow">→</text>
        </view>
        <view class="nav-card" @tap="goProducts">
          <text class="nav-icon">🎁</text>
          <text class="nav-title">商品管理</text>
          <text class="nav-desc">积分兑换商品</text>
          <text class="nav-arrow">→</text>
        </view>
        <view class="nav-card" @tap="goUsers">
          <text class="nav-icon">👤</text>
          <text class="nav-title">用户管理</text>
          <text class="nav-desc">{{ stats.totalUsers }} 位注册用户</text>
          <text class="nav-arrow">→</text>
        </view>
      </view>

      <!-- On-chain Stats -->
      <view class="section-title">⛓️ 区块链概览</view>
      <view class="chain-card card">
        <view class="chain-row">
          <text class="chain-label">链上交易总数</text>
          <text class="chain-value">{{ stats.onChainTxCount }}</text>
        </view>
        <view class="chain-row">
          <text class="chain-label">合约地址 (积分)</text>
          <text class="chain-value mono">0x3f8a...d7e2</text>
        </view>
        <view class="chain-row">
          <text class="chain-label">合约地址 (行为)</text>
          <text class="chain-value mono">0x9c2b...1a5f</text>
        </view>
        <view class="chain-row">
          <text class="chain-label">合约地址 (兑换)</text>
          <text class="chain-value mono">0x4e7d...b3c8</text>
        </view>
        <view class="chain-row">
          <text class="chain-label">底层框架</text>
          <text class="chain-value">FISCO BCOS 3.x</text>
        </view>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '@/api/request'

const stats = reactive({
  totalUsers: 0,
  activeToday: 0,
  totalCheckins: 0,
  pendingAudits: 0,
  totalCarbonReduced: 0,
  totalPointsIssued: 0,
  onChainTxCount: 0,
})

onShow(() => loadStats())

async function loadStats() {
  const res = await request({ url: '/admin/stats', method: 'GET' })
  if (res.code === 200) Object.assign(stats, res.data)
}

function goAudit() { uni.navigateTo({ url: '/pages/admin/audit' }) }
function goProducts() { uni.navigateTo({ url: '/pages/admin/products' }) }
function goUsers() { uni.navigateTo({ url: '/pages/admin/users' }) }
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; }
.page-scroll { height: 100vh; padding: $space-md; }

.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: $space-sm; margin-bottom: $space-lg; }
.stat-card {
  background: #fff; border-radius: $radius-md; padding: $space-md;
  display: flex; flex-direction: column; align-items: center; gap: 4rpx;
}
.stat-icon { font-size: 36rpx; }
.stat-value { font-size: $font-lg; font-weight: 700; color: $primary-dark; }
.stat-label { font-size: $font-xs; color: $text-light; }
.stat-sub { font-size: 20rpx; color: $text-secondary; &.warn { color: $warning; } }

.nav-grid { display: flex; flex-direction: column; gap: $space-sm; margin-bottom: $space-lg; }
.nav-card {
  background: #fff; border-radius: $radius-md; padding: $space-md $space-lg;
  display: flex; align-items: center; gap: $space-sm;
}
.nav-icon { font-size: 40rpx; }
.nav-title { font-size: $font-md; font-weight: 600; color: $text-primary; flex: 1; }
.nav-desc { font-size: $font-xs; color: $text-light; }
.nav-arrow { font-size: $font-md; color: $text-light; }

.section-title { font-size: $font-md; font-weight: 600; color: $text-primary; margin-bottom: $space-sm; }
.chain-card { padding: $space-md $space-lg; }
.chain-row { display: flex; justify-content: space-between; align-items: center; padding: $space-sm 0; }
.chain-row + .chain-row { border-top: 1rpx solid $border-color; }
.chain-label { font-size: $font-sm; color: $text-secondary; }
.chain-value { font-size: $font-sm; color: $text-primary; font-weight: 500;
  &.mono { font-family: monospace; font-size: $font-xs; }
}

.safe-bottom { height: calc($safe-bottom + 40rpx); }
</style>
