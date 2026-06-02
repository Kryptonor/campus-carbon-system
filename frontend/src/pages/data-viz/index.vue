<template>
  <view class="page">
    <scroll-view class="viz-scroll" scroll-y :show-scrollbar="false">
      <!-- Carbon Breakdown Pie Chart -->
      <view class="chart-card card">
        <view class="chart-header">
          <text class="chart-title">碳足迹构成分析</text>
          <text class="chart-subtitle">本月</text>
        </view>
        <!-- #ifdef H5 -->
        <view ref="pieDom" class="chart-dom"></view>
        <!-- #endif -->
        <!-- #ifdef MP-WEIXIN -->
        <ec-canvas canvas-id="pieCanvas" :chart-option="pieOption" class="chart-dom" />
        <!-- #endif -->
        <view class="chart-legend">
          <view v-for="item in breakdown" :key="item.name" class="legend-item">
            <view class="legend-dot" :style="{ backgroundColor: item.color }"></view>
            <text class="legend-name">{{ item.name }}</text>
            <text class="legend-value">{{ item.value }}%</text>
          </view>
        </view>
      </view>

      <!-- Campus Compare Bar Chart -->
      <view class="chart-card card">
        <view class="chart-header">
          <text class="chart-title">校园碳排放对比</text>
          <text class="chart-subtitle">我与全校平均</text>
        </view>
        <!-- #ifdef H5 -->
        <view ref="barDom" class="chart-dom"></view>
        <!-- #endif -->
        <!-- #ifdef MP-WEIXIN -->
        <ec-canvas canvas-id="barCanvas" :chart-option="barOption" class="chart-dom" />
        <!-- #endif -->
      </view>

      <!-- Green Action Radar -->
      <view class="chart-card card">
        <view class="chart-header">
          <text class="chart-title">绿色行为雷达图</text>
          <text class="chart-subtitle">我的低碳表现</text>
        </view>
        <!-- #ifdef H5 -->
        <view ref="radarDom" class="chart-dom"></view>
        <!-- #endif -->
        <!-- #ifdef MP-WEIXIN -->
        <ec-canvas canvas-id="radarCanvas" :chart-option="radarOption" class="chart-dom" />
        <!-- #endif -->
      </view>

      <!-- Tips -->
      <view class="tips-card card">
        <text class="tips-title">💡 碳减排小贴士</text>
        <view class="tips-list">
          <text class="tip-item">🌱 每天步行/骑行上学，年均减少碳排放 200kg</text>
          <text class="tip-item">🥬 每周一天素食，年均减少碳排放 350kg</text>
          <text class="tip-item">♻️ 做好垃圾分类，年均减少碳排放 120kg</text>
          <text class="tip-item">💡 人走灯灭关电器，年均减少碳排放 180kg</text>
        </view>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
// #ifdef H5
import * as echarts from 'echarts'
// #endif
import { request } from '@/api/request'

const breakdown = ref([])
const campusCompare = ref([])
const radarData = ref([])

// #ifdef H5
const pieDom = ref(null)
const barDom = ref(null)
const radarDom = ref(null)
let pieChart = null
let barChart = null
let radarChart = null
// #endif

// #ifdef MP-WEIXIN
import { computed } from 'vue'

const pieOption = computed(() => buildPieOption())
const barOption = computed(() => buildBarOption())
const radarOption = computed(() => buildRadarOption())
// #endif

function buildPieOption() {
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
    series: [{
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
      label: { show: false },
      emphasis: { scale: true, scaleSize: 8 },
      data: breakdown.value.map((d) => ({ name: d.name, value: d.value, itemStyle: { color: d.color } })),
    }],
  }
}

function buildBarOption() {
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['我', '全校平均'], textStyle: { color: '#9E9E9E', fontSize: 11 }, top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: campusCompare.value.map((d) => d.label), axisLabel: { color: '#9E9E9E', fontSize: 11 }, axisTick: { show: false } },
    yAxis: { type: 'value', name: 'kg CO₂', nameTextStyle: { color: '#9E9E9E', fontSize: 11 }, axisLabel: { color: '#9E9E9E', fontSize: 11 }, splitLine: { lineStyle: { color: '#F1F8E9' } } },
    series: [
      { name: '我', type: 'bar', data: campusCompare.value.map((d) => d.me), itemStyle: { color: '#4CAF50', borderRadius: [6, 6, 0, 0] }, barWidth: '35%' },
      { name: '全校平均', type: 'bar', data: campusCompare.value.map((d) => d.avg), itemStyle: { color: '#C8E6C9', borderRadius: [6, 6, 0, 0] }, barWidth: '35%' },
    ],
  }
}

function buildRadarOption() {
  return {
    tooltip: {},
    radar: {
      center: ['50%', '50%'],
      radius: '65%',
      indicator: radarData.value.map((d) => ({ name: d.name, max: d.max })),
      axisName: { color: '#9E9E9E', fontSize: 10 },
      splitArea: { areaStyle: { color: ['#F1F8E9', '#fff', '#F1F8E9', '#fff'] } },
    },
    series: [{
      type: 'radar',
      data: [{ value: radarData.value.map((d) => d.value), name: '我的表现' }],
      areaStyle: { color: 'rgba(76, 175, 80, 0.2)' },
      lineStyle: { color: '#4CAF50', width: 2 },
      itemStyle: { color: '#2E7D32' },
      symbol: 'circle',
      symbolSize: 5,
    }],
  }
}

onMounted(async () => {
  const [bRes, cRes, rRes] = await Promise.all([
    request({ url: '/data/carbon-breakdown', method: 'GET' }),
    request({ url: '/data/campus-compare', method: 'GET' }),
    request({ url: '/data/radar', method: 'GET' }),
  ])
  if (bRes.code === 200) breakdown.value = bRes.data
  if (cRes.code === 200) campusCompare.value = cRes.data
  if (rRes.code === 200) radarData.value = rRes.data

  // #ifdef H5
  nextTick(() => { initH5Charts() })
  // #endif
})

// #ifdef H5
function initH5Charts() {
  if (pieDom.value) { pieChart = echarts.init(pieDom.value); pieChart.setOption(buildPieOption()) }
  if (barDom.value) { barChart = echarts.init(barDom.value); barChart.setOption(buildBarOption()) }
  if (radarDom.value) { radarChart = echarts.init(radarDom.value); radarChart.setOption(buildRadarOption()) }
}

onBeforeUnmount(() => {
  pieChart?.dispose(); barChart?.dispose(); radarChart?.dispose()
})
// #endif

</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; padding-bottom: calc($safe-bottom + 120rpx); }
.viz-scroll { padding: $space-md; height: 100vh; }
.chart-card { padding: $space-lg; margin-bottom: $space-md; }
.chart-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: $space-md; }
.chart-title { font-size: $font-lg; font-weight: 600; color: $text-primary; }
.chart-subtitle { font-size: $font-xs; color: $text-light; }
.chart-dom { width: 100%; height: 380rpx; }
.chart-legend { display: flex; flex-wrap: wrap; gap: $space-sm; margin-top: $space-sm; }
.legend-item { display: flex; align-items: center; gap: 6rpx; }
.legend-dot { width: 16rpx; height: 16rpx; border-radius: 50%; }
.legend-name { font-size: $font-xs; color: $text-secondary; }
.legend-value { font-size: $font-xs; color: $text-primary; font-weight: 600; }
.tips-card { padding: $space-lg; }
.tips-title { font-size: $font-md; font-weight: 600; color: $text-primary; display: block; margin-bottom: $space-sm; }
.tips-list { display: flex; flex-direction: column; gap: $space-xs; }
.tip-item { font-size: $font-sm; color: $text-secondary; line-height: 1.6; }
.safe-bottom { height: 30rpx; }
</style>
