import axios from 'axios'

axios.defaults.baseURL = '/api'

export const getClassroomEfficiency = () => {
  return axios.get('/analysis/classroom-efficiency')
}

export const getClassHealth = () => {
  return axios.get('/analysis/class-health')
}

export const getHeatmapData = () => {
  return axios.get('/analysis/heatmap')
}

/** 教室类型占比 */
export const getClassroomTypeDistribution = () => {
  return axios.get('/analysis/classroom-type-distribution')
}

/** 周一至周五课时负荷趋势 */
export const getWeekdayLessonTrend = () => {
  return axios.get('/analysis/weekday-lesson-trend')
}

/** 课程类型学时占比 */
export const getCourseTypeHours = () => {
  return axios.get('/analysis/course-type-hours')
}

/** 教室容量区间分布 */
export const getClassroomCapacityDistribution = () => {
  return axios.get('/analysis/classroom-capacity-distribution')
}
