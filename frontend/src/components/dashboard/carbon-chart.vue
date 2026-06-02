<template>
  <view class="chart-section card">
    <view class="section-header">
      <text class="section-title">碳足迹趋势</text>
      <view class="period-switch">
        <text
          v-for="p in periods"
          :key="p.value"
          class="period-item"
          :class="{ active: currentPeriod === p.value }"
          @tap="switchPeriod(p.value)"
        >{{ p.label }}</text>
      </view>
    </view>

    <view class="chart-body">
      <!-- #ifdef H5 -->
      <view ref="chartDom" class="chart-dom"></view>
      <!-- #endif -->
      <!-- #ifdef MP-WEIXIN -->
      <view class="chart-placeholder">
        <text class="placeholder-text">📊 碳足迹趋势图</text>
        <text class="placeholder-desc">微信小程序环境下 ECharts 需要额外配置 ec-canvas 组件</text>
      </view>
      <!-- #endif -->
    </view>

    <view class="chart-summary">
      <view class="summary-item">
        <text class="summary-value">{{ weeklyTotal.toFixed(1) }}</text>
        <text class="summary-label">本周累计(kg)</text>
      </view>
      <view class="summary-item">
        <text class="summary-value" :class="trendClass">{{ weeklyChange }}%</text>
        <text class="summary-label">较上周</text>
      </view>
      <view class="summary-item">
        <text class="summary-value">{{ dailyAvg }}</text>
        <text class="summary-label">日均(kg)</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'

const props = defineProps({
  trendData: {
    type: Array,
    default: () => [],
  },
  weeklyTotal: { type: Number, default: 0 },
  weeklyChange: { type: Number, default: 0 },
})

const emit = defineEmits(['periodChange'])

const periods = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本学期', value: 'term' },
]
const currentPeriod = ref('week')

// #ifdef H5
const chartDom = ref(null)
let chartInstance = null
let echartsModule = null
// #endif

function switchPeriod(period) {
  currentPeriod.value = period
  emit('periodChange', period)
}

const dailyAvg = computed(() => {
  if (!props.trendData.length) return '0'
  const sum = props.trendData.reduce((acc, d) => acc + d.carbon, 0)
  return (sum / props.trendData.length).toFixed(1)
})

const trendClass = computed(() => {
  if (props.weeklyChange < 0) return 'trend-down'
  if (props.weeklyChange > 0) return 'trend-up'
  return ''
})

// #ifdef H5
function initChart() {
  if (!chartDom.value) return
  import('echarts').then((mod) => {
    echartsModule = mod
    chartInstance = mod.init(chartDom.value)
    setOption()
  })
}

function setOption() {
  if (!chartInstance) return
  chartInstance.setOption({
    grid: {
      left: '5%',
      right: '5%',
      bottom: '8%',
      top: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.trendData.map((d) => d.date),
      axisLine: { lineStyle: { color: '#C8E6C9' } },
      axisTick: { show: false },
      axisLabel: { color: '#9E9E9E', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      name: 'kg CO₂',
      nameTextStyle: { color: '#9E9E9E', fontSize: 11 },
      axisLabel: { color: '#9E9E9E', fontSize: 11 },
      splitLine: { lineStyle: { color: '#F1F8E9' } },
    },
    series: [
      {
        data: props.trendData.map((d) => d.carbon),
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#4CAF50', width: 2.5 },
        itemStyle: {
          color: '#2E7D32',
          borderColor: '#ffffff',
          borderWidth: 2,
        },
        areaStyle: {
          color: new echartsModule.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(76, 175, 80, 0.35)' },
            { offset: 1, color: 'rgba(76, 175, 80, 0.02)' },
          ]),
        },
      },
    ],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#ffffff',
      borderColor: '#C8E6C9',
      textStyle: { color: '#1B5E20', fontSize: 12 },
      formatter: (params) => {
        if (!params || !params[0]) return ''
        return `${params[0].name}<br/>碳排放: ${params[0].value} kg CO₂`
      },
    },
  })
}

watch(() => props.trendData, () => {
  nextTick(() => {
    setOption()
  })
}, { deep: true })

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
// #endif
</script>

<style lang="scss" scoped>
.chart-section {
  padding: $space-lg;
  margin-top: $space-md;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-md;
}

.section-title {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
}

.period-switch {
  display: flex;
  background: $bg-color;
  border-radius: $radius-round;
  padding: 4rpx;
}

.period-item {
  font-size: $font-xs;
  color: $text-secondary;
  padding: 6rpx 18rpx;
  border-radius: $radius-round;

  &.active {
    background: $primary-light;
    color: $text-white;
  }
}

/* #ifdef H5 */
.chart-body {
  width: 100%;
}

.chart-dom {
  width: 100%;
  height: 360rpx;
}
/* #endif */

/* #ifdef MP-WEIXIN */
.chart-placeholder {
  width: 100%;
  height: 360rpx;
  background: $bg-color;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $space-xs;
}

.placeholder-text {
  font-size: $font-xl;
  color: $text-secondary;
}

.placeholder-desc {
  font-size: $font-xs;
  color: $text-light;
  text-align: center;
}
/* #endif */

.chart-summary {
  display: flex;
  justify-content: space-around;
  margin-top: $space-md;
  padding-top: $space-md;
  border-top: 1rpx solid $border-color;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.summary-value {
  font-size: $font-xl;
  font-weight: 700;
  color: $text-primary;

  &.trend-down { color: $success; }
  &.trend-up { color: $danger; }
}

.summary-label {
  font-size: $font-xs;
  color: $text-light;
}
</style>
