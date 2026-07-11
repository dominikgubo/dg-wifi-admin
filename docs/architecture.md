# WiFi Admin Backend

## Responsibility

The backend is an anti-corruption layer between a REST-only application and the existing SOAP WiFi platform.

The REST client uses `cpeId` to identify the customer-premises router. The SOAP platform uses the same identifier inside `getCpeID` requests and inside the configuration sent to `updateCpeId`.

`updateCpeId` is the platform operation name. It updates the complete WiFi configuration and does not mean that the backend changes only a CPE identifier.

## Request flow

```text
REST client
    |
    | JSON
    v
WifiParameterController
    |
    v
WifiService
    |
    v
WifiPlatformClient
    |
    | SOAP 1.1 document/literal
    v
External WiFi platform
```

The controller owns HTTP and validation concerns. The application service owns the use case. The SOAP adapter owns generated SOAP types, namespaces, endpoint configuration, and fault translation.

## SOAP contract

The client is generated from `wsdl/wifi-platform.wsdl` during the Maven `generate-sources` phase.

| Operation | SOAPAction |
|---|---|
| `getCpeID` | `http://wifi-admin.local/platform/v1#getCpeID` |
| `updateCpeId` | `http://wifi-admin.local/platform/v1#updateCpeId` |

The SOAP endpoint is configurable. The local default is `http://localhost:8080/platform`.

## Validation

Missing encryption defaults to `OPEN`. A secure encryption type requires a nonblank password. An `OPEN` network must not contain a nonblank password. These rules are enforced before the SOAP platform is called.

## Error mapping

| Situation | HTTP status | Code |
|---|---:|---|
| Invalid request or business validation failure | 400 | `VALIDATION_ERROR` |
| Platform reports an unknown CPE | 404 | `CPE_NOT_FOUND` |
| Timeout, connection failure, malformed response, or other SOAP fault | 502 | `PLATFORM_ERROR` |

SOAP faults are classified by their fault code or fault text because the mock platform can return a not-found SOAP fault with HTTP 500.

## Logging policy

Application logs must not contain passwords, SOAP bodies, authorization tokens, or database credentials. The SOAP client is stateless and reused for all requests.
