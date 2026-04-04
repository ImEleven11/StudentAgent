import { request } from '../http'

export async function generatePlan(payload) {
  return request('/api/plan/generate', {
    method: 'POST',
    data: payload,
  })
}

export async function getPlan(planId) {
  return request(`/api/plan/${planId}`)
}

export async function listPlans(profileId) {
  return request('/api/plan/list', {
    query: { profileId },
  })
}

export async function completePlanTask(planId, taskId) {
  return request(`/api/plan/${planId}/task/${taskId}/complete`, {
    method: 'POST',
  })
}
