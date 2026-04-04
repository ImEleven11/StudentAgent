const STORAGE_KEYS = {
  accessToken: 'student-agent/access-token',
  activeProfileId: 'student-agent/active-profile-id',
  chatSessionId: 'student-agent/chat-session-id',
}

export function getAccessToken() {
  return localStorage.getItem(STORAGE_KEYS.accessToken) || ''
}

export function setAccessToken(token) {
  if (!token) {
    localStorage.removeItem(STORAGE_KEYS.accessToken)
    return
  }
  localStorage.setItem(STORAGE_KEYS.accessToken, token)
}

export function clearAccessToken() {
  localStorage.removeItem(STORAGE_KEYS.accessToken)
}

export function getActiveProfileId() {
  const rawValue = localStorage.getItem(STORAGE_KEYS.activeProfileId)
  if (!rawValue) {
    return null
  }

  const profileId = Number(rawValue)
  return Number.isFinite(profileId) ? profileId : null
}

export function setActiveProfileId(profileId) {
  if (profileId == null) {
    localStorage.removeItem(STORAGE_KEYS.activeProfileId)
    return
  }
  localStorage.setItem(STORAGE_KEYS.activeProfileId, String(profileId))
}

export function clearActiveProfileId() {
  localStorage.removeItem(STORAGE_KEYS.activeProfileId)
}

export function getChatSessionId() {
  return localStorage.getItem(STORAGE_KEYS.chatSessionId) || ''
}

export function setChatSessionId(sessionId) {
  if (!sessionId) {
    localStorage.removeItem(STORAGE_KEYS.chatSessionId)
    return
  }
  localStorage.setItem(STORAGE_KEYS.chatSessionId, sessionId)
}

export function clearStoredChatSessionId() {
  localStorage.removeItem(STORAGE_KEYS.chatSessionId)
}

export function clearClientSession() {
  clearAccessToken()
  clearActiveProfileId()
  clearStoredChatSessionId()
}
