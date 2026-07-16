# ADR 0005: Provider-based ingestion architecture

Status: Accepted

## Context

The original ATS dispatch represented five products. Companies marked `OTHER`
had no synchronization path, and provider growth risked ATS-specific branching
inside orchestration.

## Decision

CareerOS uses a `JobProvider` port and `JobProviderRegistry`. Companies store a
primary `ProviderType`, extensible JSON configuration, and ordered fallbacks.
Existing ATS adapters implement the same provider port through the compatible
`AtsConnector` interface. Generic HTML, RSS/Atom, and JSON adapters share that
contract.

The synchronization engine resolves providers only through the registry,
attempts the primary followed by fallbacks, and stops on first success. Each
attempt produces persistent history and Micrometer measurements.

`AtsType` remains temporarily for API/database compatibility, but
`providerType` controls new ingestion behavior.

## Consequences

- Unknown ATS products are no longer an architectural dead end.
- Dedicated Google, Meta, Netflix, Microsoft, or Apple providers require no
  orchestration changes.
- Runtime configuration errors are isolated, observable, and can fall back.
- Flexible JSON configuration requires each provider to validate its fields.
- Sync history adds storage, with indexes supporting health queries and future
  retention policies.
