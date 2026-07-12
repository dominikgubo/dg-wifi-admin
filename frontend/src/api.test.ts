import { afterEach, describe, expect, it, vi } from 'vitest'
import { getWifiConfiguration } from './api'

describe('API error handling', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('formats a JSON backend error', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ code: 'CPE_NOT_FOUND', message: 'CPE was not found.' }),
      { status: 404, headers: { 'Content-Type': 'application/json' } }
    ))
    vi.stubGlobal('fetch', fetchMock)

    await expect(getWifiConfiguration('CPE_UNKNOWN'))
      .rejects.toThrow('CPE_NOT_FOUND: CPE was not found.')
  })

  it('surfaces a plain-text error without attempting JSON parsing', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(
      'Invalid CORS request',
      { status: 403, headers: { 'Content-Type': 'text/plain' } }
    ))
    vi.stubGlobal('fetch', fetchMock)

    await expect(getWifiConfiguration('CPE_001'))
      .rejects.toThrow('Invalid CORS request')
  })

  it('uses a status fallback for an empty error response', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 502 }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(getWifiConfiguration('CPE_001'))
      .rejects.toThrow('Request failed with status 502.')
  })
})
