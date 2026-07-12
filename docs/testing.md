# Testing

## Unit tests

The unit tests cover:

- WiFi encryption/password validation.
- Defaulting missing encryption to `OPEN`.
- REST/domain mapping.
- SOAP fault classification.

## Controller tests

Controller tests verify successful GET responses, invalid PUT requests, unknown CPE handling, and platform failure mapping.

## SOAP integration tests

The SOAP integration tests use an in-process HTTP server. They verify the generated CXF client sends the expected SOAPAction, operation, CPE ID, configuration values, and response mapping.

XML prefixes are not part of the contract. Tests verify element names and values rather than requiring a specific prefix.

## PostgreSQL integration tests

`WifiConfigurationDatabaseIT` uses Testcontainers with the same PostgreSQL major version as the development database. It verifies Flyway, the JPA repository, the database mirror, and database-only GET behavior. MockWebServer remains the fast in-process server for SOAP protocol tests.

The two local test modes are:

```bash
mvn verify
mvn -Pintegration verify
```

The first command runs the Docker-independent suite and the JaCoCo gate. The second includes the PostgreSQL container tests. GitHub Actions uses the integration profile on a runner with Docker support.

## Coverage and pull requests

JaCoCo writes the report to `target/site/jacoco` and fails `verify` below 80% Java line coverage. The current quality target is 85%. Generated CXF classes and the trivial Spring Boot launcher are excluded; application, controller, mapper, validator, persistence, scheduler, and security code remain included.

## Unused imports and variables

The quality profile checks handwritten production and test Java sources with Checkstyle and PMD:

```bash
mvn -Pquality verify
```

Checkstyle detects unused imports. PMD detects unused local variables and unused assignments. The separate `Unused Java code` GitHub workflow converts the reports into exact pull request review comments and GitHub annotations, then fails when any finding exists. Generated CXF sources are outside the analyzed source roots.

## Local smoke test

Start the complete no-authorization stack:

```bash
docker compose --profile no-auth up -d --build
```

Run the test suite:

```bash
mvn clean verify
```

The backend listens on `http://localhost:8081`.

Read a configuration:

```bash
curl http://localhost:8081/wifi-parameter/CPE_001
```

Update a configuration:

```bash
curl -X PUT http://localhost:8081/wifi-parameter \
  -H "Content-Type: application/json" \
  -d '{"cpeId":"CPE_001","wifiBand":"BAND_2_4_GHZ","ssid":"Office-2G-New","encryptionType":"WPA2_PSK","password":"new-secret"}'
```

The Mockoon data is in memory. Restarting the container restores the seed values.
