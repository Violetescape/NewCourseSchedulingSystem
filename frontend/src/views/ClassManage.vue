<template>
  <div class="class-manage-page">
    <!-- 顶部搜索与操作区 -->
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>班级管理</h2>
          <p>按年级、专业与学院快速检索班级信息。</p>
        </div>
        <el-button type="primary" class="add-btn" @click="handleOpenDialog">
          添加班级
        </el-button>
      </div>

      <el-form
        class="search-form"
        :inline="true"
        :model="searchForm"
        label-width="72px"
      >
        <el-form-item label="年级">
          <el-input
            v-model="searchForm.classGrade"
            placeholder="如 2022"
            clearable
          />
        </el-form-item>
        <el-form-item label="专业">
          <el-input
            v-model="searchForm.classMajor"
            placeholder="请输入专业"
            clearable
          />
        </el-form-item>
        <el-form-item label="学院">
          <el-input
            v-model="searchForm.classDepartment"
            placeholder="请输入学院"
            clearable
          />
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
        <el-table-column prop="classId" label="编号" width="120" />
        <el-table-column prop="className" label="名称" min-width="140" />
        <el-table-column prop="classDepartment" label="学院" min-width="140" />
        <el-table-column prop="classMajor" label="专业" min-width="160" />
        <el-table-column prop="classGrade" label="年级" width="100" />
        <el-table-column prop="classNum" label="人数" width="90" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="确认删除该班级？"
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

    <!-- 新增班级弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      width="480px"
      :close-on-click-modal="false"
      title="添加班级"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="96px"
        class="dialog-form"
      >
        <el-form-item label="班级编号" prop="classId">
          <el-input
            v-model="dialogForm.classId"
            placeholder="请输入班级编号（需手动输入）"
          />
        </el-form-item>
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="dialogForm.className" placeholder="如 22计科1班" />
        </el-form-item>
        <el-form-item label="学院" prop="classDepartment">
          <el-input v-model="dialogForm.classDepartment" placeholder="所属学院" />
        </el-form-item>
        <el-form-item label="专业" prop="classMajor">
          <el-input v-model="dialogForm.classMajor" placeholder="所属专业" />
        </el-form-item>
        <el-form-item label="年级" prop="classGrade">
          <el-input v-model="dialogForm.classGrade" placeholder="如 2022" />
        </el-form-item>
        <el-form-item label="人数" prop="classNum">
          <el-input
            v-model.number="dialogForm.classNum"
            placeholder="请输入班级人数"
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
axios.defaults.baseURL = '/api'
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'



const tableData = ref([])
const tableLoading = ref(false)

const searchForm = reactive({
  classGrade: '',
  classMajor: '',
  classDepartment: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogSubmitting = ref(false)
const dialogFormRef = ref(null)

const dialogForm = reactive({
  classId: '',
  className: '',
  classDepartment: '',
  classMajor: '',
  classGrade: '',
  classNum: null
})

const dialogRules = {
  classId: [{ required: true, message: '请输入班级编号', trigger: 'blur' }],
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  classDepartment: [{ required: true, message: '请输入学院', trigger: 'blur' }],
  classMajor: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  classGrade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  classNum: [
    { required: true, message: '请输入人数', trigger: 'blur' },
    {
      type: 'number',
      message: '人数必须为数字',
      trigger: ['blur', 'change']
    }
  ]
}

const fetchClassPage = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      classGrade: searchForm.classGrade || undefined,
      classMajor: searchForm.classMajor || undefined,
      classDepartment: searchForm.classDepartment || undefined
    }
    const { data } = await axios.get('/class/page', { params })

    if (data && data.code === 1 && data.data) {
      tableData.value = data.data.rows || []
      pagination.total = data.data.total || 0
    } else {
      ElMessage.error(data?.msg || '获取班级列表失败')
    }
  } catch (error) {
    ElMessage.error('请求班级列表失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchClassPage()
}

const handleReset = () => {
  searchForm.classGrade = ''
  searchForm.classMajor = ''
  searchForm.classDepartment = ''
  pagination.pageNum = 1
  fetchClassPage()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchClassPage()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchClassPage()
}

const handleOpenDialog = () => {
  dialogVisible.value = true
  resetDialogForm()
}

const resetDialogForm = () => {
  dialogForm.classId = ''
  dialogForm.className = ''
  dialogForm.classDepartment = ''
  dialogForm.classMajor = ''
  dialogForm.classGrade = ''
  dialogForm.classNum = null
  if (dialogFormRef.value) {
    dialogFormRef.value.clearValidate()
  }
}

const handleSubmit = () => {
  if (!dialogFormRef.value) return
  dialogFormRef.value.validate(async (valid) => {
    if (!valid) return
    dialogSubmitting.value = true
    try {
      const payload = {
        classId: dialogForm.classId,
        className: dialogForm.className,
        classDepartment: dialogForm.classDepartment,
        classMajor: dialogForm.classMajor,
        classGrade: dialogForm.classGrade,
        classNum: dialogForm.classNum
      }
      const { data } = await axios.post('/class', payload)
      if (data && data.code === 1) {
        ElMessage.success('新增班级成功')
        dialogVisible.value = false
        fetchClassPage()
      } else {
        ElMessage.error(data?.msg || '新增班级失败')
      }
    } catch (error) {
      ElMessage.error('请求新增班级失败')
    } finally {
      dialogSubmitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  if (!row || !row.classId) return
  try {
    const { data } = await axios.delete(`/class/${row.classId}`)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      // 如果当前页只剩一个元素且不是第一页，删除后回到上一页
      if (tableData.value.length === 1 && pagination.pageNum > 1) {
        pagination.pageNum -= 1
      }
      fetchClassPage()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除请求失败')
  }
}

onMounted(() => {
  fetchClassPage()
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

