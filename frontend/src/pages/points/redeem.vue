<template>
  <view class="page">
    <!-- Balance Bar -->
    <view class="balance-bar">
      <text class="balance-text">可用积分：<text class="balance-num">{{ balance.total }}</text></text>
    </view>

    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <!-- Rewards Grid -->
      <view class="rewards-grid">
        <view v-for="item in rewards" :key="item.id" class="reward-card card">
          <text class="reward-image">{{ item.image }}</text>
          <text class="reward-name">{{ item.name }}</text>
          <text class="reward-desc">{{ item.description }}</text>
          <view class="reward-footer">
            <text class="reward-cost">{{ item.pointsCost }} 积分</text>
            <text class="reward-stock">库存 {{ item.stock }}</text>
          </view>
          <button
            class="redeem-btn"
            :class="{ disabled: balance.total < item.pointsCost || item.stock <= 0 }"
            :disabled="balance.total < item.pointsCost || item.stock <= 0"
            @tap="handleRedeem(item)"
          >
            {{ balance.total < item.pointsCost ? '积分不足' : item.stock <= 0 ? '已售罄' : '立即兑换' }}
          </button>
        </view>
      </view>

      <!-- History -->
      <view class="section-title">兑换记录</view>
      <view v-if="redeemHistory.length === 0" class="empty-tip">
        <text>还没有兑换记录</text>
      </view>
      <view v-for="order in redeemHistory" :key="order.id" class="order-row">
        <text class="order-icon">{{ order.image }}</text>
        <view class="order-info">
          <text class="order-name">{{ order.rewardName }}</text>
          <text class="order-date">{{ order.redeemDate }}</text>
        </view>
        <view class="order-right">
          <text class="order-cost">-{{ order.pointsCost }}</text>
          <text class="order-code">{{ order.code }}</text>
        </view>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { pointsApi } from '@/api/points'

const balance = ref({ total: 0 })
const rewards = ref([])
const redeemHistory = ref([])

onShow(() => loadData())

async function loadData() {
  const [balRes, rewRes, histRes] = await Promise.all([
    pointsApi.getBalance(),
    pointsApi.getRewards(),
    pointsApi.getRedeemHistory(),
  ])
  if (balRes.code === 200) balance.value = balRes.data
  if (rewRes.code === 200) rewards.value = rewRes.data
  if (histRes.code === 200) redeemHistory.value = histRes.data.orders
}

async function handleRedeem(item) {
  uni.showModal({
    title: '确认兑换',
    content: `确定使用 ${item.pointsCost} 积分兑换「${item.name}」吗？`,
    success: async (r) => {
      if (r.confirm) {
        const res = await pointsApi.redeem(item.id)
        if (res.code === 200) {
          uni.showToast({ title: '兑换成功！', icon: 'success' })
          await loadData()
        } else {
          uni.showToast({ title: res.message, icon: 'none' })
        }
      }
    },
  })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; padding-bottom: calc($safe-bottom + 120rpx); }

.balance-bar { padding: $space-md $space-lg; background: #fff; margin-bottom: $space-sm; }
.balance-text { font-size: $font-sm; color: $text-secondary; }
.balance-num { color: $accent-warm; font-weight: 700; font-size: $font-lg; }

.page-scroll { padding: 0 $space-md; height: calc(100vh - 120rpx); }

.rewards-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: $space-sm; }

.reward-card { padding: $space-lg; display: flex; flex-direction: column; gap: $space-xs; }
.reward-image { font-size: 48rpx; text-align: center; }
.reward-name { font-size: $font-md; font-weight: 600; color: $text-primary; }
.reward-desc { font-size: $font-xs; color: $text-light; line-height: 1.4; }
.reward-footer { display: flex; justify-content: space-between; }
.reward-cost { font-size: $font-sm; color: $accent-warm; font-weight: 700; }
.reward-stock { font-size: 20rpx; color: $text-light; }

.redeem-btn {
  width: 100%; height: 64rpx; background: $bg-gradient; border-radius: $radius-sm;
  color: #fff; font-size: $font-sm; display: flex; align-items: center;
  justify-content: center; border: none; margin-top: auto;
  &.disabled { background: #BDBDBD; color: #fff; }
}

.section-title { font-size: $font-lg; font-weight: 600; color: $text-primary; margin: $space-lg 0 $space-md; }

.order-row {
  display: flex; align-items: center; background: #fff; padding: $space-md $space-lg;
  border-radius: $radius-sm; margin-bottom: 6rpx; gap: $space-sm;
}
.order-icon { font-size: 32rpx; }
.order-info { flex: 1; }
.order-name { font-size: $font-sm; color: $text-primary; display: block; }
.order-date { font-size: $font-xs; color: $text-light; }
.order-right { text-align: right; }
.order-cost { font-size: $font-md; font-weight: 700; color: $danger; display: block; }
.order-code { font-size: 20rpx; color: $text-light; }

.empty-tip { text-align: center; padding: $space-xl; color: $text-light; font-size: $font-sm; }
.safe-bottom { height: 30rpx; }
</style>
