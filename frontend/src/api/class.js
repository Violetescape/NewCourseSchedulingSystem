import axios from 'axios'

axios.defaults.baseURL = '/api'

export const getClassPage = (params) => {
  return axios.get('/class/page', { params })
}

export const addClass = (data) => {
  return axios.post('/class', data)
}

export const deleteClass = (id) => {
  return axios.delete(`/class/${id}`)
}

