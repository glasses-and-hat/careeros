# ADR 0004: Dashboard as a composed read model

Status: Accepted

## Decision

`DailyDiscoveryService` owns dashboard composition and accesses data only via
domain ports. A single endpoint returns daily counts, ranked jobs, reminders,
watchlist activity, and statistics. The controller contains no aggregation or
repository access.

## Consequences

Clients avoid several round trips and cannot observe mutually inconsistent
client-side aggregates. The modular monolith can use one read-only transaction;
later optimization can introduce projection queries behind existing ports.
