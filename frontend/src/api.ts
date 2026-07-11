import type { ErrorBody, WifiConfiguration } from './types'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  })

  if (!response.ok) {
    const error = (await response.json()) as ErrorBody
    throw new Error(`${error.code}: ${error.message}`)
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
