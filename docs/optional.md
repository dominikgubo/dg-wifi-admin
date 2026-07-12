# Database, security, and frontend features

## PostgreSQL mirror

Start the complete no-authorization stack with the database mirror enabled:

```bash
docker compose --profile no-auth up -d --build
```

The profile reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Flyway creates the `wifi_configuration` table. This is the standard runtime profile: GET reads from PostgreSQL only, while PUT updates the platform first and then stores the platform response.

The first database population can be enabled with `PLATFORM_SYNC_ON_STARTUP=true`. The configured CPE IDs are controlled by `PLATFORM_SYNC_CPE_IDS`.

## Synchronization

The nightly synchronization is configured with:

- `PLATFORM_SYNC_CPE_IDS`
- `PLATFORM_SYNC_MAX_CPES`
- `PLATFORM_SYNC_CRON`
- `PLATFORM_SYNC_ZONE`
- `PLATFORM_SYNC_ON_STARTUP`

The scheduler processes a bounded, stable list and continues after an individual CPE fails. One application instance runs one synchronization at a time.

## Security and operations

Actuator exposes health, info, and metrics endpoints. Set `SECURITY_ENABLED=true` and provide `SECURITY_ISSUER_URI` and `SECURITY_AUDIENCE` to enable bearer JWT authentication. The issuer must support OIDC discovery and publish signing keys through its JWKS endpoint. GET requires `wifi:read`; PUT requires `wifi:write`.

### Local Keycloak smoke test

The repository includes a Keycloak development container in the `auth` profile. It imports the `wifi-admin` realm with the following test-only credentials:

- Realm: `wifi-admin`
- Read client: `wifi-read-client`
- Write client: `wifi-write-client`
- User: `test-user`
- Password: `test-password`
- API audience: `wifi-admin-api`

Start the secured application and Keycloak together:

```powershell
docker compose --profile auth up -d --build
```

Compose waits for Keycloak's readiness check before starting the secured backend because Spring performs OIDC discovery while creating the JWT decoder. The container uses `start-dev` and the credentials above only for local development; do not use them in production.

Request a read token:

```powershell
$tokenEndpoint = "http://localhost:8090/realms/wifi-admin/protocol/openid-connect/token"

$readToken = (Invoke-RestMethod -Method Post -Uri $tokenEndpoint `
  -ContentType "application/x-www-form-urlencoded" `
  -Body @{
    client_id = "wifi-read-client"
    grant_type = "password"
    username = "test-user"
    password = "test-password"
    scope = "openid"
  }).access_token
```

Use it in the standard bearer header:

```powershell
curl.exe -i "http://localhost:8081/wifi-parameter/CPE_001" `
  -H "Authorization: Bearer $readToken"
```

Request a write token and use it for PUT:

```powershell
$writeToken = (Invoke-RestMethod -Method Post -Uri $tokenEndpoint `
  -ContentType "application/x-www-form-urlencoded" `
  -Body @{
    client_id = "wifi-write-client"
    grant_type = "password"
    username = "test-user"
    password = "test-password"
    scope = "openid"
  }).access_token

$payloadPath = Join-Path $env:TEMP "wifi-update.json"

@'
{
  "cpeId": "CPE_001",
  "wifiBand": "BAND_2_4_GHZ",
  "ssid": "Office-2G-Updated",
  "encryptionType": "WPA2_PSK",
  "password": "new-secret"
}
'@ | Set-Content -LiteralPath $payloadPath -Encoding utf8

curl.exe -i -X PUT "http://localhost:8081/wifi-parameter" `
  -H "Authorization: Bearer $writeToken" `
  -H "Content-Type: application/json" `
  --data-binary "@$payloadPath"
```

The expected results are: no token returns `401`, the read token permits GET but returns `403` for PUT, the write token permits PUT, and `/actuator/health` returns `200` without a token. To re-import the realm after changing the JSON, stop and recreate the local Keycloak container with `docker compose --profile auth down` followed by `docker compose --profile auth up -d --build`.

Configure the hosted identity provider with an API/resource-server identifier matching `SECURITY_AUDIENCE`, then create the `wifi:read` and `wifi:write` scopes. Obtain a JWT from the provider using its normal client or user flow; this application validates tokens but does not issue them.

PowerShell example:

```powershell
$env:SECURITY_ENABLED = "true"
$env:SECURITY_ISSUER_URI = "https://idp.example.com/issuer"
$env:SECURITY_AUDIENCE = "wifi-admin-api"
$env:SECURITY_ALLOWED_ORIGINS = "http://localhost:5173"

mvn spring-boot:run
```

Send the token in the standard bearer header:

```powershell
$token = "<JWT issued by the configured provider>"

curl.exe -i "http://localhost:8081/wifi-parameter/CPE_001" `
  -H "Authorization: Bearer $token"
```

For updates, use the same header together with `Content-Type: application/json`. A missing or invalid token returns `401`; a valid token without the required scope returns `403`. `/actuator/health` remains public.

Set `SECURITY_ALLOWED_ORIGINS` to a comma-separated CORS allowlist. The default security mode is disabled for local development and is not suitable for production.

## Frontend

The React frontend is under `frontend`:

```bash
cd frontend
npm install
npm run dev
```

Set `VITE_API_BASE_URL` when the backend is not available through the Vite development proxy.
