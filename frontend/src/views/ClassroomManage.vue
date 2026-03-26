<template>
  <div class="class-manage-page">
    <!-- 顶部搜索与操作区 -->
    <section class="toolbar-card">
      <div class="toolbar-header">
        <div class="title-block">
          <h2>教室管理</h2>
          <p>按名称与类型快速检索教室信息。</p>
        </div>
        <el-button type="primary" class="add-btn" @click="handleOpenAddDialog">
          添加教室
        </el-button>
      </div>

      <el-form
        class="search-form"
        :inline="true"
        :model="searchForm"
        label-width="72px"
      >
        <el-form-item label="教室名称">
          <el-input
            v-model="searchForm.classroomName"
            placeholder="请输入教室名称"
            clearable
          />
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
        <el-table-column prop="classroomId" label="编号" width="120" />
        <el-table-column prop="classroomName" label="名称" min-width="140" />
        <el-table-column prop="classroomType" label="类型" min-width="140" />
        <el-table-column prop="classroomCap" label="容量" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStateTagType(row.classroomState)">
              {{ row.classroomState || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleOpenEditDialog(row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确认删除该教室？"
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

    <!-- 新增/编辑教室弹窗 -->
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
        <el-form-item label="教室编号" prop="classroomId">
          <el-input
            v-model.number="dialogForm.classroomId"
            :disabled="isEdit"
            placeholder="请输入教室编号（需手动输入）"
          />
        </el-form-item>
        <el-form-item label="教室名称" prop="classroomName">
          <el-input v-model="dialogForm.classroomName" placeholder="如 A101" />
        </el-form-item>
        <el-form-item label="教室类型" prop="classroomType">
          <el-select
            v-model="dialogForm.classroomType"
            placeholder="请选择教室类型"
            style="width: 100%"
          >
            <el-option label="普通教室" value="普通教室" />
            <el-option label="计算机机房" value="计算机机房" />
            <el-option label="实验室" value="实验室" />
          </el-select>
        </el-form-item>
        <el-form-item label="容纳人数" prop="classroomCap">
          <el-input
            v-model.number="dialogForm.classroomCap"
            placeholder="请输入容量"
          />
        </el-form-item>
        <el-form-item label="状态" prop="classroomState">
          <el-select
            v-model="dialogForm.classroomState"
            placeholder="请选择状态"
            style="width: 100%"
          >
            <el-option label="可用" value="可用" />
            <el-option label="被占用" value="被占用" />
            <el-option label="维修" value="维修" />
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
  getClassroomPage,
  addClassroom,
  updateClassroom,
  deleteClassroom
} from '../api/classroom'

const tableData = ref([])
const tableLoading = ref(false)

const searchForm = reactive({
  classroomName: '',
  classroomType: ''
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
  classroomId: null,
  classroomName: '',
  classroomType: '',
  classroomCap: null,
  classroomState: ''
})

const dialogRules = {
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

const dialogTitle = computed(() => (isEdit.value ? '编辑教室' : '添加教室'))

const getStateTagType = (state) => {
  if (state === '可用') return 'success'
  if (state === '维修') return 'danger'
  if (state === '被占用') return 'warning'
  return ''
}

const fetchClassroomPage = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      classroomName: searchForm.classroomName || undefined,
      classroomType: searchForm.classroomType || undefined
    }
    const { data } = await getClassroomPage(params)
    if (data && data.code === 1 && data.data) {
      tableData.value = data.data.rows || []
      pagination.total = data.data.total || 0
    } else {
      ElMessage.error(data?.msg || '获取教室列表失败')
    }
  } catch (error) {
    ElMessage.error('请求教室列表失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchClassroomPage()
}

const handleReset = () => {
  searchForm.classroomName = ''
  searchForm.classroomType = ''
  pagination.pageNum = 1
  fetchClassroomPage()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchClassroomPage()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchClassroomPage()
}

const resetDialogForm = () => {
  dialogForm.classroomId = null
  dialogForm.classroomName = ''
  dialogForm.classroomType = ''
  dialogForm.classroomCap = null
  dialogForm.classroomState = ''
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
  dialogForm.classroomId = row.classroomId
  dialogForm.classroomName = row.classroomName
  dialogForm.classroomType = row.classroomType
  dialogForm.classroomCap = row.classroomCap
  dialogForm.classroomState = row.classroomState
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
        classroomId: dialogForm.classroomId,
        classroomName: dialogForm.classroomName,
        classroomType: dialogForm.classroomType,
        classroomCap: dialogForm.classroomCap,
        classroomState: dialogForm.classroomState
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
        dialogVisible.value = false
        // 新增后回到第一页，编辑后停留在当前页
        if (!isEdit.value) {
          pagination.pageNum = 1
        }
        fetchClassroomPage()
      } else {
        ElMessage.error(data?.msg || (isEdit.value ? '更新教室失败' : '新增教室失败'))
      }
    } catch (error) {
      ElMessage.error(isEdit.value ? '请求更新教室失败' : '请求新增教室失败')
    } finally {
      dialogSubmitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  if (!row || !row.classroomId) return
  try {
    const { data } = await deleteClassroom(row.classroomId)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && pagination.pageNum > 1) {
        pagination.pageNum -= 1
      }
      fetchClassroomPage()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除请求失败')
  }
}

onMounted(() => {
  fetchClassroomPage()
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

