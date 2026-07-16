# ADR 0003: Deterministic read-time job scoring

Status: Accepted

## Context

Daily discovery needs explainable ranking that responds immediately to user
preference changes. AI, embeddings, and semantic search are explicitly out of
scope for Milestone 3.

## Decision

CareerOS uses a normalized, case-insensitive rule engine with externally
configured weights. Role, technology, location, remote preference, company
priority, and recency each produce a 0–100 component. The weighted total is
rounded to an integer, and ignored companies/keywords force a zero score.
Scores are calculated in the application read path rather than persisted.

## Consequences

The same inputs always produce the same output and every result contains a
component explanation. Preference updates need no rescore job. Read cost is
linear in the bounded candidate set; a future persisted projection may replace
the read adapter if volume requires it.
