import { request, requestBlob } from '../http'

function resolveFilename(contentDisposition, fallbackFilename) {
  if (!contentDisposition) {
    return fallbackFilename
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }

  const plainMatch = contentDisposition.match(/filename="?([^"]+)"?/i)
  return plainMatch?.[1] || fallbackFilename
}

export async function generateReport(payload) {
  return request('/api/report/generate', {
    method: 'POST',
    data: payload,
  })
}

export async function getReport(reportId) {
  return request(`/api/report/${reportId}`)
}

export async function listReports(profileId) {
  return request('/api/report/list', {
    query: { profileId },
  })
}

export async function polishGeneratedReport(reportId) {
  return request(`/api/report/${reportId}/polish`, {
    method: 'POST',
  })
}

export async function exportGeneratedReport(reportId, format) {
  const response = await requestBlob(`/api/report/${reportId}/export`, {
    query: { format },
  })

  return {
    blob: await response.blob(),
    filename: resolveFilename(
      response.headers.get('content-disposition'),
      format === 'word' ? `report-${reportId}.docx` : `report-${reportId}.pdf`,
    ),
  }
}
