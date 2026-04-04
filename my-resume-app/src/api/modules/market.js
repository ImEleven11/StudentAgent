import { request } from '../http'

export async function getSupplyDemand(jobId) {
  return request(`/api/market/supply-demand/${jobId}`, {
    auth: false,
  })
}

export async function getHotJobs(query = {}) {
  return request('/api/market/hot-jobs', {
    query,
    auth: false,
  })
}

export async function getMarketTrend(query = {}) {
  return request('/api/market/trend', {
    query,
    auth: false,
  })
}

export async function getMarketForecast(payload) {
  return request('/api/market/forecast', {
    method: 'POST',
    data: payload,
    auth: false,
  })
}
