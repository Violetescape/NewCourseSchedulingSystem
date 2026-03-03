import axios from 'axios'

axios.defaults.baseURL = '/api'

export const getCoursePage = (params) => {
  return axios.get('/courses', { params })
}

export const addCourse = (data) => {
  return axios.post('/courses', data)
}

export const updateCourse = (data) => {
  return axios.put('/courses', data)
}

export const deleteCourse = (id) => {
  return axios.delete(`/courses/${id}`)
}

