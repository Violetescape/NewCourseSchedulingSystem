<template>
  <div class="analysis-page">
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>排课效能分析</h2>
          <p>基于全量课表数据的教室效能、班级健康度、资源结构与负荷趋势。</p>
        </div>
      </div>
    </section>

    <!-- 新增：资源结构 + 负荷趋势 -->
    <section class="chart-section">
      <el-row :gutter="16">
        <el-col :xs="24" :md="12" :lg="8">
          <div class="chart-card">
            <div class="chart-card-header">
              <div>
                <h3>教室类型占比</h3>
                <p class="chart-subtitle">普通教室、实验室、机房等资源类型数量分布</p>
              </div>
            </div>
            <div ref="typePieDom" class="chart-box chart-box--md" />
          </div>
        </el-col>
        <el-col :xs="24" :md="12" :lg="16">
          <div class="chart-card">
            <div class="chart-card-header">
              <div>
                <h3>每日课时分布趋势</h3>
                <p class="chart-subtitle">周一至周五全校排课「连堂课时」合计，识别最拥挤工作日</p>
              </div>
            </div>
            <div ref="weekdayLineDom" class="chart-box chart-box--md" />
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="chart-row-spaced">
        <el-col :xs="24" :md="12" :lg="8">
          <div class="chart-card">
            <div class="chart-card-header">
              <div>
                <h3>课程类型学时分布</h3>
                <p class="chart-subtitle">必修 / 选修 / 公选等类型学时占比（按排课记录累加）</p>
              </div>
            </div>
            <div ref="coursePieDom" class="chart-box chart-box--md" />
          </div>
        </el-col>
        <el-col :xs="24" :md="12" :lg="16">
          <div class="chart-card">
            <div class="chart-card-header">
              <div>
                <h3>教室容量区间分布</h3>
                <p class="chart-subtitle">按核定容量划分教室数量：30人以下、30–60、60–120、120人以上</p>
              </div>
            </div>
            <div ref="capacityBarDom" class="chart-box chart-box--md" />
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 原有：效能散点 + 健康度 + 热力图 -->
    <section class="chart-section">
      <el-row :gutter="16">
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-card-header">
              <div>
                <h3>教室效能</h3>
                <p class="chart-subtitle">X：时间利用率(%)，Y：空间满载率(%)，50% 为基准线</p>
              </div>
            </div>
            <div ref="scatterChartDom" class="chart-box chart-box--tall" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-card-header">
              <div>
                <h3>班级课表健康度时序图</h3>
                <p class="chart-subtitle">第1周到第20周，多班级平滑趋势线</p>
              </div>
            </div>
            <div ref="lineChartDom" class="chart-box chart-box--tall" />
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="chart-row-spaced">
        <el-col :span="24">
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
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import {
  getClassHealth,
  getClassroomEfficiency,
  getHeatmapData,
  getClassroomTypeDistribution,
  getWeekdayLessonTrend,
  getCourseTypeHours,
  getClassroomCapacityDistribution
} from '@/api/analysis'

const scatterChartDom = ref(null)
const lineChartDom = ref(null)
const heatmapChartDom = ref(null)
const typePieDom = ref(null)
const weekdayLineDom = ref(null)
const coursePieDom = ref(null)
const capacityBarDom = ref(null)

let scatterChart = null
let lineChart = null
let heatmapChart = null
let typePieChart = null
let weekdayLineChart = null
let coursePieChart = null
let capacityBarChart = null

let resizeHandler = null
let mediaQueryHandler = null
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)')

const days = ['周一', '周二', '周三', '周四', '周五']
const sections = Array.from({ length: 12 }, (_, i) => `第 ${i + 1} 节`)
const weekOptions = Array.from({ length: 20 }, (_, i) => i + 1)
const selectedWeek = ref(1)
const heatmapVisualMax = ref(10)

/** 与设计系统一致的调色板，亮/暗背景下均可辨认 */
const SERIES_PALETTE = [
  '#5470c6',
  '#91cc75',
  '#fac858',
  '#ee6666',
  '#73c0de',
  '#3ba272',
  '#fc8452',
  '#9a60b4',
  '#ea7ccc'
]

const cache = reactive({
  efficiency: [],
  health: [],
  heatmapMap: {},
  typeDist: [],
  weekdayTrend: [],
  courseHours: [],
  capacityDist: []
})

function readChartTheme() {
  const root = document.documentElement
  const cs = getComputedStyle(root)
  const pick = (v, fb) => {
    const t = v && String(v).trim()
    return t || fb
  }
  const text = pick(cs.getPropertyValue('--el-text-color-primary'), '#303133')
  const sub = pick(cs.getPropertyValue('--el-text-color-secondary'), '#909399')
  const border = pick(cs.getPropertyValue('--el-border-color'), '#dcdfe6')
  const gridLine = pick(cs.getPropertyValue('--el-border-color-lighter'), '#ebeef5')
  const bg = pick(cs.getPropertyValue('--el-bg-color'), '#ffffff')
  return {
    text,
    sub,
    border,
    axisLine: border,
    gridLine,
    splitLine: gridLine,
    bg
  }
}

function applyBaseStyle(option, theme) {
  const t = theme || readChartTheme()
  option = option || {}
  option.backgroundColor = 'transparent'
  option.textStyle = Object.assign({ color: t.text }, option.textStyle || {})
  option.tooltip = option.tooltip || {}
  const tipText = Object.assign({ color: t.text }, option.tooltip.textStyle || {})
  option.tooltip.textStyle = tipText
  if (option.tooltip.axisPointer) {
    option.tooltip.axisPointer.lineStyle = Object.assign(
      { color: t.border },
      option.tooltip.axisPointer.lineStyle || {}
    )
  }
  return option
}

function normalizeResp(resp) {
  return resp?.data?.data ?? []
}

function normalizeHeatmapDataMap(rawData) {
  if (rawData && !Array.isArray(rawData) && typeof rawData === 'object') {
    return rawData
  }
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

function ensurePieData(list) {
  if (Array.isArray(list) && list.length > 0) {
    return list.map((item) => ({
      name: item.name ?? '-',
      value: Number(item.value ?? 0)
    }))
  }
  return [{ name: '暂无数据', value: 0 }]
}

function getScatterOption(data, theme) {
  const t = theme || readChartTheme()
  return applyBaseStyle(
    {
      animationDuration: 1000,
      grid: { left: 58, right: 20, top: 26, bottom: 45 },
      tooltip: {
        trigger: 'item',
        formatter: (params) => {
          const [x, y, name] = params.value
          return `${name}<br/>时间利用率：${x.toFixed(2)}%<br/>空间满载率：${y.toFixed(2)}%`
        }
      },
      legend: { show: false },
      xAxis: {
        type: 'value',
        name: '时间利用率(%)',
        min: 0,
        max: 100,
        nameTextStyle: { color: t.sub },
        axisLabel: { color: t.sub, formatter: '{value}%' },
        axisLine: { lineStyle: { color: t.axisLine } },
        splitLine: { lineStyle: { color: t.splitLine } }
      },
      yAxis: {
        type: 'value',
        name: '空间满载率(%)',
        nameTextStyle: { color: t.sub },
        min: 0,
        max: 100,
        axisLabel: { color: t.sub, formatter: '{value}%' },
        axisLine: { lineStyle: { color: t.axisLine } },
        splitLine: { lineStyle: { color: t.splitLine } }
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
    },
    t
  )
}

function getLineOption(data, theme) {
  const t = theme || readChartTheme()
  const xWeeks = Array.from({ length: 20 }, (_, i) => `第${i + 1}周`)
  const series = data.map((item) => ({
    name: item.className || `班级${item.classId}`,
    type: 'line',
    smooth: true,
    showSymbol: false,
    data: item.weeklyScores ?? []
  }))
  return applyBaseStyle(
    {
      animationDuration: 1200,
      color: SERIES_PALETTE,
      legend: {
        type: 'scroll',
        top: 0,
        selectedMode: true,
        textStyle: { color: t.sub },
        inactiveColor: t.gridLine,
        pageTextStyle: { color: t.sub }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross', lineStyle: { color: t.border } }
      },
      grid: { left: 50, right: 25, top: 45, bottom: 30, containLabel: true },
      xAxis: {
        type: 'category',
        data: xWeeks,
        axisLabel: { color: t.sub },
        axisLine: { lineStyle: { color: t.axisLine } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: { color: t.sub },
        axisLine: { lineStyle: { color: t.axisLine } },
        splitLine: { lineStyle: { color: t.splitLine } }
      },
      series
    },
    t
  )
}

function getHeatmapOption(data, visualMax, theme) {
  const t = theme || readChartTheme()
  return applyBaseStyle(
    {
      animationDuration: 1200,
      grid: { top: 50, left: 90, right: 26, bottom: 56 },
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
      legend: { show: false },
      visualMap: {
        min: 0,
        max: Math.max(1, visualMax || 1),
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: 6,
        textStyle: { color: t.sub },
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
        axisLabel: { color: t.sub },
        axisLine: { lineStyle: { color: t.axisLine } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'category',
        data: sections,
        axisLabel: { color: t.sub },
        axisLine: { lineStyle: { color: t.axisLine } },
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
    },
    t
  )
}

function getClassroomTypePieOption(list, theme) {
  const t = theme || readChartTheme()
  const pieData = ensurePieData(list)
  return applyBaseStyle(
    {
      color: SERIES_PALETTE,
      tooltip: {
        trigger: 'item',
        formatter: (p) => {
          const v = p.value
          const pct = p.percent != null ? p.percent.toFixed(1) : '0'
          return `${p.name}<br/>教室数：<b>${v}</b>（${pct}%）`
        }
      },
      legend: {
        type: 'scroll',
        orient: 'horizontal',
        bottom: 4,
        selectedMode: true,
        textStyle: { color: t.sub },
        inactiveColor: t.gridLine,
        pageTextStyle: { color: t.sub }
      },
      series: [
        {
          name: '教室类型',
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '46%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 6,
            borderColor: t.bg,
            borderWidth: 2
          },
          label: { color: t.sub, formatter: '{b}\n{d}%' },
          emphasis: {
            label: { show: true, fontWeight: 'bold', color: t.text },
            itemStyle: { shadowBlur: 12, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.18)' }
          },
          data: pieData
        }
      ]
    },
    t
  )
}

function getWeekdayTrendOption(list, theme) {
  const t = theme || readChartTheme()
  const categories = (list || []).map((x) => x.name || '-')
  const values = (list || []).map((x) => Number(x.value ?? 0))
  const maxVal = values.length ? Math.max(...values) : 0
  const maxIdx = maxVal > 0 ? values.indexOf(maxVal) : -1
  const markPoint =
    maxIdx >= 0
      ? {
          data: [
            {
              name: '峰值',
              coord: [categories[maxIdx], values[maxIdx]],
              value: values[maxIdx],
              itemStyle: { color: SERIES_PALETTE[3] }
            }
          ]
        }
      : undefined

  return applyBaseStyle(
    {
      color: SERIES_PALETTE,
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'line', lineStyle: { color: t.border } },
        formatter: (items) => {
          const it = items[0]
          return `${it.axisValue}<br/>合计课时：<b>${it.data}</b>`
        }
      },
      legend: {
        data: ['全校课时合计'],
        top: 4,
        selectedMode: true,
        textStyle: { color: t.sub }
      },
      grid: { left: 48, right: 28, top: 52, bottom: 36, containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: categories.length ? categories : days,
        axisLabel: { color: t.sub },
        axisLine: { lineStyle: { color: t.axisLine } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        name: '课时合计',
        nameTextStyle: { color: t.sub },
        axisLabel: { color: t.sub },
        axisLine: { lineStyle: { color: t.axisLine } },
        splitLine: { lineStyle: { color: t.splitLine } }
      },
      series: [
        {
          name: '全校课时合计',
          type: 'line',
          smooth: true,
          showSymbol: true,
          symbolSize: 8,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(84, 112, 198, 0.35)' },
              { offset: 1, color: 'rgba(84, 112, 198, 0.02)' }
            ])
          },
          lineStyle: { width: 2.5 },
          data: values.length ? values : [0, 0, 0, 0, 0],
          markPoint
        }
      ]
    },
    t
  )
}

function getCourseTypePieOption(list, theme) {
  const t = theme || readChartTheme()
  const pieData = ensurePieData(list)
  return applyBaseStyle(
    {
      color: SERIES_PALETTE,
      tooltip: {
        trigger: 'item',
        formatter: (p) => {
          const v = typeof p.value === 'number' ? p.value.toFixed(1) : p.value
          const pct = p.percent != null ? p.percent.toFixed(1) : '0'
          return `${p.name}<br/>学时：<b>${v}</b>（${pct}%）`
        }
      },
      legend: {
        type: 'scroll',
        orient: 'vertical',
        right: 8,
        top: 'middle',
        selectedMode: true,
        textStyle: { color: t.sub },
        inactiveColor: t.gridLine,
        pageTextStyle: { color: t.sub }
      },
      series: [
        {
          name: '课程类型',
          type: 'pie',
          radius: ['36%', '62%'],
          center: ['42%', '50%'],
          itemStyle: {
            borderRadius: 5,
            borderColor: t.bg,
            borderWidth: 2
          },
          label: { color: t.sub, formatter: '{b}\n{d}%' },
          emphasis: {
            label: { show: true, fontWeight: 'bold', color: t.text }
          },
          data: pieData
        }
      ]
    },
    t
  )
}

function getCapacityBarOption(list, theme) {
  const t = theme || readChartTheme()
  const categories = (list || []).map((x) => x.name || '-')
  const values = (list || []).map((x) => Number(x.value ?? 0))
  return applyBaseStyle(
    {
      color: SERIES_PALETTE,
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(84, 112, 198, 0.08)' } },
        formatter: (items) => {
          const it = items[0]
          return `${it.axisValue}<br/>教室数量：<b>${it.data}</b>`
        }
      },
      legend: {
        data: ['教室数量'],
        top: 4,
        selectedMode: true,
        textStyle: { color: t.sub }
      },
      grid: { left: 44, right: 20, top: 48, bottom: 28, containLabel: true },
      xAxis: {
        type: 'category',
        data: categories,
        axisLabel: { color: t.sub, interval: 0 },
        axisLine: { lineStyle: { color: t.axisLine } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        name: '间数',
        minInterval: 1,
        nameTextStyle: { color: t.sub },
        axisLabel: { color: t.sub },
        axisLine: { lineStyle: { color: t.axisLine } },
        splitLine: { lineStyle: { color: t.splitLine } }
      },
      series: [
        {
          name: '教室数量',
          type: 'bar',
          barMaxWidth: 48,
          itemStyle: {
            borderRadius: [6, 6, 0, 0]
          },
          emphasis: { focus: 'series' },
          data: values
        }
      ]
    },
    t
  )
}

function getWeekHeatmapData(week) {
  const weekNodesRaw = cache.heatmapMap?.[week]
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

function renderAllCharts() {
  const theme = readChartTheme()
  scatterChart?.setOption(getScatterOption(cache.efficiency, theme), true)
  lineChart?.setOption(getLineOption(cache.health, theme), true)
  typePieChart?.setOption(getClassroomTypePieOption(cache.typeDist, theme), true)
  weekdayLineChart?.setOption(getWeekdayTrendOption(cache.weekdayTrend, theme), true)
  coursePieChart?.setOption(getCourseTypePieOption(cache.courseHours, theme), true)
  capacityBarChart?.setOption(getCapacityBarOption(cache.capacityDist, theme), true)

  const heatmapData = getWeekHeatmapData(selectedWeek.value)
  heatmapChart?.setOption(getHeatmapOption(heatmapData, heatmapVisualMax.value, theme), true)
}

async function loadAnalysisData() {
  const [
    effRes,
    healthRes,
    heatRes,
    typeRes,
    weekdayRes,
    courseRes,
    capRes
  ] = await Promise.all([
    getClassroomEfficiency(),
    getClassHealth(),
    getHeatmapData(),
    getClassroomTypeDistribution(),
    getWeekdayLessonTrend(),
    getCourseTypeHours(),
    getClassroomCapacityDistribution()
  ])

  cache.efficiency = normalizeResp(effRes)
  cache.health = normalizeResp(healthRes)
  cache.heatmapMap = normalizeHeatmapDataMap(normalizeResp(heatRes))
  cache.typeDist = normalizeResp(typeRes)
  cache.weekdayTrend = normalizeResp(weekdayRes)
  cache.courseHours = normalizeResp(courseRes)
  cache.capacityDist = normalizeResp(capRes)

  heatmapVisualMax.value = Math.max(1, cache.efficiency.length || 10)
  renderAllCharts()
}

function resizeAll() {
  scatterChart?.resize()
  lineChart?.resize()
  heatmapChart?.resize()
  typePieChart?.resize()
  weekdayLineChart?.resize()
  coursePieChart?.resize()
  capacityBarChart?.resize()
}

onMounted(async () => {
  await nextTick()
  const doms = [
    scatterChartDom.value,
    lineChartDom.value,
    heatmapChartDom.value,
    typePieDom.value,
    weekdayLineDom.value,
    coursePieDom.value,
    capacityBarDom.value
  ]
  if (doms.some((d) => !d)) return

  const initOpt = { renderer: 'canvas' }
  scatterChart = echarts.init(scatterChartDom.value, undefined, initOpt)
  lineChart = echarts.init(lineChartDom.value, undefined, initOpt)
  heatmapChart = echarts.init(heatmapChartDom.value, undefined, initOpt)
  typePieChart = echarts.init(typePieDom.value, undefined, initOpt)
  weekdayLineChart = echarts.init(weekdayLineDom.value, undefined, initOpt)
  coursePieChart = echarts.init(coursePieDom.value, undefined, initOpt)
  capacityBarChart = echarts.init(capacityBarDom.value, undefined, initOpt)

  try {
    await loadAnalysisData()
  } catch (error) {
    renderAllCharts()
    // eslint-disable-next-line no-console
    console.error('加载排课分析数据失败:', error)
  }

  resizeHandler = () => resizeAll()
  window.addEventListener('resize', resizeHandler)

  mediaQueryHandler = () => {
    renderAllCharts()
    resizeAll()
  }
  prefersDark.addEventListener('change', mediaQueryHandler)
})

onBeforeUnmount(() => {
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
  if (mediaQueryHandler) prefersDark.removeEventListener('change', mediaQueryHandler)
  scatterChart?.dispose()
  lineChart?.dispose()
  heatmapChart?.dispose()
  typePieChart?.dispose()
  weekdayLineChart?.dispose()
  coursePieChart?.dispose()
  capacityBarChart?.dispose()
  scatterChart = null
  lineChart = null
  heatmapChart = null
  typePieChart = null
  weekdayLineChart = null
  coursePieChart = null
  capacityBarChart = null
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

.chart-section {
  margin-bottom: 8px;
}

.chart-row-spaced {
  margin-top: 8px;
}

.chart-card {
  padding: 14px 16px 12px;
  margin-bottom: 16px;
  height: 100%;
  box-sizing: border-box;
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

.chart-box--md {
  height: 300px;
}

.chart-box--tall {
  height: 320px;
}

.chart-box--heatmap {
  height: 460px;
}

.chart-card--full {
  width: 100%;
}
</style>
