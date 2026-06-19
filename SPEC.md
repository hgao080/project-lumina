# Lumina — Project Spec

**明** — bright, luminous (Ming's wall of light)

## Overview

Lumina is a personal web application that serves as a daily quote board, projected onto a wall in the user's room for passive, ambient reinforcement of motivational and self-help content. The name derives from 明 (míng), the Chinese character meaning "bright" or "luminous" — the same character as the creator's name — reflecting both the projection concept and the idea of quotes illuminating thinking. The core problem it solves: valuable quotes encountered throughout the day currently die in a notes app that never gets opened. By projecting the board on the wall, re-exposure is guaranteed without any deliberate action.

---

## Product Goals

- **Low-friction capture**: add a quote from a phone in seconds while consuming content
- **Guaranteed visibility**: the display is always on the wall — no deliberate action needed to revisit
- **Curation over time**: quotes accumulate into an archive; the best ones can be pinned permanently
- **Functionality first**: get the core loop working cleanly before any polish or advanced features

---

## User Flows

### 1. Adding a quote (phone)
User encounters a quote → opens `/add` on phone → enters quote text, author, category → optionally toggles "permanent" → submits → quote immediately appears on the wall display

### 2. Daily board (projector)
Page is open full-screen on the device driving the projector. It starts empty at the beginning of each day and fills as quotes are added. The page polls the backend every 30–60 seconds so new quotes appear without manual refresh. Two zones are always visible:
- **Daily board**: today's non-permanent quotes, displayed as a grid
- **Permanent section**: pinned quotes that persist regardless of the day

### 3. Archive (any device)
User visits `/archive` → sees a list of past dates → taps a date → sees all quotes from that day → can promote any quote to permanent from this view

---

## Features (MVP)

| Feature | Notes |
|---|---|
| Add quote form | Text, author, category, isPermanent toggle |
| Daily board display | Fills through the day as quotes are added |
| Permanent section | Shown alongside daily board, always visible |
| Daily board reset | Board naturally empties at midnight NZ time — no cron needed |
| Archive | Browse past days' boards |
| Promote to permanent | From archive view or via toggle at add-time |
| Quote polling | Display page refreshes data every 30–60s |
| PIN auth | Shared secret protecting POST and PATCH routes |
| Self-hosted backend | Runs on same device as projector, no cold starts |
| Remote add access | Cloudflare Tunnel public URL; no VPN needed on phone |

---

## Deferred / Future Features

- **Multi-user accounts**: each user owns their own daily boards and archives. `user_id` column already present in schema (stubbed) — auth layer + users table is the remaining work (#29)
- **Configurable refresh period** (daily / weekly / monthly): `boardDate` is a first-class date column so this is a settings change later, not a schema rewrite
- **Drag and drop between zones**: the PATCH endpoint for toggling `isPermanent` already covers the data mutation; drag-and-drop on a touchscreen is a frontend-only addition when needed
- **Collage-style layout**: quote cards arranged aesthetically rather than in a uniform grid
- **Multiple categories per quote**: current schema supports one `category` string, can be extended to an array later

---

## Tech Stack

| Layer | Choice | Rationale |
|---|---|---|
| Backend language | Kotlin | Learning goal; JVM ecosystem overlaps with Java/Spring Boot for job-market relevance |
| Backend framework | Ktor | Idiomatic Kotlin, coroutine-native, lightweight |
| Database | H2 via Exposed | Single file, zero network dependency, survives home internet outage, Exposed is JetBrains' own Kotlin SQL DSL. Sufficient for single-user load. |
| Frontend framework | Next.js (App Router, TypeScript) | Familiar stack, three simple routes, no API routes needed (all handled by Ktor) |
| Styling | Tailwind CSS | Utility-first, pairs well with minimal design direction |
| Typography | Excalifont | User's stated preference, loaded via `next/font/local` |
| Frontend hosting | Vercel | Zero-config for Next.js, free tier sufficient |
| Backend hosting | Self-hosted on projector device | No cold starts, backend always running since the display needs it, no cloud compute bill |
| Remote access | Cloudflare Tunnel | Exposes Ktor backend via a public HTTPS URL; no port forwarding or VPN required; phone reaches `/add` and Vercel SSR can reach backend for server-rendered pages |

---

## Repository Structure

```
/
├── backend/          # Ktor application
│   ├── src/
│   │   └── main/
│   │       └── kotlin/
│   │           └── com/projectlumina/
│   │               ├── Application.kt
│   │               ├── Env.kt
│   │               ├── plugins/
│   │               │   ├── Cors.kt
│   │               │   ├── Database.kt
│   │               │   ├── Dependencies.kt   # Ktor DI registrations
│   │               │   ├── Routing.kt
│   │               │   └── Serialization.kt
│   │               └── quotes/               # Feature package
│   │                   ├── QuoteDb.kt        # Exposed table + DAO
│   │                   ├── QuoteModel.kt     # Quote, QuoteInsert data classes
│   │                   ├── QuoteRepository.kt
│   │                   ├── ExposedQuoteRepository.kt
│   │                   └── QuoteRouting.kt
│   └── build.gradle.kts
├── frontend/         # Next.js application
│   ├── app/
│   │   ├── page.tsx          # Display view (projector)
│   │   ├── add/
│   │   │   └── page.tsx      # Add quote (phone)
│   │   └── archive/
│   │       ├── page.tsx      # Archive index
│   │       └── [date]/
│   │           └── page.tsx  # Archive day view
│   └── ...
├── SPEC.md
└── README.md
```

---

## Data Model

Single `quotes` table managed by Exposed:

```kotlin
object QuoteTable : UUIDTable("quotes") {
    val userId      = uuid("user_id")             // stub: single hardcoded UUID until auth ships (#29)
    val text        = text("text")
    val author      = varchar("author", 255)
    val category    = varchar("category", 50)
    val isPermanent = bool("is_permanent").default(false)
    val boardDate   = date("board_date")           // kotlinx-datetime LocalDate
    val createdAt   = datetime("created_at")       // kotlinx-datetime LocalDateTime, UTC
    val updatedAt   = datetime("updated_at")       // kotlinx-datetime LocalDateTime, UTC
}
```

**Key design decisions:**
- UUID primary key (via Exposed `UUIDTable`) — safe for future multi-user sharding, no sequential ID leakage
- `userId` stubbed now (#29) so the schema needs no migration when auth ships — all queries already filter by it
- `boardDate` is a native `date` column (not a varchar string) — Exposed + kotlinx-datetime handle serialisation
- No `Board` entity exists. A "board" is simply the result of querying `WHERE board_date = ? AND is_permanent = false`
- An empty board is zero rows — no null records or placeholder documents needed
- `createdAt` / `updatedAt` stored in UTC; display conversion is a frontend concern

**Open question — `boardDate` computation (#7):**
The spec says `POST /quotes` should compute `boardDate` server-side in `Pacific/Auckland` time. The current `QuoteInsert` model has `boardDate` as a client-supplied field. Needs a decision at `POST /quotes` implementation: server-compute (simpler, NZ-only) vs client-supplied (see also #28).

---

## API Routes

All routes served by Ktor. Base URL configured as an environment variable on the frontend (`NEXT_PUBLIC_API_URL`).

| Method | Path | Description | Auth |
|---|---|---|---|
| `POST` | `/quotes` | Create a quote. Computes `boardDate` server-side | PIN required |
| `GET` | `/quotes/today` | Today's non-permanent quotes | None |
| `GET` | `/quotes/permanent` | All permanent quotes | None |
| `GET` | `/quotes/archive` | List of distinct past `boardDate` values | None |
| `GET` | `/quotes/archive/{date}` | All quotes for a given past date | None |
| `PATCH` | `/quotes/{id}` | Toggle `isPermanent` on a quote | PIN required |

---

## Auth

A shared PIN/secret is checked on all mutating routes (`POST /quotes`, `PATCH /quotes/{id}`) via Ktor's Authentication plugin. The frontend sends the secret as a header (e.g. `X-Pin`). This prevents unauthorised writes to the board while keeping read routes (the display page) open without friction.

---

## Design Direction

- **Aesthetic**: clean and minimal — plenty of whitespace, typography does the heavy lifting
- **Font**: Excalifont throughout
- **Chinese character 明**: present as a design element in the UI — exact placement TBD during implementation (candidate locations: display page header/watermark, app branding on the add page). Should feel intentional and personal rather than decorative
- **Display page**: full-screen, no browser chrome visible, large readable text at wall-display scale
- **Add page**: optimised for phone — large tap targets, fast to submit
- **Collage layout**: a future enhancement, not part of MVP — start with a clean uniform grid

---

## Environment Variables

**Frontend** (Vercel):
```
NEXT_PUBLIC_API_URL=   # Cloudflare Tunnel public URL of the self-hosted Ktor backend
NEXT_PUBLIC_PIN=       # Shared PIN sent with mutating requests
```

**Backend** (local `.env` or system env):
```
PORT=                  # Port Ktor listens on (e.g. 8080)
PIN=                   # Shared PIN validated on mutating routes
DB_PATH=               # Path to H2 file (e.g. ./data/lumina)
ALLOWED_ORIGIN=        # Bare Vercel hostname (e.g. lumina.vercel.app) — defaults to localhost:3000 if unset
```

---

## GitHub Issues

See the project Kanban board. Issues cover the full MVP across the areas: Setup, Database, API, Auth, Frontend (Add / Display / Archive), and Self-hosting & Deployment.
