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
      <canvas
        canvas-id="carbonChart"
        id="carbonChart"
        class="chart-canvas"
        @tap="onCanvasTap"
      ></canvas>
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
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick, getCurrentInstance } from 'vue'

const instance = getCurrentInstance()

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

// ===================== Canvas 图表绘制 =====================

let drawTimer = null

/** 格式化日期标签：05-27 → 5/27 */
function formatLabel(dateStr) {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    return parseInt(parts[1]) + '/' + parseInt(parts[2])
  }
  return dateStr
}

function drawChart() {
  const ctx = uni.createCanvasContext('carbonChart', instance?.proxy)
  if (!ctx) return

  const sysInfo = uni.getSystemInfoSync()
  const dpr = sysInfo.pixelRatio || 2
  const W = 320
  const H = 180

  // 高 DPI 适配：放大 canvas 缓冲区，缩放绘制上下文
  ctx.scale(dpr, dpr)
  ctx.clearRect(0, 0, W, H)

  // 图表绘制区域（逻辑像素）
  const pad = { top: 22, right: 14, bottom: 32, left: 42 }
  const chartLeft = pad.left
  const chartRight = W - pad.right
  const chartTop = pad.top
  const chartBottom = H - pad.bottom

  const data = props.trendData
  if (!data || data.length === 0) {
    ctx.setFontSize(13)
    ctx.setFillStyle('#9E9E9E')
    ctx.setTextAlign('center')
    ctx.fillText('暂无趋势数据', W / 2, H / 2)
    // #ifdef MP-WEIXIN
    ctx.draw()
    // #endif
    return
  }

  const dates = data.map((d) => d.date)
  const values = data.map((d) => d.carbon)

  // 计算 Y 轴范围
  let minVal = Math.min(...values)
  let maxVal = Math.max(...values)
  if (maxVal - minVal < 0.5) {
    minVal = Math.max(0, minVal - 1)
    maxVal = maxVal + 1
  }
  const range = maxVal - minVal
  const yMin = Math.max(0, minVal - range * 0.15)
  const yMax = maxVal + range * 0.15

  // 数据点 → 画布坐标
  const toX = (i) =>
    data.length === 1 ? (chartLeft + chartRight) / 2 : chartLeft + (i / (data.length - 1)) * (chartRight - chartLeft)
  const toY = (v) => chartBottom - ((v - yMin) / (yMax - yMin)) * (chartBottom - chartTop)

  // ---- 1. 水平网格线 + Y 轴刻度 ----
  const gridCount = 4
  for (let i = 0; i <= gridCount; i++) {
    const ratio = i / gridCount
    const y = chartBottom - ratio * (chartBottom - chartTop)
    const val = yMin + ratio * (yMax - yMin)

    ctx.beginPath()
    ctx.setStrokeStyle(i === 0 ? '#C8E6C9' : '#F1F8E9')
    ctx.setLineWidth(i === 0 ? 1 : 0.6)
    ctx.moveTo(chartLeft, y)
    ctx.lineTo(chartRight, y)
    ctx.stroke()

    ctx.setFontSize(10)
    ctx.setFillStyle('#9E9E9E')
    ctx.setTextAlign('right')
    ctx.fillText(val.toFixed(1), chartLeft - 5, y + 4)
  }

  // Y 轴单位
  ctx.setFontSize(9)
  ctx.setFillStyle('#BDBDBD')
  ctx.setTextAlign('left')
  ctx.fillText('kg CO\u2082', 2, chartTop - 6)

  // ---- 2. X 轴日期标签 ----
  const labelStep = data.length <= 7 ? 1 : Math.ceil(data.length / 7)
  for (let i = 0; i < data.length; i += labelStep) {
    const x = toX(i)
    ctx.setFontSize(10)
    ctx.setFillStyle('#9E9E9E')
    ctx.setTextAlign('center')
    ctx.fillText(formatLabel(dates[i]), x, chartBottom + 16)
  }
  // 确保最后一个标签显示
  if ((data.length - 1) % labelStep !== 0) {
    const x = toX(data.length - 1)
    ctx.setFontSize(10)
    ctx.setFillStyle('#9E9E9E')
    ctx.setTextAlign('center')
    ctx.fillText(formatLabel(dates[dates.length - 1]), x, chartBottom + 16)
  }

  // ---- 3. 构建路径点 ----
  const points = values.map((v, i) => ({ x: toX(i), y: toY(v) }))

  // 辅助：绘制平滑曲线（Catmull-Rom → Bezier）
  function traceSmoothPath(pts) {
    ctx.moveTo(pts[0].x, pts[0].y)
    if (pts.length === 1) return
    if (pts.length === 2) {
      ctx.lineTo(pts[1].x, pts[1].y)
      return
    }
    for (let i = 0; i < pts.length - 1; i++) {
      const p0 = pts[Math.max(0, i - 1)]
      const p1 = pts[i]
      const p2 = pts[i + 1]
      const p3 = pts[Math.min(pts.length - 1, i + 2)]
      const tension = 0.3
      const cp1x = p1.x + (p2.x - p0.x) * tension
      const cp1y = p1.y + (p2.y - p0.y) * tension
      const cp2x = p2.x - (p3.x - p1.x) * tension
      const cp2y = p2.y - (p3.y - p1.y) * tension
      ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }
  }

  // ---- 4. 渐变填充区域 ----
  const gradient = ctx.createLinearGradient(0, chartTop, 0, chartBottom)
  gradient.addColorStop(0, 'rgba(76, 175, 80, 0.32)')
  gradient.addColorStop(1, 'rgba(76, 175, 80, 0.02)')

  ctx.beginPath()
  traceSmoothPath(points)
  ctx.lineTo(points[points.length - 1].x, chartBottom)
  ctx.lineTo(points[0].x, chartBottom)
  ctx.closePath()
  ctx.setFillStyle(gradient)
  ctx.fill()

  // ---- 5. 折线 ----
  ctx.beginPath()
  traceSmoothPath(points)
  ctx.setStrokeStyle('#4CAF50')
  ctx.setLineWidth(2.5)
  ctx.setLineCap('round')
  ctx.setLineJoin('round')
  ctx.stroke()

  // ---- 6. 数据点 ----
  for (const pt of points) {
    // 白色外圈
    ctx.beginPath()
    ctx.arc(pt.x, pt.y, 5, 0, Math.PI * 2)
    ctx.setFillStyle('#ffffff')
    ctx.fill()
    // 绿色内圈
    ctx.beginPath()
    ctx.arc(pt.x, pt.y, 3.5, 0, Math.PI * 2)
    ctx.setFillStyle('#2E7D32')
    ctx.fill()
  }

  // 微信小程序必须调用 draw() 才会真正渲染
  // #ifdef MP-WEIXIN
  ctx.draw()
  // #endif
  // H5 环境也需要调用 draw 来提交绘制
  // #ifdef H5
  ctx.draw()
  // #endif
}

function onCanvasTap() {
  // 预留：点击图表可扩展 tooltip 等功能
}

// 数据变化时重绘
watch(
  () => props.trendData,
  () => {
    clearTimeout(drawTimer)
    drawTimer = setTimeout(() => {
      nextTick(() => drawChart())
    }, 80)
  },
  { deep: true }
)

onMounted(() => {
  // 延迟等待 canvas 原生组件就绪
  setTimeout(() => {
    drawChart()
  }, 200)
})

onBeforeUnmount(() => {
  clearTimeout(drawTimer)
})
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

.chart-body {
  width: 100%;
}

.chart-canvas {
  width: 100%;
  height: 360rpx;
}

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
