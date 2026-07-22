# Built In Chicago local discovery

CareerOS can use Built In Chicago as a secondary discovery source for a
bounded, manual import. This integration is intentionally restricted to the
`local` and `docker` Spring profiles and is disabled in the default
configuration. It is designed for a single user's local CareerOS instance;
it is not a hosted crawler or a scheduled monitoring service.

## Use it

Docker Compose enables the feature by default because its `docker` profile is
the local containerized development environment. Open **Companies** and click
**Sync Built In**. The button appears only when the backend status endpoint is
available and the feature flag is enabled.

To disable it in Docker Compose:

```bash
BUILTIN_CHICAGO_ENABLED=false docker compose up --build -d
```

When running the backend from source, activate the local profile:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

The import can also be started directly:

```bash
curl -X POST http://localhost:8080/api/v1/aggregators/builtin-chicago/runs
```

`GET /api/v1/aggregators/builtin-chicago/status` reports whether the local
adapter is available. Both endpoints are absent outside `local`/`docker` or
when the flag is off.

## Safety boundaries

- Manual invocation only; the scheduler never calls this source.
- One server-rendered search-results page is requested per run. At most 25 job
  cards are imported by default; the hard configuration maximum is 50.
- A descriptive CareerOS personal-use user agent is sent.
- The adapter reads public job listing metadata and retains source attribution.
- The Built In listing URL is retained as the application and attribution URL;
  CareerOS does not request Cloudflare-protected job-detail pages.
- Existing jobs are skipped by canonical application URL to reduce duplicates.
- Newly discovered employers are created disabled, so they do not silently
  enter normal provider scheduling.

The defaults can be adjusted only in the local profile under
`careeros.aggregators.builtin-chicago`, including `search-path` and `max-jobs`.
Site terms and markup can change; review them before use
and disable the adapter if the permitted use changes.

## Data model

Imported postings use `sourceType=AGGREGATOR` and retain the original Built In
listing as `sourceUrl`. Normal ATS/provider jobs use
`sourceType=DIRECT_PROVIDER`. The Jobs page exposes this distinction and the
detail drawer links back to the attributed listing.
