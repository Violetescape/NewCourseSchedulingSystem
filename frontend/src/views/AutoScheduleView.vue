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
          <el-button
            id="btn-reset-schedule"
            type="danger"
            plain
            :loading="isResettingSchedule"
            @click="handleResetSchedule"
          >
            清空并重置课表
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

    <!-- 未排课任务与人机协同手动编排 -->
    <section class="task-panel-card">
      <div class="task-panel-header">
        <div>
          <h3>手动排课</h3>
          <p class="task-panel-desc">
            对「未排课」任务可手动指定教学周、星期、节次与教室；校验逻辑与自动排课一致。可将任务拖到下方空时间格快速打开编排窗口。
          </p>
        </div>
        <el-button text type="primary" :loading="taskPanelLoading" @click="refreshTaskPanel">
          刷新任务与统计
        </el-button>
      </div>
      <div class="task-stats">
        <span>未排课：<strong>{{ taskStats.unscheduled }}</strong></span>
        <span>已排课：<strong>{{ taskStats.scheduled }}</strong></span>
        <span>排课失败：<strong>{{ taskStats.failed }}</strong></span>
      </div>
      <div v-loading="taskPanelLoading" class="task-table-wrap">
        <table v-if="unscheduledTasks.length" class="task-table-native">
          <thead>
            <tr>
              <th>任务 ID</th>
              <th>课程</th>
              <th>教师</th>
              <th>班级</th>
              <th>周期（默认）</th>
              <th>连堂</th>
              <th class="col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in unscheduledTasks"
              :key="row.taskId"
              class="task-draggable-row"
              draggable="true"
              @dragstart="onTaskDragStart(row, $event)"
            >
              <td>{{ row.taskId }}</td>
              <td>{{ row.courseName || '-' }}</td>
              <td>{{ row.teacherName || '-' }}</td>
              <td>{{ row.className || '-' }}</td>
              <td>{{ row.courseStartWeek ?? '-' }} ~ {{ row.courseEndWeek ?? '-' }} 周</td>
              <td>{{ row.courseSingleHour ?? 1 }} 节</td>
              <td class="col-action">
                <el-button type="primary" link size="small" @click="openManualDialog(row)">
                  手动编排
                </el-button>
              </td>
            </tr>
          </tbody>
        </table>
        <el-empty v-else description="暂无未排课任务" :image-size="80" />
      </div>
      <div class="drag-hint">提示：在下方课表查询有效时，可将本列表中的任务（行）拖拽到空白格。</div>
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
                  :class="{ 'grid-drop-target': isGridDropTarget(sec.value, day.value) }"
                  :rowspan="getCompositeCell(sec.value, day.value).rowspan || 1"
                  @dragover.prevent="onGridDragOver"
                  @drop.prevent="onGridDrop(sec.value, day.value, $event)"
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
                  :class="{ 'grid-drop-target': isGridDropTarget(sec.value, day.value) }"
                  :rowspan="getCompositeCell(sec.value, day.value).rowspan || 1"
                  @dragover.prevent="onGridDragOver"
                  @drop.prevent="onGridDrop(sec.value, day.value, $event)"
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
                  :class="{ 'grid-drop-target': isGridDropTarget(sec.value, day.value) }"
                  :rowspan="getCompositeCell(sec.value, day.value).rowspan || 1"
                  @dragover.prevent="onGridDragOver"
                  @drop.prevent="onGridDrop(sec.value, day.value, $event)"
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

    <el-dialog
      v-model="manualVisible"
      title="手动编排排课"
      width="520px"
      destroy-on-close
      @closed="onManualDialogClosed"
    >
      <div v-if="manualTask" class="manual-task-summary">
        <span>{{ manualTask.courseName }}</span>
        <span class="sep">·</span>
        <span>{{ manualTask.teacherName }}</span>
        <span class="sep">·</span>
        <span>{{ manualTask.className }}</span>
      </div>
      <el-form label-width="100px" class="manual-form">
        <el-form-item label="起始教学周">
          <el-select v-model="manualForm.startWeek" placeholder="周次" filterable style="width: 100%">
            <el-option v-for="w in weekOptions" :key="'sw-' + w" :label="'第 ' + w + ' 周'" :value="w" />
          </el-select>
        </el-form-item>
        <el-form-item label="结束教学周">
          <el-select v-model="manualForm.endWeek" placeholder="周次" filterable style="width: 100%">
            <el-option v-for="w in weekOptions" :key="'ew-' + w" :label="'第 ' + w + ' 周'" :value="w" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="manualForm.weekday" placeholder="星期" style="width: 100%">
            <el-option v-for="d in weekdays" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="起始节次">
          <el-select v-model="manualForm.section" placeholder="节次" style="width: 100%">
            <el-option v-for="s in sections" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室">
          <el-select
            v-model="manualForm.classroomId"
            placeholder="请选择教室"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in filteredManualClassrooms"
              :key="item.classroomId"
              :label="`${item.classroomName}（容量 ${item.classroomCap ?? '-'}）`"
              :value="item.classroomId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div v-if="manualValidateHint" class="manual-validate" :class="manualValidateHint.ok ? 'is-ok' : 'is-bad'">
        {{ manualValidateHint.ok ? manualValidateHint.message : '冲突：' + manualValidateHint.message }}
      </div>
      <div v-else-if="manualForm.classroomId && manualValidateLoading" class="manual-validate is-loading">
        校验中…
      </div>
      <template #footer>
        <el-button @click="manualVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="manualSubmitting"
          :disabled="!canSubmitManual"
          @click="submitManualSchedule"
        >
          确认排课
        </el-button>
      </template>
    </el-dialog>
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
  autoSchedule,
  resetAutoScheduling,
  validateManualSchedule,
  manualSchedule as submitManualScheduleApi
} from '../api/schedule'
import { getTeachingTaskPage } from '../api/teachingTask'

const teacherOptions = ref([])
const classOptions = ref([])
const classroomOptions = ref([])
const loading = ref(false)
const isAutoScheduling = ref(false)
const isResettingSchedule = ref(false)
const rawScheduleList = ref([])

const taskPanelLoading = ref(false)
const taskStats = reactive({ unscheduled: 0, scheduled: 0, failed: 0 })
const unscheduledTasks = ref([])
const manualVisible = ref(false)
const manualTask = ref(null)
const manualForm = reactive({
  startWeek: 1,
  endWeek: 20,
  weekday: 1,
  section: 1,
  classroomId: null
})
const manualValidateHint = ref(null)
const manualValidateLoading = ref(false)
const manualSubmitting = ref(false)
let manualValidateTimer = null

const weekdays = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

const weekOptions = Array.from({ length: 20 }, (_, i) => i + 1)

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

const filteredManualClassrooms = computed(() => {
  const task = manualTask.value
  const list = classroomOptions.value || []
  if (!task) return list
  const n = task.classNum ?? 0
  const req = task.requiredClassroomType
  return list.filter((r) => {
    if (r.classroomState != null && r.classroomState !== '' && r.classroomState !== '可用') {
      return false
    }
    if (r.classroomCap != null && r.classroomCap < n) return false
    if (req && r.classroomType && r.classroomType !== req) return false
    return true
  })
})

const canSubmitManual = computed(() => {
  if (!manualTask.value || manualSubmitting.value) return false
  if (
    manualForm.classroomId == null ||
    manualForm.weekday == null ||
    manualForm.section == null
  ) {
    return false
  }
  if (!manualForm.startWeek || !manualForm.endWeek) return false
  if (manualForm.startWeek > manualForm.endWeek) return false
  return manualValidateHint.value?.ok === true
})

async function refreshTaskPanel() {
  taskPanelLoading.value = true
  try {
    await Promise.all([fetchTaskStats(), fetchUnscheduledTasks()])
  } finally {
    taskPanelLoading.value = false
  }
}

async function fetchTaskStats() {
  try {
    const [r1, r2, r3] = await Promise.all([
      getTeachingTaskPage({ pageNum: 1, pageSize: 1, taskState: '未排课' }),
      getTeachingTaskPage({ pageNum: 1, pageSize: 1, taskState: '已排课' }),
      getTeachingTaskPage({ pageNum: 1, pageSize: 1, taskState: '排课失败' })
    ])
    const pickTotal = (res) =>
      res?.data?.code === 1 && res.data.data != null ? Number(res.data.data.total ?? 0) : 0
    taskStats.unscheduled = pickTotal(r1)
    taskStats.scheduled = pickTotal(r2)
    taskStats.failed = pickTotal(r3)
  } catch {
    taskStats.unscheduled = 0
    taskStats.scheduled = 0
    taskStats.failed = 0
  }
}

async function fetchUnscheduledTasks() {
  try {
    const res = await getTeachingTaskPage({ pageNum: 1, pageSize: 500, taskState: '未排课' })
    if (res?.data?.code === 1 && res.data.data?.rows) {
      unscheduledTasks.value = res.data.data.rows
    } else {
      unscheduledTasks.value = []
    }
  } catch {
    unscheduledTasks.value = []
  }
}

function openManualDialog(row, preset = null) {
  manualTask.value = row
  manualForm.startWeek = row.courseStartWeek ?? 1
  manualForm.endWeek = row.courseEndWeek ?? 20
  manualForm.weekday = preset?.weekday ?? 1
  manualForm.section = preset?.section ?? 1
  manualForm.classroomId = null
  manualValidateHint.value = null
  manualVisible.value = true
}

function onManualDialogClosed() {
  manualTask.value = null
  manualValidateHint.value = null
}

function scheduleManualValidate() {
  clearTimeout(manualValidateTimer)
  manualValidateTimer = setTimeout(() => {
    runManualValidate()
  }, 380)
}

async function runManualValidate() {
  if (!manualVisible.value || !manualTask.value) return
  if (manualForm.classroomId == null) {
    manualValidateHint.value = null
    return
  }
  manualValidateLoading.value = true
  manualValidateHint.value = null
  try {
    const res = await validateManualSchedule({
      taskId: manualTask.value.taskId,
      classroomId: manualForm.classroomId,
      weekday: manualForm.weekday,
      section: manualForm.section,
      startWeek: manualForm.startWeek,
      endWeek: manualForm.endWeek
    })
    const body = res?.data
    if (body?.code === 1 && body.data) {
      manualValidateHint.value = {
        ok: !!body.data.ok,
        message: body.data.message || ''
      }
    } else {
      manualValidateHint.value = { ok: false, message: body?.msg || '校验失败' }
    }
  } catch (e) {
    const msg = e?.response?.data?.msg || e?.message || '校验请求失败'
    manualValidateHint.value = {
      ok: false,
      message: typeof msg === 'string' ? msg : '校验请求失败'
    }
  } finally {
    manualValidateLoading.value = false
  }
}

watch(
  manualForm,
  () => {
    if (manualVisible.value) scheduleManualValidate()
  },
  { deep: true }
)

watch(manualVisible, (v) => {
  if (v && manualTask.value) scheduleManualValidate()
})

async function submitManualSchedule() {
  if (!canSubmitManual.value || !manualTask.value) return
  manualSubmitting.value = true
  try {
    const res = await submitManualScheduleApi({
      taskId: manualTask.value.taskId,
      classroomId: manualForm.classroomId,
      weekday: manualForm.weekday,
      section: manualForm.section,
      startWeek: manualForm.startWeek,
      endWeek: manualForm.endWeek
    })
    if (res?.data?.code === 1) {
      ElMessage.success('手动排课成功')
      manualVisible.value = false
      await refreshTaskPanel()
      if (hasValidSelection.value) {
        await handleQuery()
      }
    } else {
      ElMessage.error(res?.data?.msg || '排课失败')
    }
  } catch (e) {
    const msg = e?.response?.data?.msg || e?.message || '排课失败'
    ElMessage.error(typeof msg === 'string' ? msg : '排课失败')
  } finally {
    manualSubmitting.value = false
  }
}

function isGridDropTarget(sec, weekday) {
  if (!hasValidSelection.value || loading.value) return false
  const cell = getCompositeCell(sec, weekday)
  if (cell.skip || (cell.items && cell.items.length > 0)) return false
  return true
}

function onGridDragOver() {}

function onGridDrop(sec, weekday, ev) {
  let raw = ''
  try {
    raw = ev.dataTransfer?.getData('application/json') || ''
  } catch {
    return
  }
  if (!raw) return
  let payload
  try {
    payload = JSON.parse(raw)
  } catch {
    return
  }
  const tid = payload.taskId
  const row = unscheduledTasks.value.find((t) => t.taskId === tid)
  if (!row) {
    ElMessage.warning('请从上方「未排课」列表拖拽任务')
    return
  }
  if (!isGridDropTarget(sec, weekday)) {
    ElMessage.warning('该时间格已有课或不可放置')
    return
  }
  openManualDialog(row, { weekday, section: sec })
}

function onTaskDragStart(row, ev) {
  try {
    ev.dataTransfer?.setData('application/json', JSON.stringify({ taskId: row.taskId }))
    ev.dataTransfer.effectAllowed = 'copy'
  } catch {
    /* ignore */
  }
}

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
      await refreshTaskPanel()
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

const handleResetSchedule = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将清空全校所有已排课表并将任务恢复为未排课状态，是否继续？',
      '危险操作',
      {
        type: 'warning',
        confirmButtonText: '确认清空',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch {
    return
  }

  isResettingSchedule.value = true
  try {
    const res = await resetAutoScheduling()
    const { data } = res || {}

    if (data && data.code === 1) {
      ElMessage.success('课表已清空，全部任务已恢复为未排课')
      await refreshTaskPanel()
      if (hasValidSelection.value) {
        await handleQuery()
      } else {
        buildCompositeGrid([])
      }
    } else {
      ElMessage.error(data?.msg || '重置失败')
    }
  } catch (err) {
    const msg =
      err?.response?.data?.msg ||
      err?.response?.data?.message ||
      err?.message ||
      '重置失败'
    ElMessage.error(msg)
  } finally {
    isResettingSchedule.value = false
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
  refreshTaskPanel()
})
</script>

<style scoped>
.toolbar-header {
  margin-bottom: 16px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
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
  color: var(--el-text-color-secondary);
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

.content-cell-composite {
  height: 92px;
  background: var(--el-fill-color-blank);
  vertical-align: top;
}

.cell-card-stack {
  height: 100%;
  width: 100%;
  box-sizing: border-box;
  border-radius: 8px;
  padding: 10px 10px 8px;
  margin-bottom: 6px;
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-5);
  box-shadow: none;
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
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  height: 100%;
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}

.break-row .section-cell {
  background: var(--el-fill-color-light);
}

.break-cell {
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.task-panel-card {
  margin-bottom: 16px;
  padding: 16px 18px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
}

.task-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.task-panel-header h3 {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
}

.task-panel-desc {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  max-width: 720px;
}

.task-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 24px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.task-stats strong {
  color: var(--el-color-primary);
  font-weight: 600;
}

.task-table-wrap {
  min-height: 80px;
}

.task-table-native {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.task-table-native th,
.task-table-native td {
  border: 1px solid var(--el-border-color-lighter);
  padding: 8px 10px;
  text-align: left;
}

.task-table-native th {
  background: var(--el-fill-color-light);
  font-weight: 600;
}

.task-draggable-row {
  cursor: grab;
}

.task-draggable-row:active {
  cursor: grabbing;
}

.task-table-native .col-action {
  white-space: nowrap;
  width: 112px;
}

.drag-hint {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.grid-drop-target {
  box-shadow: inset 0 0 0 2px var(--el-color-primary-light-5);
}

.manual-task-summary {
  margin-bottom: 14px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.manual-task-summary .sep {
  margin: 0 6px;
  opacity: 0.45;
}

.manual-form {
  margin-top: 4px;
}

.manual-validate {
  margin-top: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.45;
}

.manual-validate.is-ok {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success);
  border: 1px solid var(--el-color-success-light-5);
}

.manual-validate.is-bad {
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
  border: 1px solid var(--el-color-danger-light-5);
}

.manual-validate.is-loading {
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}
</style>
