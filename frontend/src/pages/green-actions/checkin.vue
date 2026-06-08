<template>
  <view class="page">
    <!-- Header -->
    <view class="header card">
      <text class="header-icon">{{ icon }}</text>
      <text class="header-name">{{ name }}</text>
      <view class="points-badge">+{{ points }} 碳积分</view>
      <text class="daily-hint" v-if="dailyLimit > 0">今日还可打卡 <text class="hint-num">{{ dailyRemaining }}</text> 次</text>
    </view>

    <!-- Category Select (for recycling) -->
    <view class="section card" v-if="isRecycle">
      <text class="section-title">物品类型</text>
      <view class="category-grid">
        <view
          v-for="cat in recycleCategories"
          :key="cat.id"
          class="cat-item"
          :class="{ selected: selectedCategory === cat.id }"
          @tap="selectedCategory = cat.id"
        >
          <text class="cat-icon">{{ cat.icon }}</text>
          <text class="cat-name">{{ cat.name }}</text>
          <text class="cat-points">+{{ cat.points }}分</text>
        </view>
      </view>
    </view>

    <!-- Photo -->
    <view class="section card">
      <text class="section-title">拍照上传</text>
      <view class="photo-area" @tap="chooseImage">
        <image v-if="photoPath" :src="photoPath" class="photo-preview" mode="aspectFill" />
        <view v-else class="photo-placeholder">
          <text class="photo-icon">📸</text>
          <text>点击拍照</text>
          <text class="photo-hint">仅支持 jpg/png，大小不超过5MB</text>
        </view>
      </view>
    </view>

    <!-- Notes -->
    <view class="section card">
      <text class="section-title">备注（可选）</text>
      <textarea class="notes-input" v-model="notes" placeholder="简单描述一下吧..." />
    </view>

    <!-- Submit -->
    <button class="submit-btn" :disabled="submitting || !canSubmit" @tap="handleSubmit">
      {{ submitting ? '提交中...' : !photoPath ? '请先拍照' : isRecycle && !selectedCategory ? '请选择物品类型' : canSubmit ? '提交审核' : '今日次数已用完' }}
    </button>

    <!-- Result Modal -->
    <view class="modal-overlay" v-if="showResult" @tap="showResult = false">
      <view class="modal-card" @tap.stop>
        <text class="modal-icon">{{ result.status === 'passed' ? '✅' : result.status === 'pending' ? '⏳' : '❌' }}</text>
        <text class="modal-title">{{ result.title }}</text>
        <text class="modal-desc">{{ result.desc }}</text>

        <view class="modal-detail" v-if="result.aiResult">
          <view class="md-row">
            <text class="md-label">AI 置信度</text>
            <text class="md-value" :class="{ low: result.aiResult.confidence < 0.80 }">
              {{ (result.aiResult.confidence * 100).toFixed(0) }}%
            </text>
          </view>
          <view class="md-row" v-if="result.txHash">
            <text class="md-label">链上交易</text>
            <text class="md-value tx">{{ shortTx(result.txHash) }}</text>
          </view>
        </view>

        <view class="modal-hint" v-if="result.status === 'pending'">
          <text>AI 置信度低于 80%，已提交管理员人工审核，审核通过后积分自动到账</text>
        </view>

        <button class="modal-btn" @tap="closeResult">我知道了</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { actionsApi } from '@/api/actions'

const actionId = ref('')
const name = ref('')
const icon = ref('')
const points = ref(0)
const dailyLimit = ref(0)
const dailyRemaining = ref(99)
const photoPath = ref('')
const notes = ref('')
const submitting = ref(false)
const showResult = ref(false)
const result = ref({})
const selectedCategory = ref('')

const isRecycle = computed(() => actionId.value === 'recycle' || name.value.includes('回收'))

const recycleCategories = [
  { id: 'bottle', icon: '🫙', name: '塑料瓶', points: 5 },
  { id: 'paper', icon: '📄', name: '废纸', points: 3 },
  { id: 'book', icon: '📚', name: '旧书籍', points: 8 },
  { id: 'can', icon: '🥫', name: '易拉罐', points: 5 },
  { id: 'battery', icon: '🔋', name: '废电池', points: 10 },
  { id: 'other', icon: '📦', name: '其他回收物', points: 3 },
]

const canSubmit = computed(() => {
  if (!photoPath.value) return false
  if (isRecycle.value && !selectedCategory.value) return false
  if (dailyLimit.value > 0 && dailyRemaining.value <= 0) return false
  return true
})

onLoad((options) => {
  if (!options) return
  actionId.value = options.id || ''
  name.value = decodeURIComponent(options.name || '')
  icon.value = decodeURIComponent(options.icon || '')
  points.value = parseInt(options.points) || 0
  dailyLimit.value = actionId.value === 'cleanPlate' ? 3 : isRecycle.value ? 5 : 99
  dailyRemaining.value = dailyLimit.value
})

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera', 'album'],
    success: (res) => { photoPath.value = res.tempFilePaths[0] },
  })
}

async function handleSubmit() {
  if (!canSubmit.value) return

  submitting.value = true
  uni.showLoading({ title: 'AI 审核中...' })

  try {
    const res = await actionsApi.checkin({
      actionId: actionId.value,
      category: selectedCategory.value,
      photo: photoPath.value,
      notes: notes.value,
      date: new Date().toISOString().slice(0, 16).replace('T', ' '),
    })

    uni.hideLoading()

    if (res.code === 200) {
      result.value = {
        status: res.data.status || 'passed',
        title: res.data.status === 'pending' ? '待人工审核' : '打卡成功！',
        desc: res.data.status === 'pending'
          ? '你的打卡记录已提交，等待管理员审核'
          : `获得 ${res.data.points || points.value} 碳积分`,
        aiResult: res.data.aiResult || { confidence: 0.85 },
        txHash: res.data.txHash || '',
      }
    } else {
      result.value = {
        status: 'rejected',
        title: '提交失败',
        desc: res.message || '请稍后重试',
      }
    }
  } catch (e) {
    uni.hideLoading()
    result.value = { status: 'rejected', title: '网络错误', desc: '请检查网络连接' }
  }

  submitting.value = false
  showResult.value = true
}

function closeResult() {
  showResult.value = false
  if (result.value.status === 'passed' || result.value.status === 'pending') {
    setTimeout(() => uni.navigateBack(), 300)
  }
}

function shortTx(hash) {
  if (!hash || hash.length < 12) return hash
  return hash.slice(0, 8) + '...' + hash.slice(-6)
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; padding: $space-md; padding-bottom: calc($safe-bottom + 60rpx); }

.header { padding: $space-xl; text-align: center; margin-bottom: $space-md; }
.header-icon { font-size: 64rpx; display: block; margin-bottom: $space-sm; }
.header-name { font-size: $font-xl; font-weight: 700; color: $text-primary; display: block; }
.points-badge { display: inline-block; background: #FFF8E1; padding: 6rpx 24rpx; border-radius: $radius-round; font-size: $font-sm; color: $accent-warm; font-weight: 600; margin-top: $space-sm; }
.daily-hint { display: block; margin-top: $space-sm; font-size: $font-sm; color: $text-secondary; }
.hint-num { color: $primary; font-weight: 700; }

.section { padding: $space-lg; margin-bottom: $space-md; }
.section-title { font-size: $font-md; font-weight: 600; color: $text-primary; margin-bottom: $space-md; display: block; }

.category-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: $space-sm; }
.cat-item { padding: $space-md; text-align: center; border-radius: $radius-md; border: 2rpx solid $border-color; background: #fff; }
.cat-item.selected { border-color: $primary-light; background: #E8F5E9; }
.cat-icon { font-size: 32rpx; display: block; }
.cat-name { font-size: $font-xs; color: $text-primary; display: block; margin: 4rpx 0; }
.cat-points { font-size: 20rpx; color: $accent-warm; font-weight: 600; }

.photo-area { width: 100%; height: 400rpx; background: #F5F5F5; border-radius: $radius-md; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.photo-placeholder { display: flex; flex-direction: column; align-items: center; gap: $space-xs; }
.photo-icon { font-size: 56rpx; }
.photo-hint { font-size: $font-xs; color: $text-light; }
.photo-preview { width: 100%; height: 100%; }

.notes-input { width: 100%; height: 140rpx; background: $bg-color; border-radius: $radius-sm; padding: $space-sm; font-size: $font-sm; box-sizing: border-box; }

.submit-btn {
  width: 100%; height: 96rpx; background: $bg-gradient; border-radius: $radius-md; color: #fff;
  font-size: $font-lg; font-weight: 600; margin-top: $space-lg; display: flex;
  align-items: center; justify-content: center; border: none;
  &:disabled { opacity: 0.5; }
}

/* Modal */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; align-items: center;
  justify-content: center; z-index: 999; padding: $space-xl;
}
.modal-card {
  background: #fff; border-radius: $radius-lg; padding: $space-xl;
  width: 100%; max-width: 560rpx; text-align: center;
}
.modal-icon { font-size: 72rpx; display: block; margin-bottom: $space-sm; }
.modal-title { font-size: $font-xl; font-weight: 700; color: $text-primary; display: block; }
.modal-desc { font-size: $font-sm; color: $text-secondary; margin: $space-sm 0; display: block; }
.modal-detail { background: $bg-color; border-radius: $radius-sm; padding: $space-md; margin: $space-md 0; }
.md-row { display: flex; justify-content: space-between; padding: 6rpx 0; }
.md-label { font-size: $font-xs; color: $text-light; }
.md-value { font-size: $font-sm; color: $text-primary; font-weight: 600;
  &.low { color: $warning; }
  &.tx { font-family: monospace; font-size: $font-xs; }
}
.modal-hint { background: #FFF8E1; padding: $space-sm $space-md; border-radius: $radius-sm; margin: $space-sm 0; font-size: $font-xs; color: #F57F17; }
.modal-btn { width: 100%; height: 80rpx; background: $bg-gradient; color: #fff; border-radius: $radius-md; border: none; font-size: $font-md; margin-top: $space-md; }
</style>
