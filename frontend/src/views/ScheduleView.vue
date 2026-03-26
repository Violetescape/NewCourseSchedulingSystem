<template>
  <div class="schedule-page">
    <!-- 顶部操作区 -->
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>课表可视化</h2>
          <p>按教学周查询并以网格方式呈现课表。</p>
        </div>
      </div>

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
            >
              <el-option
                v-for="item in classOptions"
                :key="item.classId"
                :label="item.className"
                :value="item.classId"
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
            >
              <el-option
                v-for="item in teacherOptions"
                :key="item.teacherId"
                :label="item.teacherName"
                :value="item.teacherId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="教学周">
            <el-select
              v-model="queryForm.week"
              placeholder="请选择教学周"
              style="width: 160px"
            >
              <el-option
                v-for="w in weekOptions"
                :key="w"
                :label="`第 ${w} 周`"
                :value="w"
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

    <!-- 二维课表网格 -->
    <section class="table-card">
      <div class="grid-wrapper">
        <table class="schedule-grid">
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
                  v-if="!getCell(sec.value - 1, day.value - 1)?.skip"
                  class="content-cell"
                  :rowspan="getCell(sec.value - 1, day.value - 1)?.rowspan || 1"
                >
                  <div
                    v-if="getCell(sec.value - 1, day.value - 1)"
                    class="cell-card"
                  >
                    <div class="cell-title">
                      {{
                        getCell(sec.value - 1, day.value - 1).courseName || '-'
                      }}
                    </div>
                    <div class="cell-sub">
                      {{
                        getCell(sec.value - 1, day.value - 1).classroomName ||
                          '-'
                      }}
                    </div>
                    <div class="cell-meta">
                      <span v-if="queryForm.mode === 'class'">
                        {{
                          getCell(sec.value - 1, day.value - 1).teacherName ||
                            '-'
                        }}
                      </span>
                      <span v-else>
                        {{
                          getCell(sec.value - 1, day.value - 1).className || '-'
                        }}
                      </span>
                    </div>
                    <div
                      v-if="getCourseWeeksText(getCell(sec.value - 1, day.value - 1))"
                      class="course-weeks"
                    >
                      [第 {{ getCourseWeeksText(getCell(sec.value - 1, day.value - 1)) }} 周]
                    </div>
                  </div>
                  <div v-else class="cell-empty">—</div>
                </td>
              </template>
            </tr>

            <!-- 午休行 -->
            <tr class="break-row">
              <td class="section-cell break-label">午休</td>
              <td class="break-cell" colspan="7">午休 · 休息时间</td>
            </tr>

            <!-- 下午 6-9 节 -->
            <tr v-for="sec in sections.slice(5, 9)" :key="sec.value">
              <td class="section-cell">{{ sec.label }}</td>
              <template v-for="day in weekdays" :key="`${sec.value}-${day.value}`">
                <td
                  v-if="!getCell(sec.value - 1, day.value - 1)?.skip"
                  class="content-cell"
                  :rowspan="getCell(sec.value - 1, day.value - 1)?.rowspan || 1"
                >
                  <div
                    v-if="getCell(sec.value - 1, day.value - 1)"
                    class="cell-card"
                  >
                    <div class="cell-title">
                      {{
                        getCell(sec.value - 1, day.value - 1).courseName || '-'
                      }}
                    </div>
                    <div class="cell-sub">
                      {{
                        getCell(sec.value - 1, day.value - 1).classroomName ||
                          '-'
                      }}
                    </div>
                    <div class="cell-meta">
                      <span v-if="queryForm.mode === 'class'">
                        {{
                          getCell(sec.value - 1, day.value - 1).teacherName ||
                            '-'
                        }}
                      </span>
                      <span v-else>
                        {{
                          getCell(sec.value - 1, day.value - 1).className || '-'
                        }}
                      </span>
                    </div>
                    <div
                      v-if="getCourseWeeksText(getCell(sec.value - 1, day.value - 1))"
                      class="course-weeks"
                    >
                      [第 {{ getCourseWeeksText(getCell(sec.value - 1, day.value - 1)) }} 周]
                    </div>
                  </div>
                  <div v-else class="cell-empty">—</div>
                </td>
              </template>
            </tr>

            <!-- 晚饭行 -->
            <tr class="break-row">
              <td class="section-cell break-label">晚饭</td>
              <td class="break-cell" colspan="7">晚饭 · 休息时间</td>
            </tr>

            <!-- 晚上 10-12 节 -->
            <tr v-for="sec in sections.slice(9, 12)" :key="sec.value">
              <td class="section-cell">{{ sec.label }}</td>
              <template v-for="day in weekdays" :key="`${sec.value}-${day.value}`">
                <td
                  v-if="!getCell(sec.value - 1, day.value - 1)?.skip"
                  class="content-cell"
                  :rowspan="getCell(sec.value - 1, day.value - 1)?.rowspan || 1"
                >
                  <div
                    v-if="getCell(sec.value - 1, day.value - 1)"
                    class="cell-card"
                  >
                    <div class="cell-title">
                      {{
                        getCell(sec.value - 1, day.value - 1).courseName || '-'
                      }}
                    </div>
                    <div class="cell-sub">
                      {{
                        getCell(sec.value - 1, day.value - 1).classroomName ||
                          '-'
                      }}
                    </div>
                    <div class="cell-meta">
                      <span v-if="queryForm.mode === 'class'">
                        {{
                          getCell(sec.value - 1, day.value - 1).teacherName ||
                            '-'
                        }}
                      </span>
                      <span v-else>
                        {{
                          getCell(sec.value - 1, day.value - 1).className || '-'
                        }}
                      </span>
                    </div>
                    <div
                      v-if="getCourseWeeksText(getCell(sec.value - 1, day.value - 1))"
                      class="course-weeks"
                    >
                      [第 {{ getCourseWeeksText(getCell(sec.value - 1, day.value - 1)) }} 周]
                    </div>
                  </div>
                  <div v-else class="cell-empty">—</div>
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
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeacherPage } from '../api/teacher'
import { getClassPage } from '../api/class'
import { getClassSchedule, getTeacherSchedule } from '../api/schedule'

const teacherOptions = ref([])
const classOptions = ref([])
const loading = ref(false)

const weekOptions = Array.from({ length: 20 }, (_, i) => i + 1)

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
  teacherId: null,
  week: 1
})

const createEmptyGrid = () => {
  return Array.from({ length: 12 }, () =>
    Array.from({ length: 7 }, () => null)
  )
}

const grid = ref(createEmptyGrid())

const clearGrid = () => {
  grid.value = createEmptyGrid()
}

const fetchOptions = async () => {
  try {
    const [teacherRes, classRes] = await Promise.all([
      getTeacherPage({ pageNum: 1, pageSize: 1000 }),
      getClassPage({ pageNum: 1, pageSize: 1000 })
    ])

    if (teacherRes.data?.code === 1 && teacherRes.data.data) {
      teacherOptions.value = teacherRes.data.data.rows || []
    }
    if (classRes.data?.code === 1 && classRes.data.data) {
      classOptions.value = classRes.data.data.rows || []
    }
  } catch (e) {
    ElMessage.error('加载班级/教师选项失败')
  }
}

const fillGrid = (list) => {
  clearGrid()
  ;(list || []).forEach((item) => {
    const startRow = (item.section || 0) - 1
    const col = (item.weekday || 0) - 1
    const span = item.courseSingleHour || 1

    if (startRow < 0 || startRow >= 12 || col < 0 || col >= 7) return

    for (let offset = 0; offset < span; offset++) {
      const r = startRow + offset
      if (r >= 12) break
      if (offset === 0) {
        grid.value[r][col] = {
          ...item,
          rowspan: span
        }
      } else {
        grid.value[r][col] = { skip: true }
      }
    }
  })
}

const getCell = (row, col) => {
  const r = grid.value[row]
  if (!r) return null
  return r[col] || null
}

/** 获取周次显示文本，优先 courseStartWeek/courseEndWeek，支持 StartWeek/EndWeek */
function getCourseWeeksText(item) {
  if (!item || item.skip) return ''
  const s = item.courseStartWeek ?? item.StartWeek
  const e = item.courseEndWeek ?? item.EndWeek
  if (s != null && e != null) return `${s}-${e}`
  if (s != null) return String(s)
  if (e != null) return String(e)
  return ''
}

const handleQuery = async () => {
  if (queryForm.mode === 'class' && !queryForm.classId) {
    ElMessage.warning('请选择班级')
    return
  }
  if (queryForm.mode === 'teacher' && !queryForm.teacherId) {
    ElMessage.warning('请选择教师')
    return
  }

  loading.value = true
  try {
    let res
    if (queryForm.mode === 'class') {
      res = await getClassSchedule(queryForm.classId, queryForm.week)
    } else {
      res = await getTeacherSchedule(queryForm.teacherId, queryForm.week)
    }

    const { data } = res
    if (data && data.code === 1) {
      fillGrid(data.data || [])
    } else {
      clearGrid()
      ElMessage.error(data?.msg || '查询课表失败')
    }
  } catch (e) {
    clearGrid()
    ElMessage.error('请求课表失败')
  } finally {
    loading.value = false
  }
}

const handleClear = () => {
  queryForm.classId = null
  queryForm.teacherId = null
  queryForm.week = 1
  clearGrid()
}

// 模式切换时清空网格，并清理另一侧选择
watch(
  () => queryForm.mode,
  (mode) => {
    clearGrid()
    if (mode === 'class') {
      queryForm.teacherId = null
    } else {
      queryForm.classId = null
    }
  }
)

// 教学周变更时清空网格（避免显示旧数据）
watch(
  () => queryForm.week,
  () => {
    clearGrid()
  }
)

onMounted(() => {
  fetchOptions()
})
</script>

<style scoped>
.schedule-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar-card,
.table-card {
  border-radius: 8px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  padding: 16px 18px;
  box-shadow: var(--el-box-shadow-light);
}

.toolbar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
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
  color: var(--el-text-color-primary);
}

.title-block p {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.query-console {
  margin-top: 4px;
}

.search-form {
  margin-top: 0;
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
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
  overflow: hidden;
}

.schedule-grid th,
.schedule-grid td {
  border-right: 1px solid var(--el-border-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);
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
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-weight: 600;
  text-align: left;
}

.day-cell {
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-weight: 600;
  text-align: center;
}

.section-cell {
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-weight: 500;
  width: 140px;
}

.content-cell {
  min-height: 92px;
  background: var(--el-fill-color-blank);
  vertical-align: top;
}

.cell-card {
  width: 100%;
  box-sizing: border-box;
  border-radius: 8px;
  padding: 10px 10px 8px;
  margin-bottom: 6px;
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-5);
  box-shadow: none;
}

.cell-card:last-of-type {
  margin-bottom: 0;
}

.cell-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  line-height: 1.2;
  margin-bottom: 6px;
}

.cell-sub {
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin-bottom: 4px;
}

.cell-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.course-weeks,
.cell-week {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.course-weeks {
  display: block;
}

.cell-empty {
  min-height: 48px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}
</style>

