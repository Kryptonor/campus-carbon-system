<template>
  <view class="actions-section">
    <view class="section-header">
      <text class="section-title">绿色行动打卡</text>
      <text class="section-more" @tap="$emit('more')">更多 →</text>
    </view>

    <view class="actions-grid">
      <view
        v-for="action in actions"
        :key="action.id"
        class="action-item"
        @tap="$emit('action', action)"
      >
        <view class="action-icon-wrap" :style="{ backgroundColor: action.color + '20' }">
          <text class="action-emoji">{{ iconMap[action.icon] || '🌱' }}</text>
        </view>
        <text class="action-name">{{ action.name }}</text>
        <text class="action-points" :style="{ color: action.color }">+{{ action.points }}分</text>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  actions: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['action', 'more'])

const iconMap = {
  walk: '🚶',
  bike: '🚲',
  bus: '🚌',
  recycle: '♻️',
  goods: '📦',
  power: '💡',
  plastic: '🚫',
  tree: '🌳',
}
</script>

<style lang="scss" scoped>
.actions-section {
  margin-top: $space-md;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 $space-sm;
  margin-bottom: $space-md;
}

.section-title {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
}

.section-more {
  font-size: $font-sm;
  color: $primary-light;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-sm;
}

.action-item {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-md $space-xs;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-xs;
  box-shadow: $shadow-sm;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.95);
  }
}

.action-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-emoji {
  font-size: 32rpx;
}

.action-name {
  font-size: $font-xs;
  color: $text-primary;
  text-align: center;
  line-height: 1.3;
}

.action-points {
  font-size: 20rpx;
  font-weight: 600;
}
</style>
