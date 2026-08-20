# WikiScrapper (Spring Boot)

Java / Spring Boot 4.1 port of the .NET WikiScrapper prototype: **English and Polish** Wikipedia summaries for **16 Polish voivodeships** and **world countries** (193 UN members + Vatican City + Palestine — 195 in total), stored locally, with a Thymeleaf MVC UI and a documented REST API.

The .NET original lives at `source/repos/WikiScrapper` (separate database on the same SQL Express instance).

## Features

- **Bilingual Wikipedia sync** — fetches summaries from both `en.wikipedia.org` and `pl.wikipedia.org` REST APIs. Each entity stores EN + PL description, URL, and fetch timestamp. HTTP calls run **in parallel** on virtual threads (bounded by `wikipedia.max-concurrency`, default 8). A worker that receives HTTP 429 waits and retries; other workers keep going. Database saves are **batched** (25 per transaction; Hibernate JDBC batch size 25). Synchronization is a **background job** (`POST /api/sync` → `202`); poll `GET /api/sync/status` for progress. Overlapping runs return `409`.

- **UI language switch** — navbar English / Polski control sets a `wiki_lang` cookie. The cookie drives both Wikipedia content and UI chrome (`messages.properties` / `messages_pl.properties`).

- **Web UI** (Spring MVC + Thymeleaf + Bootstrap):
  - **Dashboard** — sync trigger, live progress / nav badge, fetch stats, recent audit logs
  - **Voivodeships** — cards with detail modals for the active language
  - **Countries** — search (live AJAX, debounced; server returns a Thymeleaf fragment), status filter, sort, classic pagination, or **page size “All”** with scroll virtualization (chunked `GET /api/countries`)
  - **Logs** — filterable application audit log

- **REST API** (springdoc OpenAPI): `GET /api/voivodeships`, `GET /api/countries`, `GET /api/sync/status`, `POST /api/sync`. Pass `?lang=en` or `?lang=pl` on list/detail endpoints (defaults to English).

- **Logging** — SLF4J / Logback plus a database-backed audit log in the UI.

- **Error handling** — Spring problem details for API errors; per-item failure isolation during sync.

- **Tests** — JUnit 5 + Mockito (Wikipedia client, sync orchestration); test profile uses in-memory H2 (no SQL Server required).

## Project structure

```
src/main/java/com/ots/wikiscrapper/
  domain/          Entities, DTOs, value types
  data/            Repositories, seed, query services
  service/         Wikipedia client, DataSyncService, SyncJobService
  web/             MVC + REST controllers, UI messages
  config/          HTTP clients, OpenAPI, resilience helpers
src/main/resources/
  templates/       Thymeleaf views
  static/          CSS / JS
  messages*.properties
  application.properties
src/test/          Unit tests (H2)
```

## Prerequisites

- **Java 21+**
- **SQL Server Express** with the **`SQLEXPRESS`** instance running (Windows authentication) — unless you use the optional H2 profile

Both this app and the .NET original use the same Express instance but **separate databases**:

| App | Database |
|-----|----------|
| .NET | `WikiScrapper` |
| Spring Boot (this project) | `WikiScrapperJava` |

Create the databases once (if they do not exist):

```powershell
sqlcmd -S "localhost\SQLEXPRESS" -Q "IF DB_ID('WikiScrapper') IS NULL CREATE DATABASE WikiScrapper; IF DB_ID('WikiScrapperJava') IS NULL CREATE DATABASE WikiScrapperJava;"
```

## Database connection

Configured in `src/main/resources/application.properties`:

- **Instance (SSMS / sqlcmd):** `localhost\SQLEXPRESS`
- **JDBC URL:** `jdbc:sqlserver://localhost:63171;databaseName=WikiScrapperJava;...;integratedSecurity=true`
- **Auth:** Windows integrated (your Windows user)

The Microsoft JDBC driver connects over **TCP** and needs an explicit port. SQL Server Browser is often stopped on Express, so the port is set directly (currently `63171`). If the app cannot connect after a machine restart, read the current dynamic port:

```powershell
(Get-ItemProperty 'HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\MSSQL17.SQLEXPRESS\MSSQLServer\SuperSocketNetLib\Tcp\IPAll').TcpDynamicPorts
```

Update `spring.datasource.url` in `application.properties` with the new port.

Windows integrated auth requires `mssql-jdbc_auth-*.x64.dll` on `java.library.path`. Gradle copies it from Maven into `lib/native/` (also runs before `compileJava`).

**IntelliJ:** use the shared run configuration **WikiscrapperApplication** (`.run/WikiscrapperApplication.run.xml`), or add this VM option under *Run → Edit Configurations*:

```text
-Djava.library.path=$PROJECT_DIR$/lib/native
```

Wikipedia settings: `wikipedia.en-base-url`, `wikipedia.pl-base-url`, `wikipedia.max-concurrency`.

### Optional: H2 (no SQL Server)

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=h2"
```

See `application-h2.properties` (file-based H2 under `./data/`, console at `/h2-console`).

## Running (fresh machine)

1. Install JDK 21+ and ensure SQL Express `SQLEXPRESS` is running (or use the H2 profile above).
2. Create the databases with the `sqlcmd` script (if using SQL Server).
3. Confirm / update the TCP port in `application.properties` if needed.
4. From the project root:

```powershell
.\gradlew.bat bootRun
```

(On Unix: `./gradlew bootRun`. Gradle applies the native library path automatically.)

On first start, Hibernate creates/updates tables (`ddl-auto=update`) and seed data is inserted when tables are empty.

Then open:

- **Web UI:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html

On the dashboard, press **Synchronize with Wikipedia**. The job runs in the background (progress + nav badge). Sync covers **both languages** for every entity.

Use the language dropdown in the navbar to switch UI + Wikipedia content. On Countries, try search, filters, and page size **All** (virtual scroll).

## Tests

```powershell
.\gradlew.bat test
```

Tests use an in-memory H2 database (`src/test/resources/application.properties`); they do not require SQL Server.
