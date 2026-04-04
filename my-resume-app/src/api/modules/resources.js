import { request } from '../http'

export async function getCourseResources(query = {}) {
  return request('/api/resources/courses', {
    query,
    auth: false,
  })
}

export async function getBookResources(query = {}) {
  return request('/api/resources/books', {
    query,
    auth: false,
  })
}

export async function getProjectResources(query = {}) {
  return request('/api/resources/projects', {
    query,
    auth: false,
  })
}

export async function getInternshipResources(query = {}) {
  return request('/api/resources/internships', {
    query,
    auth: false,
  })
}

export async function getCompetitionResources(query = {}) {
  return request('/api/resources/competitions', {
    query,
    auth: false,
  })
}
