<template>
  <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="nav-bar-content" :style="{ height: navHeight + 'px' }">
      <view class="nav-left" @tap="handleBack" v-if="showBack">
        <text class="nav-back">←</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">{{ title }}</text>
      </view>
      <view class="nav-right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { onReady } from '@dcloudio/uni-app'

const props = defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: false },
})

const emit = defineEmits(['back'])

const statusBarHeight = computed(() => {
  const sysInfo = uni.getSystemInfoSync()
  return sysInfo.statusBarHeight || 20
})

const navHeight = 44

function handleBack() {
  if (props.showBack) {
    emit('back')
    uni.navigateBack({ delta: 1 })
  }
}
</script>

<style lang="scss" scoped>
.nav-bar {
  background: $bg-gradient;
  padding: 0 $space-md;
  box-sizing: border-box;
}

.nav-bar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
}

.nav-left, .nav-right {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.nav-left {
  justify-content: flex-start;
}

.nav-right {
  justify-content: flex-end;
}

.nav-back {
  font-size: $font-xl;
  color: $text-white;
  font-weight: bold;
}

.nav-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-title {
  color: $text-white;
  font-size: $font-lg;
  font-weight: 600;
}
</style>
