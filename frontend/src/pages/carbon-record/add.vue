<template>
  <view class="page">
    <!-- Step 1: Take Photo -->
    <view class="step-section" v-if="step === 1">
      <view class="photo-area" @tap="chooseImage">
        <view v-if="!photoPath" class="photo-placeholder">
          <text class="photo-icon">📸</text>
          <text class="photo-text">拍照记录碳足迹</text>
          <text class="photo-hint">拍下你的出行、餐饮、用电等场景</text>
          <text class="photo-hint">AI 将自动识别并计算碳排放量</text>
        </view>
        <image v-else :src="photoPath" class="photo-preview" mode="aspectFill" />
      </view>

      <!-- 行为类型选择 -->
      <view class="behavior-select card">
        <text class="section-label">选择记录类型</text>
        <view class="type-grid">
          <view
            v-for="t in behaviorTypes"
            :key="t.id"
            class="type-item"
            :class="{ selected: selectedBehaviorType === t.id }"
            @tap="selectedBehaviorType = t.id"
          >
            <text class="type-icon">{{ t.icon }}</text>
            <text class="type-name">{{ t.name }}</text>
          </view>
        </view>
      </view>

      <view v-if="photoPath" class="photo-actions">
        <button class="retake-btn" @tap="chooseImage">重新拍照</button>
        <button class="analyze-btn" @tap="startAnalyze" :disabled="analyzing">
          {{ analyzing ? 'AI 分析中...' : '🔍 AI 智能分析' }}
        </button>
      </view>
    </view>

    <!-- Step 2: AI Analysis Result (后端已自动入库+积分，无需再次提交) -->
    <view class="step-section" v-if="step === 2">
      <view class="result-header">
        <text class="result-badge" :class="aiResult.decision === 'PASS' ? 'badge-pass' : 'badge-reject'">
          {{ aiResult.decision === 'PASS' ? '✅ AI 核验通过' : '❌ AI 核验未通过' }}
        </text>
        <text class="result-confidence">置信度 {{ pct(aiResult.score) }}%</text>
      </view>

      <view class="result-card card">
        <view class="result-icon-row">
          <text class="result-cat-icon">{{ behaviorIcon(selectedBehaviorType) }}</text>
          <view>
            <text class="result-category">行为类型：{{ behaviorName(selectedBehaviorType) }}</text>
            <text class="result-unit">AI 识别标签：{{ aiResult.label || '未识别' }}</text>
          </view>
        </view>
        <view class="result-points" v-if="aiResult.decision === 'PASS'">
          <text class="points-big">+{{ aiResult.points || 0 }}</text>
          <text class="points-label">碳积分已到账</text>
        </view>
        <view class="result-suggestion" v-else>
          <text class="suggestion-icon">💡</text>
          <text class="suggestion-text">图片与所选行为类型不匹配，或置信度低于阈值({{ (aiResult.threshold * 100).toFixed(0) }}%)，请重新拍照</text>
        </view>
      </view>

      <!-- 审核中状态 -->
      <view class="result-card card" v-if="aiResult.decision === 'PASS' && aiResult.points === 0">
        <view class="result-suggestion">
          <text class="suggestion-icon">⏳</text>
          <text class="suggestion-text">AI 置信度偏低，记录已提交，等待管理员人工审核通过后积分到账</text>
        </view>
      </view>

      <view class="submit-actions">
        <button class="btn-back" @tap="step = 1">继续记录</button>
        <button class="btn-submit" @tap="goBack">完成</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { recordApi } from '@/api/record'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const step = ref(1)
const photoPath = ref('')
const analyzing = ref(false)
const aiResult = ref({})
const selectedBehaviorType = ref('recycle')

const behaviorTypes = [
  { id: 'walk', icon: '🚶', name: '步行出行' },
  { id: 'bike', icon: '🚲', name: '骑行出行' },
  { id: 'bus', icon: '🚌', name: '公交出行' },
  { id: 'recycle', icon: '♻️', name: '垃圾分类' },
  { id: 'oldGoods', icon: '📦', name: '旧物回收' },
  { id: 'savePower', icon: '💡', name: '节约用电' },
  { id: 'noPlastic', icon: '🥤', name: '拒绝一次性塑料' },
  { id: 'plantTree', icon: '🌳', name: '植树护绿' },
  { id: 'clean_plate', icon: '🍽️', name: '光盘行动' },
  { id: 'vegan', icon: '🥬', name: '绿色素食' },
  { id: 'stairs', icon: '🪜', name: '走楼梯' },
]

// 后端返回的 score 是 BigDecimal(0~1)，转为百分数字符串
function pct(val) {
  if (val === undefined || val === null) return '0'
  return (Number(val) * 100).toFixed(0)
}

// 根据 behaviorType id 查找图标
function behaviorIcon(id) {
  return behaviorTypes.find(t => t.id === id)?.icon || '📝'
}

// 根据 behaviorType id 查找名称
function behaviorName(id) {
  return behaviorTypes.find(t => t.id === id)?.name || id
}

function goBack() {
  uni.navigateBack()
}

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera', 'album'],
    success: (res) => {
      photoPath.value = res.tempFilePaths[0]
    },
  })
}

async function startAnalyze() {
  analyzing.value = true
  uni.showLoading({ title: 'AI 核验中...' })
  const userId = userStore.userInfo?.id || 0
  try {
    const res = await recordApi.aiAnalyze(photoPath.value, userId, selectedBehaviorType.value)
    uni.hideLoading()
    analyzing.value = false
    if (res.code === 200) {
      // 后端 AiVerifyResponse: { recordId, decision, label, score, threshold, points }
      aiResult.value = res.data
      step.value = 2
    } else {
      uni.showToast({ title: res.message || 'AI 核验失败，请重试', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    analyzing.value = false
    uni.showToast({ title: '网络异常，请重试', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; padding: $space-md; }

/* 行为类型选择器 */
.behavior-select { padding: $space-md; margin-bottom: $space-md; }
.section-label { font-size: $font-sm; font-weight: 600; color: $text-secondary; display: block; margin-bottom: $space-sm; }
.type-grid { display: flex; flex-wrap: wrap; gap: $space-sm; }
.type-item {
  display: flex; align-items: center; gap: 6rpx;
  padding: 8rpx 20rpx; border-radius: $radius-round;
  border: 1rpx solid $border-color; background: #fff;
  font-size: $font-xs;
  &.selected { border-color: $primary; background: #E8F5E9; }
}
.type-icon { font-size: 24rpx; }
.type-name { color: $text-primary; }

.photo-area {
  width: 100%; height: 500rpx; background: #fff; border-radius: $radius-lg;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: $space-md; overflow: hidden; box-shadow: $shadow-sm;
}
.photo-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: $space-sm;
  padding: $space-xl;
}
.photo-icon { font-size: 80rpx; }
.photo-text { font-size: $font-lg; color: $text-primary; font-weight: 600; }
.photo-hint { font-size: $font-sm; color: $text-light; text-align: center; }
.photo-preview { width: 100%; height: 100%; }

.photo-actions { display: flex; gap: $space-md; margin-bottom: $space-md; }
.retake-btn {
  flex: 1; height: 88rpx; background: $bg-color; border-radius: $radius-md;
  color: $text-primary; font-size: $font-md; display: flex; align-items: center;
  justify-content: center; border: 1rpx solid $border-color;
}
.analyze-btn {
  flex: 2; height: 88rpx; background: $bg-gradient; border-radius: $radius-md;
  color: #fff; font-size: $font-md; font-weight: 600; display: flex;
  align-items: center; justify-content: center; border: none;
  &:disabled { opacity: 0.6; }
}

.result-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: $space-md;
}
.result-badge { font-size: $font-sm; padding: 6rpx 20rpx; border-radius: $radius-round; }
.badge-pass { background: $primary-lighter; color: $primary-dark; }
.badge-reject { background: #FFEBEE; color: #C62828; }
.result-confidence { font-size: $font-xs; color: $text-light; }

.result-card { padding: $space-lg; margin-bottom: $space-md; }
.result-icon-row { display: flex; align-items: center; gap: $space-md; margin-bottom: $space-md; }
.result-cat-icon { font-size: 56rpx; }
.result-category { font-size: $font-lg; font-weight: 600; color: $text-primary; display: block; }
.result-unit { font-size: $font-sm; color: $text-secondary; }
.result-points { text-align: center; padding: $space-md 0; }
.points-big { font-size: 56rpx; font-weight: 800; color: $primary; display: block; }
.points-label { font-size: $font-sm; color: $text-secondary; }
.result-suggestion { display: flex; gap: $space-sm; padding: $space-sm; background: #FFF8E1; border-radius: $radius-sm; }
.suggestion-icon { font-size: 28rpx; }
.suggestion-text { font-size: $font-sm; color: #F57F17; flex: 1; }

.submit-actions { display: flex; gap: $space-md; margin-top: $space-lg; }
.btn-back, .btn-submit {
  flex: 1; height: 96rpx; border-radius: $radius-md; font-size: $font-lg;
  font-weight: 600; display: flex; align-items: center; justify-content: center; border: none;
}
.btn-back { background: $bg-color; color: $text-secondary; border: 1rpx solid $border-color; }
.btn-submit { background: $bg-gradient; color: #fff; }
</style>
