<template>
  <view class="page">
    <!-- Loading -->
    <view v-if="loading" class="loading-state">
      <text class="loading-spinner">⏳</text>
      <text>加载中...</text>
    </view>

    <!-- Content -->
    <scroll-view v-else-if="record" class="detail-scroll" scroll-y :show-scrollbar="false">
      <!-- Photo -->
      <view class="photo-section" v-if="record.photo">
        <image :src="record.photo" class="detail-photo" mode="widthFix" />
      </view>
      <view v-else class="photo-empty">
        <text class="empty-icon">📸</text>
        <text>暂无照片</text>
      </view>

      <!-- Carbon Hero -->
      <view class="hero card">
        <text class="hero-icon">{{ record.categoryIcon || '📝' }}</text>
        <text class="hero-value">{{ record.carbonAmount || 0 }}</text>
        <text class="hero-unit">kg CO₂</text>
        <view class="hero-tag" :style="{ backgroundColor: (catColor || '#9E9E9E') + '20', color: catColor || '#9E9E9E' }">
          {{ record.category || '-' }}
        </view>
      </view>

      <!-- Basic Info -->
      <view class="info-card card">
        <text class="section-label">基本信息</text>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-key">类别</text>
            <text class="info-val">{{ record.categoryIcon || '' }} {{ record.category || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-key">数量</text>
            <text class="info-val">{{ record.quantity || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-key">单位碳排</text>
            <text class="info-val">{{ record.aiResult?.carbonPerUnit || '-' }} kg/单位</text>
          </view>
          <view class="info-item">
            <text class="info-key">记录时间</text>
            <text class="info-val">{{ record.date || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- Notes -->
      <view class="info-card card" v-if="record.notes">
        <text class="section-label">备注</text>
        <text class="notes-text">{{ record.notes }}</text>
      </view>

      <!-- AI Analysis -->
      <view class="ai-card card" v-if="record.aiResult">
        <text class="section-label">🤖 AI 分析结果</text>
        <view class="ai-badge">
          <text>识别类别：{{ record.aiResult.category }}</text>
          <text class="ai-conf">置信度 {{ (record.aiResult.confidence * 100).toFixed(0) }}%</text>
        </view>
        <view class="ai-row">
          <text class="ai-detail-label">单位碳排放</text>
          <text class="ai-detail-value">{{ record.aiResult.carbonPerUnit }} kg CO₂/{{ record.aiResult.unit }}</text>
        </view>
        <view class="ai-suggestion" v-if="record.aiResult.suggestion">
          <text class="sug-title">💡 减排建议</text>
          <text class="sug-text">{{ record.aiResult.suggestion }}</text>
        </view>
      </view>

      <!-- Carbon Equivalent -->
      <view class="info-card card" v-if="record.carbonAmount">
        <text class="section-label">🌍 碳排放换算</text>
        <view class="equiv-grid">
          <view class="equiv-item">
            <text class="equiv-icon">🌳</text>
            <text class="equiv-num">{{ (record.carbonAmount / 20).toFixed(2) }}</text>
            <text class="equiv-desc">棵树吸收/年</text>
          </view>
          <view class="equiv-item">
            <text class="equiv-icon">🚗</text>
            <text class="equiv-num">{{ (record.carbonAmount / 0.2).toFixed(1) }}</text>
            <text class="equiv-desc">公里驾车</text>
          </view>
          <view class="equiv-item">
            <text class="equiv-icon">⚡</text>
            <text class="equiv-num">{{ (record.carbonAmount / 0.8).toFixed(1) }}</text>
            <text class="equiv-desc">度电消耗</text>
          </view>
        </view>
      </view>

      <!-- Delete -->
      <view class="action-section">
        <button class="delete-btn" @tap="handleDelete">🗑️ 删除此记录</button>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>

    <!-- Not Found -->
    <view v-else class="not-found">
      <text class="nf-icon">🔍</text>
      <text class="nf-text">记录不存在或已被删除</text>
      <button class="nf-back" @tap="goBack">返回列表</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { recordApi } from '@/api/record'

const loading = ref(true)
const record = ref(null)
const loadError = ref(false)

const catColors = {
  '餐饮': '#FF9800',
  '出行': '#29B6F6',
  '用电': '#FFB300',
  '废弃物': '#8BC34A',
  '购物': '#E91E63',
}

const catColor = computed(() => {
  if (!record.value?.category) return null
  return catColors[record.value.category] || null
})

onLoad(async (options) => {
  const id = options?.id
  if (!id) {
    loading.value = false
    return
  }
  await loadDetail(id)
})

async function loadDetail(id) {
  loading.value = true
  try {
    const res = await recordApi.getDetail(id)
    if (res.code === 200 && res.data) {
      record.value = res.data
    } else {
      record.value = null
    }
  } catch (e) {
    record.value = null
  }
  loading.value = false
}

function handleDelete() {
  if (!record.value) return
  uni.showModal({
    title: '删除确认',
    content: `确定删除这条 ${record.value.carbonAmount}kg 的碳排放记录吗？`,
    confirmColor: '#F44336',
    success: async (r) => {
      if (r.confirm) {
        const res = await recordApi.deleteRecord(record.value.id)
        if (res.code === 200) {
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 800)
        } else {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    },
  })
}

function goBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $bg-color;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  gap: $space-md;
  color: $text-secondary;
  font-size: $font-md;
}
.loading-spinner {
  font-size: 64rpx;
}

.detail-scroll {
  height: 100vh;
  padding: $space-md;
}

.photo-section {
  width: 100%;
  border-radius: $radius-lg;
  overflow: hidden;
  margin-bottom: $space-md;
  box-shadow: $shadow-md;
}
.detail-photo {
  width: 100%;
  display: block;
}
.photo-empty {
  width: 100%;
  height: 200rpx;
  background: #fff;
  border-radius: $radius-lg;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $space-xs;
  margin-bottom: $space-md;
  color: $text-light;
  font-size: $font-sm;
}
.empty-icon {
  font-size: 48rpx;
}

/* Hero */
.hero {
  padding: $space-xl;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-xs;
  margin-bottom: $space-md;
}
.hero-icon {
  font-size: 56rpx;
}
.hero-value {
  font-size: 64rpx;
  font-weight: 800;
  color: $primary-dark;
  line-height: 1;
}
.hero-unit {
  font-size: $font-sm;
  color: $text-secondary;
}
.hero-tag {
  font-size: $font-sm;
  font-weight: 600;
  padding: 4rpx 24rpx;
  border-radius: $radius-round;
  margin-top: $space-xs;
}

/* Info */
.info-card {
  padding: $space-lg;
  margin-bottom: $space-md;
}
.section-label {
  font-size: $font-sm;
  font-weight: 600;
  color: $text-secondary;
  display: block;
  margin-bottom: $space-md;
  padding-bottom: $space-sm;
  border-bottom: 1rpx solid $border-color;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-md;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.info-key {
  font-size: $font-xs;
  color: $text-light;
}
.info-val {
  font-size: $font-md;
  color: $text-primary;
  font-weight: 500;
}
.notes-text {
  font-size: $font-sm;
  color: $text-primary;
  line-height: 1.6;
}

/* AI */
.ai-card {
  padding: $space-lg;
  margin-bottom: $space-md;
}
.ai-badge {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #E8F5E9;
  padding: $space-sm $space-md;
  border-radius: $radius-sm;
  margin-bottom: $space-sm;
  font-size: $font-sm;
  color: $text-primary;
}
.ai-conf {
  font-size: $font-xs;
  color: $primary-light;
  font-weight: 600;
}
.ai-row {
  display: flex;
  justify-content: space-between;
  padding: $space-sm 0;
  font-size: $font-sm;
}
.ai-detail-label {
  color: $text-secondary;
}
.ai-detail-value {
  color: $text-primary;
  font-weight: 600;
}
.ai-suggestion {
  margin-top: $space-sm;
  background: #FFF8E1;
  padding: $space-md;
  border-radius: $radius-sm;
}
.sug-title {
  font-size: $font-sm;
  font-weight: 600;
  color: #F57F17;
  display: block;
  margin-bottom: $space-xs;
}
.sug-text {
  font-size: $font-sm;
  color: #6D4C00;
  line-height: 1.6;
}

/* Equivalent */
.equiv-grid {
  display: flex;
  justify-content: space-around;
}
.equiv-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}
.equiv-icon {
  font-size: 36rpx;
}
.equiv-num {
  font-size: $font-lg;
  font-weight: 700;
  color: $primary-dark;
}
.equiv-desc {
  font-size: $font-xs;
  color: $text-light;
}

/* Delete */
.action-section {
  margin-top: $space-md;
  padding-bottom: $space-lg;
}
.delete-btn {
  width: 100%;
  height: 88rpx;
  background: #fff;
  border-radius: $radius-md;
  color: $danger;
  font-size: $font-md;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid rgba(244, 67, 54, 0.3);
}

/* Not Found */
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  gap: $space-md;
}
.nf-icon {
  font-size: 80rpx;
}
.nf-text {
  font-size: $font-md;
  color: $text-secondary;
}
.nf-back {
  margin-top: $space-lg;
  background: $bg-gradient;
  color: #fff;
  border: none;
  padding: $space-sm $space-xl;
  border-radius: $radius-round;
  font-size: $font-sm;
}

.safe-bottom {
  height: calc($safe-bottom + 40rpx);
}
</style>
