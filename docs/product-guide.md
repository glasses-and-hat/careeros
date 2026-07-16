# CareerOS Product and Operations Guide

CareerOS is a self-hosted job discovery and application-management platform.
It collects normalized jobs from multiple applicant tracking systems (ATS),
scores them deterministically against personal preferences, organizes target
companies, tracks every application stage, calculates follow-up reminders, and
presents the workflow through a React application and REST API.

This guide covers the complete project as implemented through Milestone 4.

## What CareerOS does

CareerOS has two major parts:

- A Java 21/Spring Boot backend backed by PostgreSQL. It owns the domain model,
  ATS ingestion, matching, reminders, analytics, persistence, validation, and
  OpenAPI contract.
- A React 19/Vite frontend. It is the primary daily interface and uses an
  OpenAPI-generated typed client with TanStack Query.

CareerOS is currently single-tenant. Preferences and data belong to the one
person operating the installation; authentication and multi-user ownership are
not implemented yet.

The matching engine is deliberately deterministic. It does not use OpenAI,
embeddings, semantic search, or a vector database. Every score can be explained
from role, technology, location, remote preference, company priority, and
recency inputs.

## Recommended daily workflow

### Initial setup

1. Start PostgreSQL, the backend, and the frontend.
2. Add companies through `POST /api/v1/companies` or Swagger UI. Select the ATS
   and provide its board identifier.
3. Create preferences through `POST /api/v1/preferences`. Include target roles,
   technologies, locations, minimum score, remote preference, salary range,
   ignored companies, and ignored keywords.
4. Create watchlists such as Dream Companies, Chicago, Remote, Startups,
   FinTech, or Cloud.
5. Trigger the first ATS ingestion run.

### Morning review

1. Open the Dashboard and scan new jobs, high matches, follow-ups, and the top
   ranked opportunities.
2. Open Jobs and search or sort the scored results. Jobs below 60 are hidden by
   default in the frontend.
3. Open a job row to inspect component scores and the match explanation.
4. Add interesting jobs to the application tracker as `WISHLIST` or `APPLIED`.
5. Review Notifications for follow-ups and interviews.

### During the day

1. Move application cards across the Kanban board as conversations progress.
   Status changes use optimistic updates and are persisted to the backend.
2. Record recruiter names, referrals, interview dates, follow-up dates, notes,
   resume versions, and the job link through the Applications API.
3. Run saved searches for frequently used combinations such as Remote Java or
   Senior Backend.

### Weekly review

1. Review Analytics for response, interview, and offer rates.
2. Compare applications by company and discovered jobs by ATS.
3. Adjust preferences, ignored keywords, company priority, and minimum score
   based on the quality of results.
4. Disable noisy companies or split them into more focused watchlists.

## Running CareerOS

### Prerequisites

- Java 21 or newer
- Docker and Docker Compose
- Node.js 20 or newer with npm

The Maven wrapper is included, so a system Maven installation is unnecessary.

### Option 1: backend with Docker Compose, frontend with Vite

This is the simplest full development setup.

```bash
cp .env.example .env
docker compose up --build -d
```

Wait until the backend is healthy:

```bash
curl http://localhost:8080/actuator/health
```

Then start the frontend:

```bash
cd frontend
npm install
npm run dev
```

Open:

- Frontend: `http://localhost:5173`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health endpoint: `http://localhost:8080/actuator/health`

Vite proxies `/api` requests to the backend at `localhost:8080`.

Stop the services without deleting data:

```bash
docker compose down
```

PostgreSQL data is stored in the named `careeros-postgres-data` volume. Running
`docker compose down -v` deletes that volume and all CareerOS data.

### Option 2: PostgreSQL in Docker, backend from source

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The default database configuration is:

| Setting | Default |
|---|---|
| Database | `careeros` |
| Username | `careeros` |
| Password | `careeros` |
| PostgreSQL port | `5432` |
| Backend port | `8080` |

Override these values in `.env` for Docker or with `DB_NAME`, `DB_USERNAME`,
`DB_PASSWORD`, `DB_HOST`, `DB_PORT`, and `SERVER_PORT` environment variables.

### Production frontend build

```bash
cd frontend
npm ci
npm run build
npm run preview
```

The deployable static output is written to `frontend/dist`. In production,
serve that directory from a static host or reverse proxy and route `/api` to
the Spring Boot service. Configure history fallback to `index.html` so direct
navigation to `/jobs` or `/analytics` works.

## Frontend pages

### Dashboard — `/`

The landing page answers the most important daily questions in one request:

- New jobs today
- High-match jobs
- Applications in progress
- Interview activity
- Follow-ups needing attention
- Highest-ranked jobs
- Recently discovered jobs
- Upcoming reminders

Use this as the morning starting point. Top-match cards show company, role,
location, remote status, posted date, and overall match score.

### Jobs — `/jobs`

The jobs table presents the last 30 days of scored jobs. It supports global
search, sorting, pagination, keyboard row selection, and a detail drawer.

The drawer breaks down role, technology, location, company, and recency scores
and lists the backend match explanation. Match colors mean:

- 90–100: excellent
- 75–89: good
- 60–74: moderate
- Below 60: hidden by default

### Applications — `/applications`

Applications are shown as a horizontally scrollable Kanban board. The columns
cover Wishlist, Applied, Recruiter Contacted, Recruiter Screen, Technical,
System Design, Behavioral, Final, Offer, and Rejected.

Drag a card to change its status. The frontend updates immediately, rolls back
on failure, and refreshes application data after the backend responds.

### Companies — `/companies`

Shows registered companies, ATS type, priority, monitoring state, and careers
link. Company creation and complete editing are available through the REST API
and Swagger UI; the current page focuses on monitoring visibility.

### Watchlists — `/watchlists`

Shows company groups and each membership's numeric priority and enabled state.
Use separate watchlists for different strategies rather than putting every
company into one list. Full membership management is available through the
watchlist API.

### Saved Searches — `/searches`

Create reusable searches with a name, target title, minimum score, and remote
preference, then execute them with one click. The API additionally supports
company, location, technology, and sorting fields.

### Analytics — `/analytics`

Displays monthly application totals, response rate, interview rate, offer
rate, average match score, pipeline funnel, ATS distribution, and applications
by company. Analytics become more useful when application statuses and dates
are updated consistently.

### Settings — `/settings`

Controls light, dark, or system theme and documents keyboard shortcuts. Theme
and collapsed-sidebar state are persisted in the browser.

### Global interface

- Press `Ctrl + K` or `Cmd + K` to open the command palette.
- Press `G`, then `D` for Dashboard.
- Press `G`, then `J` for Jobs.
- Press `G`, then `A` for Applications.
- Use the bell button to open follow-up and interview notifications.

## REST API reference

Swagger UI is the authoritative interactive reference. List endpoints accept
Spring pagination and sorting parameters such as `page=0`, `size=20`, and
`sort=postedDate,desc`.

### Dashboard and discovery

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/dashboard` | Aggregated daily dashboard read model |
| GET | `/api/reminders` | Follow-up, inactivity, no-response, and interview reminders |
| GET | `/api/analytics` | Aggregated job and application statistics |
| GET | `/api/jobs/timeline` | Scored jobs by time period and filters |

Timeline supports `period` (`TODAY`, `YESTERDAY`, `LAST_7_DAYS`, or
`LAST_30_DAYS`), `companyId`, `minimumScore`, `remote`, `location`, `ats`, and
pagination/sorting parameters.

### Companies

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/companies` | Register a monitored company |
| GET | `/api/v1/companies` | List/filter companies |
| GET | `/api/v1/companies/{id}` | Get one company |
| PUT | `/api/v1/companies/{id}` | Update company details and monitoring state |
| DELETE | `/api/v1/companies/{id}` | Delete a company |

Company filters include `name`, `atsType`, `priority`, and `enabled`.

Supported ATS types are `GREENHOUSE`, `LEVER`, `ASHBY`, `WORKDAY`,
`SMARTRECRUITERS`, and `OTHER`. Except for `OTHER`, a connector-specific
`atsIdentifier` is required.

### ATS ingestion

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/ingestion/runs` | Synchronize every enabled company |
| POST | `/api/v1/ingestion/companies/{companyId}/runs` | Synchronize one company |

The all-company run isolates failures: one ATS or company failure does not stop
the remaining companies. Existing `(company, externalId)` jobs are skipped.

Scheduled ingestion is disabled by default. Enable and tune it with:

```yaml
careeros:
  ingestion:
    enabled: true
    poll-interval: 30m
    connect-timeout: 5s
    read-timeout: 10s
```

### Jobs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/jobs` | Create a normalized posting manually |
| GET | `/api/v1/jobs` | List/filter postings |
| GET | `/api/v1/jobs/{id}` | Get complete posting details |
| PUT | `/api/v1/jobs/{id}` | Update a posting |
| DELETE | `/api/v1/jobs/{id}` | Delete a posting |

Job filters include `companyId`, `title`, `location`, `employmentType`,
`remote`, and `postedAfter`.

### Preferences

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/preferences` | Create matching preferences |
| GET | `/api/v1/preferences` | List preference records |
| GET | `/api/v1/preferences/{id}` | Get preferences |
| PUT | `/api/v1/preferences/{id}` | Update preferences |
| DELETE | `/api/v1/preferences/{id}` | Delete preferences |

The most recently updated preference record is used by discovery. For a clear
single-tenant workflow, keep one preference record and update it instead of
creating several.

### Applications

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/applications` | Track a job |
| GET | `/api/applications` | List tracked applications |
| GET | `/api/applications/{id}` | Get an application |
| PUT | `/api/applications/{id}` | Update status and details |
| DELETE | `/api/applications/{id}` | Remove an application |

Supported statuses are `WISHLIST`, `APPLIED`, `RECRUITER_CONTACTED`,
`REFERRAL_REQUESTED`, `RECRUITER_SCREEN`, `TECHNICAL_INTERVIEW`,
`SYSTEM_DESIGN`, `BEHAVIORAL`, `FINAL_INTERVIEW`, `OFFER`, `REJECTED`, and
`WITHDRAWN`.

### Watchlists

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/watchlists` | Create a watchlist |
| GET | `/api/watchlists` | List watchlists |
| GET | `/api/watchlists/{id}` | Get a watchlist |
| PUT | `/api/watchlists/{id}` | Rename a watchlist |
| PUT | `/api/watchlists/{id}/companies/{companyId}` | Add/update membership |
| DELETE | `/api/watchlists/{id}/companies/{companyId}` | Remove a company |
| DELETE | `/api/watchlists/{id}` | Delete a watchlist |

Membership priority is an integer from 0 to 100.

### Saved searches

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/saved-searches` | Create a saved search |
| GET | `/api/saved-searches` | List searches |
| GET | `/api/saved-searches/{id}` | Get a search |
| PUT | `/api/saved-searches/{id}` | Update a search |
| POST | `/api/saved-searches/{id}/execute` | Execute and return jobs |
| DELETE | `/api/saved-searches/{id}` | Delete a search |

## Useful API examples

Register a Greenhouse company:

```bash
curl -X POST http://localhost:8080/api/v1/companies \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Example Corp",
    "careerUrl": "https://boards.greenhouse.io/example",
    "atsType": "GREENHOUSE",
    "priority": "HIGH",
    "enabled": true,
    "atsIdentifier": "example"
  }'
```

Create preferences:

```bash
curl -X POST http://localhost:8080/api/v1/preferences \
  -H 'Content-Type: application/json' \
  -d '{
    "roles": ["Senior Backend Engineer", "Staff Engineer"],
    "technologies": ["Java", "Spring Boot", "Kafka", "AWS"],
    "locations": ["Chicago", "Remote"],
    "minimumScore": 75,
    "remoteOnly": false,
    "salaryMin": 160000,
    "salaryMax": 240000,
    "salaryCurrency": "USD",
    "ignoredCompanies": [],
    "ignoredKeywords": ["clearance required"],
    "visaSponsorshipPreferred": false
  }'
```

Run ingestion and inspect today's matches:

```bash
curl -X POST http://localhost:8080/api/v1/ingestion/runs
curl 'http://localhost:8080/api/jobs/timeline?period=TODAY&minimumScore=75'
```

Track an application:

```bash
curl -X POST http://localhost:8080/api/applications \
  -H 'Content-Type: application/json' \
  -d '{
    "jobPostingId": "JOB_UUID",
    "status": "APPLIED",
    "appliedDate": "2026-07-15",
    "followUpDate": "2026-07-22",
    "resumeVersion": "backend-v3",
    "recruiterName": "Jordan Lee",
    "notes": "Applied through referral"
  }'
```

## Matching and reminders

Default matching weights are configured in `application.yml`:

| Component | Weight |
|---|---:|
| Role | 30% |
| Technology | 30% |
| Location | 15% |
| Remote preference | 10% |
| Company priority | 10% |
| Recency | 5% |

Weights must total 100 or the backend refuses to start. Matching is calculated
at read time, so preference changes affect the next dashboard or timeline
request without a batch rescore.

Reminders are derived automatically for:

- Follow-up dates that are due
- Interviews scheduled for tomorrow
- Applied jobs with no recruiter response after seven days
- Non-terminal applications inactive for fourteen days

Accurate dates and statuses are therefore essential to useful reminders and
analytics.

## Development and testing

Backend unit tests:

```bash
./mvnw test
```

Backend integration tests with Testcontainers:

```bash
./mvnw verify
```

Frontend checks:

```bash
cd frontend
npm test
npm run build
npm run e2e
npm audit --omit=dev
```

Regenerate the frontend API client after an API contract change:

```bash
cd frontend
npm run api:generate
```

The generator reads `frontend/openapi/careeros.yaml` and writes models,
request functions, query hooks, and query keys under
`frontend/src/api/generated`. Do not edit generated files manually.

## Getting the best results

- Prefer a focused list of roles and technologies. Very broad preferences make
  every score less meaningful.
- Use canonical technology names found in job descriptions, such as `Spring
  Boot`, `PostgreSQL`, or `Kubernetes`.
- Keep the minimum score around 70–75 initially, then tune it after reviewing a
  week of results.
- Put genuinely unwanted terms into ignored keywords. They force a zero score,
  so use them sparingly.
- Give high company priority only to organizations where it should materially
  affect ranking.
- Always record applied and follow-up dates. They drive reminders and monthly
  analytics.
- Update statuses promptly after recruiter replies and interviews. Response,
  interview, and offer rates depend on the status history's current state.
- Create small, purposeful watchlists and saved searches rather than one large
  catch-all configuration.
- Use the dashboard for triage, Jobs for discovery, Applications for execution,
  and Analytics for weekly strategy changes.

## Troubleshooting

### Backend cannot connect to PostgreSQL

Confirm the container is healthy with `docker compose ps`, check `.env`, and
verify port 5432 is not occupied by another local PostgreSQL installation.

### Frontend shows network errors

Confirm the backend health endpoint responds on port 8080. The Vite proxy only
works when using `npm run dev`; a separately hosted production build needs a
reverse proxy or same-origin API configuration.

### No jobs appear after ingestion

Check that companies are enabled, their `atsIdentifier` matches the ATS board
slug/token, and the ingestion response reports successful companies. Also
remember that the Jobs page hides scores below 60 by default.

### Dashboard scores look wrong

Inspect `/api/v1/preferences`, ensure the intended record is the most recently
updated one, and review each job's `matchExplanation` from the timeline API.

### Swagger or generated client is out of date

Use the live `/v3/api-docs` output as the backend source of truth, update the
checked-in frontend OpenAPI snapshot, run `npm run api:generate`, and rebuild.

## Additional documentation

- [Main README](../README.md)
- [Architecture](architecture.md)
- [Deterministic scoring ADR](adr/0003-deterministic-read-time-job-scoring.md)
- [Dashboard read-model ADR](adr/0004-dashboard-composed-read-model.md)
