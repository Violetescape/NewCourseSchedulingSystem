import axios from 'axios'

axios.defaults.baseURL = '/api'

// 分页查询教师限排时间
export const getUnavailablePage = (params) => {
  return axios.get('/teacher-unavailable', { params })
}

// 新增教师限排记录
export const addUnavailable = (data) => {
  return axios.post('/teacher-unavailable', data)
}

// 更新教师限排记录
export const updateUnavailable = (data) => {
  return axios.put('/teacher-unavailable', data)
}

// 删除教师限排记录
export const deleteUnavailable = (id) => {
  return axios.delete(`/teacher-unavailable/${id}`)
}

