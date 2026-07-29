<template>
  <view class="profile-page">
    <!-- Profile Header -->
    <view class="profile-header">
      <view class="profile-bg"></view>
      <view class="profile-info">
        <view class="profile-avatar">
          <text class="avatar-text">{{ avatarText }}</text>
        </view>
        <view class="profile-names">
          <text class="profile-name">{{ userStore.userName }}</text>
          <text class="profile-id">学号：{{ userStore.studentId }}</text>
        </view>
      </view>
    </view>

    <!-- Carbon Data Dashboard -->
    <view class="carbon-dash card">
      <view class="dash-main">
        <text class="dash-main-label">碳积分总额</text>
        <text class="dash-main-value">{{ userInfo.pointsBalance || 0 }}</text>
        <text class="dash-main-hint">≈ {{ ((userInfo.pointsBalance || 0) * 0.05).toFixed(1) }} kg CO₂ 减排量</text>
      </view>
      <view class="dash-grid">
        <view class="dash-item">
          <text class="dash-item-val green">{{ monthlyStats.earned }}</text>
          <text class="dash-item-label">本月获得</text>
        </view>
        <view class="dash-item">
          <text class="dash-item-val orange">{{ monthlyStats.spent }}</text>
          <text class="dash-item-label">本月消耗</text>
        </view>
        <view class="dash-item">
          <text class="dash-item-val">{{ carbonReduced.toFixed(1) }}kg</text>
          <text class="dash-item-label">累计减排</text>
        </view>
        <view class="dash-item">
          <text class="dash-item-val">{{ ecoTreeCount }}</text>
          <text class="dash-item-label">≈ 棵树/年</text>
        </view>
      </view>
    </view>

    <!-- Info Card -->
    <view class="info-card card">
      <view class="info-row">
        <text class="info-label">学院</text>
        <text class="info-value">{{ userStore.department }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">班级</text>
        <text class="info-value">{{ userInfo.className || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">年级</text>
        <text class="info-value">{{ userInfo.grade || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">加入日期</text>
        <text class="info-value">{{ formatDate(userInfo.createdAt) || '-' }}</text>
      </view>
      <view class="info-row" @tap="editProfile">
        <text class="info-label">资料编辑</text>
        <text class="info-value edit-link">修改 →</text>
      </view>
    </view>

    <!-- Blockchain Info -->
    <view class="info-card card">
      <view class="card-title">
        <text class="title-icon">⛓️</text>
        <text class="title-text">区块链账户</text>
      </view>
      <view class="info-row">
        <text class="info-label">钱包地址</text>
        <text class="info-value bc-address">{{ shortAddress }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">链上积分</text>
        <text class="info-value points-value">{{ onChainBalance }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">链上存证数</text>
        <text class="info-value">{{ onChainTxCount }}</text>
      </view>
    </view>

    <!-- Actions -->
    <view class="action-list">
      <view class="action-item" @tap="handleAction('records')">
        <text class="action-icon">📋</text>
        <text class="action-text">碳足迹记录</text>
        <text class="action-arrow">→</text>
      </view>
      <view class="action-item" @tap="handleAction('blockchain')">
        <text class="action-icon">🔗</text>
        <text class="action-text">链上交易记录</text>
        <text class="action-arrow">→</text>
      </view>
      <view class="action-item" @tap="handleAction('about')">
        <text class="action-icon">ℹ️</text>
        <text class="action-text">关于我们</text>
        <text class="action-arrow">→</text>
      </view>
    </view>

    <!-- Logout -->
    <view class="logout-section">
      <button class="logout-btn" @tap="handleLogout">退出登录</button>
    </view>

    <!-- Edit Modal -->
    <view class="modal-overlay" v-if="showEdit" @tap="showEdit = false">
      <view class="modal-card" @tap.stop>
        <text class="modal-title">编辑资料</text>

        <view class="modal-form">
          <view class="form-item">
            <text class="form-label">姓名</text>
            <input class="form-input" v-model="editForm.name" placeholder="请输入姓名" />
          </view>
          <view class="form-item">
            <text class="form-label">学院</text>
            <picker mode="selector" :range="departments" @change="onEditDept">
              <view class="form-picker">{{ editForm.department || '请选择' }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">班级</text>
            <input class="form-input" v-model="editForm.className" placeholder="请输入班级" />
          </view>
        </view>

        <view class="modal-actions">
          <button class="btn-cancel" @tap="showEdit = false">取消</button>
          <button class="btn-confirm" @tap="handleSave">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { formatAddress } from '@/utils/web3'
import { request } from '@/api/request'
import { actionsApi } from '@/api/actions'

const userStore = useUserStore()

const showEdit = ref(false)
const onChainBalance = ref(0)
const onChainTxCount = ref(0)
const monthlyStats = reactive({ earned: 0, spent: 0 })

const editForm = reactive({
  name: '',
  department: '',
  className: '',
})

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

const userInfo = computed(() => userStore.userInfo || {})
const avatarText = computed(() => {
  const name = userStore.userName
  return name ? name.charAt(0) : '学'
})

const shortAddress = computed(() => {
  return formatAddress(userStore.walletAddress) || '尚未开通'
})

const carbonReduced = computed(() => (userInfo.value.pointsBalance || 0) * 0.05)
const ecoTreeCount = computed(() => (carbonReduced.value / 20).toFixed(2))

onShow(() => {
  userStore.fetchProfile()
  loadBlockchainInfo()
  loadMonthlyStats()
})

async function loadMonthlyStats() {
  // 本月获得：从打卡记录中汇总本月积分
  try {
    const histRes = await actionsApi.getMyHistory(1, 200)
    if (histRes.code === 200) {
      const allRecords = histRes.data.records || histRes.data.content || []
      const now = new Date()
      const thisMonth = now.getMonth()
      const thisYear = now.getFullYear()
      const monthRecords = allRecords.filter((r) => {
        if (!r.date) return false
        const d = new Date(r.date)
        return d.getFullYear() === thisYear && d.getMonth() === thisMonth
      })
      monthlyStats.earned = monthRecords.reduce((s, r) => s + (r.points || 0), 0)
    }
  } catch (e) { /* ignore */ }

  // 本月消耗：从兑换记录中汇总
  try {
    const redeemRes = await request({ url: '/points/redeem-history', method: 'GET' })
    if (redeemRes.code === 200) {
      const list = redeemRes.data.records || redeemRes.data || []
      const now = new Date()
      const thisMonth = now.getMonth()
      const thisYear = now.getFullYear()
      monthlyStats.spent = list
        .filter((r) => {
          const d = new Date(r.createdAt || r.date || r.redeemedAt)
          return d.getFullYear() === thisYear && d.getMonth() === thisMonth
        })
        .reduce((s, r) => s + (r.points || r.pointsSpent || 0), 0)
    }
  } catch (e) { /* ignore */ }
}

async function loadBlockchainInfo() {
  try {
    const res = await request({ url: '/blockchain/points-balance', method: 'GET' })
    if (res.code === 200) {
      onChainBalance.value = res.data.onChainBalance || 0
      onChainTxCount.value = res.data.totalBalance || 0
    }
  } catch (e) { /* ignore */ }
}

function editProfile() {
  editForm.name = userInfo.value.name || ''
  editForm.department = userStore.department
  editForm.className = userInfo.value.className || ''
  showEdit.value = true
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.slice(0, 10)
}

function onEditDept(e) {
  editForm.department = departments[e.detail.value]
}

async function handleSave() {
  const result = await userStore.updateProfile({
    name: editForm.name,
    department: editForm.department,
    className: editForm.className,
  })
  if (result.success) {
    uni.showToast({ title: '保存成功', icon: 'success' })
    showEdit.value = false
  } else {
    uni.showToast({ title: result.message, icon: 'none' })
  }
}

function handleAction(type) {
  const tips = {
    records: '碳足迹记录页面开发中',
    blockchain: '链上交易记录页面开发中',
    about: '关于我们页面开发中',
  }
  uni.showToast({ title: tips[type] || '开发中', icon: 'none' })
}

function handleLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    },
  })
}
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $bg-color;
}

.profile-header {
  background: $bg-gradient;
  padding: 60rpx $space-lg 40rpx;
  position: relative;
  overflow: hidden;
}

.profile-bg {
  position: absolute;
  right: -60rpx;
  top: -60rpx;
  width: 300rpx;
  height: 300rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.profile-info {
  display: flex;
  align-items: center;
  gap: $space-md;
  position: relative;
  z-index: 1;
}

.profile-avatar {
  width: 100rpx;
  height: 100rpx;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid rgba(255, 255, 255, 0.4);
}

.avatar-text {
  color: $text-white;
  font-size: 44rpx;
  font-weight: 700;
}

.profile-name {
  color: $text-white;
  font-size: $font-xl;
  font-weight: 700;
  display: block;
}

.profile-id {
  color: rgba(255, 255, 255, 0.8);
  font-size: $font-sm;
}

/* Carbon Dashboard */
.carbon-dash {
  margin: -20rpx $space-md 0;
  padding: $space-lg;
  position: relative;
  z-index: 2;
  border-radius: $radius-lg;
}

.dash-main {
  text-align: center;
  padding-bottom: $space-md;
  border-bottom: 1rpx solid $border-color;
  margin-bottom: $space-md;
}

.dash-main-label {
  font-size: $font-xs;
  color: $text-light;
  display: block;
}

.dash-main-value {
  font-size: 64rpx;
  font-weight: 800;
  color: $primary-dark;
  display: block;
  line-height: 1.2;
}

.dash-main-hint {
  font-size: $font-xs;
  color: $text-secondary;
}

.dash-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-sm;
}

.dash-item {
  text-align: center;
}

.dash-item-val {
  font-size: $font-lg;
  font-weight: 700;
  color: $text-primary;
  display: block;
  &.green { color: $success; }
  &.orange { color: $accent-warm; }
}

.dash-item-label {
  font-size: 20rpx;
  color: $text-light;
}

/* Info Cards */
.info-card {
  margin: $space-md $space-md 0;
  padding: $space-md $space-lg;
}

.card-title {
  display: flex;
  align-items: center;
  gap: $space-xs;
  padding-bottom: $space-sm;
  margin-bottom: $space-sm;
  border-bottom: 1rpx solid $border-color;
}

.title-icon {
  font-size: $font-lg;
}

.title-text {
  font-size: $font-md;
  font-weight: 600;
  color: $text-primary;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-sm 0;

  & + & {
    border-top: 1rpx solid $border-color;
  }
}

.info-label {
  font-size: $font-sm;
  color: $text-secondary;
}

.info-value {
  font-size: $font-sm;
  color: $text-primary;
  font-weight: 500;

  &.edit-link {
    color: $primary-light;
  }

  &.bc-address {
    font-size: $font-xs;
    font-family: monospace;
    color: $text-secondary;
  }

  &.points-value {
    color: $accent-warm;
    font-weight: 700;
    font-size: $font-md;
  }
}

.action-list {
  margin: $space-md;
}

.action-item {
  display: flex;
  align-items: center;
  background: $bg-card;
  padding: $space-md $space-lg;

  & + & {
    border-top: 1rpx solid $border-color;
  }

  &:first-child {
    border-radius: $radius-md $radius-md 0 0;
  }

  &:last-child {
    border-radius: 0 0 $radius-md $radius-md;
  }

  &:only-child {
    border-radius: $radius-md;
  }
}

.action-icon {
  font-size: 36rpx;
  margin-right: $space-sm;
}

.action-text {
  flex: 1;
  font-size: $font-md;
  color: $text-primary;
}

.action-arrow {
  color: $text-light;
  font-size: $font-md;
}

.logout-section {
  padding: $space-xl $space-md;
  padding-bottom: calc($safe-bottom + $space-xl);
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  background: $bg-white;
  border-radius: $radius-md;
  color: $danger;
  font-size: $font-md;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid rgba(244, 67, 54, 0.3);
}

/* Edit Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: $space-xl;
}

.modal-card {
  background: $bg-white;
  border-radius: $radius-lg;
  padding: $space-xl;
  width: 100%;
  max-width: 600rpx;
}

.modal-title {
  font-size: $font-lg;
  font-weight: 700;
  color: $text-primary;
  text-align: center;
  display: block;
  margin-bottom: $space-lg;
}

.modal-form {
  .form-item {
    margin-bottom: $space-md;
  }

  .form-label {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    margin-bottom: $space-xs;
  }

  .form-input, .form-picker {
    width: 100%;
    height: 80rpx;
    background: $bg-color;
    border-radius: $radius-sm;
    padding: 0 $space-sm;
    font-size: $font-md;
    color: $text-primary;
    box-sizing: border-box;
    display: flex;
    align-items: center;
  }
}

.modal-actions {
  display: flex;
  gap: $space-md;
  margin-top: $space-lg;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  height: 80rpx;
  border-radius: $radius-md;
  font-size: $font-md;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.btn-cancel {
  background: $bg-color;
  color: $text-secondary;
}

.btn-confirm {
  background: $bg-gradient;
  color: $text-white;
}
</style>
