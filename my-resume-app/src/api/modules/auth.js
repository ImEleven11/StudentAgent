import { request } from '../http'
import { clearClientSession, setAccessToken } from '../session'

export async function registerUser(payload) {
  return request('/api/user/register', {
    method: 'POST',
    data: payload,
    auth: false,
  })
}

export async function loginUser(payload) {
  const response = await request('/api/user/login', {
    method: 'POST',
    data: payload,
    auth: false,
  })

  setAccessToken(response.accessToken)
  return response
}

export async function logoutUser() {
  try {
    await request('/api/user/logout', {
      method: 'POST',
    })
  } finally {
    clearClientSession()
  }
}

export async function getCurrentUserProfile() {
  return request('/api/user/profile')
}

export async function updateCurrentUserProfile(payload) {
  return request('/api/user/profile', {
    method: 'PUT',
    data: payload,
  })
}
