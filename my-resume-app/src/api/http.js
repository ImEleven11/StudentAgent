import { clearClientSession, getAccessToken } from './session'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

function buildUrl(path, query) {
  const url = new URL(`${API_BASE_URL}${path}`, window.location.origin)

  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value == null || value === '') {
        return
      }

      if (Array.isArray(value)) {
        value.forEach((item) => {
          if (item != null && item !== '') {
            url.searchParams.append(key, item)
          }
        })
        return
      }

      url.searchParams.set(key, value)
    })
  }

  return url.toString()
}

async function parseErrorPayload(response) {
  const contentType = response.headers.get('content-type') || ''

  if (contentType.includes('application/json')) {
    const payload = await response.json().catch(() => null)
    return payload?.message || payload?.error || `请求失败 (${response.status})`
  }

  const text = await response.text().catch(() => '')
  return text || `请求失败 (${response.status})`
}

export async function request(path, options = {}) {
  const {
    method = 'GET',
    query,
    data,
    headers = {},
    auth = true,
  } = options

  const requestHeaders = new Headers(headers)
  const token = getAccessToken()

  if (auth && token) {
    requestHeaders.set('Authorization', `Bearer ${token}`)
  }

  let body
  if (data instanceof FormData) {
    body = data
  } else if (data != null) {
    requestHeaders.set('Content-Type', 'application/json')
    body = JSON.stringify(data)
  }

  const response = await fetch(buildUrl(path, query), {
    method,
    headers: requestHeaders,
    body,
  })

  if (response.status === 401 && auth) {
    clearClientSession()
  }

  if (!response.ok) {
    throw new Error(await parseErrorPayload(response))
  }

  if (response.status === 204) {
    return null
  }

  const payload = await response.json().catch(() => null)
  if (payload && typeof payload.code !== 'undefined') {
    if (payload.code !== 0) {
      throw new Error(payload.message || '请求失败')
    }
    return payload.data
  }

  return payload
}

export async function requestBlob(path, options = {}) {
  const {
    method = 'GET',
    query,
    headers = {},
    auth = true,
  } = options

  const requestHeaders = new Headers(headers)
  const token = getAccessToken()

  if (auth && token) {
    requestHeaders.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(buildUrl(path, query), {
    method,
    headers: requestHeaders,
  })

  if (response.status === 401 && auth) {
    clearClientSession()
  }

  if (!response.ok) {
    throw new Error(await parseErrorPayload(response))
  }

  return response
}
