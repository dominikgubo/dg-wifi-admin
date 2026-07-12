import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('WiFi configuration form', () => {
  it('validates a secured configuration before sending it', async () => {
    const user = userEvent.setup()
    render(<App />)

    await user.type(screen.getByLabelText('CPE ID'), 'CPE_001')
    await user.type(screen.getByLabelText('SSID'), 'Office')
    await user.selectOptions(screen.getByLabelText('Encryption'), 'WPA2_PSK')
    await user.click(screen.getByRole('button', { name: 'Save configuration' }))

    expect(screen.getByRole('status')).toHaveTextContent('A password is required for secured networks.')
  })
})
