<template>
  <view class="page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view v-for="u in users" :key="u.id" class="user-card card">
        <view class="user-left">
          <view class="user-avatar" :class="{ frozen: u.status === 'frozen' }">{{ u.name.charAt(0) }}</view>
          <view class="user-info">
            <text class="user-name">{{ u.name }}</text>
            <text class="user-sid">{{ u.studentId }}</text>
            <text class="user-class">{{ u.className }}</text>
          </view>
        </view>
        <view class="user-right">
          <text class="user-points">{{ u.points }}分</text>
          <text class="user-carbon">{{ u.carbonReduced }}kg</text>
          <view class="user-status" :class="u.status">{{ statusLabels[u.status] }}</view>
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

const users = ref([])
const statusLabels = { active: '正常', frozen: '冻结', inactive: '非活跃' }

onShow(() => loadData())

async function loadData() {
  const res = await request({ url: '/admin/users', method: 'GET' })
  if (res.code === 200) users.value = res.data.users
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; }
.page-scroll { height: 100vh; padding: $space-md; }

.user-card { padding: $space-md $space-lg; margin-bottom: 6rpx; display: flex; justify-content: space-between; align-items: center; }
.user-left { display: flex; align-items: center; gap: $space-sm; flex: 1; }
.user-avatar {
  width: 72rpx; height: 72rpx; border-radius: 50%; background: $bg-gradient;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: $font-md; font-weight: 600;
  &.frozen { background: #BDBDBD; }
}
.user-name { font-size: $font-md; font-weight: 500; color: $text-primary; display: block; }
.user-sid { font-size: $font-xs; color: $text-light; }
.user-class { font-size: 20rpx; color: $text-light; }
.user-right { text-align: right; }
.user-points { font-size: $font-md; font-weight: 700; color: $accent-warm; display: block; }
.user-carbon { font-size: $font-xs; color: $text-secondary; }
.user-status {
  font-size: 20rpx; padding: 2rpx 12rpx; border-radius: $radius-round; display: inline-block; margin-top: 4rpx;
  &.active { background: #E8F5E9; color: $success; }
  &.frozen { background: #FFEBEE; color: $danger; }
  &.inactive { background: #F5F5F5; color: $text-light; }
}

.safe-bottom { height: calc($safe-bottom + 40rpx); }
</style>
