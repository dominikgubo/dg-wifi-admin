# Setup and running the application

This guide is the single entry point for running WiFi Admin. The application
provides a REST API and React user interface, and it uses a SOAP WiFi platform
behind the scenes. The included Mockoon container supplies that platform for
local development.

## Prerequisites

- Docker Desktop with Docker Compose v2 for the containerized stack.
- Java 21 and Maven for a manually run backend.
- Node.js 22 or later and npm for a manually run frontend.

The default local ports are:

| Service | URL / port |
|---|---|
| Frontend | `http://localhost:5173` |
| REST API | `http://localhost:8081` |
| SOAP platform mock | `http://localhost:8080/platform` |
| PostgreSQL | `localhost:5432` |
| Keycloak (secured stack only) | `http://localhost:8090` |

## Run the complete stack with Docker

For the normal local stack, with no authorization required:

```bash
docker compose --profile no-auth up -d --build
```

Open the frontend at `http://localhost:5173`. The backend is available at
`http://localhost:8081`; its health endpoint is
`http://localhost:8081/actuator/health`.

This profile starts the frontend, backend, PostgreSQL, and the SOAP platform
mock. The backend uses the `database` Spring profile, runs Flyway migrations,
and synchronizes `CPE_001` through `CPE_004` into PostgreSQL when it starts.
GET reads that database mirror; PUT updates the SOAP platform first and then
persists its response.

To stop and remove the stack (including the Compose network), run:

```bash
docker compose --profile no-auth down
```

Add `-v` only when you also want to remove PostgreSQL data.

### Run with local authorization

Use the `auth` profile to include Keycloak and require bearer tokens:

```bash
docker compose --profile auth up -d --build
```

Do not start `auth` and `no-auth` together: both publish the backend and
frontend ports. The local Keycloak realm and test-only credentials are
documented in [optional.md](optional.md).

## Run the application manually

Use one of these two backend modes. The first is fastest for REST-to-SOAP
development; the second also uses the PostgreSQL mirror.

### Direct REST-to-SOAP mode

Start just the SOAP mock:

```bash
docker compose --profile no-auth up -d platform-mock
```

Then start the backend from the repository root:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The backend calls the mock directly. This mode does not start or use
PostgreSQL, so GET reads the SOAP platform.

### Database-mirror mode

Start the SOAP mock and PostgreSQL:

```bash
docker compose --profile no-auth up -d platform-mock postgres
```

In PowerShell, enable the database profile and populate its initial CPE set:

```powershell
$env:SPRING_PROFILES_ACTIVE = "database"
$env:PLATFORM_SYNC_ON_STARTUP = "true"
mvn spring-boot:run
```

The default connection settings match the bundled PostgreSQL service. In this
mode GET reads PostgreSQL only, so enable startup synchronization or add data
before making the first GET request.

### Run the frontend manually

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The Vite development server proxies `/wifi-parameter` to
`http://localhost:8081` by default. If the backend has another public URL,
set `VITE_API_BASE_URL` before starting Vite; for example, in PowerShell:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:18081"
npm run dev
```

## Verify the API

After either start method, read a seeded configuration:

```bash
curl http://localhost:8081/wifi-parameter/CPE_001
```

Update it with:

```bash
curl -X PUT http://localhost:8081/wifi-parameter \
  -H "Content-Type: application/json" \
  -d '{"cpeId":"CPE_001","wifiBand":"BAND_2_4_GHZ","ssid":"Office-2G-New","encryptionType":"WPA2_PSK","password":"new-secret"}'
```

The REST contract is in [../openapi/openapi.yaml](../openapi/openapi.yaml).
The mock stores changes only in memory; restarting `platform-mock` restores
its seeded data.

## Configuration reference

Spring properties can be supplied through environment variables. The values
below are the local defaults unless stated otherwise.

| Environment variable | Purpose |
|---|---|
| `SERVER_PORT` | Backend port (`8081`). |
| `PLATFORM_URL` | SOAP endpoint (`http://localhost:8080/platform`). |
| `PLATFORM_CONNECT_TIMEOUT` / `PLATFORM_READ_TIMEOUT` | SOAP timeouts (`2s` / `5s`). |
| `SPRING_PROFILES_ACTIVE` | Set to `database` for PostgreSQL, Flyway, and synchronization. |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection settings. |
| `PLATFORM_SYNC_CPE_IDS` | Comma-separated CPE IDs to mirror. |
| `PLATFORM_SYNC_MAX_CPES` | Maximum IDs processed by a synchronization run. |
| `PLATFORM_SYNC_CRON`, `PLATFORM_SYNC_ZONE` | Scheduler expression and time zone. |
| `PLATFORM_SYNC_ON_STARTUP` | Populate the mirror during application startup (`false` by default). |
| `SECURITY_ENABLED` | Require bearer JWTs (`false` by default). |
| `SECURITY_ISSUER_URI`, `SECURITY_AUDIENCE` | Required when security is enabled. |
| `SECURITY_ALLOWED_ORIGINS` | Comma-separated frontend origins permitted by CORS. |
| `HOST_FRONTEND_PORT`, `HOST_BACKEND_PORT` | Docker Compose host-port overrides. |
| `HOST_PLATFORM_PORT`, `HOST_POSTGRES_PORT`, `HOST_KEYCLOAK_PORT` | Further Compose host-port overrides. |
| `VITE_API_BASE_URL` | Frontend API URL, baked in at frontend build time. |

For production, supply the database credentials and security values from a
secret manager or deployment environment. Do not use the local PostgreSQL or
Keycloak credentials in production.

## Tests and further documentation

Run the fast backend suite with `mvn verify`; add `-Pintegration` for
Testcontainers PostgreSQL tests. See [testing.md](testing.md) for the test
matrix, [architecture.md](architecture.md) for the runtime design, and
[optional.md](optional.md) for database, security, and frontend details.
