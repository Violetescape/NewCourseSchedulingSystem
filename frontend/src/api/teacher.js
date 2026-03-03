import axios from 'axios'

axios.defaults.baseURL = '/api'

export const getTeacherPage = (params) => {
  return axios.get('/teachers', { params })
}

export const addTeacher = (data) => {
  return axios.post('/teachers', data)
}

export const updateTeacher = (data) => {
  return axios.put('/teachers', data)
}

export const deleteTeacher = (id) => {
  return axios.delete(`/teachers/${id}`)
}

