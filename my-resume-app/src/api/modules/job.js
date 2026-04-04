import { request } from '../http'

export async function searchJobs(query) {
  return request('/api/job/search', {
    query,
    auth: false,
  })
}

export async function getJobDetail(jobId) {
  return request(`/api/job/${jobId}`, {
    auth: false,
  })
}

export async function getJobPath(jobId) {
  return request(`/api/job/${jobId}/path`, {
    auth: false,
  })
}
