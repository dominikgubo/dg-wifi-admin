# Code tour

This document is the shortest route through the application. Read it before changing a class or adding another abstraction.

## Start here

1. Read [openapi/openapi.yaml](../openapi/openapi.yaml) to understand the REST payload and status codes.
2. Read [wsdl/wifi-platform.wsdl](../wsdl/wifi-platform.wsdl) to understand the SOAP operations and types.
3. Start the platform mock with `docker compose up -d`.
4. Start the backend with `mvn spring-boot:run -Dspring-boot.run.profiles=local`.
5. Run `GET /wifi-parameter/CPE_001`.
6. Trace `WifiParameterController` to `WifiService`, then to `WifiPlatformClient` and `CxfWifiPlatformClient`.
7. Run a PUT with a changed SSID and then repeat the GET.
8. Read the matching controller and SOAP tests.
9. Only then inspect the optional database and scheduler code.

## System map

```text
REST client
    |
    | JSON over HTTP
    v
controllers.WifiParameterController
    |
    | API payload mapping and validation
    v
services.wifi.WifiService
    |
    | mandatory mode: SOAP platform
    | database profile: database mirror for GET, SOAP first for PUT
    v
services.platform.WifiPlatformClient
    |
    v
clients.soap.CxfWifiPlatformClient
    |
    | generated CXF classes and SOAP 1.1
    v
External WiFi platform
```

## Package responsibilities

| Package | Responsibility |
|---|---|
| `controllers` | HTTP routes, request binding, and REST error handling |
| `services.wifi` | GET and PUT use-case orchestration |
| `services.platform` | Interface for the external WiFi platform boundary |
| `services.synchronization` | Bounded scheduled synchronization of configured CPE IDs |
| `clients.soap` | Reusable CXF SOAP client and SOAP fault classification |
| `mappers` | Explicit conversions at API, SOAP, and persistence boundaries |
| `validators` | Request-level business rules for encryption and passwords |
| `models.api` | JSON request and response records |
| `models.domain` | Internal WiFi configuration model and enums |
| `persistence.entities` | JPA entities; they do not leave the persistence layer |
| `persistence.repositories` | Spring Data repository interfaces |
| `persistence.stores` | Database mirror abstraction and JPA implementation |
| `configuration` | Properties, SOAP endpoint setup, scheduling, and security beans |
| `exceptions` | Application exceptions used by the service and adapters |

Generated SOAP classes are produced in `target/generated-sources/cxf` from the repository WSDL. They are not handwritten, manually edited, or committed.

## GET flow

1. `WifiParameterController` receives a nonblank `cpeId`.
2. `WifiService.getConfiguration` checks whether a `WifiConfigurationStore` exists.
3. In the default/local profile no store exists, so the service calls `WifiPlatformClient.getConfiguration`.
4. `CxfWifiPlatformClient` creates a generated `GetCpeIdRequest` and sends SOAP 1.1 with the WSDL action.
5. `SoapWifiConfigurationMapper` converts the generated response to the domain record. Missing encryption becomes `OPEN`; a blank password becomes `null`.
6. `WifiConfigurationMapper` converts the domain record to the JSON response.

With the `database` profile, GET reads only from the mirror. A missing row becomes `CPE_NOT_FOUND`; it does not fall back to SOAP.

## PUT flow

1. Spring binds the JSON body to `WifiConfigurationPayload`.
2. Jakarta validation checks required fields and enum values.
3. `WifiConfigurationValidator` enforces the single encryption/password rule.
4. `WifiConfigurationMapper` converts the payload to the domain record.
5. `WifiService` calls `WifiPlatformClient.updateConfiguration` first.
6. `CxfWifiPlatformClient` sends the complete configuration through the generated `updateCpeId` operation. The name is misleading: it updates WiFi settings, not just an identifier.
7. The response from the platform is mapped back and returned. In database mode that returned configuration is then saved to the mirror.

## SOAP boundary

The platform namespace is `http://wifi-admin.local/platform/v1`.

| REST action | SOAP operation | SOAPAction |
|---|---|---|
| GET | `getCpeID` | `http://wifi-admin.local/platform/v1#getCpeID` |
| PUT | `updateCpeId` | `http://wifi-admin.local/platform/v1#updateCpeId` |

The client is a singleton Spring bean. Its endpoint and timeouts come from `platform.url`, `platform.connect-timeout`, and `platform.read-timeout`; the WSDL address is not used for runtime routing. SOAP bodies are not logged because they contain passwords.

## Database profile

The database profile enables Flyway, PostgreSQL, JPA, and the mirror store. `GET` is database-only. `PUT` is platform-first and persists the platform response. A database availability failure is mapped to HTTP 503 with `MIRROR_UNAVAILABLE`.

The scheduler uses `platform.sync.cpe-ids` because the WSDL has no operation for discovering all CPE IDs. It processes the IDs in order, limits them with `platform.sync.max-cpes`, continues after an individual failure, and never calls `updateCpeId`.

## Error mapping

| Cause | HTTP status | Code |
|---|---:|---|
| Invalid JSON, missing field, invalid enum, or password rule | 400 | `VALIDATION_ERROR` |
| SOAP fault identifying an unknown CPE | 404 | `CPE_NOT_FOUND` |
| Timeout, connection error, malformed SOAP, or unexpected SOAP fault | 502 | `PLATFORM_ERROR` |
| Database mirror unavailable | 503 | `MIRROR_UNAVAILABLE` |

The SOAP adapter classifies fault content rather than using HTTP 500 alone, because the mock can return a not-found SOAP fault with HTTP 500.

## Test map

| Behavior | Test |
|---|---|
| REST GET, PUT, validation, 404, and 502 responses | `controllers.WifiParameterControllerTest` |
| Encryption/password business rules | `validators.WifiConfigurationValidatorTest` |
| REST/domain conversion | `mappers.api.WifiConfigurationMapperTest` |
| SOAP action, namespace, operation, and response conversion | `clients.soap.CxfWifiPlatformClientIntegrationTest` |
| SOAP not-found classification | `clients.soap.SoapFaultClassifierTest` |
| Platform-first PUT and database-first GET orchestration | `services.wifi.WifiServiceTest` |
| Entity conversion and database failure mapping | `mappers.persistence.WifiConfigurationEntityMapperTest`, `persistence.stores.JpaWifiConfigurationStoreTest` |
| Scheduler limit and failure isolation | `services.synchronization.PlatformSynchronizerTest` |
| Real PostgreSQL schema and mirror behavior | `persistence.WifiConfigurationDatabaseIT` |
| Application default profile and security beans | `configuration.ApplicationContextTest`, `configuration.SecurityConfigurationTest` |

Run the fast suite with `mvn verify`. Run the Docker-backed PostgreSQL suite with `mvn -Pintegration verify`.

## Runtime profiles and configuration

- Default/local: stateless REST-to-SOAP flow, database and security disabled.
- `database`: PostgreSQL mirror, Flyway, and scheduler enabled.
- Production: provide external endpoint, database, security issuer, and CORS values through environment variables or a secret manager.

The backend listens on port 8081 by default. Important overrides include `SERVER_PORT`, `PLATFORM_URL`, `PLATFORM_CONNECT_TIMEOUT`, `PLATFORM_READ_TIMEOUT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PLATFORM_SYNC_CPE_IDS`, `PLATFORM_SYNC_MAX_CPES`, `SECURITY_ENABLED`, and `SECURITY_ISSUER_URI`.
