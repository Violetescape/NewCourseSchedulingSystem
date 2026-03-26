<template>
  <div class="class-manage-page">
    <!-- 顶部搜索与操作区 -->
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>教师限排时间管理</h2>
          <p>维护教师在特定周次、星期与节次的不可排课时间。</p>
        </div>
        <el-button type="primary" class="add-btn" @click="handleOpenAddDialog">
          新增限排时间
        </el-button>
      </div>

      <el-form
        class="search-form"
        :inline="true"
        :model="searchForm"
        label-width="72px"
      >
        <el-form-item label="教师">
          <el-select
            v-model="searchForm.teacherId"
            placeholder="请选择教师"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option
              v-for="item in teacherOptions"
              :key="item.teacherId"
              :label="item.teacherName"
              :value="item.teacherId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select
            v-model="searchForm.unWeekday"
            placeholder="请选择星期"
            clearable
            style="width: 160px"
          >
            <el-option
              v-for="item in weekdayOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
        <el-table-column prop="unId" label="记录编号" width="110" />
        <el-table-column prop="teacherName" label="教师姓名" min-width="140" />
        <el-table-column prop="unWeek" label="周次" width="120">
          <template #default="{ row }">
            <span>{{ formatWeek(row.unWeek) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unWeekday" label="星期" width="120">
          <template #default="{ row }">
            <span>{{ formatWeekday(row.unWeekday) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unSection" label="节次" width="120">
          <template #default="{ row }">
            <span>{{ formatSection(row.unSection) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="unRemark"
          label="原因说明"
          min-width="200"
          show-overflow-tooltip
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
              title="确认删除该限排记录？"
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

    <!-- 新增/编辑限排时间弹窗 -->
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
        <el-form-item label="教师" prop="teacherId">
          <el-select
            v-model="dialogForm.teacherId"
            placeholder="请选择教师"
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
        <el-form-item label="周次">
          <el-input
            v-model.number="dialogForm.unWeek"
            placeholder="第几周，留空表示每周"
            clearable
          />
        </el-form-item>
        <el-form-item label="星期" prop="unWeekday">
          <el-select
            v-model="dialogForm.unWeekday"
            placeholder="请选择星期"
            style="width: 100%"
          >
            <el-option
              v-for="item in weekdayOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="节次" prop="unSection">
          <el-input
            v-model.number="dialogForm.unSection"
            placeholder="第几节课，如 1 表示 1-2 节"
          />
        </el-form-item>
        <el-form-item label="原因说明">
          <el-input
            v-model="dialogForm.unRemark"
            placeholder="如：教研会议、出差等"
            maxlength="50"
            show-word-limit
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
  getUnavailablePage,
  addUnavailable,
  updateUnavailable,
  deleteUnavailable
} from '../api/teacherUnavailable'
import { getTeacherPage } from '../api/teacher'

const tableData = ref([])
const tableLoading = ref(false)

// 教师下拉选项
const teacherOptions = ref([])

const weekdayOptions = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

const searchForm = reactive({
  teacherId: null,
  unWeekday: null
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
  unId: null,
  teacherId: null,
  unWeek: null,
  unWeekday: null,
  unSection: null,
  unRemark: ''
})

const dialogRules = {
  teacherId: [
    { required: true, message: '请选择教师', trigger: 'change' }
  ],
  unWeekday: [
    { required: true, message: '请选择星期', trigger: 'change' }
  ],
  unSection: [
    { required: true, message: '请输入节次', trigger: 'blur' },
    { type: 'number', message: '节次必须为数字', trigger: ['blur', 'change'] }
  ]
}

const dialogTitle = computed(() =>
  isEdit.value ? '编辑教师限排时间' : '新增教师限排时间'
)

// 展示格式化函数
const formatWeek = (week) => {
  if (week === null || week === undefined) return '每周'
  return `第${week}周`
}

const formatWeekday = (weekday) => {
  const found = weekdayOptions.find((item) => item.value === weekday)
  return found ? found.label : '-'
}

const formatSection = (section) => {
  if (section === null || section === undefined) return '-'
  return `第${section}节`
}

// 分页查询教师限排记录
const fetchUnavailablePage = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      teacherId: searchForm.teacherId || undefined,
      unWeekday: searchForm.unWeekday || undefined
    }
    const { data } = await getUnavailablePage(params)
    if (data && data.code === 1 && data.data) {
      tableData.value = data.data.rows || []
      pagination.total = data.data.total || 0
    } else {
      ElMessage.error(data?.msg || '获取教师限排记录失败')
    }
  } catch (error) {
    ElMessage.error('请求教师限排记录失败')
  } finally {
    tableLoading.value = false
  }
}

// 加载教师下拉（pageSize=1000 模拟全量）
const fetchTeacherOptions = async () => {
  try {
    const { data } = await getTeacherPage({ pageNum: 1, pageSize: 1000 })
    if (data && data.code === 1 && data.data) {
      teacherOptions.value = data.data.rows || []
    }
  } catch (error) {
    ElMessage.error('加载教师列表失败')
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchUnavailablePage()
}

const handleReset = () => {
  searchForm.teacherId = null
  searchForm.unWeekday = null
  pagination.pageNum = 1
  fetchUnavailablePage()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchUnavailablePage()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchUnavailablePage()
}

const resetDialogForm = () => {
  dialogForm.unId = null
  dialogForm.teacherId = null
  dialogForm.unWeek = null
  dialogForm.unWeekday = null
  dialogForm.unSection = null
  dialogForm.unRemark = ''
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
  dialogForm.unId = row.unId
  dialogForm.teacherId = row.teacherId
  dialogForm.unWeek = row.unWeek
  dialogForm.unWeekday = row.unWeekday
  dialogForm.unSection = row.unSection
  dialogForm.unRemark = row.unRemark || ''
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
        unId: dialogForm.unId,
        teacherId: dialogForm.teacherId,
        unWeek: dialogForm.unWeek,
        unWeekday: dialogForm.unWeekday,
        unSection: dialogForm.unSection,
        unRemark: dialogForm.unRemark
      }

      let res
      if (isEdit.value) {
        res = await updateUnavailable(payload)
      } else {
        // 新增时不传 unId
        // eslint-disable-next-line no-unused-vars
        const { unId, ...createPayload } = payload
        res = await addUnavailable(createPayload)
      }
      const { data } = res

      if (data && data.code === 1) {
        ElMessage.success(isEdit.value ? '更新限排记录成功' : '新增限排记录成功')
        dialogVisible.value = false
        if (!isEdit.value) {
          pagination.pageNum = 1
        }
        fetchUnavailablePage()
      } else {
        ElMessage.error(data?.msg || (isEdit.value ? '更新限排记录失败' : '新增限排记录失败'))
      }
    } catch (error) {
      ElMessage.error(isEdit.value ? '请求更新限排记录失败' : '请求新增限排记录失败')
    } finally {
      dialogSubmitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  if (!row || !row.unId) return
  try {
    const { data } = await deleteUnavailable(row.unId)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && pagination.pageNum > 1) {
        pagination.pageNum -= 1
      }
      fetchUnavailablePage()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除请求失败')
  }
}

onMounted(() => {
  fetchTeacherOptions()
  fetchUnavailablePage()
})
</script>

<style scoped>
.dialog-form {
  padding-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

