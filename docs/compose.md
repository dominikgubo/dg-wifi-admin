# Complete Docker Compose stack

`docker-compose.yml` contains the platform mock, PostgreSQL, the Spring Boot
backend, the React frontend, and the local Keycloak realm. Use exactly one
profile for a complete stack:

## No authorization

```bash
docker compose --profile no-auth up -d --build
```

This starts the frontend at <http://localhost:5173>, the REST API at
<http://localhost:8081>, PostgreSQL, and Mockoon at <http://localhost:8080>.

## Authorization enabled

```bash
docker compose --profile auth up -d --build
```

This starts the same services plus Keycloak at <http://localhost:8090>. The
backend waits for Keycloak health, PostgreSQL readiness, and the platform mock
before it starts. The frontend's `Bearer token` field accepts an access token
from the local realm.

If a default host port is already in use, override it before starting the
profile. The frontend API URL and CORS origin follow the overrides:

```powershell
$env:HOST_BACKEND_PORT = "18081"
$env:HOST_FRONTEND_PORT = "15173"
docker compose --profile no-auth up -d --build
```

Do not enable both profiles at once because they publish the same frontend and
backend ports. Stop the active profile before switching:

```bash
docker compose --profile no-auth down
docker compose --profile auth down
```

The secured stack uses the test-only Keycloak credentials already documented in
[`optional.md`](optional.md). The token endpoint is:

```text
http://localhost:8090/realms/wifi-admin/protocol/openid-connect/token
```

The issued token is valid for the backend's Docker-network issuer
`http://keycloak:8080/realms/wifi-admin`. For a quick smoke test, request a
read token with the `wifi-read-client` client and call:

```powershell
curl.exe -i http://localhost:8081/wifi-parameter/CPE_001 `
  -H "Authorization: Bearer $readToken"
```

Compose uses health checks and conditional `depends_on` entries instead of
fixed sleeps. PostgreSQL migrations and the initial platform synchronization
therefore run only after their dependencies are ready.
