# ADR 0006: Dedicated Microsoft Careers provider

Status: Accepted

## Context

Microsoft's current careers experience is backed by Eightfold/PCSX and
SuccessFactors rather than one of CareerOS's standard ATS connectors. The
Microsoft company was consequently stored as `OTHER`, disabled, and had no
provider configuration. Generic HTML is a poor fit because search results are
loaded from a public JSON endpoint and the rendered page is not a stable job
feed.

## Decision

Add `MICROSOFT_CAREERS` as a company-specific implementation of the existing
`JobProvider` port. The provider calls Microsoft's public PCSX search endpoint,
uses stable position IDs and canonical application paths, and normalizes
location, posting date, department, and work-location mode.

Search query, location, and result limit are company configuration rather than
hardcoded orchestration policy. The adapter restricts requests to the official
HTTPS host, caps results, and retries transient failures. The existing
Microsoft row is migrated from disabled `OTHER` monitoring to the dedicated
provider.

## Consequences

- Microsoft jobs participate in normal ingestion, scoring, dashboard, and
  application workflows.
- Scheduler and registry logic remain unchanged.
- Search synchronization is intentionally bounded and does not attempt to
  mirror Microsoft's entire catalog.
- The search response provides summary metadata rather than the full job
  description; full-detail enrichment can be added independently if needed.
- Other Eightfold tenants may later justify a generic Eightfold provider, but
  Microsoft-specific configuration is isolated until their contracts are
  verified.
