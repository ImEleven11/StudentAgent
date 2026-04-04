import { request } from '../http'

export async function sendChatMessage(payload) {
  return request('/api/chat/send', {
    method: 'POST',
    data: payload,
  })
}

export async function getChatHistory(sessionId, page = 1, size = 50) {
  return request('/api/chat/history', {
    query: { sessionId, page, size },
  })
}

export async function clearChatHistory(sessionId) {
  return request(`/api/chat/session/${sessionId}`, {
    method: 'DELETE',
  })
}
