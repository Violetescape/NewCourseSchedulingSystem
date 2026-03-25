import axios from 'axios'

axios.defaults.baseURL = '/api'

// 按班级查询课表（week 不传或为 null 则返回全量）
export const getClassSchedule = (classId, week) => {
  return axios.get(`/schedules/class/${classId}`, {
    params: week != null ? { week } : {}
  })
}

// 按教师查询课表（week 不传或为 null 则返回全量）
export const getTeacherSchedule = (teacherId, week) => {
  return axios.get(`/schedules/teacher/${teacherId}`, {
    params: week != null ? { week } : {}
  })
}

// 按教室查询课表（week 不传则返回全量，用于全局聚合课表）
export const getClassroomSchedule = (classroomId, week) => {
  return axios.get(`/schedules/classroom/${classroomId}`, {
    params: week != null ? { week } : {}
  })
}

// 一键自动排课
export const autoSchedule = () => {
  // 单次排课可能耗时较长，这里为单个请求配置较长超时时间
  // 后端映射为 POST /auto-schedule（结合 baseURL '/api' 实际为 /api/auto-schedule）
  return axios.post('/auto-schedule', null, {
    timeout: 30000
  })
}

