import { request } from '../http'

export async function uploadResumeFile(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request('/api/profile/upload-resume', {
    method: 'POST',
    data: formData,
  })
}

export async function createProfileByManualInput(payload) {
  return request('/api/profile/manual-input', {
    method: 'POST',
    data: payload,
  })
}

export async function getStudentProfile(profileId) {
  return request(`/api/profile/${profileId}`)
}

export async function updateStudentProfile(profileId, payload) {
  return request(`/api/profile/${profileId}`, {
    method: 'PUT',
    data: payload,
  })
}

export async function getProfileMissingFields(profileId) {
  return request(`/api/profile/${profileId}/missing-fields`)
}

export async function completeStudentProfile(profileId, payload) {
  return request(`/api/profile/${profileId}/complete`, {
    method: 'POST',
    data: payload,
  })
}
