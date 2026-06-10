<template>
  <view class="page">
    <!-- Top Summary -->
    <view class="summary-bar">
      <view class="summary-left">
        <text class="summary-icon">🌍</text>
        <view class="summary-text">
          <text class="summary-label">累计碳排放</text>
          <text class="summary-value">{{ userStore.userInfo?.carbonTotal?.toFixed(1) || 0 }} kg</text>
        </view>
      </view>
      <view class="summary-right" @tap="goDataViz">
        <text class="viz-btn">📊 数据分析</text>
      </view>
    </view>

    <!-- Record List -->
    <scroll-view
      class="record-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      :show-scrollbar="false"
    >
      <view v-if="records.length === 0" class="empty-state">
        <text class="empty-icon">📸</text>
        <text class="empty-text">还没有碳足迹记录</text>
        <text class="empty-desc">点击下方按钮拍照记录吧</text>
      </view>

      <view v-for="record in records" :key="record.id" class="record-card card" @tap="goDetail(record)">
        <view class="record-left">
          <view class="record-icon-wrap" :style="{ backgroundColor: getCatColor(record.category) + '20' }">
            <text class="record-icon">{{ record.categoryIcon }}</text>
          </view>
        </view>
        <view class="record-center">
          <text class="record-category">{{ record.category }}</text>
          <text class="record-note">{{ record.notes }}</text>
          <text class="record-date">{{ record.date }}</text>
        </view>
        <view class="record-right">
          <text class="record-carbon">{{ record.carbonAmount }} kg</text>
          <text class="record-qty">{{ record.quantity }}</text>
        </view>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>

    <!-- FAB -->
    <view class="fab" @tap="goAdd">
      <text class="fab-icon">📷</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { recordApi } from '@/api/record'

const userStore = useUserStore()
const records = ref([])
const refreshing = ref(false)

const catColors = {
  '餐饮': '#FF9800',
  '出行': '#29B6F6',
  '用电': '#FFB300',
  '废弃物': '#8BC34A',
  '购物': '#E91E63',
}
function getCatColor(cat) { return catColors[cat] || '#9E9E9E' }

onShow(() => loadRecords())

async function loadRecords() {
  const res = await recordApi.getList(1, 50)
  if (res.code === 200) records.value = res.data.records || res.data.content || []
}

async function onRefresh() {
  refreshing.value = true
  await Promise.all([loadRecords(), userStore.fetchProfile()])
  refreshing.value = false
}

function goDetail(record) {
  uni.navigateTo({ url: `/pages/carbon-record/detail?id=${record.id}` })
}

function goAdd() {
  uni.navigateTo({ url: '/pages/carbon-record/add' })
}

function goDataViz() {
  uni.navigateTo({ url: '/pages/data-viz/index' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $bg-color;
  padding-bottom: calc($safe-bottom + 120rpx);
}

.summary-bar {
  margin: $space-md;
  padding: $space-md $space-lg;
  background: $bg-gradient;
  border-radius: $radius-md;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.summary-left { display: flex; align-items: center; gap: $space-sm; }
.summary-icon { font-size: 40rpx; }
.summary-text { display: flex; flex-direction: column; }
.summary-label { color: rgba(255,255,255,0.85); font-size: $font-xs; }
.summary-value { color: #fff; font-size: $font-xl; font-weight: 700; }
.viz-btn { color: #fff; font-size: $font-sm; background: rgba(255,255,255,0.2); padding: 8rpx 20rpx; border-radius: $radius-round; }

.record-scroll { padding: 0 $space-md; height: calc(100vh - 200rpx); }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding-top: 200rpx; gap: $space-sm;
}
.empty-icon { font-size: 80rpx; }
.empty-text { font-size: $font-md; color: $text-secondary; }
.empty-desc { font-size: $font-sm; color: $text-light; }

.record-card {
  display: flex; align-items: center; padding: $space-md $space-lg;
  margin-bottom: $space-sm; gap: $space-sm;
}
.record-icon-wrap {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.record-icon { font-size: 36rpx; }
.record-center { flex: 1; }
.record-category { font-size: $font-md; font-weight: 600; color: $text-primary; display: block; }
.record-note { font-size: $font-sm; color: $text-secondary; display: block; margin: 4rpx 0; }
.record-date { font-size: $font-xs; color: $text-light; }
.record-right { text-align: right; }
.record-carbon { font-size: $font-lg; font-weight: 700; color: $primary-dark; }
.record-qty { font-size: $font-xs; color: $text-light; display: block; }

.fab {
  position: fixed; bottom: 140rpx; right: 40rpx;
  width: 100rpx; height: 100rpx; border-radius: 50%;
  background: $bg-gradient; box-shadow: 0 8rpx 24rpx rgba(46,125,50,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 99;
}
.fab-icon { font-size: 44rpx; }

.safe-bottom { height: 30rpx; }
</style>
