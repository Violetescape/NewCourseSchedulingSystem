import axios from 'axios'

axios.defaults.baseURL = '/api'

// 按班级查询课表（week 作为 query 参数）
export const getClassSchedule = (classId, week) => {
  return axios.get(`/schedules/class/${classId}`, { params: { week } })
}

// 按教师查询课表（week 作为 query 参数）
export const getTeacherSchedule = (teacherId, week) => {
  return axios.get(`/schedules/teacher/${teacherId}`, { params: { week } })
}

