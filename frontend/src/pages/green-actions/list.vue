<template>
  <view class="page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <!-- Stats Bar -->
      <view class="stats-bar">
        <view class="stat-item">
          <text class="stat-value">{{ stats.todayCheckins }}</text>
          <text class="stat-label">今日打卡</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ stats.totalCarbon }}</text>
          <text class="stat-label">累计减碳(kg)</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ stats.totalPoints }}</text>
          <text class="stat-label">累计积分</text>
        </view>
      </view>

      <!-- Primary Actions -->
      <view class="section-header">
        <text class="section-title">核心行动</text>
      </view>

      <!-- 光盘打卡 -->
      <view class="primary-action card" @tap="goCheckin(cleanPlateAction)">
        <view class="pa-left">
          <view class="pa-icon-wrap" style="background: #4CAF5020;">
            <text class="pa-icon">🍽️</text>
          </view>
          <view class="pa-info">
            <text class="pa-name">光盘打卡</text>
            <text class="pa-desc">拍摄餐后光盘照片，AI自动审核</text>
            <view class="pa-tags">
              <text class="pa-tag green">+10 碳积分</text>
              <text class="pa-tag">今日剩余 {{ cleanPlateRemaining }} 次</text>
            </view>
          </view>
        </view>
        <view class="pa-right">
          <view class="pa-circle" :class="{ full: cleanPlateRemaining <= 0 }">
            <text class="pa-circle-num">{{ cleanPlateRemaining }}</text>
          </view>
          <text class="pa-arrow">→</text>
        </view>
      </view>

      <!-- 垃圾回收 -->
      <view class="primary-action card" @tap="goRecycle">
        <view class="pa-left">
          <view class="pa-icon-wrap" style="background: #29B6F620;">
            <text class="pa-icon">♻️</text>
          </view>
          <view class="pa-info">
            <text class="pa-name">垃圾回收</text>
            <text class="pa-desc">选择物品类型，拍照上传AI核验</text>
            <view class="pa-tags">
              <text class="pa-tag blue">+5~30 碳积分</text>
              <text class="pa-tag">今日剩余 {{ recycleRemaining }} 次</text>
            </view>
          </view>
        </view>
        <view class="pa-right">
          <view class="pa-circle" :class="{ full: recycleRemaining <= 0 }">
            <text class="pa-circle-num">{{ recycleRemaining }}</text>
          </view>
          <text class="pa-arrow">→</text>
        </view>
      </view>

      <!-- Other Green Actions -->
      <view class="section-header">
        <text class="section-title">更多绿色行动</text>
      </view>

      <view class="actions-grid">
        <view
          v-for="action in otherActions"
          :key="action.id"
          class="action-card card"
          @tap="goCheckin(action)"
        >
          <text class="ac-icon">{{ action.icon }}</text>
          <text class="ac-name">{{ action.name }}</text>
          <text class="ac-points">+{{ action.points }}分</text>
          <text class="ac-count">{{ action.totalCheckins }}人参与</text>
        </view>
      </view>

      <!-- Recent Check-ins -->
      <view class="section-header">
        <text class="section-title">最近打卡</text>
      </view>

      <view v-if="records.length === 0" class="empty-tip">
        <text>暂无打卡记录</text>
      </view>

      <view v-for="r in records" :key="r.id" class="record-row">
        <text class="rec-icon">{{ r.icon }}</text>
        <view class="rec-info">
          <text class="rec-name">{{ r.actionName }}</text>
          <text class="rec-date">{{ r.date }}</text>
        </view>
        <view class="rec-right">
          <text class="rec-status" :class="r.status">{{ statusLabels[r.status || 'passed'] }}</text>
          <text class="rec-points">+{{ r.points }}</text>
        </view>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { actionsApi } from '@/api/actions'

const actions = ref([])
const records = ref([])
const stats = reactive({ todayCheckins: 0, totalCarbon: 0, totalPoints: 0 })

const statusLabels = { passed: '已通过', pending: '待审核', rejected: '已驳回' }

const cleanPlateAction = computed(() => actions.value.find((a) => a.id === 'cleanPlate' || a.id === 'clean_plate') || defaultCleanPlate)
const defaultCleanPlate = { id: 'clean_plate', name: '光盘打卡', icon: '🍽️', points: 10, dailyLimit: 3, description: '拍摄餐后光盘照片，AI自动审核' }

const cleanPlateRemaining = computed(() => {
  const todayCount = records.value.filter((r) => (r.actionId === 'cleanPlate' || r.actionId === 'clean_plate') && isToday(r.date)).length
  return Math.max(0, 3 - todayCount)
})

const recycleRemaining = computed(() => {
  const todayCount = records.value.filter((r) => r.actionId && r.actionId.startsWith('recycle') && isToday(r.date)).length
  return Math.max(0, 5 - todayCount)
})

const otherActions = computed(() => actions.value.filter((a) => !['cleanPlate', 'clean_plate', 'recycle'].includes(a.id)))

function isToday(dateStr) {
  if (!dateStr) return false
  const today = new Date().toISOString().slice(0, 10)
  return dateStr.slice(0, 10) === today
}

onShow(() => loadData())

async function loadData() {
  const [actRes, histRes] = await Promise.all([
    actionsApi.getActionList(),
    actionsApi.getMyHistory(),
  ])
  if (actRes.code === 200) actions.value = actRes.data
  if (histRes.code === 200) {
    records.value = histRes.data.records || histRes.data.content || []
    const todayRecords = records.value.filter((r) => isToday(r.date))
    stats.todayCheckins = todayRecords.length
    stats.totalPoints = records.value.reduce((s, r) => s + (r.points || 0), 0)
    stats.totalCarbon = (stats.totalPoints * 0.05).toFixed(1)
  }
}

function goCheckin(action) {
  const params = `id=${action.id}&name=${encodeURIComponent(action.name)}&icon=${encodeURIComponent(action.icon)}&points=${action.points}`
  uni.navigateTo({ url: `/pages/green-actions/checkin?${params}` })
}

function goRecycle() {
  uni.navigateTo({ url: '/pages/green-actions/checkin?id=recycle&name=' + encodeURIComponent('垃圾回收') + '&icon=' + encodeURIComponent('♻️') + '&points=5' })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; }

.page-scroll { padding: 0 $space-md; height: 100vh; }

.stats-bar {
  margin: $space-md 0; padding: $space-md $space-lg;
  background: $bg-gradient; border-radius: $radius-md;
  display: flex; justify-content: space-around;
}
.stat-item { text-align: center; }
.stat-value { color: #fff; font-size: $font-xl; font-weight: 700; display: block; }
.stat-label { color: rgba(255,255,255,0.8); font-size: $font-xs; }

.section-header { margin: $space-lg 0 $space-md; }
.section-title { font-size: $font-lg; font-weight: 600; color: $text-primary; }

.primary-action {
  padding: $space-lg; display: flex; justify-content: space-between; align-items: center;
  margin-bottom: $space-sm;
}
.pa-left { display: flex; align-items: center; gap: $space-sm; flex: 1; }
.pa-icon-wrap {
  width: 96rpx; height: 96rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.pa-icon { font-size: 44rpx; }
.pa-name { font-size: $font-lg; font-weight: 700; color: $text-primary; display: block; }
.pa-desc { font-size: $font-xs; color: $text-light; margin: 4rpx 0; }
.pa-tags { display: flex; gap: $space-xs; margin-top: 4rpx; }
.pa-tag { font-size: 20rpx; padding: 2rpx 12rpx; border-radius: $radius-round; background: #F5F5F5; color: $text-secondary;
  &.green { color: $primary-light; background: #E8F5E9; }
  &.blue { color: #29B6F6; background: #E1F5FE; }
}
.pa-right { display: flex; align-items: center; gap: $space-sm; }
.pa-circle {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  background: #E8F5E9; display: flex; align-items: center; justify-content: center;
  &.full { background: #FFEBEE; }
}
.pa-circle-num { font-size: $font-lg; font-weight: 700; color: $primary; }
.full .pa-circle-num { color: $danger; }
.pa-arrow { font-size: $font-lg; color: $text-light; }

.actions-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: $space-sm; }
.action-card { padding: $space-md; text-align: center; display: flex; flex-direction: column; align-items: center; gap: 4rpx; }
.ac-icon { font-size: 40rpx; }
.ac-name { font-size: $font-xs; color: $text-primary; font-weight: 500; }
.ac-points { font-size: 22rpx; color: $accent-warm; font-weight: 700; }
.ac-count { font-size: 20rpx; color: $text-light; }

.record-row {
  display: flex; align-items: center; background: #fff; padding: $space-md $space-lg;
  border-radius: $radius-sm; margin-bottom: 6rpx; gap: $space-sm;
}
.rec-icon { font-size: 32rpx; }
.rec-info { flex: 1; }
.rec-name { font-size: $font-sm; color: $text-primary; display: block; font-weight: 500; }
.rec-date { font-size: $font-xs; color: $text-light; }
.rec-right { text-align: right; }
.rec-status { font-size: 20rpx; padding: 2rpx 10rpx; border-radius: $radius-round;
  &.passed { background: #E8F5E9; color: $success; }
  &.pending { background: #FFF8E1; color: $warning; }
  &.rejected { background: #FFEBEE; color: $danger; }
}
.rec-points { font-size: $font-md; font-weight: 700; color: $accent-warm; display: block; }

.empty-tip { text-align: center; padding: $space-xl; color: $text-light; font-size: $font-sm; }
.safe-bottom { height: 50rpx; }
</style>
