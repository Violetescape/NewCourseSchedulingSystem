<template>
  <div class="auto-schedule-page">
    <!-- 顶部操作区：一键排课 -->
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>自动排课</h2>
          <p>一键生成课表，并支持按班级、教室、教师多维度查看全局课表。</p>
        </div>
        <div class="toolbar-actions">
          <el-button
            id="btn-auto-schedule"
            type="primary"
            plain
            :loading="isAutoScheduling"
            @click="handleAutoSchedule"
          >
            一键自动排课
          </el-button>
        </div>
      </div>

      <!-- 多维度课表查询控制台 -->
      <div class="query-console">
        <el-form
          class="search-form"
          :inline="true"
          :model="queryForm"
          label-width="72px"
        >
          <el-form-item label="查询模式">
            <el-radio-group v-model="queryForm.mode">
              <el-radio-button label="class">按班级</el-radio-button>
              <el-radio-button label="classroom">按教室</el-radio-button>
              <el-radio-button label="teacher">按教师</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item v-if="queryForm.mode === 'class'" label="班级">
            <el-select
              v-model="queryForm.classId"
              placeholder="请选择班级"
              filterable
              clearable
              style="width: 240px"
              @change="handleQuery"
            >
              <el-option
                v-for="item in classOptions"
                :key="item.classId"
                :label="item.className"
                :value="item.classId"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-else-if="queryForm.mode === 'classroom'" label="教室">
            <el-select
              v-model="queryForm.classroomId"
              placeholder="请选择教室"
              filterable
              clearable
              style="width: 240px"
              @change="handleQuery"
            >
              <el-option
                v-for="item in classroomOptions"
                :key="item.classroomId"
                :label="item.classroomName"
                :value="item.classroomId"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-else label="教师">
            <el-select
              v-model="queryForm.teacherId"
              placeholder="请选择教师"
              filterable
              clearable
              style="width: 240px"
              @change="handleQuery"
            >
              <el-option
                v-for="item in teacherOptions"
                :key="item.teacherId"
                :label="item.teacherName"
                :value="item.teacherId"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleQuery">
              查询
            </el-button>
            <el-button @click="handleClear">清空</el-button>
          </el-form-item>
        </el-form>
      </div>
    </section>

    <!-- 全局聚合课表 -->
    <section class="table-card">
      <div v-if="loading" class="grid-loading">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载课表数据中...</span>
      </div>
      <div
        v-else-if="!hasValidSelection"
        class="grid-empty-hint"
      >
        <el-empty description="请在上方选择班级、教室或教师后查询，查看其全局聚合课表。" />
      </div>
      <div v-else class="grid-wrapper">
        <table class="schedule-grid schedule-grid-composite">
          <thead>
            <tr>
              <th class="corner-cell">节次 / 星期</th>
              <th v-for="day in weekdays" :key="day.value" class="day-cell">
                {{ day.label }}
              </th>
            </tr>
          </thead>
          <tbody>
            <!-- 上午 1-5 节 -->
            <tr v-for="sec in sections.slice(0, 5)" :key="sec.value">
              <td class="section-cell">{{ sec.label }}</td>
              <template v-for="day in weekdays" :key="`${sec.value}-${day.value}`">
                <td
                  v-if="!getCompositeCell(sec.value, day.value).skip"
                  class="content-cell content-cell-composite"
                  :rowspan="getCompositeCell(sec.value, day.value).rowspan || 1"
                >
                  <div
                    v-for="(item, idx) in getCompositeCell(sec.value, day.value).items"
                    :key="`${sec.value}-${day.value}-${item.courseName}-${idx}`"
                    class="cell-card cell-card-stack"
                  >
                    <div class="cell-title">{{ item.courseName || '-' }}</div>
                    <div class="cell-sub">{{ item.classroomName || '-' }}</div>
                    <div class="cell-meta">
                      <span v-if="queryForm.mode === 'class'">
                        {{ item.teacherName || '-' }}
                      </span>
                      <span v-else-if="queryForm.mode === 'classroom'">
                        {{ item.className || '-' }}{{ item.teacherName ? ` · ${item.teacherName}` : '' }}
                      </span>
                      <span v-else>
                        {{ item.className || '-' }}
                      </span>
                    </div>
                    <div
                      v-if="formatWeekRange(item)"
                      class="course-weeks"
                    >
                      [第 {{ formatWeekRange(item) }} 周]
                    </div>
                  </div>
                  <div v-if="getCompositeCell(sec.value, day.value).items.length === 0" class="cell-empty">
                    —
                  </div>
                </td>
              </template>
            </tr>

            <tr class="break-row">
              <td class="section-cell break-label">午休</td>
              <td class="break-cell" colspan="7">午休 · 休息时间</td>
            </tr>

            <!-- 下午 6-9 节 -->
            <tr v-for="sec in sections.slice(5, 9)" :key="sec.value">
              <td class="section-cell">{{ sec.label }}</td>
              <template v-for="day in weekdays" :key="`${sec.value}-${day.value}`">
                <td
                  v-if="!getCompositeCell(sec.value, day.value).skip"
                  class="content-cell content-cell-composite"
                  :rowspan="getCompositeCell(sec.value, day.value).rowspan || 1"
                >
                  <div
                    v-for="(item, idx) in getCompositeCell(sec.value, day.value).items"
                    :key="`${sec.value}-${day.value}-${item.courseName}-${idx}`"
                    class="cell-card cell-card-stack"
                  >
                    <div class="cell-title">{{ item.courseName || '-' }}</div>
                    <div class="cell-sub">{{ item.classroomName || '-' }}</div>
                    <div class="cell-meta">
                      <span v-if="queryForm.mode === 'class'">
                        {{ item.teacherName || '-' }}
                      </span>
                      <span v-else-if="queryForm.mode === 'classroom'">
                        {{ item.className || '-' }}{{ item.teacherName ? ` · ${item.teacherName}` : '' }}
                      </span>
                      <span v-else>
                        {{ item.className || '-' }}
                      </span>
                    </div>
                    <div
                      v-if="formatWeekRange(item)"
                      class="course-weeks"
                    >
                      [第 {{ formatWeekRange(item) }} 周]
                    </div>
                  </div>
                  <div v-if="getCompositeCell(sec.value, day.value).items.length === 0" class="cell-empty">
                    —
                  </div>
                </td>
              </template>
            </tr>

            <tr class="break-row">
              <td class="section-cell break-label">晚饭</td>
              <td class="break-cell" colspan="7">晚饭 · 休息时间</td>
            </tr>

            <!-- 晚上 10-12 节 -->
            <tr v-for="sec in sections.slice(9, 12)" :key="sec.value">
              <td class="section-cell">{{ sec.label }}</td>
              <template v-for="day in weekdays" :key="`${sec.value}-${day.value}`">
                <td
                  v-if="!getCompositeCell(sec.value, day.value).skip"
                  class="content-cell content-cell-composite"
                  :rowspan="getCompositeCell(sec.value, day.value).rowspan || 1"
                >
                  <div
                    v-for="(item, idx) in getCompositeCell(sec.value, day.value).items"
                    :key="`${sec.value}-${day.value}-${item.courseName}-${idx}`"
                    class="cell-card cell-card-stack"
                  >
                    <div class="cell-title">{{ item.courseName || '-' }}</div>
                    <div class="cell-sub">{{ item.classroomName || '-' }}</div>
                    <div class="cell-meta">
                      <span v-if="queryForm.mode === 'class'">
                        {{ item.teacherName || '-' }}
                      </span>
                      <span v-else-if="queryForm.mode === 'classroom'">
                        {{ item.className || '-' }}{{ item.teacherName ? ` · ${item.teacherName}` : '' }}
                      </span>
                      <span v-else>
                        {{ item.className || '-' }}
                      </span>
                    </div>
                    <div
                      v-if="formatWeekRange(item)"
                      class="course-weeks"
                    >
                      [第 {{ formatWeekRange(item) }} 周]
                    </div>
                  </div>
                  <div v-if="getCompositeCell(sec.value, day.value).items.length === 0" class="cell-empty">
                    —
                  </div>
                </td>
              </template>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getTeacherPage } from '../api/teacher'
import { getClassPage } from '../api/class'
import { getClassroomPage } from '../api/classroom'
import {
  getClassSchedule,
  getTeacherSchedule,
  getClassroomSchedule,
  autoSchedule
} from '../api/schedule'

const teacherOptions = ref([])
const classOptions = ref([])
const classroomOptions = ref([])
const loading = ref(false)
const isAutoScheduling = ref(false)
const rawScheduleList = ref([])

const weekdays = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

const sections = [
  { value: 1, label: '第 1 节（上午）' },
  { value: 2, label: '第 2 节（上午）' },
  { value: 3, label: '第 3 节（上午）' },
  { value: 4, label: '第 4 节（上午）' },
  { value: 5, label: '第 5 节（上午）' },
  { value: 6, label: '第 6 节（下午）' },
  { value: 7, label: '第 7 节（下午）' },
  { value: 8, label: '第 8 节（下午）' },
  { value: 9, label: '第 9 节（下午）' },
  { value: 10, label: '第 10 节（晚上）' },
  { value: 11, label: '第 11 节（晚上）' },
  { value: 12, label: '第 12 节（晚上）' }
]

const queryForm = reactive({
  mode: 'class',
  classId: null,
  classroomId: null,
  teacherId: null
})

const hasValidSelection = computed(() => {
  if (queryForm.mode === 'class') return !!queryForm.classId
  if (queryForm.mode === 'classroom') return !!queryForm.classroomId
  if (queryForm.mode === 'teacher') return !!queryForm.teacherId
  return false
})

const fetchOptions = async () => {
  try {
    const [teacherRes, classRes, classroomRes] = await Promise.all([
      getTeacherPage({ pageNum: 1, pageSize: 1000 }),
      getClassPage({ pageNum: 1, pageSize: 1000 }),
      getClassroomPage({ pageNum: 1, pageSize: 1000 })
    ])

    if (teacherRes.data?.code === 1 && teacherRes.data.data) {
      teacherOptions.value = teacherRes.data.data.rows || []
    }
    if (classRes.data?.code === 1 && classRes.data.data) {
      classOptions.value = classRes.data.data.rows || []
    }
    if (classroomRes.data?.code === 1 && classroomRes.data.data) {
      classroomOptions.value = classroomRes.data.data.rows || []
    }
  } catch (e) {
    ElMessage.error('加载班级/教师/教室选项失败')
  }
}

/**
 * 前端聚合去重：后端按周打散（16周=16条），需按 Course_ID + Weekday + Section + 教室 聚合
 * 计算 StartWeek/EndWeek，以及 StartSection/RowSpan（节次跨度，用于跨行合并）
 * @param {Array} rawData 后端返回的排课数组
 * @returns {Array} 去重后，每条含 StartWeek、EndWeek、StartSection、RowSpan
 */
function processScheduleData(rawData) {
  if (!Array.isArray(rawData) || rawData.length === 0) return []
  const map = new Map()
  for (const item of rawData) {
    const w = item.week != null ? Number(item.week) : null
    const sec = item.section != null ? Number(item.section) : 1
    const courseId = item.scheduleCourseId ?? item.courseId ?? ''
    const classroomId = item.scheduleClassroomId ?? item.classroomId ?? ''
    const key = `${courseId}|${classroomId}|${item.weekday ?? ''}|${sec}|${item.courseName ?? ''}|${item.classroomName ?? ''}|${item.className ?? ''}|${item.teacherName ?? ''}`
    if (!key.replace(/\|/g, '')) continue
    const existing = map.get(key)
    if (existing) {
      if (w != null) {
        existing.StartWeek = existing.StartWeek != null ? Math.min(existing.StartWeek, w) : w
        existing.EndWeek = existing.EndWeek != null ? Math.max(existing.EndWeek, w) : w
      }
      existing.MinSection = Math.min(existing.MinSection ?? sec, sec)
      existing.MaxSection = Math.max(existing.MaxSection ?? sec, sec)
    } else {
      const copy = { ...item }
      copy.StartWeek = w
      copy.EndWeek = w
      copy.StartSection = sec
      copy.MinSection = sec
      copy.MaxSection = sec
      map.set(key, copy)
    }
  }
  return Array.from(map.values()).map((item) => {
    const startSec = item.section ?? item.MinSection ?? 1
   const span = (item.courseSingleHour ?? (item.MaxSection && item.MinSection ? item.MaxSection - item.MinSection + 1 : 0)) || 1
    let maxSpan = 12 - startSec + 1
    if (startSec <= 5) maxSpan = Math.min(maxSpan, 6 - startSec)
    else if (startSec <= 9) maxSpan = Math.min(maxSpan, 10 - startSec)
    item.StartSection = startSec
    item.RowSpan = Math.max(1, Math.min(maxSpan, span))
    return item
  })
}

// 网格：每格为 { items: [...], rowspan: N } 或 { skip: true }
const compositeGrid = ref(
  Array.from({ length: 12 }, () =>
    Array.from({ length: 7 }, () => ({ items: [], rowspan: 1, skip: false }))
  )
)

const buildCompositeGrid = (list) => {
  let processed = []
  try {
    processed = processScheduleData(Array.isArray(list) ? list : [])
  } catch (err) {
    console.error('[AutoSchedule] processScheduleData 异常:', err)
    compositeGrid.value = Array.from({ length: 12 }, () =>
      Array.from({ length: 7 }, () => ({ items: [], rowspan: 1, skip: false }))
    )
    return
  }
  const grid = Array.from({ length: 12 }, () =>
    Array.from({ length: 7 }, () => ({ items: [], rowspan: 1, skip: false }))
  )
  processed.forEach((item) => {
    const row = (item.StartSection ?? item.section ?? 1) - 1
    const col = (item.weekday || 0) - 1
    const span = item.RowSpan ?? (item.courseSingleHour || 1)
    if (row < 0 || row >= 12 || col < 0 || col >= 7) return
    const cell = grid[row][col]
    if (cell.skip) return
    cell.items.push(item)
    cell.rowspan = Math.max(cell.rowspan, span)
  })
  for (let r = 0; r < 12; r++) {
    for (let c = 0; c < 7; c++) {
      const cell = grid[r][c]
      const items = cell?.items
      if (Array.isArray(items) && items.length > 0 && (cell.rowspan || 1) > 1) {
        for (let k = 1; k < cell.rowspan; k++) {
          const rr = r + k
          if (rr < 12) grid[rr][c] = { skip: true }
        }
      }
    }
  }
  compositeGrid.value = grid
}

const getCompositeCell = (section, weekday) => {
  const row = section - 1
  const col = weekday - 1
  const r = compositeGrid.value[row]
  if (!r) return { items: [], rowspan: 1, skip: true }
  const cell = r[col]
  if (!cell) return { items: [], rowspan: 1, skip: true }
  return {
    items: Array.isArray(cell.items) ? cell.items : [],
    rowspan: cell.rowspan || 1,
    skip: !!cell.skip
  }
}

/** 动态计算并格式化周次显示，优先用聚合的 StartWeek/EndWeek，否则用 courseStartWeek/courseEndWeek */
function formatWeekRange(item) {
  if (!item) return ''
  const start = item.StartWeek ?? item.courseStartWeek
  const end = item.EndWeek ?? item.courseEndWeek
  if (start != null && end != null) return `${start} - ${end}`
  if (start != null) return String(start)
  if (end != null) return String(end)
  return ''
}

const handleQuery = async () => {
  if (!hasValidSelection.value) {
    buildCompositeGrid([])
    return
  }

  loading.value = true
  try {
    let res
    if (queryForm.mode === 'class') {
      res = await getClassSchedule(queryForm.classId, null)
    } else if (queryForm.mode === 'classroom') {
      res = await getClassroomSchedule(queryForm.classroomId, null)
    } else {
      res = await getTeacherSchedule(queryForm.teacherId, null)
    }

    const body = res?.data
    const code = body?.code ?? body?.status
    const list = Array.isArray(body?.data) ? body.data : (Array.isArray(body) ? body : [])
    if (code === 1 || code === 200) {
      rawScheduleList.value = list
      buildCompositeGrid(list)
    } else {
      rawScheduleList.value = []
      buildCompositeGrid([])
      ElMessage.error(body?.msg || body?.message || '查询课表失败')
    }
  } catch (e) {
    rawScheduleList.value = []
    buildCompositeGrid([])
    const msg = e?.response?.data?.msg || e?.response?.data?.message || e?.message || '请求课表失败'
    console.error('[AutoSchedule] 课表查询异常:', e)
    ElMessage.error(typeof msg === 'string' ? msg : '请求课表失败')
  } finally {
    loading.value = false
  }
}

const handleAutoSchedule = async () => {
  try {
    await ElMessageBox.confirm(
      '自动排课过程可能耗时较长，执行期间请勿重复操作。确认现在开始排课吗？',
      '提示',
      {
        type: 'warning',
        confirmButtonText: '开始排课',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  isAutoScheduling.value = true
  try {
    const res = await autoSchedule()
    const { data } = res || {}

    if (data && data.code === 1) {
      ElMessage.success('排课完成！')
      if (hasValidSelection.value) {
        await handleQuery()
      }
    } else {
      ElMessage.error(data?.msg || '排课失败')
    }
  } catch (err) {
    const msg =
      err?.response?.data?.msg ||
      err?.response?.data?.message ||
      err?.message ||
      '排课失败'
    ElMessage.error(msg)
  } finally {
    isAutoScheduling.value = false
  }
}

const handleClear = () => {
  queryForm.classId = null
  queryForm.classroomId = null
  queryForm.teacherId = null
  buildCompositeGrid([])
}

watch(
  () => queryForm.mode,
  () => {
    queryForm.classId = null
    queryForm.classroomId = null
    queryForm.teacherId = null
    buildCompositeGrid([])
  }
)

onMounted(() => {
  fetchOptions()
})
</script>

<style scoped>
.auto-schedule-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar-card,
.table-card {
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.35);
  padding: 16px 18px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.45);
}

.toolbar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-block h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #f9fafb;
}

.title-block p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #9ca3af;
}

.query-console {
  margin-top: 4px;
}

.search-form {
  margin-top: 0;
}

.grid-loading,
.grid-empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  color: #94a3b8;
}

.loading-icon {
  font-size: 32px;
  margin-bottom: 12px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.grid-wrapper {
  width: 100%;
  overflow-x: auto;
}

.schedule-grid {
  width: 100%;
  table-layout: fixed;
  border-collapse: separate;
  border-spacing: 0;
  min-width: 980px;
  background: transparent;
  border: 1px solid rgba(30, 64, 175, 0.5);
  border-radius: 14px;
  overflow: hidden;
}

.schedule-grid th,
.schedule-grid td {
  border-right: 1px solid rgba(30, 64, 175, 0.35);
  border-bottom: 1px solid rgba(30, 64, 175, 0.35);
  padding: 10px;
  vertical-align: top;
}

.schedule-grid tr:last-child td {
  border-bottom: none;
}

.schedule-grid th:last-child,
.schedule-grid td:last-child {
  border-right: none;
}

.corner-cell {
  width: 140px;
  background: rgba(15, 23, 42, 0.95);
  color: #e5e7eb;
  font-weight: 600;
  text-align: left;
}

.day-cell {
  background: rgba(15, 23, 42, 0.95);
  color: #e5e7eb;
  font-weight: 600;
  text-align: center;
}

.section-cell {
  background: rgba(15, 23, 42, 0.85);
  color: #cbd5e1;
  font-weight: 500;
  width: 140px;
}

.content-cell-composite {
  height: 92px;
  background: rgba(2, 6, 23, 0.35);
  vertical-align: top;
}

.cell-card-stack {
  height: 100%;
  width: 100%;
  box-sizing: border-box;
  border-radius: 12px;
  padding: 10px 10px 8px;
  margin-bottom: 6px;
  background: linear-gradient(
    135deg,
    rgba(29, 78, 216, 0.18),
    rgba(15, 23, 42, 0.35)
  );
  border: 1px solid rgba(148, 163, 184, 0.25);
  box-shadow: 0 10px 24px rgba(2, 6, 23, 0.35);
}

.cell-card-stack:last-of-type {
  margin-bottom: 0;
}

.content-cell-composite > .cell-card-stack:only-of-type {
  height: 100%;
}

.cell-title {
  font-size: 13px;
  font-weight: 600;
  color: #f9fafb;
  line-height: 1.2;
  margin-bottom: 6px;
}

.cell-sub {
  font-size: 12px;
  color: #cbd5e1;
  opacity: 0.9;
  margin-bottom: 4px;
}

.cell-meta {
  font-size: 12px;
  color: #94a3b8;
}

.course-weeks,
.cell-week {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
  opacity: 0.9;
}

.course-weeks {
  display: block;
}

.cell-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  height: 100%;
  color: rgba(148, 163, 184, 0.6);
  font-size: 12px;
}

.break-row .section-cell {
  background: rgba(15, 23, 42, 0.75);
}

.break-cell {
  background: rgba(15, 23, 42, 0.5);
  color: #94a3b8;
  font-size: 12px;
}
</style>
