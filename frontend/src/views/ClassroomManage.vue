<template>
  <div class="classroom-dt-page">
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>教室数字孪生 · 楼宇矩阵</h2>
          <p>按大区聚合展示 A/B/C 区 1-4 楼教室分布，点击房间查看效能详情。</p>
        </div>
        <div class="toolbar-actions">
          <el-button type="primary" class="add-btn" @click="openAddDrawer">
            添加教室
          </el-button>
        </div>
      </div>

      <el-form class="search-form" :inline="true" :model="searchForm" label-width="72px">
        <el-form-item label="教室名称">
          <el-input v-model="searchForm.classroomName" placeholder="请输入教室名称" clearable />
        </el-form-item>
        <el-form-item label="教室类型">
          <el-select
            v-model="searchForm.classroomType"
            placeholder="请选择类型"
            clearable
            style="width: 180px"
          >
            <el-option label="普通教室" value="普通教室" />
            <el-option label="计算机机房" value="计算机机房" />
            <el-option label="实验室" value="实验室" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="tableLoading" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="building-card" v-loading="tableLoading">
      <div class="building-header">
        <div class="building-title">
          <span class="badge">A/B/C 区矩阵</span>
          <span class="subtitle">Digital Twin Building Matrix</span>
        </div>
        <div class="legend">
          <div class="legend-item">
            <span class="legend-dot legend-dot--available" />
            <span>可用</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot legend-dot--maintain" />
            <span>维修</span>
          </div>
          <div class="legend-item">
            <span class="legend-chip">60</span>
            <span>小教室</span>
          </div>
          <div class="legend-item">
            <span class="legend-chip legend-chip--lg">120</span>
            <span>大教室(双倍宽)</span>
          </div>
        </div>
      </div>

      <div class="areas">
        <div v-for="areaBlock in areasGrouped" :key="areaBlock.area" class="area-block">
          <div class="area-title-row">
            <div class="area-title">
              {{ areaBlock.area }} 区
            </div>
          </div>

          <div class="floors">
            <div v-for="floorBlock in areaBlock.floors" :key="`${areaBlock.area}-${floorBlock.floor}`" class="floor-row">
              <div class="floor-label">
                <div class="floor-label-inner">
                  <div class="floor-num">{{ floorBlock.floor }}</div>
                  <div class="floor-text">楼</div>
                </div>
              </div>

              <div class="rooms-grid">
                <button
                  v-for="room in floorBlock.rooms"
                  :key="room.classroomId ?? room.classroomName"
                  type="button"
                  class="room-card"
                  :class="[
                    room.classroomState === '维修' ? 'is-maintain' : 'is-available',
                    room.classroomCap >= 120 ? 'is-large' : 'is-small'
                  ]"
                  @click="openEditDrawer(room)"
                >
                  <div class="room-top">
                    <div class="room-name">{{ room.classroomName }}</div>
                  </div>

                  <div class="room-mid">
                    <div class="room-meta">
                      <span class="meta-item">
                        <span class="meta-icon" aria-hidden="true">👥</span>
                        <span class="meta-text">{{ room.classroomCap }}人</span>
                      </span>
                      <span class="meta-item">
                        <span class="meta-icon" aria-hidden="true">🏷</span>
                        <span class="meta-text">{{ room.classroomType || '普通教室' }}</span>
                      </span>
                    </div>
                    <div class="room-state">
                      <span
                        class="state-pill"
                        :class="room.classroomState === '维修' ? 'state-pill--maintain' : 'state-pill--ok'"
                      >
                        {{ room.classroomState || '未知' }}
                      </span>
                    </div>
                  </div>

                  <div class="room-bottom">
                    <el-progress
                      :percentage="room.utilizationRate"
                      :stroke-width="10"
                      :color="getUtilizationColor(room.utilizationRate)"
                      striped
                      striped-flow
                    />
                    <div class="util-hint">
                      利用率 <b>{{ room.utilizationRate }}%</b>
                    </div>
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      size="520px"
      :with-header="false"
      :close-on-click-modal="false"
      class="dt-drawer"
      @opened="handleDrawerOpened"
      @closed="handleDrawerClosed"
    >
      <div class="drawer-header">
        <div class="drawer-title">
          <div class="drawer-room-name">{{ drawerTitle }}</div>
          <div class="drawer-subtitle">状态 / 容量 / 类型可编辑，并展示时间利用率与空间满载率对比（满分100%）</div>
        </div>
        <div class="drawer-header-actions">
          <el-button text @click="drawerVisible = false">关闭</el-button>
        </div>
      </div>

      <div class="drawer-body">
        <section class="drawer-section">
          <div class="section-title">教室信息</div>
          <el-form
            ref="drawerFormRef"
            :model="drawerForm"
            :rules="drawerRules"
            label-width="96px"
            class="drawer-form"
          >
            <el-form-item label="教室编号" prop="classroomId">
              <el-input
                v-model.number="drawerForm.classroomId"
                :disabled="isEdit"
                placeholder="请输入教室编号（需手动输入）"
              />
            </el-form-item>
            <el-form-item label="教室名称" prop="classroomName">
              <el-input v-model="drawerForm.classroomName" placeholder="如 C101" />
            </el-form-item>
            <el-form-item label="教室类型" prop="classroomType">
              <el-select v-model="drawerForm.classroomType" placeholder="请选择教室类型" style="width: 100%">
                <el-option label="普通教室" value="普通教室" />
                <el-option label="计算机机房" value="计算机机房" />
                <el-option label="实验室" value="实验室" />
              </el-select>
            </el-form-item>
            <el-form-item label="容纳人数" prop="classroomCap">
              <el-input-number
                v-model="drawerForm.classroomCap"
                :min="10"
                :max="400"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="状态" prop="classroomState">
              <el-select v-model="drawerForm.classroomState" placeholder="请选择状态" style="width: 100%">
                <el-option label="可用" value="可用" />
                <el-option label="被占用" value="被占用" />
                <el-option label="维修" value="维修" />
              </el-select>
            </el-form-item>
          </el-form>

          <div class="drawer-footer">
            <div class="drawer-footer-left">
              <el-popconfirm
                v-if="isEdit"
                title="确认删除该教室？"
                confirm-button-text="删除"
                cancel-button-text="取消"
                confirm-button-type="danger"
                @confirm="handleDeleteById(drawerForm.classroomId)"
              >
                <template #reference>
                  <el-button type="danger" plain>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
            <div class="drawer-footer-right">
              <el-button @click="drawerVisible = false">取 消</el-button>
              <el-button type="primary" :loading="drawerSubmitting" @click="handleSubmit">
                保 存
              </el-button>
            </div>
          </div>
        </section>

        <section class="drawer-section drawer-section--chart">
          <div class="section-title">教室效能维度对比柱状图</div>
          <div ref="barChartDom" class="bar-box" />
          <div class="chart-hint">
            指标：时间利用率 / 空间满载率（均满分 100%，无数据默认 0）
          </div>
        </section>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  getClassroomPage,
  addClassroom,
  updateClassroom,
  deleteClassroom
} from '../api/classroom'
import { getClassroomEfficiency } from '../api/analysis'

const tableData = ref([])
const tableLoading = ref(false)

const searchForm = reactive({
  classroomName: '',
  classroomType: ''
})

const drawerVisible = ref(false)
const drawerSubmitting = ref(false)
const drawerFormRef = ref(null)
const isEdit = ref(false)

const drawerForm = reactive({
  classroomId: null,
  classroomName: '',
  classroomType: '',
  classroomCap: 60,
  classroomState: '可用'
})

const drawerRules = {
  classroomId: [
    { required: true, message: '请输入教室编号', trigger: 'blur' }
  ],
  classroomName: [
    { required: true, message: '请输入教室名称', trigger: 'blur' }
  ],
  classroomType: [
    { required: true, message: '请选择教室类型', trigger: 'change' }
  ],
  classroomCap: [
    { required: true, message: '请输入容纳人数', trigger: 'blur' },
    {
      type: 'number',
      message: '容量必须为数字',
      trigger: ['blur', 'change']
    }
  ],
  classroomState: [
    { required: true, message: '请选择教室状态', trigger: 'change' }
  ]
}

const drawerTitle = computed(() => {
  const name = drawerForm.classroomName || '-'
  return isEdit.value ? `编辑教室 · ${name}` : '添加教室'
})

function parseFloorFromName(name) {
  const n = String(name || '').trim()
  if (n.length < 2) return null
  const after = n.slice(1)
  // 先提取字母后连续数字，例如 C101 -> 101, B1201 -> 1201, B12 -> 12
  const m = after.match(/^(\d+)/)
  if (!m) return null
  const digits = m[1]
  // 常见教室号最后两位为房间号，前缀视为楼层；短编号直接作为楼层
  // 兼容两位短编号（如 B12：1楼-2号），两位时取首位为楼层
  let floorPart = digits
  if (digits.length === 2) {
    floorPart = digits[0]
  } else if (digits.length >= 3) {
    floorPart = digits.slice(0, -2)
  }
  const floor = Number.parseInt(floorPart, 10)
  return Number.isFinite(floor) ? floor : null
}

function parseAreaFromName(name) {
  const n = String(name || '').trim()
  if (!n) return ''
  return String(n[0] || '').toUpperCase()
}

const efficiencyByRoomName = reactive({})
const efficiencyByRoomId = reactive({})

function toNumOrZero(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

function getEfficiencyKeyForRoom(room) {
  const nameKey = String(room?.classroomName ?? '').trim()
  const idKey = String(room?.classroomId ?? '').trim()
  return { nameKey, idKey }
}

const classroomsEnriched = computed(() => {
  return (tableData.value || []).map((r) => {
    const { nameKey, idKey } = getEfficiencyKeyForRoom(r)
    const eff = (idKey && efficiencyByRoomId[idKey]) || (nameKey && efficiencyByRoomName[nameKey]) || {}
    const timeV = Math.round(toNumOrZero(eff.timeUtilization))
    const spaceV = Math.round(toNumOrZero(eff.spaceLoadRate))
    return {
      ...r,
      utilizationRate: Math.max(0, Math.min(100, timeV)),
      spaceLoadRate: Math.max(0, Math.min(100, spaceV))
    }
  })
})

const areasGrouped = computed(() => {
  const areaOrder = ['A', 'B', 'C']
  const defaultFloorOrderByArea = {
    A: [1],
    B: [1, 2],
    C: [1, 2, 3, 4]
  }

  const areaMap = new Map()
  for (const room of classroomsEnriched.value) {
    const area = parseAreaFromName(room.classroomName)
    const floor = parseFloorFromName(room.classroomName)
    if (!area || !floor) continue

    if (!areaMap.has(area)) areaMap.set(area, new Map())
    const floorMap = areaMap.get(area)
    if (!floorMap.has(floor)) floorMap.set(floor, [])
    floorMap.get(floor).push(room)
  }

  return areaOrder.map((area) => {
    // 只为 A/B 区按“实际数据”动态裁剪空白楼层
    const dataFloors = Array.from(areaMap.get(area)?.keys() || []).sort((a, b) => a - b)
    const shouldDynamic = area === 'A' || area === 'B'
    const floorOrder = (shouldDynamic && dataFloors.length ? dataFloors : defaultFloorOrderByArea[area]) || []
    const floors = floorOrder.map((floor) => {
      const rooms = (areaMap.get(area)?.get(floor) || []).slice().sort((a, b) => {
        const an = String(a.classroomName || '')
        const bn = String(b.classroomName || '')
        return an.localeCompare(bn)
      })
      return { floor, rooms }
    })
    return { area, floors }
  })
})

function getUtilizationColor(rate) {
  if (rate > 85) return '#f97316' // 橙红
  if (rate < 30) return '#22c55e' // 绿色
  return '#3b82f6' // 默认蓝色
}

const fetchClassroomPage = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: 1,
      pageSize: 9999,
      classroomName: searchForm.classroomName || undefined,
      classroomType: searchForm.classroomType || undefined
    }

    const [classRes, effRes] = await Promise.all([getClassroomPage(params), getClassroomEfficiency()])

    const classData = classRes?.data
    if (classData && classData.code === 1 && classData.data) {
      tableData.value = classData.data.rows || []
    } else {
      ElMessage.error(classData?.msg || '获取教室列表失败')
      tableData.value = []
    }

    const effList = effRes?.data?.data || []
    // 清空旧映射，避免切换查询后沿用旧数据
    Object.keys(efficiencyByRoomName).forEach((k) => delete efficiencyByRoomName[k])
    Object.keys(efficiencyByRoomId).forEach((k) => delete efficiencyByRoomId[k])

    for (const item of effList || []) {
      const nameKey = String(item?.classroomName ?? '').trim()
      const idKey = String(item?.classroomId ?? '').trim()
      if (idKey) efficiencyByRoomId[idKey] = item
      if (nameKey) efficiencyByRoomName[nameKey] = item
    }
  } catch (error) {
    ElMessage.error('请求教室列表失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  fetchClassroomPage()
}

const handleReset = () => {
  searchForm.classroomName = ''
  searchForm.classroomType = ''
  fetchClassroomPage()
}

const resetDrawerForm = () => {
  drawerForm.classroomId = null
  drawerForm.classroomName = ''
  drawerForm.classroomType = '普通教室'
  drawerForm.classroomCap = 60
  drawerForm.classroomState = '可用'
  if (drawerFormRef.value) drawerFormRef.value.clearValidate()
}

const openAddDrawer = () => {
  isEdit.value = false
  resetDrawerForm()
  drawerVisible.value = true
}

const openEditDrawer = (row) => {
  if (!row) return
  isEdit.value = true
  drawerForm.classroomId = row.classroomId
  drawerForm.classroomName = row.classroomName
  drawerForm.classroomType = row.classroomType
  drawerForm.classroomCap = row.classroomCap
  drawerForm.classroomState = row.classroomState
  if (drawerFormRef.value) drawerFormRef.value.clearValidate()
  drawerVisible.value = true
}

const handleSubmit = () => {
  if (!drawerFormRef.value) return
  drawerFormRef.value.validate(async (valid) => {
    if (!valid) return
    drawerSubmitting.value = true
    try {
      const payload = {
        classroomId: drawerForm.classroomId,
        classroomName: drawerForm.classroomName,
        classroomType: drawerForm.classroomType,
        classroomCap: drawerForm.classroomCap,
        classroomState: drawerForm.classroomState
      }

      let res
      if (isEdit.value) {
        res = await updateClassroom(payload)
      } else {
        res = await addClassroom(payload)
      }
      const { data } = res

      if (data && data.code === 1) {
        ElMessage.success(isEdit.value ? '更新教室成功' : '新增教室成功')
        drawerVisible.value = false
        fetchClassroomPage()
      } else {
        ElMessage.error(data?.msg || (isEdit.value ? '更新教室失败' : '新增教室失败'))
      }
    } catch (error) {
      ElMessage.error(isEdit.value ? '请求更新教室失败' : '请求新增教室失败')
    } finally {
      drawerSubmitting.value = false
    }
  })
}

const handleDeleteById = async (classroomId) => {
  if (!classroomId) return
  try {
    const { data } = await deleteClassroom(classroomId)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      drawerVisible.value = false
      fetchClassroomPage()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除请求失败')
  }
}

const barChartDom = ref(null)
let drawerBarChart = null
let resizeHandler = null

const drawerMetrics = computed(() => {
  const nameKey = String(drawerForm.classroomName ?? '').trim()
  const idKey = String(drawerForm.classroomId ?? '').trim()
  const eff = (idKey && efficiencyByRoomId[idKey]) || (nameKey && efficiencyByRoomName[nameKey]) || {}
  const timeV = Math.round(toNumOrZero(eff.timeUtilization))
  const spaceV = Math.round(toNumOrZero(eff.spaceLoadRate))
  return {
    timeUtilization: Math.max(0, Math.min(100, timeV)),
    spaceLoadRate: Math.max(0, Math.min(100, spaceV))
  }
})

function getDrawerBarOption(metrics) {
  const timeV = metrics?.timeUtilization ?? 0
  const spaceV = metrics?.spaceLoadRate ?? 0

  return {
    backgroundColor: 'transparent',
    animationDuration: 800,
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const t = params?.[0]?.axisValue
        const v0 = params?.[0]?.value ?? 0
        const v1 = params?.[1]?.value ?? 0
        return `${t}<br/>时间利用率：${v0}%<br/>空间满载率：${v1}%`
      }
    },
    grid: { left: 56, right: 18, top: 24, bottom: 40 },
    xAxis: {
      type: 'category',
      data: ['时间利用率', '空间满载率'],
      axisLabel: { color: '#6b7280' },
      axisLine: { lineStyle: { color: 'rgba(107,114,128,0.35)' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { formatter: '{value}%', color: '#6b7280' },
      splitLine: { lineStyle: { color: 'rgba(148,163,184,0.25)' } }
    },
    series: [
      {
        type: 'bar',
        barWidth: '44%',
        data: [timeV, spaceV],
        itemStyle: {
          color: (p) => (p.dataIndex === 0 ? '#3b82f6' : '#f97316')
        },
        label: {
          show: true,
          position: 'top',
          color: '#111827',
          fontWeight: 700,
          formatter: (v) => `${v.value}%`
        }
      }
    ]
  }
}

async function ensureDrawerBarChart() {
  await nextTick()
  if (!barChartDom.value) return
  if (!drawerBarChart) {
    drawerBarChart = echarts.init(barChartDom.value, undefined, { renderer: 'canvas' })
  }
}

async function renderDrawerBarWithLoading() {
  await ensureDrawerBarChart()
  if (!drawerBarChart) return

  drawerBarChart.showLoading('default', {
    text: '加载效能数据...',
    color: '#3b82f6',
    textColor: '#111827',
    maskColor: 'rgba(255,255,255,0.7)'
  })
  await new Promise((r) => window.setTimeout(r, 380))
  drawerBarChart.hideLoading()
  drawerBarChart.setOption(getDrawerBarOption(drawerMetrics.value), true)
  drawerBarChart.resize()
}

const handleDrawerOpened = () => {
  renderDrawerBarWithLoading()
}

const handleDrawerClosed = () => {
  if (drawerFormRef.value) drawerFormRef.value.clearValidate()
}

watch(
  () => [drawerVisible.value, drawerForm.classroomId, drawerForm.classroomName],
  () => {
    if (!drawerVisible.value) return
    renderDrawerBarWithLoading()
  }
)

onMounted(() => {
  fetchClassroomPage()
  resizeHandler = () => {
    if (drawerBarChart) drawerBarChart.resize()
  }
  window.addEventListener('resize', resizeHandler, { passive: true })
})

onBeforeUnmount(() => {
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
  if (drawerBarChart) {
    drawerBarChart.dispose()
    drawerBarChart = null
  }
})
</script>

<style scoped>
.classroom-dt-page {
  --dt-bg: #f0f2f5;
  --dt-card: #ffffff;
  --dt-card-2: #f8fafc;
  --dt-border: rgba(17, 24, 39, 0.12);
  --dt-border-2: rgba(59, 130, 246, 0.22);
  --dt-text: #303133;
  --dt-sub: #6b7280;
  --dt-glow: 0 12px 30px rgba(59, 130, 246, 0.12);

  min-height: calc(100vh - 40px);
  padding: 18px;
  color: var(--dt-text);
  background: var(--dt-bg);
}

.toolbar-card {
  border: 1px solid var(--dt-border);
  background: #ffffff;
  border-radius: 14px;
  padding: 16px 16px 10px;
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.06);
}

.toolbar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 8px;
}

.title-block h2 {
  margin: 0;
  font-size: 18px;
  letter-spacing: 0.2px;
}

.title-block p {
  margin: 6px 0 0;
  color: var(--dt-sub);
  font-size: 12px;
}

.search-form {
  margin-top: 8px;
}

.building-card {
  margin-top: 14px;
  border: 1px solid var(--dt-border);
  background: #ffffff;
  border-radius: 16px;
  padding: 14px 14px 18px;
  box-shadow: 0 18px 56px rgba(17, 24, 39, 0.06);
}

.building-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 4px 2px 10px;
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
}

.building-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.badge {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid rgba(59, 130, 246, 0.35);
  background: rgba(59, 130, 246, 0.10);
  color: #0f172a;
  font-weight: 700;
  letter-spacing: 0.4px;
}

.subtitle {
  color: var(--dt-sub);
  font-size: 12px;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 10px 14px;
  color: var(--dt-sub);
  font-size: 12px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid rgba(148, 163, 184, 0.25);
}

.legend-dot--available {
  background: rgba(59, 130, 246, 0.85);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.legend-dot--maintain {
  background: rgba(248, 113, 113, 0.85);
  box-shadow: 0 0 0 3px rgba(248, 113, 113, 0.10);
}

.legend-chip {
  height: 18px;
  min-width: 30px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  border: 1px solid rgba(17, 24, 39, 0.12);
  background: #ffffff;
  color: rgba(17, 24, 39, 0.92);
  font-weight: 700;
}

.legend-chip--lg {
  border-color: rgba(96, 165, 250, 0.35);
  background: rgba(59, 130, 246, 0.12);
}

.areas {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-top: 10px;
}

.area-block {
  border-top: 1px solid rgba(17, 24, 39, 0.06);
  padding-top: 10px;
}

.area-title-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 6px 6px 12px;
}

.area-title {
  font-size: 14px;
  font-weight: 900;
  color: rgba(17, 24, 39, 0.95);
  letter-spacing: 0.2px;
}

.floors {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 14px;
}

.floor-row {
  display: grid;
  grid-template-columns: 84px 1fr;
  gap: 14px;
  align-items: stretch;
}

.floor-label {
  border-radius: 14px;
  border: 1px solid rgba(17, 24, 39, 0.10);
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.floor-label::before {
  content: '';
  position: absolute;
  inset: -40%;
  background: radial-gradient(circle at 30% 20%, rgba(59, 130, 246, 0.14), transparent 55%);
  transform: rotate(18deg);
}

.floor-label-inner {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1;
}

.floor-num {
  font-size: 26px;
  font-weight: 900;
  color: #0f172a;
  text-shadow: 0 0 22px rgba(59, 130, 246, 0.22);
}

.floor-text {
  margin-top: 6px;
  font-size: 12px;
  letter-spacing: 6px;
  text-indent: 6px;
  color: rgba(107, 114, 128, 0.95);
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(10, minmax(0, 1fr));
  gap: 12px;
}

.room-card {
  grid-column: span 2;
  width: 100%;
  border-radius: 14px;
  border: 1px solid var(--dt-border);
  padding: 12px 12px 10px;
  text-align: left;
  cursor: pointer;
  color: var(--dt-text);
  background: #ffffff;
  box-shadow: 0 10px 26px rgba(17, 24, 39, 0.06);
  transition: transform 220ms ease, box-shadow 220ms ease, border-color 220ms ease;
  position: relative;
  overflow: hidden;
}

.room-card.is-large {
  grid-column: span 4;
}

.room-card.is-available::before {
  content: '';
  position: absolute;
  inset: -30%;
  background:
    radial-gradient(circle at 20% 10%, rgba(59, 130, 246, 0.18), transparent 52%),
    radial-gradient(circle at 80% 0%, rgba(124, 58, 237, 0.10), transparent 48%),
    radial-gradient(circle at 60% 100%, rgba(16, 185, 129, 0.06), transparent 55%);
  transform: rotate(10deg);
  opacity: 0.85;
  pointer-events: none;
}

.room-card.is-maintain {
  background:
    repeating-linear-gradient(
      135deg,
      rgba(239, 68, 68, 0.18) 0px,
      rgba(239, 68, 68, 0.18) 10px,
      rgba(243, 244, 246, 1) 10px,
      rgba(243, 244, 246, 1) 20px
    ),
    linear-gradient(180deg, rgba(254, 202, 202, 0.72), rgba(255, 255, 255, 1));
  border-color: rgba(239, 68, 68, 0.35);
}

.room-card:hover {
  transform: translateY(-6px);
  border-color: rgba(96, 165, 250, 0.45);
  box-shadow: var(--dt-glow);
}

.room-top {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 8px;
}

.room-name {
  font-size: 20px;
  font-weight: 900;
  letter-spacing: 0.6px;
  text-shadow: 0 0 18px rgba(59, 130, 246, 0.18);
}

.room-mid {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 0 10px;
  border-top: 1px solid rgba(17, 24, 39, 0.08);
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
}

.room-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: rgba(31, 41, 55, 0.75);
  font-size: 12px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  width: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  opacity: 0.9;
}

.room-state {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.state-pill {
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(17, 24, 39, 0.12);
  background: rgba(2, 6, 23, 0.02);
  color: rgba(17, 24, 39, 0.78);
}

.state-pill--ok {
  border-color: rgba(59, 130, 246, 0.25);
  background: rgba(59, 130, 246, 0.12);
}

.state-pill--maintain {
  border-color: rgba(239, 68, 68, 0.28);
  background: rgba(239, 68, 68, 0.10);
}

.room-bottom {
  position: relative;
  padding-top: 10px;
}

.util-hint {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(107, 114, 128, 0.95);
}

.dt-drawer :deep(.el-drawer__body) {
  padding: 0;
  background: #ffffff;
  color: var(--dt-text);
}

.drawer-header {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.drawer-room-name {
  font-weight: 900;
  letter-spacing: 0.3px;
  font-size: 16px;
}

.drawer-subtitle {
  margin-top: 6px;
  color: rgba(107, 114, 128, 0.95);
  font-size: 12px;
}

.drawer-body {
  padding: 14px 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.drawer-section {
  border: 1px solid rgba(17, 24, 39, 0.10);
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 1), rgba(248, 250, 252, 0.92));
  padding: 12px;
  box-shadow: 0 18px 46px rgba(17, 24, 39, 0.06);
}

.section-title {
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.2px;
  color: rgba(17, 24, 39, 0.95);
  margin-bottom: 10px;
}

.drawer-footer {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.drawer-footer-right {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.bar-box {
  width: 100%;
  height: 260px;
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.18);
  background: radial-gradient(600px 260px at 50% 0%, rgba(59, 130, 246, 0.12), transparent 60%);
}

.chart-hint {
  margin-top: 8px;
  color: rgba(107, 114, 128, 0.95);
  font-size: 12px;
  line-height: 1.4;
}

/* 让 Element Plus 表单在浅色下更贴合 */
.classroom-dt-page :deep(.el-input__wrapper),
.classroom-dt-page :deep(.el-select__wrapper),
.classroom-dt-page :deep(.el-textarea__inner) {
  background: #ffffff;
  box-shadow: none;
  border: 1px solid rgba(209, 213, 219, 0.95);
}

.classroom-dt-page :deep(.el-input__inner),
.classroom-dt-page :deep(.el-select__selected-item) {
  color: rgba(17, 24, 39, 0.92);
}

.classroom-dt-page :deep(.el-form-item__label) {
  color: rgba(107, 114, 128, 0.95);
}

.classroom-dt-page :deep(.el-progress__text) {
  color: rgba(17, 24, 39, 0.80);
}

@media (max-width: 1100px) {
  .rooms-grid {
    grid-template-columns: repeat(8, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .floor-row {
    grid-template-columns: 70px 1fr;
  }
  .rooms-grid {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}
</style>

