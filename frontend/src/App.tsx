import { useState } from 'react'
import { getApiToken, getWifiConfiguration, setApiToken, updateWifiConfiguration } from './api'
import type { EncryptionType, WifiBand, WifiConfiguration } from './types'

const encryptionTypes: EncryptionType[] = [
  'OPEN',
  'WEP',
  'WPA_PSK',
  'WPA2_PSK',
  'WPA3_SAE',
  'WPA2_ENTERPRISE'
]

const emptyConfiguration: WifiConfiguration = {
  cpeId: '',
  wifiBand: 'BAND_2_4_GHZ',
  ssid: '',
  encryptionType: 'OPEN',
  password: null
}

function validate(configuration: WifiConfiguration) {
  if (!configuration.cpeId.trim() || !configuration.ssid.trim()) {
    return 'CPE ID and SSID are required.'
  }
  if (configuration.encryptionType === 'OPEN' && configuration.password?.trim()) {
    return 'OPEN networks cannot contain a password.'
  }
  if (configuration.encryptionType !== 'OPEN' && !configuration.password?.trim()) {
    return 'A password is required for secured networks.'
  }
  return null
}

export default function App() {
  const [configuration, setConfiguration] = useState(emptyConfiguration)
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const [apiToken, setApiTokenInput] = useState(getApiToken())
  const authEnabled = import.meta.env.VITE_AUTH_ENABLED === 'true'

  function update<K extends keyof WifiConfiguration>(key: K, value: WifiConfiguration[K]) {
    setConfiguration((current) => ({ ...current, [key]: value }))
  }

  async function load() {
    if (!configuration.cpeId.trim()) {
      setMessage('CPE ID is required.')
      return
    }
    setBusy(true)
    setMessage('')
    try {
      setConfiguration(await getWifiConfiguration(configuration.cpeId))
      setMessage('Configuration loaded.')
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Lookup failed.')
    } finally {
      setBusy(false)
    }
  }

  async function save() {
    const validationError = validate(configuration)
    if (validationError) {
      setMessage(validationError)
      return
    }
    setBusy(true)
    setMessage('')
    try {
      setConfiguration(await updateWifiConfiguration(configuration))
      setMessage('Configuration updated.')
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Update failed.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="page">
      <section className="card">
        <p className="eyebrow">MojTelekom</p>
        <h1>WiFi configuration</h1>
        <p className="intro">Read and update a router configuration through the REST adapter.</p>

        {authEnabled && (
          <div className="auth-box">
            <label>
              Bearer token
              <input
                type="password"
                value={apiToken}
                onChange={(event) => setApiTokenInput(event.target.value)}
                placeholder="Paste a Keycloak access token"
              />
            </label>
            <button
              type="button"
              onClick={() => {
                setApiToken(apiToken)
                setMessage(apiToken.trim() ? 'Token stored.' : 'Stored token cleared.')
              }}
            >
              Use token
            </button>
          </div>
        )}

        <label>
          CPE ID
          <input value={configuration.cpeId} onChange={(event) => update('cpeId', event.target.value)} />
        </label>

        <button type="button" onClick={load} disabled={busy}>Load configuration</button>

        <div className="grid">
          <label>
            WiFi band
            <select value={configuration.wifiBand} onChange={(event) => update('wifiBand', event.target.value as WifiBand)}>
              <option value="BAND_2_4_GHZ">2.4 GHz</option>
              <option value="BAND_5_GHZ">5 GHz</option>
            </select>
          </label>

          <label>
            Encryption
            <select value={configuration.encryptionType} onChange={(event) => update('encryptionType', event.target.value as EncryptionType)}>
              {encryptionTypes.map((type) => <option key={type} value={type}>{type}</option>)}
            </select>
          </label>
        </div>

        <label>
          SSID
          <input value={configuration.ssid} onChange={(event) => update('ssid', event.target.value)} />
        </label>

        <label>
          Password
          <input
            type="password"
            value={configuration.password ?? ''}
            onChange={(event) => update('password', event.target.value || null)}
          />
        </label>

        <button type="button" onClick={save} disabled={busy}>Save configuration</button>
        <p role="status" className="message">{message}</p>
      </section>
    </main>
  )
}
