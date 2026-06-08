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

      <view v-if="photoPath" class="photo-actions">
        <button class="retake-btn" @tap="chooseImage">重新拍照</button>
        <button class="analyze-btn" @tap="startAnalyze" :disabled="analyzing">
          {{ analyzing ? 'AI 分析中...' : '🔍 AI 智能分析' }}
        </button>
      </view>
    </view>

    <!-- Step 2: AI Analysis Result -->
    <view class="step-section" v-if="step === 2">
      <view class="result-header">
        <text class="result-badge">AI 分析完成</text>
        <text class="result-confidence">置信度 {{ (aiResult.confidence * 100).toFixed(0) }}%</text>
      </view>

      <view class="result-card card">
        <view class="result-icon-row">
          <text class="result-cat-icon">{{ aiResult.categoryIcon }}</text>
          <view>
            <text class="result-category">类别：{{ aiResult.category }}</text>
            <text class="result-unit">单位碳排放：{{ aiResult.carbonPerUnit }} kg CO₂/{{ aiResult.unit }}</text>
          </view>
        </view>
        <view class="result-suggestion">
          <text class="suggestion-icon">💡</text>
          <text class="suggestion-text">{{ aiResult.suggestion }}</text>
        </view>
      </view>

      <!-- Adjust Form -->
      <view class="form-card card">
        <view class="form-row">
          <text class="form-label">数量</text>
          <input class="form-input" v-model="form.quantity" type="digit" placeholder="输入数量" />
          <text class="form-unit">{{ aiResult.unit || '份' }}</text>
        </view>
        <view class="form-row">
          <text class="form-label">预估碳排</text>
          <text class="form-value">{{ estimatedCarbon }} kg CO₂</text>
        </view>
        <view class="form-row">
          <text class="form-label">备注</text>
          <input class="form-input" v-model="form.notes" placeholder="添加备注（可选）" />
        </view>
      </view>

      <view class="submit-actions">
        <button class="btn-back" @tap="step = 1">返回重拍</button>
        <button class="btn-submit" @tap="submitRecord">确认提交</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { recordApi } from '@/api/record'

const step = ref(1)
const photoPath = ref('')
const analyzing = ref(false)
const aiResult = ref({})
const form = reactive({ quantity: '', notes: '' })

const estimatedCarbon = computed(() => {
  const q = parseFloat(form.quantity) || 1
  return (q * (aiResult.value.carbonPerUnit || 0)).toFixed(2)
})

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
  uni.showLoading({ title: 'AI 分析中...' })
  const res = await recordApi.aiAnalyze(photoPath.value)
  uni.hideLoading()
  analyzing.value = false
  if (res.code === 200) {
    aiResult.value = res.data
    form.quantity = '1'
    form.notes = ''
    step.value = 2
  } else {
    uni.showToast({ title: 'AI 分析失败，请重试', icon: 'none' })
  }
}

async function submitRecord() {
  const data = {
    category: aiResult.value.category,
    categoryIcon: aiResult.value.categoryIcon,
    carbonAmount: parseFloat(estimatedCarbon.value),
    quantity: `${form.quantity || 1}${aiResult.value.unit || '份'}`,
    photo: photoPath.value,
    notes: form.notes || `AI识别：${aiResult.value.category}，${aiResult.value.carbonPerUnit}kg/${aiResult.value.unit}`,
    aiResult: aiResult.value,
  }

  uni.showLoading({ title: '保存中...' })
  const res = await recordApi.create(data)
  uni.hideLoading()
  if (res.code === 200) {
    uni.showToast({ title: '记录成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } else {
    uni.showToast({ title: res.message || '保存失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; padding: $space-md; }

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
.result-badge { background: $primary-lighter; color: $primary-dark; font-size: $font-sm; padding: 6rpx 20rpx; border-radius: $radius-round; }
.result-confidence { font-size: $font-xs; color: $text-light; }

.result-card { padding: $space-lg; margin-bottom: $space-md; }
.result-icon-row { display: flex; align-items: center; gap: $space-md; margin-bottom: $space-md; }
.result-cat-icon { font-size: 56rpx; }
.result-category { font-size: $font-lg; font-weight: 600; color: $text-primary; display: block; }
.result-unit { font-size: $font-sm; color: $text-secondary; }
.result-suggestion { display: flex; gap: $space-sm; padding: $space-sm; background: #FFF8E1; border-radius: $radius-sm; }
.suggestion-icon { font-size: 28rpx; }
.suggestion-text { font-size: $font-sm; color: #F57F17; flex: 1; }

.form-card { padding: $space-lg; margin-bottom: $space-md; }
.form-row { display: flex; align-items: center; margin-bottom: $space-md; }
.form-label { width: 140rpx; font-size: $font-sm; color: $text-secondary; }
.form-input { flex: 1; height: 72rpx; background: $bg-color; border-radius: $radius-sm; padding: 0 $space-sm; font-size: $font-md; }
.form-unit { font-size: $font-sm; color: $text-light; width: 60rpx; text-align: right; }
.form-value { font-size: $font-lg; font-weight: 700; color: $primary-dark; }

.submit-actions { display: flex; gap: $space-md; margin-top: $space-lg; }
.btn-back, .btn-submit {
  flex: 1; height: 96rpx; border-radius: $radius-md; font-size: $font-lg;
  font-weight: 600; display: flex; align-items: center; justify-content: center; border: none;
}
.btn-back { background: $bg-color; color: $text-secondary; border: 1rpx solid $border-color; }
.btn-submit { background: $bg-gradient; color: #fff; }
</style>
