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

