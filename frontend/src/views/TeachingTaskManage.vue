<template>
  <div class="class-manage-page">
    <!-- 顶部搜索与操作区 -->
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>教学任务管理</h2>
          <p>按教师、课程、班级与状态检索教学任务。</p>
        </div>
        <el-button type="primary" class="add-btn" @click="handleOpenAddDialog">
          新增教学任务
        </el-button>
      </div>

      <el-form
        class="search-form"
        :inline="true"
        :model="searchForm"
        label-width="72px"
      >
        <el-form-item label="授课教师">
          <el-select
            v-model="searchForm.teacherId"
            placeholder="请选择教师"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="item in teacherOptions"
              :key="item.teacherId"
              :label="item.teacherName"
              :value="item.teacherId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="授课课程">
          <el-select
            v-model="searchForm.courseId"
            placeholder="请选择课程"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.courseId"
              :label="item.courseName"
              :value="item.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上课班级">
          <el-select
            v-model="searchForm.classId"
            placeholder="请选择班级"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option
              v-for="item in classOptions"
              :key="item.classId"
              :label="item.className"
              :value="item.classId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select
            v-model="searchForm.taskState"
            placeholder="请选择状态"
            clearable
            style="width: 160px"
          >
            <el-option label="未排课" value="未排课" />
            <el-option label="已排课" value="已排课" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <!-- 表格区 -->
    <section class="table-card">
      <el-table
        v-loading="tableLoading"
        :data="tableData"
        border
        size="small"
        header-row-class-name="table-header-row"
        class="class-table"
      >
        <el-table-column prop="taskId" label="任务编号" width="120" />
        <el-table-column
          prop="teacherName"
          label="授课教师"
          min-width="140"
        />
        <el-table-column
          prop="courseName"
          label="授课课程"
          min-width="180"
        />
        <el-table-column
          prop="className"
          label="上课班级"
          min-width="180"
        />
        <el-table-column prop="taskState" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getTaskStateTagType(row.taskState)">
              {{ row.taskState || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleOpenEditDialog(row)"
            >
              编辑
            </el-button>
            <el-popconfirm
              title="确认删除该教学任务？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              confirm-button-type="danger"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pagination.pageSize"
          :current-page="pagination.pageNum"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </section>

    <!-- 新增/编辑教学任务弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      width="520px"
      :close-on-click-modal="false"
      :title="dialogTitle"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="96px"
        class="dialog-form"
      >
        <el-form-item label="授课教师" prop="teacherId">
          <el-select
            v-model="dialogForm.teacherId"
            placeholder="请选择授课教师"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in teacherOptions"
              :key="item.teacherId"
              :label="item.teacherName"
              :value="item.teacherId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="授课课程" prop="courseId">
          <el-select
            v-model="dialogForm.courseId"
            placeholder="请选择授课课程"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.courseId"
              :label="item.courseName"
              :value="item.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上课班级" prop="classId">
          <el-select
            v-model="dialogForm.classId"
            placeholder="请选择上课班级"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in classOptions"
              :key="item.classId"
              :label="item.className"
              :value="item.classId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态" prop="taskState">
          <el-select
            v-model="dialogForm.taskState"
            placeholder="请选择任务状态"
            style="width: 100%"
          >
            <el-option label="未排课" value="未排课" />
            <el-option label="已排课" value="已排课" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="dialogSubmitting" @click="handleSubmit">
            确 定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTeachingTaskPage,
  addTeachingTask,
  updateTeachingTask,
  deleteTeachingTask
} from '../api/teachingTask'
import { getTeacherPage } from '../api/teacher'
import { getCoursePage } from '../api/course'
import { getClassPage } from '../api/class'

const tableData = ref([])
const tableLoading = ref(false)

// 下拉选项数据
const teacherOptions = ref([])
const courseOptions = ref([])
const classOptions = ref([])

const searchForm = reactive({
  teacherId: null,
  courseId: null,
  classId: null,
  taskState: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogSubmitting = ref(false)
const dialogFormRef = ref(null)
const isEdit = ref(false)

const dialogForm = reactive({
  taskId: null,
  teacherId: null,
  courseId: null,
  classId: null,
  taskState: '未排课'
})

const dialogRules = {
  teacherId: [
    { required: true, message: '请选择授课教师', trigger: 'change' }
  ],
  courseId: [
    { required: true, message: '请选择授课课程', trigger: 'change' }
  ],
  classId: [
    { required: true, message: '请选择上课班级', trigger: 'change' }
  ],
  taskState: [
    { required: true, message: '请选择任务状态', trigger: 'change' }
  ]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑教学任务' : '新增教学任务'))

const getTaskStateTagType = (state) => {
  if (state === '已排课') return 'success'
  if (state === '未排课') return 'warning'
  return ''
}

// 分页查询教学任务
const fetchTeachingTaskPage = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      teacherId: searchForm.teacherId || undefined,
      courseId: searchForm.courseId || undefined,
      classId: searchForm.classId || undefined,
      taskState: searchForm.taskState || undefined
    }
    const { data } = await getTeachingTaskPage(params)
    if (data && data.code === 1 && data.data) {
      tableData.value = data.data.rows || []
      pagination.total = data.data.total || 0
    } else {
      ElMessage.error(data?.msg || '获取教学任务列表失败')
    }
  } catch (error) {
    ElMessage.error('请求教学任务列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 拉取教师 / 课程 / 班级下拉数据（pageSize=1000 模拟全量）
const fetchSelectOptions = async () => {
  try {
    const [teacherRes, courseRes, classRes] = await Promise.all([
      getTeacherPage({ pageNum: 1, pageSize: 1000 }),
      getCoursePage({ pageNum: 1, pageSize: 1000 }),
      getClassPage({ pageNum: 1, pageSize: 1000 })
    ])

    if (teacherRes.data?.code === 1 && teacherRes.data.data) {
      teacherOptions.value = teacherRes.data.data.rows || []
    }
    if (courseRes.data?.code === 1 && courseRes.data.data) {
      courseOptions.value = courseRes.data.data.rows || []
    }
    if (classRes.data?.code === 1 && classRes.data.data) {
      classOptions.value = classRes.data.data.rows || []
    }
  } catch (error) {
    ElMessage.error('加载下拉选项失败，请稍后重试')
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchTeachingTaskPage()
}

const handleReset = () => {
  searchForm.teacherId = null
  searchForm.courseId = null
  searchForm.classId = null
  searchForm.taskState = ''
  pagination.pageNum = 1
  fetchTeachingTaskPage()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchTeachingTaskPage()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchTeachingTaskPage()
}

const resetDialogForm = () => {
  dialogForm.taskId = null
  dialogForm.teacherId = null
  dialogForm.courseId = null
  dialogForm.classId = null
  dialogForm.taskState = '未排课'
  if (dialogFormRef.value) {
    dialogFormRef.value.clearValidate()
  }
}

const handleOpenAddDialog = () => {
  isEdit.value = false
  resetDialogForm()
  dialogVisible.value = true
}

const handleOpenEditDialog = (row) => {
  if (!row) return
  isEdit.value = true
  dialogForm.taskId = row.taskId
  dialogForm.teacherId = row.teacherId
  dialogForm.courseId = row.courseId
  dialogForm.classId = row.classId
  dialogForm.taskState = row.taskState || '未排课'
  if (dialogFormRef.value) {
    dialogFormRef.value.clearValidate()
  }
  dialogVisible.value = true
}

const handleSubmit = () => {
  if (!dialogFormRef.value) return
  dialogFormRef.value.validate(async (valid) => {
    if (!valid) return
    dialogSubmitting.value = true
    try {
      const payload = {
        taskId: dialogForm.taskId,
        teacherId: dialogForm.teacherId,
        courseId: dialogForm.courseId,
        classId: dialogForm.classId,
        taskState: dialogForm.taskState
      }

      let res
      if (isEdit.value) {
        res = await updateTeachingTask(payload)
      } else {
        // 新增时不应携带 taskId
        // eslint-disable-next-line no-unused-vars
        const { taskId, ...createPayload } = payload
        res = await addTeachingTask(createPayload)
      }
      const { data } = res

      if (data && data.code === 1) {
        ElMessage.success(isEdit.value ? '更新教学任务成功' : '新增教学任务成功')
        dialogVisible.value = false
        if (!isEdit.value) {
          pagination.pageNum = 1
        }
        fetchTeachingTaskPage()
      } else {
        ElMessage.error(data?.msg || (isEdit.value ? '更新教学任务失败' : '新增教学任务失败'))
      }
    } catch (error) {
      ElMessage.error(isEdit.value ? '请求更新教学任务失败' : '请求新增教学任务失败')
    } finally {
      dialogSubmitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  if (!row || !row.taskId) return
  try {
    const { data } = await deleteTeachingTask(row.taskId)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && pagination.pageNum > 1) {
        pagination.pageNum -= 1
      }
      fetchTeachingTaskPage()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除请求失败')
  }
}

onMounted(() => {
  fetchSelectOptions()
  fetchTeachingTaskPage()
})
</script>

<style scoped>
.class-manage-page {
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
  margin-bottom: 12px;
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

.add-btn {
  border-radius: 999px;
}

.search-form {
  margin-top: 4px;
}

.class-table {
  margin-top: 4px;
  --el-table-bg-color: transparent;
  --el-table-header-bg-color: rgba(15, 23, 42, 0.9);
  --el-table-tr-bg-color: transparent;
  --el-table-border-color: rgba(30, 64, 175, 0.5);
}

.table-header-row th {
  background-color: rgba(15, 23, 42, 0.95) !important;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.dialog-form {
  padding-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

