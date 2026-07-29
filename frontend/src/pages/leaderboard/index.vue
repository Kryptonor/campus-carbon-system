<template>
  <view class="page">
    <!-- Tab Switch -->
    <view class="tab-row">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @tap="switchTab(tab.value)"
      >{{ tab.label }}</view>
    </view>

    <!-- ===== Personal Rankings (碳减排/积分) ===== -->
    <template v-if="currentTab !== 'class'">
      <!-- Top 3 -->
      <view class="top3-row" v-if="rankList.length >= 3">
        <view class="top-item">
          <text class="top-medal">🥈</text>
          <view class="top-avatar rank-2">{{ rankList[1].name.charAt(0) }}</view>
          <text class="top-name">{{ rankList[1].name }}</text>
          <text class="top-score">{{ rankList[1].score }}</text>
        </view>
        <view class="top-item top-1">
          <text class="top-medal">👑</text>
          <view class="top-avatar rank-1">{{ rankList[0].name.charAt(0) }}</view>
          <text class="top-name">{{ rankList[0].name }}</text>
          <text class="top-score">{{ rankList[0].score }}</text>
        </view>
        <view class="top-item">
          <text class="top-medal">🥉</text>
          <view class="top-avatar rank-3">{{ rankList[2].name.charAt(0) }}</view>
          <text class="top-name">{{ rankList[2].name }}</text>
          <text class="top-score">{{ rankList[2].score }}</text>
        </view>
      </view>

      <!-- Rank List -->
      <scroll-view class="rank-scroll" scroll-y :show-scrollbar="false">
        <view v-for="item in rankList.slice(3)" :key="item.rank" class="rank-row">
          <text class="rank-num">{{ item.rank }}</text>
          <view class="rank-avatar-sm">{{ item.name.charAt(0) }}</view>
          <view class="rank-info">
            <text class="rank-name">{{ item.name }}</text>
            <text class="rank-dept">{{ item.department }}</text>
          </view>
          <text class="rank-score">{{ item.score }}</text>
        </view>
        <view class="safe-bottom"></view>
      </scroll-view>
    </template>

    <!-- ===== 班级人均榜 ===== -->
    <template v-else>
      <!-- ECharts Bar Chart (H5 only) -->
      <!-- #ifdef H5 -->
      <view class="chart-card card">
        <view class="chart-header">
          <text class="chart-title">Top 10 班级人均碳减排</text>
        </view>
        <div ref="classBarDom" class="chart-dom"></div>
      </view>
      <!-- #endif -->
      <!-- #ifdef MP-WEIXIN -->
      <view class="chart-card card">
        <view class="chart-header">
          <text class="chart-title">Top 10 班级人均碳减排</text>
        </view>
        <ec-canvas canvas-id="classBarCanvas" :chart-option="classBarOption" class="chart-dom" />
      </view>
      <!-- #endif -->

      <!-- Class Rank List -->
      <scroll-view class="rank-scroll class-scroll" scroll-y :show-scrollbar="false">
        <view v-for="item in classRankList" :key="item.rank" class="class-row">
          <view class="cls-rank-badge" :class="'top-' + item.rank" v-if="item.rank <= 3">
            <text>{{ ['','🥇','🥈','🥉'][item.rank] }}</text>
          </view>
          <text v-else class="cls-rank-num">{{ item.rank }}</text>
          <view class="cls-info">
            <text class="cls-name">{{ item.className }}</text>
            <text class="cls-dept">{{ item.department }}</text>
          </view>
          <view class="cls-stats">
            <view class="cls-stat-item">
              <text class="cls-stat-val">{{ item.perCapitaCarbon }}</text>
              <text class="cls-stat-label">人均减碳(kg)</text>
            </view>
            <view class="cls-stat-item">
              <text class="cls-stat-val">{{ item.perCapitaPoints }}</text>
              <text class="cls-stat-label">人均积分</text>
            </view>
          </view>
          <text class="cls-count">{{ item.studentCount }}人</text>
        </view>
        <view class="safe-bottom"></view>
      </scroll-view>
    </template>

    <!-- My Rank (fixed bottom, only for personal tabs) -->
    <view class="my-rank" v-if="currentTab !== 'class'">
      <text class="my-num">{{ myRank }}</text>
      <view class="my-avatar">{{ userStore.userName.charAt(0) }}</view>
      <view class="my-info">
        <text class="my-name">{{ userStore.userName }}</text>
        <text class="my-dept">{{ userStore.department }}</text>
      </view>
      <text class="my-score">{{ myScore }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, watch, onBeforeUnmount, getCurrentInstance } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { leaderboardApi } from '@/api/leaderboard'

const userStore = useUserStore()
const tabs = [
  { label: '碳减排排行', value: 'carbon' },
  { label: '积分排行', value: 'points' },
  { label: '班级人均榜', value: 'class' },
]
const currentTab = ref('carbon')
const rankList = ref([])
const classRankList = ref([])
const myRank = ref(0)
const myScore = ref(0)

// #ifdef H5
import * as echarts from 'echarts'
const classBarDom = ref(null)
let classBarChart = null
// #endif

// #ifdef MP-WEIXIN
import { computed } from 'vue'

const classBarOption = computed(() => {
  const names = classRankList.value.slice(0, 10).map((c) => c.className)
  const points = classRankList.value.slice(0, 10).map((c) => c.perCapitaPoints)
  return {
    tooltip: { trigger: 'axis', formatter: '{b}\n人均积分: {c}' },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '2%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { color: '#666', fontSize: 8, rotate: 30 }, axisTick: { show: false } },
    yAxis: { type: 'value', name: '人均积分', nameTextStyle: { color: '#999', fontSize: 10 }, axisLabel: { color: '#999', fontSize: 10 }, splitLine: { lineStyle: { color: '#F1F8E9' } } },
    series: [{ type: 'bar', data: points.map((v, i) => ({ value: v, itemStyle: { color: i < 3 ? '#FFB300' : '#66BB6A', borderRadius: [6, 6, 0, 0] } })), barWidth: '50%' }],
  }
})
// #endif

onShow(() => loadData())

async function loadData() {
  const res = await leaderboardApi.getRankList(currentTab.value)
  if (res.code === 200) {
    if (currentTab.value === 'class') {
      classRankList.value = res.data.ranks || []
      // #ifdef H5
      nextTick(() => initClassBarChart())
      // #endif
    } else {
      rankList.value = res.data.ranks || []
      myRank.value = res.data.myRank || 0
      myScore.value = res.data.myScore || 0
    }
  }
}

function switchTab(tab) {
  currentTab.value = tab
  loadData()
}

// #ifdef H5
watch(classRankList, (list) => {
  if (currentTab.value === 'class' && list.length > 0) {
    nextTick(() => initClassBarChart())
  }
})

function initClassBarChart() {
  if (!classBarDom.value) return
  classBarChart?.dispose()
  classBarChart = echarts.init(classBarDom.value)
  classBarChart.setOption(buildClassBarOptionH5())
}

function buildClassBarOptionH5() {
  const names = classRankList.value.slice(0, 10).map((c) => c.className)
  const points = classRankList.value.slice(0, 10).map((c) => c.perCapitaPoints)
  return {
    tooltip: { trigger: 'axis', formatter: '{b}<br/>人均积分: {c}' },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '2%', containLabel: true },
    xAxis: {
      type: 'category', data: names,
      axisLabel: { color: '#666', fontSize: 10, rotate: 30 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value', name: '人均积分',
      nameTextStyle: { color: '#999', fontSize: 10 },
      axisLabel: { color: '#999', fontSize: 10 },
      splitLine: { lineStyle: { color: '#F1F8E9' } },
    },
    series: [{
      type: 'bar',
      data: points.map((v, i) => ({
        value: v,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: i < 3 ? '#FFB300' : '#66BB6A' },
            { offset: 1, color: i < 3 ? '#FF9800' : '#2E7D32' },
          ]),
          borderRadius: [6, 6, 0, 0],
        },
      })),
      barWidth: '50%',
    }],
  }
}

onBeforeUnmount(() => {
  classBarChart?.dispose()
})
// #endif

</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; padding-bottom: calc($safe-bottom + 200rpx); }

.tab-row { display: flex; padding: $space-md $space-xl; gap: $space-sm; }
.tab-item {
  flex: 1; height: 64rpx; text-align: center; line-height: 64rpx;
  background: #fff; border-radius: $radius-round; font-size: $font-sm; color: $text-secondary;
  &.active { background: $primary-light; color: #fff; font-weight: 600; }
}

.top3-row {
  display: flex; justify-content: center; align-items: flex-end;
  padding: $space-lg $space-md; gap: $space-sm;
}
.top-item { display: flex; flex-direction: column; align-items: center; gap: 6rpx; flex: 1; }
.top-1 { margin-bottom: 20rpx; }
.top-medal { font-size: 36rpx; }
.top-avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: $font-lg; font-weight: 700; color: #fff;
  &.rank-1 { background: linear-gradient(135deg, #FFB300, #FFA000); width: 96rpx; height: 96rpx; }
  &.rank-2 { background: linear-gradient(135deg, #90A4AE, #78909C); }
  &.rank-3 { background: linear-gradient(135deg, #FF8A65, #FF7043); }
}
.top-name { font-size: $font-sm; color: $text-primary; font-weight: 600; }
.top-score { font-size: $font-xs; color: $accent-warm; font-weight: 700; }

.rank-scroll { padding: 0 $space-md; height: calc(100vh - 560rpx); }
.class-scroll { padding: 0 $space-md; height: calc(100vh - 740rpx); }

.rank-row {
  display: flex; align-items: center; background: #fff; padding: $space-md $space-lg;
  border-radius: $radius-sm; margin-bottom: 6rpx; gap: $space-sm;
}
.rank-num { font-size: $font-md; font-weight: 700; color: $text-light; width: 48rpx; text-align: center; }
.rank-avatar-sm {
  width: 64rpx; height: 64rpx; border-radius: 50%; background: #E8F5E9;
  display: flex; align-items: center; justify-content: center;
  font-size: $font-sm; font-weight: 600; color: $primary;
}
.rank-info { flex: 1; }
.rank-name { font-size: $font-sm; color: $text-primary; display: block; font-weight: 500; }
.rank-dept { font-size: $font-xs; color: $text-light; }
.rank-score { font-size: $font-md; font-weight: 700; color: $accent-warm; }

/* Class Rank */
.chart-card { padding: $space-lg; margin: $space-md; }
.chart-header { margin-bottom: $space-md; }
.chart-title { font-size: $font-lg; font-weight: 600; color: $text-primary; }
.chart-dom { width: 100%; height: 420rpx; }
.chart-placeholder {
  margin: $space-md; padding: $space-xl; text-align: center; background: #fff; border-radius: $radius-md;
  color: $text-secondary; font-size: $font-md; display: flex; flex-direction: column; gap: $space-xs;
}
.ph-sub { font-size: $font-xs; color: $text-light; }

.class-row {
  display: flex; align-items: center; background: #fff; padding: $space-md $space-lg;
  border-radius: $radius-sm; margin-bottom: 6rpx; gap: $space-sm;
}
.cls-rank-badge { font-size: 32rpx; width: 48rpx; text-align: center; }
.cls-rank-num { font-size: $font-md; font-weight: 700; color: $text-light; width: 48rpx; text-align: center; }
.cls-info { flex: 1; }
.cls-name { font-size: $font-sm; color: $text-primary; display: block; font-weight: 500; }
.cls-dept { font-size: $font-xs; color: $text-light; }
.cls-stats { display: flex; gap: $space-md; }
.cls-stat-item { text-align: right; }
.cls-stat-val { font-size: $font-sm; color: $accent-warm; font-weight: 700; display: block; }
.cls-stat-label { font-size: 18rpx; color: $text-light; }
.cls-count { font-size: $font-xs; color: $text-secondary; min-width: 60rpx; text-align: right; }

.my-rank {
  position: fixed; bottom: 100rpx; left: 0; right: 0;
  display: flex; align-items: center; background: $bg-gradient;
  padding: $space-md $space-lg;
  padding-bottom: calc($safe-bottom + $space-md);
  gap: $space-sm; z-index: 50;
}
.my-num { font-size: $font-lg; font-weight: 700; color: rgba(255,255,255,0.8); width: 48rpx; text-align: center; }
.my-avatar {
  width: 64rpx; height: 64rpx; border-radius: 50%;
  background: rgba(255,255,255,0.25); display: flex; align-items: center;
  justify-content: center; font-weight: 700; color: #fff;
}
.my-info { flex: 1; }
.my-name { color: #fff; font-size: $font-md; display: block; font-weight: 600; }
.my-dept { color: rgba(255,255,255,0.7); font-size: $font-xs; }
.my-score { color: $accent-warm; font-size: $font-xl; font-weight: 700; }

.safe-bottom { height: 30rpx; }
</style>
