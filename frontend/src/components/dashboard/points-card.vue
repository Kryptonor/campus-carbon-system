<template>
  <view class="points-card card" @tap="$emit('tap')">
    <view class="points-bg"></view>
    <view class="points-content">
      <view class="points-left">
        <text class="points-label">我的积分</text>
        <view class="points-value-row">
          <text class="points-value">{{ points }}</text>
          <text class="points-unit">分</text>
        </view>
        <view class="points-today">
          <text class="today-label">今日 +{{ todayPoints }}</text>
        </view>
      </view>
      <view class="points-right">
        <view class="points-icon-wrap">
          <text class="points-icon">🏆</text>
        </view>
        <text class="points-action">积分中心 →</text>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  points: { type: Number, default: 0 },
  todayPoints: { type: Number, default: 0 },
})

defineEmits(['tap'])
</script>

<style lang="scss" scoped>
.points-card {
  background: $bg-gradient;
  padding: $space-lg;
  position: relative;
  overflow: hidden;
}

.points-bg {
  position: absolute;
  right: -30rpx;
  top: -30rpx;
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
}

.points-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.points-label {
  color: rgba(255, 255, 255, 0.85);
  font-size: $font-sm;
}

.points-value-row {
  display: flex;
  align-items: baseline;
  margin: $space-xs 0;
}

.points-value {
  color: $text-white;
  font-size: $font-title;
  font-weight: 700;
  line-height: 1;
}

.points-unit {
  color: rgba(255, 255, 255, 0.9);
  font-size: $font-lg;
  margin-left: 4rpx;
}

.points-today {
  background: rgba(255, 255, 255, 0.25);
  padding: 2rpx 16rpx;
  border-radius: $radius-round;
  display: inline-block;
}

.today-label {
  color: $text-white;
  font-size: $font-xs;
}

.points-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-xs;
}

.points-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.points-icon {
  font-size: 40rpx;
}

.points-action {
  color: rgba(255, 255, 255, 0.9);
  font-size: $font-xs;
}
</style>
