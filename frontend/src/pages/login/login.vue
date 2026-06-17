<template>
  <view class="login-page">
    <!-- Logo & Header -->
    <view class="login-header">
      <view class="logo-wrap">
        <text class="logo-icon">🌍</text>
      </view>
      <text class="app-name">校园碳足迹</text>
      <text class="app-desc">绿色行动 · 积分激励 · 低碳校园</text>
    </view>

    <!-- Login Form -->
    <view class="login-form">
      <view class="form-item">
        <view class="input-wrap">
          <text class="input-icon">👤</text>
          <input
            class="form-input"
            v-model="account"
            placeholder="请输入学号"
            placeholder-style="color: #A5D6A7"
            type="text"
          />
        </view>
      </view>

      <view class="form-item">
        <view class="input-wrap">
          <text class="input-icon">🔒</text>
          <input
            class="form-input"
            v-model="password"
            placeholder="请输入密码"
            placeholder-style="color: #A5D6A7"
            :password="!showPwd"
            type="text"
          />
          <text class="pwd-toggle" @tap="showPwd = !showPwd">
            {{ showPwd ? '隐藏' : '显示' }}
          </text>
        </view>
      </view>

      <button
        class="login-btn"
        :class="{ loading: loading }"
        :disabled="loading"
        @tap="handleLogin"
      >
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <view class="form-extra">
        <text class="extra-link" @tap="goRegister">没有账号？立即注册</text>
      </view>
    </view>


    <!-- Blockchain Status -->
    <view class="blockchain-status">
      <view class="bc-dot"></view>
      <text class="bc-text">FISCO BCOS 区块链已连接</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const account = ref('')
const password = ref('')
const showPwd = ref(false)
const loading = ref(false)

async function handleLogin() {
  if (!account.value.trim()) {
    uni.showToast({ title: '请输入学号', icon: 'none' })
    return
  }
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  loading.value = true
  const result = await userStore.login(account.value.trim(), password.value)
  loading.value = false

  if (result.success) {
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 800)
  } else {
    uni.showToast({ title: result.message || '登录失败', icon: 'none' })
  }
}


function goRegister() {
  uni.navigateTo({ url: '/pages/register/register' })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F5E9 0%, #F1F8E9 40%, #FFFFFF 100%);
  padding: 0 $space-xl;
  display: flex;
  flex-direction: column;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120rpx;
  padding-bottom: 60rpx;
}

.logo-wrap {
  width: 140rpx;
  height: 140rpx;
  background: $bg-gradient;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $space-md;
  box-shadow: 0 8rpx 32rpx rgba(46, 125, 50, 0.3);
}

.logo-icon {
  font-size: 64rpx;
}

.app-name {
  font-size: 40rpx;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $space-xs;
}

.app-desc {
  font-size: $font-sm;
  color: $text-secondary;
}

.login-form {
  margin-top: $space-md;
}

.form-item {
  margin-bottom: $space-md;
}

.input-wrap {
  display: flex;
  align-items: center;
  background: $bg-white;
  border-radius: $radius-md;
  padding: 0 $space-md;
  height: 96rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $border-color;
}

.input-icon {
  font-size: 36rpx;
  margin-right: $space-sm;
}

.form-input {
  flex: 1;
  font-size: $font-md;
  color: $text-primary;
  height: 100%;
}

.pwd-toggle {
  font-size: 32rpx;
  padding: $space-xs;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  background: $bg-gradient;
  border-radius: $radius-md;
  color: $text-white;
  font-size: $font-lg;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: $space-lg;
  box-shadow: 0 4rpx 16rpx rgba(46, 125, 50, 0.3);
  border: none;

  &.loading {
    opacity: 0.7;
  }
}

.form-extra {
  display: flex;
  justify-content: center;
  margin-top: $space-md;
}

.extra-link {
  color: $primary-light;
  font-size: $font-sm;
}

.wechat-section {
  margin-top: auto;
  padding-bottom: 60rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: $space-lg;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background: $border-color;
}

.divider-text {
  font-size: $font-xs;
  color: $text-light;
  margin: 0 $space-md;
}

.wechat-btn {
  width: 100%;
  height: 88rpx;
  background: #07C160;
  border-radius: $radius-md;
  color: #fff;
  font-size: $font-md;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-xs;
  border: none;
}

.wechat-icon {
  font-size: 32rpx;
}

.wechat-text {
  font-weight: 600;
}

.blockchain-status {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: $space-lg;
}

.bc-dot {
  width: 12rpx;
  height: 12rpx;
  background: $accent;
  border-radius: 50%;
  margin-right: $space-xs;
}

.bc-text {
  font-size: $font-xs;
  color: $text-light;
}
</style>
