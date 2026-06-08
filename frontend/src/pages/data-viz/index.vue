<template>
  <view class="page">
    <scroll-view class="viz-scroll" scroll-y :show-scrollbar="false">
      <!-- ===== Carbon Breakdown Pie Chart ===== -->
      <view class="chart-card card">
        <view class="chart-header">
          <text class="chart-title">碳足迹构成分析</text>
          <text class="chart-subtitle">本月</text>
        </view>
        <!-- #ifdef H5 -->
        <view class="chart-body">
          <view ref="pieDom" class="chart-dom"></view>
        </view>
        <!-- #endif -->
        <!-- #ifdef MP-WEIXIN -->
        <view class="chart-body">
          <ec-canvas canvas-id="pieCanvas" :chart-option="pieOption" class="chart-dom" />
        </view>
        <!-- #endif -->
        <view class="chart-legend">
          <view v-for="item in breakdown" :key="item.name" class="legend-item">
            <view class="legend-dot" :style="{ backgroundColor: item.color }"></view>
            <text class="legend-name">{{ item.name }}</text>
            <text class="legend-value">{{ item.value }}%</text>
          </view>
        </view>
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { request } from '@/api/request'

// #ifdef H5
import * as echarts from 'echarts'
// #endif

const breakdown = ref([])

// ===================== H5 DOM Refs =====================
const pieDom = ref(null)

let pieChart = null

// ===================== ECharts Options =====================

function buildPieOption() {
  const data = breakdown.value
  if (!data || data.length === 0) return {}
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}%',
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          formatter: '{c}%',
          color: '#ffffff',
          fontSize: 10,
        },
        emphasis: {
          label: {
            fontSize: 14,
            fontWeight: 'bold',
          },
        },
        data: data.map((d) => ({
          value: d.value,
          name: d.name,
          itemStyle: { color: d.color },
        })),
      },
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '42%',
        style: {
          text: '碳足迹',
          textAlign: 'center',
          fill: '#1B5E20',
          fontSize: 13,
        },
      },
      {
        type: 'text',
        left: 'center',
        top: '52%',
        style: {
          text: '构成',
          textAlign: 'center',
          fill: '#9E9E9E',
          fontSize: 11,
        },
      },
    ],
  }
}

// ===================== MP-WEIXIN Computed Options =====================
const pieOption = computed(() => buildPieOption())

// ===================== H5 Chart Init =====================
// #ifdef H5

function initPieChart() {
  if (!pieDom.value) return
  pieChart?.dispose()
  pieChart = echarts.init(pieDom.value)
  pieChart.setOption(buildPieOption())
}

function handleResize() {
  pieChart?.resize()
}

// #endif

// ===================== Lifecycle =====================

onMounted(async () => {
  const bRes = await request({ url: '/data/carbon-breakdown', method: 'GET' })
  if (bRes.code === 200) breakdown.value = bRes.data

  // #ifdef H5
  await nextTick()
  setTimeout(() => {
    initPieChart()
  }, 100)
  window.addEventListener('resize', handleResize)
  // #endif
})

onBeforeUnmount(() => {
  // #ifdef H5
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  // #endif
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; padding-bottom: calc($safe-bottom + 120rpx); }
.viz-scroll { padding: $space-md; height: 100vh; }
.chart-card { padding: $space-lg; margin-bottom: $space-md; }
.chart-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: $space-md; }
.chart-title { font-size: $font-lg; font-weight: 600; color: $text-primary; }
.chart-subtitle { font-size: $font-xs; color: $text-light; }
.chart-body { width: 100%; }
.chart-dom { width: 100%; height: 400rpx; }
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
