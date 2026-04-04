import { request } from '../http'

export async function createAssessment(payload) {
  return request('/api/assessment/create', {
    method: 'POST',
    data: payload,
  })
}

export async function submitAssessment(assessmentId, payload) {
  return request(`/api/assessment/${assessmentId}/submit`, {
    method: 'POST',
    data: payload,
  })
}

export async function getAssessment(assessmentId) {
  return request(`/api/assessment/${assessmentId}`)
}

export async function listAssessmentHistory(planId) {
  return request(`/api/assessment/history/${planId}`)
}

export async function adjustPlanByAssessment(assessmentId, payload) {
  return request(`/api/assessment/${assessmentId}/adjust`, {
    method: 'POST',
    data: payload,
  })
}
