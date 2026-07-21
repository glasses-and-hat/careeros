# CareerOS Complete Guide

CareerOS is a self-hosted career operating system for discovering jobs, ranking
them against personal preferences, monitoring target companies, tracking
applications, managing follow-ups, and producing locally tailored resumes.

The application is designed for one daily workflow:

```text
Monitor companies → ingest jobs → rank matches → review details
→ save/apply → track progress → follow up → tailor and retain resumes
```

CareerOS currently operates as a single-user application. Authentication,
multi-tenant ownership, cloud AI providers, semantic search, and a hosted
deployment are not implemented.

## Major capabilities

- Monitors company career pages through pluggable providers.
- Supports Greenhouse, Lever, Ashby, Workday, and SmartRecruiters.
- Supports configurable HTML, RSS/Atom, and JSON providers.
- Supports primary and fallback provider chains.
- Imports and validates large Ashby company catalogs.
- Records provider sync history, failures, timing, and health metrics.
- Normalizes jobs from different providers into one job model.
- Refreshes existing jobs when an ATS changes a title, description, location,
  posted date, or application URL.
- Scores jobs deterministically against user preferences.
- Groups target companies into prioritized watchlists.
- Provides saved searches and timeline-based discovery.
- Tracks applications through a Kanban workflow.
- Calculates follow-up and interview reminders.
- Provides aggregate application and discovery analytics.
- Generates grounded DOCX and PDF resumes using local Ollama, python-docx,
  and LibreOffice.
- Stores every generated resume as a versioned artifact.

## Technology and architecture

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3 |
| Architecture | Modular monolith, Hexagonal Architecture |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Frontend | React 19, TypeScript, Vite |
| Server state | TanStack Query |
| Tables | TanStack Table |
| Styling | Tailwind CSS and reusable UI primitives |
| Charts | Recharts |
| Animation | Framer Motion |
| Local AI | Ollama REST API |
| Documents | python-docx and LibreOffice headless |
| API contract | OpenAPI and Orval-generated TypeScript client |
| Tests | JUnit, Mockito, Testcontainers, Vitest, RTL, Playwright |

Controllers do not access repositories directly. Application services contain
use-case orchestration, domain objects contain business behavior, repository
interfaces act as ports, and Spring Data/provider integrations are adapters.
See [architecture.md](architecture.md) for diagrams and design details.

## Recommended daily workflow

### Morning

1. Start CareerOS and open the Dashboard.
2. Run **Sync enabled** from Companies if scheduled ingestion is disabled.
3. Review new jobs and the highest matches.
4. Open promising jobs to review the complete description and score breakdown.
5. Use the bookmark to save a job to the `WISHLIST` column.
6. Use **Apply** to open the employer page and immediately create an `APPLIED`
   tracker entry. Use the toast's **Undo** action if the application was not
   actually submitted.

### During the day

1. Use the Applications Kanban board to move applications through stages.
2. Click an application card to view its full job details without leaving the
   board.
3. Record recruiter, referral, interview, follow-up, and notes through the
   Applications API as needed.
4. Generate a tailored resume from a job detail drawer.

### Weekly

1. Review reminders and inactive applications.
2. Review response, interview, and offer rates in Analytics.
3. Adjust matching preferences and company priorities.
4. Enable or disable companies based on signal quality.
5. Archive unused resume versions and restore them when needed.

## Frontend pages

### Dashboard — `/`

The landing page provides new-job counts, high matches, application activity,
interviews, follow-ups, top matches, recent discovery activity, reminders, and
recent resume generations.

Jobs already moved into Applications are removed from Dashboard discovery so
the page remains focused on unreviewed opportunities. Top matches support:

- Save to Wishlist
- View details and tailor a resume
- Apply and automatically track the application

### Jobs — `/jobs`

The Jobs page is the complete discovery table. It supports:

- Global company/title search
- Minimum-score filtering
- Remote-only filtering
- Column visibility
- Sorting and pagination
- Deep links such as `/jobs?job=<job-id>`
- Application status (`New`, `Wishlist`, `Applied`, `Rejected`, and others)

Selecting a row opens a drawer with the complete description, location,
employment type, salary, posted date, match components, application link,
resume-generation action, and generation history.

### Applications — `/applications`

Applications use a horizontally scrollable Kanban board with these stages:

- Wishlist
- Applied
- Recruiter Contacted
- Recruiter Screen
- Technical Interview
- System Design
- Behavioral
- Final Interview
- Offer
- Rejected

Drag-and-drop updates status optimistically and rolls back on failure. Cards
show the job title, company, location, remote status, applied date, recruiter,
follow-up date, notes, and external link. Clicking a card opens an in-page
modal with full job details and description.

### Companies — `/companies`

Companies can be searched, filtered by monitoring state/provider/priority,
and sorted. The page supports creating companies, editing provider
configuration, enabling/disabling monitoring, removing companies, and
triggering ingestion.

Provider configuration is capability-based rather than ATS-only. A company
can have a primary provider and fallback providers.

### Watchlists — `/watchlists`

Watchlists group companies by strategy, such as Dream Companies, Chicago,
Remote, Startups, FinTech, Cloud, or AI. Each membership has an independent
priority and enabled state.

### Saved Searches — `/searches`

Saved searches persist reusable title, company, location, technology, remote,
minimum-score, and sorting criteria. They can be executed with one API call.

### Analytics — `/analytics`

Analytics includes applications this month, response rate, interview rate,
offer rate, average match score, status distribution, company distribution,
ATS distribution, and matching trends.

### Resumes — `/resumes`

The Resume Library supports:

- Search by job, model, or status
- Active, archived, and all-file filters
- Validation-result filters
- Newest, oldest, version, and job-title sorting
- Select all visible files
- Multi-file archive
- Restore archived versions
- Compare two selected versions
- Download DOCX and PDF artifacts

### Settings — `/settings`

Settings provides light, dark, and system themes. Theme and sidebar state are
stored in the browser.

## Job matching

Matching is deterministic and does not require an LLM. Default weights are:

| Rule | Weight |
|---|---:|
| Role | 30% |
| Technology | 30% |
| Location | 15% |
| Remote preference | 10% |
| Company priority | 10% |
| Recency | 5% |

Weights are configured under `careeros.matching.weights` and must total 100.
Preferences support roles, technologies, locations, remote-only behavior,
minimum score, salary, company priorities, ignored companies, ignored
keywords, visa sponsorship, and USA-only filtering.

Scores are calculated at read time, so preference changes take effect without
reprocessing stored jobs.

## Providers and ingestion

Supported provider types:

- `GREENHOUSE`
- `LEVER`
- `ASHBY`
- `WORKDAY`
- `SMARTRECRUITERS`
- `GENERIC_HTML`
- `GENERIC_RSS`
- `GENERIC_JSON`
- `CUSTOM_PROVIDER`

Each enabled company defines a provider identifier and optional JSON
configuration. The registry resolves the provider without scheduler switch
statements. A failed primary provider advances to configured fallbacks; the
first successful provider stops the chain.

Greenhouse reads use bounded retries for transient network timeouts. Provider
attempts record jobs found, added, updated, duration, success, and errors.

The supplied Ashby catalog is stored in `config/ashby-boards.txt`. Import it
idempotently with:

```bash
node scripts/import-ashby-companies.mjs
```

The importer validates public Ashby endpoints, extracts display names,
deduplicates existing companies, and creates missing companies disabled by
default. Enable selected companies from the Companies page to prevent an
unexpectedly large synchronization run.

## Local resume tailoring

Resume tailoring uses the stored job description automatically; the guidance
field is optional. The pipeline is:

```text
Job + immutable master DOCX
→ extract resume text and bullets
→ render versioned prompt
→ local Ollama structured generation
→ grounding validation
→ retry once if invalid
→ conservative fallback if still invalid
→ versioned DOCX
→ LibreOffice PDF
→ persisted metadata and artifacts
```

The validator rejects unknown employers, technologies, certifications, and
metrics. Resume content is sent only to the configured Ollama endpoint.

Artifacts are stored as:

```text
data/resumes/<company>/<date>/<job-id>/v####/<configured-name>.docx
data/resumes/<company>/<date>/<job-id>/v####/<configured-name>.pdf
```

The master resume is never overwritten.

## Running with Docker Compose

### Prerequisites

- Docker Desktop or Colima
- Docker Compose
- Node.js 20+ and npm for the Vite frontend
- Ollama on the host for resume generation

With Colima:

```bash
colima start
```

From the CareerOS repository root:

```bash
docker compose up --build -d
```

This starts PostgreSQL and the Spring Boot backend. Flyway runs automatically.
Check health:

```bash
curl http://localhost:8080/actuator/health
```

Start the frontend in another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Actuator health: `http://localhost:8080/actuator/health`

Stop CareerOS without deleting PostgreSQL data:

```bash
docker compose down
```

Do not use `docker compose down -v` unless you intentionally want to delete
the PostgreSQL volume and all persisted data.

## Running from source

Start only PostgreSQL:

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

Then start the frontend as described above. The backend requires Java 21.

## Local AI setup

On macOS:

```bash
brew install ollama
brew services start ollama
ollama pull llama3.1:8b
ollama pull mistral:7b
ollama pull nomic-embed-text
brew install --cask libreoffice
```

For Docker, place the master resume at `data/master-resume.docx`. The Docker
image includes an isolated Python environment with python-docx and includes
LibreOffice. Ollama remains on the host and is reached through
`host.docker.internal`.

Important environment variables:

| Variable | Purpose |
|---|---|
| `OLLAMA_BASE_URL` | Ollama REST endpoint |
| `OLLAMA_PRIMARY_MODEL` | Primary tailoring model |
| `OLLAMA_FAST_MODEL` | Faster optional model |
| `OLLAMA_EMBED_MODEL` | Local embedding model |
| `MASTER_RESUME_PATH` | Immutable master DOCX |
| `RESUME_OUTPUT_DIRECTORY` | Versioned artifact root |
| `RESUME_FILE_BASENAME` | Human-readable output filename |
| `LIBREOFFICE_PATH` | `soffice` binary |
| `RESUME_PROCESS_TIMEOUT` | Document process timeout |

## REST API overview

Swagger UI is the authoritative interactive contract. Most collection
endpoints support `page`, `size`, and `sort`.

### Discovery and analytics

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/dashboard` | Aggregated daily dashboard |
| GET | `/api/jobs/timeline` | Scored jobs and application status |
| GET | `/api/reminders` | Follow-up and interview reminders |
| GET | `/api/analytics` | Aggregated statistics |

Timeline periods are `TODAY`, `YESTERDAY`, `LAST_7_DAYS`, and
`LAST_30_DAYS`. Filters include company, minimum score, remote, location, and
ATS.

### Companies and ingestion

| Method | Endpoint | Purpose |
|---|---|---|
| POST/GET | `/api/v1/companies` | Create or list companies |
| GET/PUT/DELETE | `/api/v1/companies/{id}` | Company operations |
| POST | `/api/v1/ingestion/runs` | Sync every enabled company |
| POST | `/api/v1/ingestion/companies/{id}/runs` | Sync one company |
| GET | `/api/sync-history` | All provider attempts |
| GET | `/api/sync-history/company/{id}` | Company sync history |
| GET | `/api/sync-history/provider/{provider}` | Provider sync history |
| GET | `/api/provider-health` | Aggregated provider health |

### Jobs and preferences

| Method | Endpoint | Purpose |
|---|---|---|
| POST/GET | `/api/v1/jobs` | Create or list jobs |
| GET/PUT/DELETE | `/api/v1/jobs/{id}` | Complete job operations |
| POST/GET | `/api/v1/preferences` | Create or list preferences |
| GET/PUT/DELETE | `/api/v1/preferences/{id}` | Preference operations |

### Applications

| Method | Endpoint | Purpose |
|---|---|---|
| POST/GET | `/api/applications` | Create or list applications |
| GET/PUT/DELETE | `/api/applications/{id}` | Read, update, or undo/delete |

Statuses include `WISHLIST`, `APPLIED`, `RECRUITER_CONTACTED`,
`REFERRAL_REQUESTED`, `RECRUITER_SCREEN`, `TECHNICAL_INTERVIEW`,
`SYSTEM_DESIGN`, `BEHAVIORAL`, `FINAL_INTERVIEW`, `OFFER`, `REJECTED`, and
`WITHDRAWN`.

### Watchlists and saved searches

| Method | Endpoint | Purpose |
|---|---|---|
| POST/GET | `/api/watchlists` | Create or list watchlists |
| GET/PUT/DELETE | `/api/watchlists/{id}` | Watchlist operations |
| PUT/DELETE | `/api/watchlists/{id}/companies/{companyId}` | Membership operations |
| POST/GET | `/api/saved-searches` | Create or list searches |
| GET/PUT/DELETE | `/api/saved-searches/{id}` | Search operations |
| POST | `/api/saved-searches/{id}/execute` | Execute a saved search |

### Resumes

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/resumes/generate` | Generate a tailored version |
| GET | `/api/resumes` | List resume versions |
| GET | `/api/resumes/{id}` | Get resume metadata |
| GET | `/api/resumes/job/{jobId}` | Job generation history |
| GET | `/api/resumes/{id}/download/{format}` | Download DOCX or PDF |
| DELETE | `/api/resumes/{id}` | Archive a version |
| POST | `/api/resumes/{id}/restore` | Restore an archived version |
| GET | `/api/resumes/health` | Ollama and model diagnostics |

## Testing and verification

Backend unit and controller tests:

```bash
./mvnw test
```

Backend integration tests with PostgreSQL Testcontainers:

```bash
./mvnw verify
```

Frontend tests and build:

```bash
cd frontend
npm test -- --run
npm run build
```

Playwright smoke tests:

```bash
cd frontend
npm run e2e
```

Use Java 21 for backend tests. Newer unsupported JDKs may prevent Mockito's
Byte Buddy agent from attaching even when the application code compiles.

## Troubleshooting

### Backend is not reachable

```bash
docker compose ps
docker compose logs --tail=200 app
curl http://localhost:8080/actuator/health
```

### Greenhouse sync timeout

Greenhouse requests retry transient failures. A company can still fail after
the bounded attempts; rerun that company from the Companies page or use the
single-company ingestion endpoint.

### Company is disabled

Disabled companies are stored but excluded from all-company synchronization.
Enable the company after confirming its provider identifier and public feed.

### Resume generation fails

Check:

```bash
curl http://localhost:8080/api/resumes/health
ollama list
docker compose logs --tail=300 app
```

Confirm the master DOCX exists, Ollama has the configured model, and the
output directory is writable.

### Free Docker disk space

Inspect usage before deleting anything:

```bash
docker system df
```

Do not remove the CareerOS PostgreSQL volume unless the stored database can be
discarded.

## Current limitations

- Single-user and unauthenticated.
- Scheduled ingestion is disabled by default.
- Some large company catalogs are intentionally disabled until reviewed.
- Generic HTML providers depend on stable selectors.
- Job deduplication is provider/external-ID based rather than semantic.
- A provider cannot always determine whether a removed job was filled or
  merely unpublished.
- Resume bullet replacement depends on recognizable DOCX paragraph/list
  structure.
- PDF generation requires LibreOffice.
- No cloud AI provider is implemented; resume processing is local by design.

## Related documentation

- [Architecture](architecture.md)
- [Local AI resumes](local-ai-resumes.md)
- [Provider architecture ADR](adr/0005-provider-based-ingestion-architecture.md)
- [Dashboard ADR](adr/0004-dashboard-composed-read-model.md)
- [Deterministic scoring ADR](adr/0003-deterministic-read-time-job-scoring.md)
