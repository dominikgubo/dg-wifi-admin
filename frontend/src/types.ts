export type WifiBand = 'BAND_2_4_GHZ' | 'BAND_5_GHZ'

export type EncryptionType =
  | 'OPEN'
  | 'WEP'
  | 'WPA_PSK'
  | 'WPA2_PSK'
  | 'WPA3_SAE'
  | 'WPA2_ENTERPRISE'

export type WifiConfiguration = {
  cpeId: string
  wifiBand: WifiBand
  ssid: string
  encryptionType: EncryptionType
  password: string | null
}

export type ErrorBody = {
  message: string
  code: string
}
