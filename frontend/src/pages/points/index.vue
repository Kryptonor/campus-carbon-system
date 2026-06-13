<template>
  <view class="page">
    <!-- Balance Card -->
    <view class="balance-card">
      <view class="balance-bg"></view>
      <view class="balance-content">
        <view class="balance-header">
          <text class="balance-label">我的积分余额</text>
          <text class="balance-address">{{ shortAddr }}</text>
        </view>
        <text class="balance-number">{{ balance.total }}</text>
        <view class="balance-row">
          <text class="balance-today">今日 +{{ balance.todayEarned }} 积分</text>
          <text class="balance-chain">链上存证 ⛓️</text>
        </view>
      </view>
    </view>

    <!-- Quick Actions -->
    <view class="quick-row">
      <view class="quick-item" @tap="goRedeem">
        <text class="quick-icon">🎁</text>
        <text class="quick-text">兑换商城</text>
      </view>
      <view class="quick-item" @tap="openTxList">
        <text class="quick-icon">📋</text>
        <text class="quick-text">积分明细</text>
      </view>
      <view class="quick-item" @tap="openBlockchain">
        <text class="quick-icon">🔗</text>
        <text class="quick-text">链上记录</text>
      </view>
    </view>

    <!-- Transaction List -->
    <view class="section-header">
      <text class="section-title">积分流水</text>
    </view>

    <scroll-view class="tx-scroll" scroll-y :show-scrollbar="false">
      <view v-for="tx in transactions" :key="tx.id" class="tx-row">
        <view class="tx-left">
          <text class="tx-type" :class="tx.type">{{ tx.type === 'income' ? '+' : '-' }}</text>
          <view class="tx-info">
            <text class="tx-desc">{{ tx.desc }}</text>
            <text class="tx-date">{{ tx.date }}</text>
          </view>
        </view>
        <text class="tx-points" :class="tx.type">{{ tx.type === 'income' ? '+' : '' }}{{ tx.points }}</text>
      </view>
      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { pointsApi } from '@/api/points'
import { formatAddress } from '@/utils/web3'

const userStore = useUserStore()
const balance = ref({ total: 0, onChain: 0, offChain: 0, todayEarned: 0 })
const transactions = ref([])

const shortAddr = computed(() => formatAddress(userStore.walletAddress) || '未开通钱包')

onShow(() => loadData())

async function loadData() {
  const [balRes, txRes] = await Promise.all([
    pointsApi.getBalance(),
    pointsApi.getTransactions(),
  ])
  if (balRes.code === 200) balance.value = balRes.data
  if (txRes.code === 200) transactions.value = txRes.data.transactions
}

function goRedeem() { uni.navigateTo({ url: '/pages/points/redeem' }) }
function openTxList() { uni.showToast({ title: '滑动查看下方积分流水', icon: 'none' }) }
function openBlockchain() { uni.showToast({ title: '区块链交易已展示在积分流水中', icon: 'none' }) }
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; padding-bottom: calc($safe-bottom + 120rpx); }

.balance-card {
  margin: $space-md; padding: $space-xl;
  background: $bg-gradient; border-radius: $radius-lg;
  position: relative; overflow: hidden;
}
.balance-bg {
  position: absolute; right: -40rpx; top: -40rpx;
  width: 240rpx; height: 240rpx; border-radius: 50%;
  background: rgba(255,255,255,0.1);
}
.balance-content { position: relative; z-index: 1; }
.balance-header { display: flex; justify-content: space-between; align-items: center; }
.balance-label { color: rgba(255,255,255,0.9); font-size: $font-sm; }
.balance-address { color: rgba(255,255,255,0.6); font-size: 20rpx; font-family: monospace; }
.balance-number { color: #fff; font-size: 72rpx; font-weight: 700; display: block; margin: $space-sm 0; line-height: 1; }
.balance-row { display: flex; justify-content: space-between; }
.balance-today { color: rgba(255,255,255,0.85); font-size: $font-xs; background: rgba(255,255,255,0.2); padding: 2rpx 16rpx; border-radius: $radius-round; }
.balance-chain { color: rgba(255,255,255,0.7); font-size: $font-xs; }

.quick-row {
  display: flex; margin: 0 $space-md $space-md; gap: $space-sm;
}
.quick-item {
  flex: 1; background: #fff; border-radius: $radius-md; padding: $space-md;
  display: flex; flex-direction: column; align-items: center; gap: $space-xs;
  box-shadow: $shadow-sm;
}
.quick-icon { font-size: 40rpx; }
.quick-text { font-size: $font-sm; color: $text-primary; font-weight: 500; }

.section-header { padding: $space-sm $space-lg; }
.section-title { font-size: $font-lg; font-weight: 600; color: $text-primary; }

.tx-scroll { padding: 0 $space-md; height: calc(100vh - 520rpx); }

.tx-row {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: $space-md $space-lg; border-radius: $radius-sm; margin-bottom: 6rpx;
}
.tx-left { display: flex; align-items: center; gap: $space-sm; }
.tx-type {
  font-size: $font-lg; font-weight: 700; width: 48rpx; text-align: center;
  &.income { color: $success; }
  &.expense { color: $danger; }
}
.tx-desc { font-size: $font-sm; color: $text-primary; display: block; }
.tx-date { font-size: $font-xs; color: $text-light; }
.tx-points {
  font-size: $font-md; font-weight: 700;
  &.income { color: $success; }
  &.expense { color: $danger; }
}

.safe-bottom { height: 30rpx; }
</style>
