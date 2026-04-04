import { request } from '../http'

export async function analyzeMatch(payload) {
  return request('/api/match/analyze', {
    method: 'POST',
    data: payload,
  })
}

export async function recommendJobs(payload) {
  return request('/api/match/recommend', {
    method: 'POST',
    data: payload,
  })
}
