<template>
  <view class="carbon-card card">
    <view class="card-header">
      <view class="header-left">
        <text class="header-icon">🌿</text>
        <text class="header-title">今日碳足迹</text>
      </view>
      <text class="header-date">{{ todayDate }}</text>
    </view>

    <view class="carbon-value-row">
      <text class="carbon-value">{{ summary.todayCarbon }}</text>
      <text class="carbon-unit">kg CO₂</text>
    </view>

    <view class="carbon-compare" :class="trendDirection">
      <text class="compare-text">
        较昨日{{ trendDirection === 'down' ? '减少' : '增加' }}
        {{ Math.abs(summary.carbonChange) }}%
      </text>
      <text class="compare-icon">{{ trendDirection === 'down' ? '↓' : '↑' }}</text>
    </view>

    <view class="carbon-divider"></view>

    <view class="carbon-goal-row">
      <text class="goal-label">月度碳排放目标</text>
      <text class="goal-value">{{ summary.goalCarbon }} kg</text>
    </view>
    <view class="progress-bar">
      <view class="progress-fill" :style="{ width: summary.goalProgress + '%' }" :class="progressClass">
      </view>
    </view>
    <text class="progress-text">
      已排放 {{ (summary.goalCarbon * summary.goalProgress / 100).toFixed(1) }} kg · 剩余 {{ (summary.goalCarbon * (1 - summary.goalProgress / 100)).toFixed(1) }} kg
    </text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  summary: {
    type: Object,
    default: () => ({
      todayCarbon: 0,
      carbonChange: 0,
      goalCarbon: 150,
      goalProgress: 0,
    }),
  },
})

const todayDate = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const trendDirection = computed(() => props.summary.carbonChange <= 0 ? 'down' : 'up')

const progressClass = computed(() => {
  const p = props.summary.goalProgress
  if (p > 80) return 'progress-danger'
  if (p > 50) return 'progress-warning'
  return 'progress-safe'
})
</script>

<style lang="scss" scoped>
.carbon-card {
  padding: $space-lg;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-md;
}

.header-left {
  display: flex;
  align-items: center;
  gap: $space-xs;
}

.header-icon {
  font-size: $font-lg;
}

.header-title {
  font-size: $font-md;
  color: $text-primary;
  font-weight: 600;
}

.header-date {
  font-size: $font-sm;
  color: $text-light;
}

.carbon-value-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin: $space-md 0;
}

.carbon-value {
  font-size: 80rpx;
  font-weight: 700;
  color: $primary;
  line-height: 1;
}

.carbon-unit {
  font-size: $font-md;
  color: $text-secondary;
  margin-left: $space-xs;
}

.carbon-compare {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $space-xs $space-sm;
  border-radius: $radius-round;
  align-self: center;
  margin: 0 auto;
  width: fit-content;

  &.down {
    background-color: rgba(76, 175, 80, 0.1);
    .compare-text, .compare-icon { color: $success; }
  }
  &.up {
    background-color: rgba(244, 67, 54, 0.1);
    .compare-text, .compare-icon { color: $danger; }
  }
}

.compare-text {
  font-size: $font-sm;
  margin-right: 4rpx;
}

.compare-icon {
  font-size: $font-sm;
  font-weight: bold;
}

.carbon-divider {
  height: 1rpx;
  background: $border-color;
  margin: $space-md 0;
}

.carbon-goal-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: $space-sm;
}

.goal-label {
  font-size: $font-sm;
  color: $text-secondary;
}

.goal-value {
  font-size: $font-sm;
  color: $text-primary;
  font-weight: 600;
}

.progress-bar {
  height: 12rpx;
  background: #E8F5E9;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: $space-xs;
}

.progress-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.5s ease;

  &.progress-safe { background: $success; }
  &.progress-warning { background: $warning; }
  &.progress-danger { background: $danger; }
}

.progress-text {
  font-size: $font-xs;
  color: $text-light;
}
</style>
