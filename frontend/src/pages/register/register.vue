<template>
  <view class="register-page">
    <view class="page-header">
      <text class="page-title">创建账号</text>
      <text class="page-desc">加入校园低碳行动，赚取积分赢奖励</text>
    </view>

    <view class="register-form">
      <view class="form-item">
        <text class="form-label">学号</text>
        <view class="input-wrap">
          <input
            class="form-input"
            v-model="form.studentId"
            placeholder="请输入学号"
            placeholder-style="color: #A5D6A7"
            type="text"
          />
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">姓名</text>
        <view class="input-wrap">
          <input
            class="form-input"
            v-model="form.name"
            placeholder="请输入真实姓名"
            placeholder-style="color: #A5D6A7"
            type="text"
          />
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">学院</text>
        <picker
          mode="selector"
          :range="departments"
          @change="onDepartmentChange"
        >
          <view class="picker-wrap" :class="{ placeholder: !form.department }">
            {{ form.department || '请选择学院' }}
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">班级</text>
        <view class="input-wrap">
          <input
            class="form-input"
            v-model="form.className"
            placeholder="例如：软件工程2401班"
            placeholder-style="color: #A5D6A7"
            type="text"
          />
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">密码</text>
        <view class="input-wrap">
          <input
            class="form-input"
            v-model="form.password"
            placeholder="请设置密码（6-20位）"
            placeholder-style="color: #A5D6A7"
            :password="!showPwd"
            type="text"
          />
          <text class="pwd-toggle" @tap="showPwd = !showPwd">
            {{ showPwd ? '🙈' : '👁️' }}
          </text>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">确认密码</text>
        <view class="input-wrap">
          <input
            class="form-input"
            v-model="confirmPassword"
            placeholder="请再次输入密码"
            placeholder-style="color: #A5D6A7"
            :password="!showPwd"
            type="text"
          />
        </view>
      </view>

      <button
        class="register-btn"
        :class="{ loading: loading }"
        :disabled="loading"
        @tap="handleRegister"
      >
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <view class="form-extra">
        <text>已有账号？</text>
        <text class="extra-link" @tap="goLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const form = reactive({
  studentId: '',
  name: '',
  department: '',
  className: '',
  password: '',
})

const confirmPassword = ref('')
const showPwd = ref(false)
const loading = ref(false)

const departments = [
  '计算机科学与技术学院',
  '软件学院',
  '信息与通信工程学院',
  '电子工程学院',
  '数学与统计学院',
  '物理学院',
  '化学化工学院',
  '生命科学学院',
  '经济管理学院',
  '外国语学院',
  '法学院',
  '马克思主义学院',
]

function onDepartmentChange(e) {
  form.department = departments[e.detail.value]
}

function validateForm() {
  if (!form.studentId.trim()) return '请输入学号'
  if (!/^\d{6,12}$/.test(form.studentId.trim())) return '学号格式不正确'
  if (!form.name.trim()) return '请输入姓名'
  if (!form.department) return '请选择学院'
  if (!form.className.trim()) return '请输入班级'
  if (!form.password || form.password.length < 6) return '密码至少6位'
  if (form.password !== confirmPassword.value) return '两次密码输入不一致'
  return null
}

async function handleRegister() {
  const error = validateForm()
  if (error) {
    uni.showToast({ title: error, icon: 'none' })
    return
  }

  loading.value = true
  const result = await userStore.register({
    studentId: form.studentId.trim(),
    name: form.name.trim(),
    department: form.department,
    className: form.className.trim(),
    password: form.password,
  })
  loading.value = false

  if (result.success) {
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } else {
    uni.showToast({ title: result.message || '注册失败', icon: 'none' })
  }
}

function goLogin() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F5E9 0%, #F1F8E9 20%, #FFFFFF 100%);
  padding: 0 $space-xl;
}

.page-header {
  padding-top: 40rpx;
  padding-bottom: 40rpx;
}

.page-title {
  font-size: 40rpx;
  font-weight: 700;
  color: $text-primary;
  display: block;
  margin-bottom: $space-xs;
}

.page-desc {
  font-size: $font-sm;
  color: $text-secondary;
}

.register-form {
  margin-top: $space-sm;
}

.form-item {
  margin-bottom: $space-lg;
}

.form-label {
  font-size: $font-sm;
  color: $text-primary;
  font-weight: 600;
  margin-bottom: $space-xs;
  display: block;
}

.input-wrap {
  display: flex;
  align-items: center;
  background: $bg-white;
  border-radius: $radius-md;
  padding: 0 $space-md;
  height: 88rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $border-color;
}

.form-input {
  flex: 1;
  font-size: $font-md;
  color: $text-primary;
  height: 100%;
}

.picker-wrap {
  display: flex;
  align-items: center;
  background: $bg-white;
  border-radius: $radius-md;
  padding: 0 $space-md;
  height: 88rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $border-color;
  font-size: $font-md;
  color: $text-primary;

  &.placeholder {
    color: #A5D6A7;
  }
}

.pwd-toggle {
  font-size: 28rpx;
  padding: $space-xs;
}

.register-btn {
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
  margin-top: $space-xl;
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
  font-size: $font-sm;
  color: $text-secondary;
  gap: $space-xs;
}

.extra-link {
  color: $primary-light;
}
</style>
