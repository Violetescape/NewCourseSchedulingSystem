import axios from 'axios'

axios.defaults.baseURL = '/api'

export const getClassroomPage = (params) => {
  return axios.get('/classrooms', { params })
}

export const addClassroom = (data) => {
  return axios.post('/classrooms', data)
}

export const updateClassroom = (data) => {
  return axios.put('/classrooms', data)
}

export const deleteClassroom = (id) => {
  return axios.delete(`/classrooms/${id}`)
}

