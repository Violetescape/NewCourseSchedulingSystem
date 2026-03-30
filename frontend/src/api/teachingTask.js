import axios from 'axios'

axios.defaults.baseURL = '/api'

// 分页查询教学任务列表（返回 TeachingTaskVO 分页）
export const getTeachingTaskPage = (params) => {
  return axios.get('/teaching-tasks', { params })
}

// 新增教学任务
export const addTeachingTask = (data) => {
  return axios.post('/teaching-tasks', data)
}

// 更新教学任务
export const updateTeachingTask = (data) => {
  return axios.put('/teaching-tasks', data)
}

// 删除教学任务
export const deleteTeachingTask = (id) => {
  return axios.delete(`/teaching-tasks/${id}`)
}

// 下载教学任务导入模板
export const getTeachingTaskTemplateUrl = () => {
  return '/api/teaching-tasks/downloadTemplate'
}

// Excel 导入教学任务
export const importTeachingTaskExcel = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post('/teaching-tasks/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

