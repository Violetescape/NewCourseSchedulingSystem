<template>
  <div class="analysis-page">
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>排课效能分析</h2>
          <p>基于全量课表数据的教室效能、班级健康度与资源热力分布。</p>
        </div>
      </div>
    </section>

    <section class="chart-grid">
      <div class="chart-card">
        <div class="chart-card-header">
          <div>
            <h3>教室效能波士顿矩阵</h3>
            <p class="chart-subtitle">X：时间利用率(%)，Y：空间满载率(%)，50% 为基准线</p>
          </div>
        </div>
        <div ref="scatterChartDom" class="chart-box chart-box--scatter" />
      </div>

      <div class="chart-card">
        <div class="chart-card-header">
          <div>
            <h3>班级课表健康度时序图</h3>
            <p class="chart-subtitle">第1周到第20周，多班级平滑趋势线</p>
          </div>
        </div>
        <div ref="lineChartDom" class="chart-box chart-box--line" />
      </div>

      <div class="chart-card chart-card--full">
        <div class="chart-card-header">
          <div>
            <h3>全校排课热力图</h3>
            <p class="chart-subtitle">横轴：周一到周五；纵轴：第1到第12节；可切换教学周查看绝对占用量</p>
          </div>
          <el-select
            v-model="selectedWeek"
            class="week-selector"
            size="small"
            style="width: 132px"
            @change="handleWeekChange"
          >
            <el-option
              v-for="w in weekOptions"
              :key="w"
              :label="`第 ${w} 周`"
              :value="w"
            />
          </el-select>
        </div>
        <div ref="heatmapChartDom" class="chart-box chart-box--heatmap" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { getClassHealth, getClassroomEfficiency, getHeatmapData } from '@/api/analysis'

const scatterChartDom = ref(null)
const lineChartDom = ref(null)
const heatmapChartDom = ref(null)

let scatterChart = null
let lineChart = null
let heatmapChart = null
let resizeHandler = null

const TEXT_COLOR = '#111827'
const SUB_TEXT_COLOR = '#94a3b8'
const AXIS_LINE_COLOR = 'rgba(148, 163, 184, 0.35)'
const GRID_LINE_COLOR = 'rgba(148, 163, 184, 0.15)'

const days = ['周一', '周二', '周三', '周四', '周五']
const sections = Array.from({ length: 12 }, (_, i) => `第 ${i + 1} 节`)
const weekOptions = Array.from({ length: 20 }, (_, i) => i + 1)
const selectedWeek = ref(1)
const heatmapDataMap = ref({})
const heatmapVisualMax = ref(10)

function applyBaseStyle(option) {
  option = option || {}
  option.backgroundColor = 'transparent'
  option.textStyle = option.textStyle || { color: TEXT_COLOR }
  option.tooltip = option.tooltip || {}
  option.tooltip.textStyle = { color: TEXT_COLOR }
  return option
}

function getScatterOption(data) {
  return applyBaseStyle({
    animationDuration: 1000,
    grid: { left: 58, right: 20, top: 26, bottom: 45 },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const [x, y, name] = params.value
        return `${name}<br/>时间利用率：${x.toFixed(2)}%<br/>空间满载率：${y.toFixed(2)}%`
      }
    },
    xAxis: {
      type: 'value',
      name: '时间利用率(%)',
      min: 0,
      max: 100,
      nameTextStyle: { color: SUB_TEXT_COLOR },
      axisLabel: { color: SUB_TEXT_COLOR, formatter: '{value}%' },
      axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
      splitLine: { lineStyle: { color: GRID_LINE_COLOR } }
    },
    yAxis: {
      type: 'value',
      name: '空间满载率(%)',
      nameTextStyle: { color: SUB_TEXT_COLOR },
      min: 0,
      max: 100,
      axisLabel: { color: SUB_TEXT_COLOR, formatter: '{value}%' },
      axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
      splitLine: { lineStyle: { color: GRID_LINE_COLOR } }
    },
    markLine: {
      silent: true,
      lineStyle: { color: 'rgba(248, 113, 113, 0.85)', type: 'dashed', width: 1.5 },
      data: [{ xAxis: 50 }, { yAxis: 50 }]
    },
    series: [
      {
        name: '教室效能',
        type: 'scatter',
        symbolSize: 14,
        data: data.map((item) => [item.timeUtilization, item.spaceLoadRate, item.classroomName]),
        itemStyle: {
          color: 'rgba(96, 165, 250, 0.92)',
          borderColor: 'rgba(191, 219, 254, 0.9)',
          borderWidth: 1
        },
        emphasis: { scale: true }
      }
    ]
  })
}

function getLineOption(data) {
  const xWeeks = Array.from({ length: 20 }, (_, i) => `第${i + 1}周`)
  const series = data.map((item) => ({
    name: item.className || `班级${item.classId}`,
    type: 'line',
    smooth: true,
    showSymbol: false,
    data: item.weeklyScores ?? []
  }))
  return applyBaseStyle({
    animationDuration: 1200,
    legend: {
      type: 'scroll',
      top: 0,
      textStyle: { color: SUB_TEXT_COLOR }
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: { left: 50, right: 25, top: 45, bottom: 30, containLabel: true },
    xAxis: {
      type: 'category',
      data: xWeeks,
      axisLabel: { color: SUB_TEXT_COLOR },
      axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { color: SUB_TEXT_COLOR },
      axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
      splitLine: { lineStyle: { color: GRID_LINE_COLOR } }
    },
    series
  })
}

function getHeatmapOption(data, visualMax) {
  return applyBaseStyle({
    animationDuration: 1200,
    grid: { top: 50, left: 90, right: 26, bottom: 28 },
    tooltip: {
      position: 'top',
      formatter: (params) => {
        const value = params.value || []
        const x = value[0]
        const y = value[1]
        const v = value[2]
        const day = days[x] || '-'
        const section = sections[y] || '-'
        return `${day}<br/>${section}<br/><b>${v}</b> 间教室占用`
      }
    },
    visualMap: {
      min: 0,
      max: Math.max(1, visualMax || 1),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      textStyle: { color: SUB_TEXT_COLOR },
      inRange: {
        color: [
          'rgba(191, 219, 254, 0.25)',
          'rgba(96, 165, 250, 0.45)',
          'rgba(59, 130, 246, 0.72)',
          'rgba(220, 38, 38, 0.92)'
        ]
      },
      outOfRange: { color: 'rgba(148, 163, 184, 0.08)' }
    },
    xAxis: {
      type: 'category',
      data: days,
      splitArea: { show: false },
      axisLabel: { color: SUB_TEXT_COLOR },
      axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'category',
      data: sections,
      axisLabel: { color: SUB_TEXT_COLOR },
      axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
      axisTick: { show: false }
    },
    series: [
      {
        type: 'heatmap',
        data,
        label: { show: false },
        emphasis: {
          focus: 'self',
          itemStyle: {
            borderColor: 'rgba(255, 255, 255, 0.95)',
            borderWidth: 2
          }
        },
        blur: { itemStyle: { opacity: 1 } },
        itemStyle: {
          borderColor: 'rgba(148, 163, 184, 0.18)',
          borderWidth: 1
        },
        animationDuration: 1200
      }
    ]
  })
}

function normalizeResp(resp) {
  return resp?.data?.data ?? []
}

function normalizeHeatmapDataMap(rawData) {
  // 新接口：{ "1": [...], "2": [...] ... }
  if (rawData && !Array.isArray(rawData) && typeof rawData === 'object') {
    return rawData
  }
  // 旧接口：[{ weekday, section, occupiedCount }, ...]
  if (Array.isArray(rawData)) {
    const legacyWeekList = rawData
    const map = {}
    for (const w of weekOptions) {
      map[w] = legacyWeekList
    }
    return map
  }
  return {}
}

function getWeekHeatmapData(week) {
  const weekNodesRaw = heatmapDataMap.value?.[week]
  const weekNodes = Array.isArray(weekNodesRaw) ? weekNodesRaw : []
  return weekNodes.map((n) => [n.weekday, n.section, n.occupiedCount])
}

function renderHeatmapByWeek(week) {
  if (!heatmapChart) return
  const heatmapData = getWeekHeatmapData(week)
  heatmapChart.setOption({
    series: [{ data: heatmapData }]
  })
}

function handleWeekChange(week) {
  renderHeatmapByWeek(week)
}

async function loadAnalysisData() {
  const [effRes, healthRes, heatRes] = await Promise.all([
    getClassroomEfficiency(),
    getClassHealth(),
    getHeatmapData()
  ])

  const efficiencyData = normalizeResp(effRes)
  const healthData = normalizeResp(healthRes)
  heatmapDataMap.value = normalizeHeatmapDataMap(normalizeResp(heatRes))
  heatmapVisualMax.value = Math.max(1, efficiencyData.length || 10)
  const initialHeatmapData = getWeekHeatmapData(selectedWeek.value)

  scatterChart?.setOption(getScatterOption(efficiencyData), true)
  lineChart?.setOption(getLineOption(healthData), true)
  heatmapChart?.setOption(getHeatmapOption(initialHeatmapData, heatmapVisualMax.value), true)
}

onMounted(async () => {
  await nextTick()
  if (!scatterChartDom.value || !lineChartDom.value || !heatmapChartDom.value) return

  scatterChart = echarts.init(scatterChartDom.value, undefined, { renderer: 'canvas' })
  lineChart = echarts.init(lineChartDom.value, undefined, { renderer: 'canvas' })
  heatmapChart = echarts.init(heatmapChartDom.value, undefined, { renderer: 'canvas' })

  try {
    await loadAnalysisData()
  } catch (error) {
    const empty = []
    scatterChart?.setOption(getScatterOption(empty), true)
    lineChart?.setOption(getLineOption(empty), true)
    heatmapChart?.setOption(getHeatmapOption(days.flatMap((_, x) => sections.map((__, y) => [x, y, 0]))), true)
    // eslint-disable-next-line no-console
    console.error('加载排课分析数据失败:', error)
  }

  resizeHandler = () => {
    scatterChart?.resize()
    lineChart?.resize()
    heatmapChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)
})

onBeforeUnmount(() => {
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
  scatterChart?.dispose()
  lineChart?.dispose()
  heatmapChart?.dispose()
  scatterChart = null
  lineChart = null
  heatmapChart = null
})

watch(selectedWeek, (week) => {
  renderHeatmapByWeek(week)
})
</script>

<style scoped>
.toolbar-header {
  margin-bottom: 0;
}

.title-block h2 {
  margin: 0 0 8px;
}

.title-block p {
  margin: 0;
  color: var(--el-text-color-regular);
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 16px;
}

.chart-card {
  padding: 14px 16px 12px;
}

.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.chart-card h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.chart-subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.week-selector {
  flex-shrink: 0;
}

.chart-box {
  width: 100%;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}

.chart-box--scatter,
.chart-box--line {
  height: 320px;
}

.chart-box--heatmap {
  height: 460px;
}

.chart-grid > .chart-card {
  grid-column: span 6;
}

.chart-grid > .chart-card--full {
  grid-column: span 12;
}

@media (max-width: 1100px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .chart-grid > .chart-card {
    grid-column: span 12;
  }
}
</style>

