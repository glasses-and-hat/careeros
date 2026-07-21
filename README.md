# CareerOS

CareerOS is a self-hosted career operating system for discovering jobs,
ranking them against personal preferences, monitoring target companies,
tracking applications and follow-ups, and generating factually grounded
resumes with local AI.

It combines a Java/Spring Boot backend with a React application and is built
for a practical daily workflow:

```text
Monitor companies -> ingest jobs -> rank matches -> review and apply
-> track progress -> follow up -> generate and retain resume versions
```

The [complete application guide](docs/careeros-complete-guide.md) explains
every page, API group, workflow, configuration option, and known limitation.

## What is implemented

- Pluggable job providers for Greenhouse, Lever, Ashby, Workday,
  SmartRecruiters, generic HTML, RSS/Atom, and JSON sources
- Primary and fallback provider chains, synchronization history, health
  aggregation, retries, and Micrometer metrics
- Deterministic job scoring by role, technology, location, remote preference,
  company priority, and recency
- US-only and Chicagoland/remote preference filtering
- Company catalog, provider configuration, watchlists, and bulk ingestion
- Feature-flagged, manual Built In Chicago discovery for local use
- Searchable job discovery with score breakdowns and application state
- Application Kanban board, reminders, dashboard, saved searches, and analytics
- Locally generated DOCX resume versions using Ollama and python-docx
- React interface with dark mode, keyboard navigation, responsive layouts,
  optimistic updates, and an OpenAPI-generated API client

CareerOS is currently a single-user application. Authentication, multi-tenant
ownership, semantic search, cloud AI providers, and hosted deployment are not
implemented.

## Technology

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3 |
| Architecture | Modular monolith, Hexagonal Architecture |
| Database | PostgreSQL 16, Flyway |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS |
| Data/UI | TanStack Query, TanStack Table, React Hook Form, Zod, Zustand |
| Visualization | Recharts, Framer Motion |
| API contract | OpenAPI, springdoc, Orval-generated TypeScript client |
| Local AI | Ollama REST API |
| Documents | python-docx |
| Testing | JUnit, Mockito, Testcontainers, Vitest, RTL, Playwright |

## Architecture

CareerOS uses Ports and Adapters. Controllers call application use cases;
application services orchestrate domain behavior; repository/provider
interfaces are ports; and persistence, HTTP integrations, document processing,
and REST controllers are adapters. Controllers do not access repositories
directly.

Backend capabilities are organized under `src/main/java/com/careeros`.
The frontend follows a feature/page structure under `frontend/src`, with
TanStack Query owning server state and Zustand limited to persisted UI state.

See [Architecture](docs/architecture.md) for module and sequence diagrams and
[ADR-0005](docs/adr/0005-provider-based-ingestion-architecture.md) for the
provider-based ingestion decision.

## Local Built In Chicago discovery

The local Docker setup exposes a manual **Sync Built In** action on the
Companies page. It imports a small, paced set of senior engineering listings,
preserves source attribution, prefers direct employer application links, and
never runs from the scheduler. The adapter is unavailable outside the
`local`/`docker` profiles and the global feature default is off. Set
`BUILTIN_CHICAGO_ENABLED=false` to hide and disable it in Docker Compose.

See the [local discovery guide](docs/builtin-chicago-local-discovery.md) and
[ADR-0006](docs/adr/0006-local-secondary-job-aggregators.md) for its operating
boundaries and design rationale.

## Quick start

### Prerequisites

- Docker-compatible runtime and Docker Compose
- Node.js 20 or newer with npm
- Ollama only when generating resumes

On macOS with Colima, start the container runtime from any directory:

```bash
colima start
```

Run the backend and PostgreSQL from the repository root:

```bash
cp .env.example .env
docker compose up --build -d
curl http://localhost:8080/actuator/health
```

Docker Compose starts PostgreSQL and the Spring Boot backend. Start the Vite
frontend separately:

```bash
cd frontend
npm install
npm run dev
```

Open:

- Application: <http://localhost:5173>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Backend health: <http://localhost:8080/actuator/health>

Stop the backend and database without deleting stored data:

```bash
docker compose down
```

Stop the frontend with `Ctrl+C` in its terminal. Avoid
`docker compose down -v` unless the PostgreSQL data may be deleted.

## Running from source

Start only PostgreSQL in Docker, then run the backend and frontend in separate
terminals:

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

Database defaults and all supported environment variables are documented in
the [complete guide](docs/careeros-complete-guide.md#running-from-source).

## Local resume generation

CareerOS sends resume content only to the locally configured Ollama server.
The master DOCX is never overwritten; every output is stored as a versioned
artifact.

For a Docker Compose setup:

1. Install and start Ollama on the host.
2. Pull `llama3.1:8b` (plus optional fast and embedding models).
3. Place the immutable master resume at `data/master-resume.docx`.
4. Start CareerOS and verify `GET /api/resumes/health`.

Generated files use this structure:

```text
data/resumes/<company>/<date>/<job-id>/v####/<configured-name>.docx
```

See [Local AI resumes](docs/local-ai-resumes.md) for installation,
configuration, privacy, and troubleshooting details.

## Main application routes

| Route | Purpose |
|---|---|
| `/` | Daily dashboard, top matches, activity, and reminders |
| `/jobs` | Filtered job discovery and job details |
| `/applications` | Application Kanban and in-page job details |
| `/companies` | Company monitoring, providers, and manual sync |
| `/watchlists` | Prioritized company groups |
| `/searches` | Reusable job searches |
| `/analytics` | Discovery and application metrics |
| `/resumes` | Resume history, filtering, comparison, downloads, and archive |
| `/settings` | Theme and interface preferences |

## REST API

Swagger UI is the authoritative interactive contract. The major API groups
are:

| Area | Base endpoints |
|---|---|
| Dashboard/discovery | `/api/dashboard`, `/api/jobs/timeline` |
| Analytics/reminders | `/api/analytics`, `/api/reminders` |
| Companies | `/api/v1/companies` |
| Ingestion | `/api/v1/ingestion` |
| Jobs/preferences | `/api/v1/jobs`, `/api/v1/preferences` |
| Applications | `/api/applications` |
| Watchlists/searches | `/api/watchlists`, `/api/saved-searches` |
| Provider operations | `/api/sync-history`, `/api/provider-health` |
| Resume operations | `/api/resumes` |

Lists support resource-appropriate pagination, filtering, sorting, and
validation. Errors use `application/problem+json` responses.

## Testing

Backend unit/controller tests:

```bash
./mvnw test
```

Backend integration tests with PostgreSQL Testcontainers:

```bash
./mvnw verify
```

Frontend tests and production build:

```bash
cd frontend
npm test
npm run build
```

Playwright smoke tests require the application services to be running:

```bash
cd frontend
npx playwright test
```

## Database migrations

Flyway exclusively owns the schema in
`src/main/resources/db/migration`. Hibernate schema generation is disabled.
Add a new versioned migration for every schema change and never modify a
migration already applied to a shared database.

## Documentation

- [Complete application and operations guide](docs/careeros-complete-guide.md)
- [Architecture](docs/architecture.md)
- [Local AI resume module](docs/local-ai-resumes.md)
- [Architecture decision records](docs/adr)

## Useful operational commands

```bash
docker compose ps
docker compose logs --tail=200 app
docker system df
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/resumes/health
```

Review Docker disk usage before pruning data. The PostgreSQL volume contains
the application's persisted companies, jobs, applications, and resume
metadata.
