<template>
  <div class="class-manage-page">
    <!-- 顶部搜索与操作区 -->
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>课程管理</h2>
          <p>按课程名称与类型快速检索课程信息。</p>
        </div>
        <el-button type="primary" class="add-btn" @click="handleOpenAddDialog">
          添加课程
        </el-button>
      </div>

      <el-form
        class="search-form"
        :inline="true"
        :model="searchForm"
        label-width="72px"
      >
        <el-form-item label="课程名称">
          <el-input
            v-model="searchForm.courseName"
            placeholder="请输入课程名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select
            v-model="searchForm.courseType"
            placeholder="请选择课程类型"
            clearable
            style="width: 160px"
          >
            <el-option :value="1" label="必修课" />
            <el-option :value="2" label="专业选修" />
            <el-option :value="3" label="公选课" />
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
        <el-table-column prop="courseId" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column
          prop="courseType"
          label="课程类型"
          width="120"
        >
          <template #default="{ row }">
            <span>
              {{
                row.courseType === 1
                  ? '必修课'
                  : row.courseType === 2
                    ? '专业选修'
                    : row.courseType === 3
                      ? '公选课'
                      : '-'
              }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="courseStartWeek"
          label="起始周"
          width="90"
        />
        <el-table-column
          prop="courseEndWeek"
          label="结束周"
          width="90"
        />
        <el-table-column
          prop="courseSingleHour"
          label="单次课时"
          width="100"
        />
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
              title="确认删除该课程？"
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

    <!-- 新增/编辑课程弹窗 -->
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
        <el-form-item label="课程编号" prop="courseId">
          <el-input
            v-model.number="dialogForm.courseId"
            :disabled="isEdit"
            placeholder="请输入课程编号（需手动输入）"
          />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="dialogForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select
            v-model="dialogForm.courseType"
            placeholder="请选择课程类型"
            style="width: 220px"
          >
            <el-option :value="1" label="必修课" />
            <el-option :value="2" label="专业选修" />
            <el-option :value="3" label="公选课" />
          </el-select>
        </el-form-item>
        <el-form-item label="起始周" prop="courseStartWeek">
          <el-input
            v-model.number="dialogForm.courseStartWeek"
            placeholder="请输入起始周（数字）"
          />
        </el-form-item>
        <el-form-item label="结束周" prop="courseEndWeek">
          <el-input
            v-model.number="dialogForm.courseEndWeek"
            placeholder="请输入结束周（数字）"
          />
        </el-form-item>
        <el-form-item label="单次课时" prop="courseSingleHour">
          <el-input
            v-model.number="dialogForm.courseSingleHour"
            placeholder="请输入单次课时（数字）"
          />
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
  getCoursePage,
  addCourse,
  updateCourse,
  deleteCourse
} from '../api/course'

const tableData = ref([])
const tableLoading = ref(false)

const searchForm = reactive({
  courseName: '',
  courseType: null
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
  courseId: null,
  courseName: '',
  courseType: null,
  courseStartWeek: null,
  courseEndWeek: null,
  courseSingleHour: null
})

const dialogRules = {
  courseId: [
    { required: true, message: '请输入课程编号', trigger: 'blur' }
  ],
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  courseType: [
    { required: true, message: '请选择课程类型', trigger: 'change' }
  ],
  courseStartWeek: [
    { required: true, message: '请输入起始周', trigger: 'blur' },
    { type: 'number', message: '起始周必须为数字', trigger: ['blur', 'change'] }
  ],
  courseEndWeek: [
    { required: true, message: '请输入结束周', trigger: 'blur' },
    { type: 'number', message: '结束周必须为数字', trigger: ['blur', 'change'] }
  ],
  courseSingleHour: [
    { required: true, message: '请输入单次课时', trigger: 'blur' },
    { type: 'number', message: '单次课时必须为数字', trigger: ['blur', 'change'] }
  ]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑课程' : '添加课程'))

const fetchCoursePage = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      courseName: searchForm.courseName || undefined,
      courseType: searchForm.courseType || undefined
    }
    const { data } = await getCoursePage(params)
    if (data && data.code === 1 && data.data) {
      tableData.value = data.data.rows || []
      pagination.total = data.data.total || 0
    } else {
      ElMessage.error(data?.msg || '获取课程列表失败')
    }
  } catch (error) {
    ElMessage.error('请求课程列表失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchCoursePage()
}

const handleReset = () => {
  searchForm.courseName = ''
  searchForm.courseType = null
  pagination.pageNum = 1
  fetchCoursePage()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchCoursePage()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchCoursePage()
}

const resetDialogForm = () => {
  dialogForm.courseId = null
  dialogForm.courseName = ''
  dialogForm.courseType = null
  dialogForm.courseStartWeek = null
  dialogForm.courseEndWeek = null
  dialogForm.courseSingleHour = null
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
  dialogForm.courseId = row.courseId
  dialogForm.courseName = row.courseName
  dialogForm.courseType = row.courseType
  dialogForm.courseStartWeek = row.courseStartWeek
  dialogForm.courseEndWeek = row.courseEndWeek
  dialogForm.courseSingleHour = row.courseSingleHour
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
        courseId: dialogForm.courseId,
        courseName: dialogForm.courseName,
        courseType: dialogForm.courseType,
        courseStartWeek: dialogForm.courseStartWeek,
        courseEndWeek: dialogForm.courseEndWeek,
        courseSingleHour: dialogForm.courseSingleHour
      }

      let res
      if (isEdit.value) {
        res = await updateCourse(payload)
      } else {
        res = await addCourse(payload)
      }
      const { data } = res

      if (data && data.code === 1) {
        ElMessage.success(isEdit.value ? '更新课程成功' : '新增课程成功')
        dialogVisible.value = false
        if (!isEdit.value) {
          pagination.pageNum = 1
        }
        fetchCoursePage()
      } else {
        ElMessage.error(data?.msg || (isEdit.value ? '更新课程失败' : '新增课程失败'))
      }
    } catch (error) {
      ElMessage.error(isEdit.value ? '请求更新课程失败' : '请求新增课程失败')
    } finally {
      dialogSubmitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  if (!row || !row.courseId) return
  try {
    const { data } = await deleteCourse(row.courseId)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && pagination.pageNum > 1) {
        pagination.pageNum -= 1
      }
      fetchCoursePage()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除请求失败')
  }
}

onMounted(() => {
  fetchCoursePage()
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

