# Optional features

The optional features are disabled by default. The mandatory mode remains stateless and talks directly to the SOAP platform.

## PostgreSQL mirror

Start PostgreSQL and the platform mock with the database profile:

```bash
docker compose --profile database up -d
```

Start the backend with the `database` profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=database
```

The profile reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Flyway creates the `wifi_configuration` table. GET reads from PostgreSQL only. PUT updates the platform first and then stores the platform response.

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

Actuator exposes health, info, and metrics endpoints. Set `SECURITY_ENABLED=true` and provide `SECURITY_ISSUER_URI` to enable bearer JWT authentication. GET requires `wifi:read`; PUT requires `wifi:write`.

Set `SECURITY_ALLOWED_ORIGINS` to a comma-separated CORS allowlist. The default security mode is disabled for local development and is not suitable for production.

## Frontend

The React frontend is under `frontend`:

```bash
cd frontend
npm install
npm run dev
```

Set `VITE_API_BASE_URL` when the backend is not available through the Vite development proxy.
