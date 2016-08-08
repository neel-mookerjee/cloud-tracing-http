# Cloud Tracing
Cloud Tracing is a logging and tracing solution for Cloud applications.

## Features
---
* Adds request, trace and session ids to the Slf4J MDC, so you can trace and debug all the logs in a log aggregator
* Provides an abstraction for collecting, passing and logging traces with required and consistent information
* Instrument the following ingress and egress points from Spring applications - servlet filter, rest template
* Seamlessly provides client information to the service during an RPC call
* Apply JWT for representation claims securely
* Make Client Context information available to services

### Sample Configuration
---
```yml

cloud:
  tracing:
    enabled: true
    client:
      context:
        enabled: true

jwt:
  signature:
    verify:
      enabled: false
      ignoreNullToken: true
  expected:
    issuer: token-service
    audience: api-services
  auth:
    service:
      x509:
        url: https://token-service.apps.int.dev.mer.cfio/key/x509
```
