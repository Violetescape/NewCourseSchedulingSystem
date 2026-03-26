<template>
  <div class="class-manage-page">
    <!-- 顶部搜索与操作区 -->
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>教师管理</h2>
          <p>按姓名与院系快速检索教师信息。</p>
        </div>
        <el-button type="primary" class="add-btn" @click="handleOpenAddDialog">
          添加教师
        </el-button>
      </div>

      <el-form
        class="search-form"
        :inline="true"
        :model="searchForm"
        label-width="72px"
      >
        <el-form-item label="教师姓名">
          <el-input
            v-model="searchForm.teacherName"
            placeholder="请输入教师姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="所属院系">
          <el-input
            v-model="searchForm.teacherDepartment"
            placeholder="请输入所属院系"
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
        <el-table-column prop="teacherId" label="工号" width="120" />
        <el-table-column prop="teacherName" label="姓名" min-width="140" />
        <el-table-column
          prop="teacherDepartment"
          label="院系"
          min-width="160"
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
              title="确认删除该教师？"
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

    <!-- 新增/编辑教师弹窗 -->
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
        <el-form-item label="教师工号" prop="teacherId">
          <el-input
            v-model.number="dialogForm.teacherId"
            :disabled="isEdit"
            placeholder="请输入教师工号（需手动输入）"
          />
        </el-form-item>
        <el-form-item label="教师姓名" prop="teacherName">
          <el-input v-model="dialogForm.teacherName" placeholder="请输入教师姓名" />
        </el-form-item>
        <el-form-item label="所属院系" prop="teacherDepartment">
          <el-input
            v-model="dialogForm.teacherDepartment"
            placeholder="请输入所属院系/部门"
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
  getTeacherPage,
  addTeacher,
  updateTeacher,
  deleteTeacher
} from '../api/teacher'

const tableData = ref([])
const tableLoading = ref(false)

const searchForm = reactive({
  teacherName: '',
  teacherDepartment: ''
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
  teacherId: null,
  teacherName: '',
  teacherDepartment: ''
})

const dialogRules = {
  teacherId: [
    { required: true, message: '请输入教师工号', trigger: 'blur' }
  ],
  teacherName: [
    { required: true, message: '请输入教师姓名', trigger: 'blur' }
  ],
  teacherDepartment: [
    { required: true, message: '请输入所属院系', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑教师' : '添加教师'))

const fetchTeacherPage = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      teacherName: searchForm.teacherName || undefined,
      teacherDepartment: searchForm.teacherDepartment || undefined
    }
    const { data } = await getTeacherPage(params)
    if (data && data.code === 1 && data.data) {
      tableData.value = data.data.rows || []
      pagination.total = data.data.total || 0
    } else {
      ElMessage.error(data?.msg || '获取教师列表失败')
    }
  } catch (error) {
    ElMessage.error('请求教师列表失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchTeacherPage()
}

const handleReset = () => {
  searchForm.teacherName = ''
  searchForm.teacherDepartment = ''
  pagination.pageNum = 1
  fetchTeacherPage()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchTeacherPage()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchTeacherPage()
}

const resetDialogForm = () => {
  dialogForm.teacherId = null
  dialogForm.teacherName = ''
  dialogForm.teacherDepartment = ''
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
  dialogForm.teacherId = row.teacherId
  dialogForm.teacherName = row.teacherName
  dialogForm.teacherDepartment = row.teacherDepartment
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
        teacherId: dialogForm.teacherId,
        teacherName: dialogForm.teacherName,
        teacherDepartment: dialogForm.teacherDepartment
      }

      let res
      if (isEdit.value) {
        res = await updateTeacher(payload)
      } else {
        res = await addTeacher(payload)
      }
      const { data } = res

      if (data && data.code === 1) {
        ElMessage.success(isEdit.value ? '更新教师成功' : '新增教师成功')
        dialogVisible.value = false
        if (!isEdit.value) {
          pagination.pageNum = 1
        }
        fetchTeacherPage()
      } else {
        ElMessage.error(data?.msg || (isEdit.value ? '更新教师失败' : '新增教师失败'))
      }
    } catch (error) {
      ElMessage.error(isEdit.value ? '请求更新教师失败' : '请求新增教师失败')
    } finally {
      dialogSubmitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  if (!row || !row.teacherId) return
  try {
    const { data } = await deleteTeacher(row.teacherId)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && pagination.pageNum > 1) {
        pagination.pageNum -= 1
      }
      fetchTeacherPage()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除请求失败')
  }
}

onMounted(() => {
  fetchTeacherPage()
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

