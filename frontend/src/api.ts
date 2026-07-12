import type { ErrorBody, WifiConfiguration } from './types'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''
const apiTokenStorageKey = 'wifi-admin-api-token'

export function getApiToken() {
  return window.localStorage.getItem(apiTokenStorageKey) ?? ''
}

export function setApiToken(token: string) {
  const normalizedToken = token.trim()
  if (normalizedToken) {
    window.localStorage.setItem(apiTokenStorageKey, normalizedToken)
  } else {
    window.localStorage.removeItem(apiTokenStorageKey)
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = getApiToken()
  const response = await fetch(`${baseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    ...options
  })

  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? ''
    let message = `Request failed with status ${response.status}.`

    if (contentType.includes('application/json')) {
      try {
        const error = (await response.json()) as Partial<ErrorBody>
        if (error.message) {
          message = error.code ? `${error.code}: ${error.message}` : error.message
        }
      } catch {
        // Keep the status-based fallback when a server labels an invalid body as JSON.
      }
    } else {
      const text = (await response.text()).trim()
      if (text) {
        message = text
      }
    }

    throw new Error(message)
  }

  return (await response.json()) as T
}

export function getWifiConfiguration(cpeId: string) {
  return request<WifiConfiguration>(`/wifi-parameter/${encodeURIComponent(cpeId)}`)
}

export function updateWifiConfiguration(configuration: WifiConfiguration) {
  return request<WifiConfiguration>('/wifi-parameter', {
    method: 'PUT',
    body: JSON.stringify(configuration)
  })
}
