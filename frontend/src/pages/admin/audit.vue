<template>
  <view class="page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view v-if="audits.length === 0" class="empty-tip">
        <text class="empty-icon">✅</text>
        <text>暂无待审核记录</text>
      </view>

      <view v-for="item in audits" :key="item.id" class="audit-card card">
        <view class="audit-header">
          <view class="audit-user">
            <text class="audit-avatar">{{ item.studentName.charAt(0) }}</text>
            <view class="audit-user-info">
              <text class="audit-name">{{ item.studentName }}</text>
              <text class="audit-sid">{{ item.studentId }}</text>
            </view>
          </view>
          <view class="audit-confidence" :class="{ low: item.aiConfidence < 0.70 }">
            <text>AI {{ (item.aiConfidence * 100).toFixed(0) }}%</text>
          </view>
        </view>

        <view class="audit-body">
          <view class="audit-action-info">
            <text class="audit-action-name">{{ item.actionName }}</text>
            <text class="audit-date">{{ item.date }}</text>
          </view>
          <view class="audit-category" v-if="item.category">
            <text class="cat-tag">{{ categoryLabels[item.category] || item.category }}</text>
          </view>
        </view>

        <view class="audit-actions">
          <button class="btn-reject" @tap="handleReview(item, false)">驳回</button>
          <button class="btn-approve" @tap="handleReview(item, true)">通过</button>
        </view>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '@/api/request'

const audits = ref([])
const categoryLabels = {
  bottle: '塑料瓶', paper: '废纸', book: '旧书籍', can: '易拉罐',
  battery: '废电池', other: '其他回收物',
}

onShow(() => loadData())

async function loadData() {
  const res = await request({ url: '/admin/audit-list', method: 'GET' })
  if (res.code === 200) audits.value = res.data.audits
}

async function handleReview(item, approved) {
  const res = await request({
    url: '/admin/audit-review',
    method: 'POST',
    data: { id: item.id, approved },
  })
  if (res.code === 200) {
    uni.showToast({ title: approved ? '已通过' : '已驳回', icon: 'success' })
    audits.value = audits.value.filter((a) => a.id !== item.id)
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; }
.page-scroll { height: 100vh; padding: $space-md; }

.audit-card { padding: $space-lg; margin-bottom: $space-sm; }
.audit-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $space-sm; }
.audit-user { display: flex; align-items: center; gap: $space-sm; }
.audit-avatar {
  width: 64rpx; height: 64rpx; border-radius: 50%;
  background: $bg-gradient; color: #fff; display: flex;
  align-items: center; justify-content: center; font-weight: 600; font-size: $font-sm;
}
.audit-name { font-size: $font-md; font-weight: 600; color: $text-primary; display: block; }
.audit-sid { font-size: $font-xs; color: $text-light; }
.audit-confidence {
  background: #E8F5E9; color: $success; padding: 4rpx 16rpx; border-radius: $radius-round;
  font-size: $font-xs; font-weight: 600;
  &.low { background: #FFEBEE; color: $danger; }
}

.audit-body { margin-bottom: $space-md; }
.audit-action-name { font-size: $font-md; color: $text-primary; font-weight: 500; }
.audit-date { font-size: $font-xs; color: $text-light; margin-left: $space-sm; }
.cat-tag { display: inline-block; background: #E1F5FE; color: #29B6F6; padding: 2rpx 16rpx; border-radius: $radius-round; font-size: $font-xs; margin-top: 6rpx; }

.audit-actions { display: flex; gap: $space-sm; }
.btn-reject, .btn-approve {
  flex: 1; height: 72rpx; border-radius: $radius-md; font-size: $font-sm;
  display: flex; align-items: center; justify-content: center; border: none;
}
.btn-reject { background: #FFEBEE; color: $danger; }
.btn-approve { background: $bg-gradient; color: #fff; }

.empty-tip { text-align: center; padding: 120rpx 0; color: $text-secondary; font-size: $font-md; }
.empty-icon { font-size: 72rpx; display: block; margin-bottom: $space-sm; }

.safe-bottom { height: calc($safe-bottom + 40rpx); }
</style>
