# ADR 0006: Local secondary job aggregators

## Status

Accepted

## Context

Provider ingestion begins with a company and reads its authoritative careers
system. Regional job marketplaces invert that relationship: one source
discovers postings across many employers. Treating Built In Chicago as a
company provider would distort the provider registry, lose source attribution,
and risk scheduling marketplace crawling alongside ordinary ATS syncs.

## Decision

Introduce a separate `AggregatorJobSource` outbound port and an application
service that normalizes aggregator results into the existing company and job
domains. The first adapter is Built In Chicago, subject to these boundaries:

- bean and HTTP endpoint availability is restricted to `local` and `docker`;
- an explicit property controls enablement and defaults to false globally;
- runs are manual and bounded to one server-rendered listing request;
- persisted postings retain source type and source URL;
- direct employer application URLs are canonicalized and used for deduplication;
- employers unknown to CareerOS are created disabled.

The primary `JobProviderRegistry` and scheduled provider synchronization remain
unchanged.

## Consequences

CareerOS gains a useful regional discovery route without presenting a third-
party marketplace as an authoritative ATS. Local-only activation and manual
execution limit operational and terms-of-use risk. The HTML adapter remains
inherently sensitive to upstream markup changes, so parser tests and clear
failure diagnostics are required. Additional aggregators can implement the
same port, but each must make its own access-policy and deployment decision.
