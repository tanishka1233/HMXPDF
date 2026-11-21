# HMXPDF Analytics — Architecture Audit

**Date:** 2026-11-21
**Scope:** Android app, admin panel, Supabase backend
**Target environment for all writes:** staging `hwwnksxfrllkrqonngob` only

## 1. Repositories and current branches

| Repo | Branch | Commit at audit |
|---|---|---|
| `tanishka1233/HMXPDF` (Android) | `feature/hmxpdf-backend-integration` | `b06a9f1` |
| `tanishka1233/HMXPDF-Admin` | `main` | `331627b` |
| `HMXPANEL/HMXPDF` | **not accessed** | — |

## 2. Android application

### Actual architecture (verified from source)

- Kotlin, Jetpack Compose, Material 3, custom liquid-glass design system.
- **No Hilt and no Room in this repository.** State is `ViewModel` + `StateFlow`; persistence is `SharedPreferences`-backed manager objects (`AppSettingsManager`, `RecentFilesManager`, `OnboardingManager`, `Preferences`).
- `kotlinx.serialization` is already a dependency; used by the Phase 6 backend client.
- Existing network layer: `com.hmx.pdf.backend` added in Phase 6, built on `HttpURLConnection` with a shared bounded `RetryLoop`. No Retrofit/OkHttp/Ktor.
- Build variants: `distribution` flavors `play`/`foss`/`fdroid`; build types `debug`/`release` plus Phase 6 `staging`. `staging` is the only variant with `HMX_BACKEND_ENABLED = true`.
- Remote config and flags: `RemoteConfigRepository`, `RemoteFlags`, `FeatureFlags`; published document cached as last-known-good.
- Diagnostics: separate consent (`hmx_diagnostics` prefs, default off), explicit two-step report form, install ID `hmx-<uuid>` generated only at first submission.
- Tests: JVM unit tests under `app/src/test`; 24 backend tests in Phase 6; full suite 399 tests, 0 failures.
- CI: `.github/workflows/android.yml` runs Play-debug unit tests, lint, debug APK, opt-in staging compile + APK, release guard, and publishes the unit-test summary. Release job requires explicit input and signing secrets.

### Reusable components

| Need | Reuse |
|---|---|
| HTTP transport | `HttpJson` (timeouts, no redirects, JSON) |
| Retry/backoff | `RetryLoop` + `RetryPolicy` (3 attempts, 400/800 ms) |
| Build gating | `BackendConfig` / `BuildConfig.HMX_BACKEND_*` |
| Storage | Private `SharedPreferences` (no Room available) |
| Version data | `AppInfo.versionName/versionCode` |
| Settings UI | `SettingsSection`, `SettingsNavRow`, `DiagnosticsSettingsSection` pattern |

### Conflicts / risks identified

1. **Diagnostic consent is separate and must remain so.** Analytics gets its own prefs file, ID namespace and consent version. No shared toggle.
2. **No Room/Hilt.** Introducing either for analytics would be an architecture change; a bounded JSON queue in `SharedPreferences` is consistent with existing code.
3. **No process-lifecycle library.** `androidx.lifecycle-process` is not a declared dependency. Session tracking will use `MainActivity`/`DocumentActivity` lifecycle plus a persisted 30-minute timeout, not a new dependency.
4. **INTERNET is merged by a dependency** into every variant; the staging manifest declares it explicitly. Analytics remains compiled out of `debug`/`release`.
5. **Diagnostic install ID `hmx-…` must never be reused.** Analytics uses `hmxa-<uuid>` in a different prefs file.
6. **Crash capture** must chain the existing default handler, never swallow the crash, and never perform blocking network I/O on the crashing thread.

## 3. Supabase backend

### Existing schema (`hmx`)

| Object | Purpose | Analytics relevance |
|---|---|---|
| `admin_members` | admin identity + role/status | reuse for authorization |
| `reports` | diagnostic reports | must stay isolated |
| `app_config_versions` | remote config | reuse flags/limits |
| `admin_audit_log` | append-only audit | reuse for deletion audit |
| `system_settings` | `report_retention_days` | add analytics retention keys |
| `submit_rate_limits` | salted bucket counters | reuse `consume_rate_limit` |
| domain functions | `require_admin`, `review_report`, config lifecycle, admin read models | style/conventions to follow |

- RLS is enabled **and forced** on every table; **no policies** exist (default deny). `hmx` schema is revoked from `anon`/`authenticated`; only `service_role` has usage and table/function grants.
- Default privileges revoke future table/function access from `public`; new analytics objects inherit that posture.
- `reports` retention default 30 days via `purge_expired_reports`; cron function pattern uses `HMX_CRON_SECRET`, constant-time compare, bounded batches, audit entries.
- Edge Functions: `admin-api` (JWT + membership re-verified server-side), `submit-report` (publishable key, allowlisted payload), `get-app-config` (public read), `purge-expired-reports` (cron secret). Shared modules: `auth`, `client`, `cors`, `errors`, `validate`.
- Rate limiting uses `hmx.consume_rate_limit(bucket, limit, window)` with salted hashes, never raw IPs in logs.

### Conflicts / risks identified

1. **No analytics objects exist.** No duplicate/legacy tables to reconcile.
2. **Default-deny means direct client inserts are impossible**; ingestion must be an Edge Function using the service role.
3. **Reports and analytics must be physically separate**; deletion must touch only analytics tables.
4. **`system_settings` is the established place for retention**, so analytics retention keys go there rather than a new settings table.
5. **No scheduler is configured** for reports; analytics cleanup will follow the same documented-only approach (no production scheduler).
6. Migration ordering: latest is `20261121053000`; the analytics migration will use `20261121150000` to avoid collision.

## 4. Admin panel

- React 19 + TypeScript + Vite + Tailwind 4 + Radix, React Router 7, TanStack Query, `sonner`, `lucide-react`.
- Auth: Supabase session in `AdminSessionProvider`; every `/admin/*` call re-validates the JWT and membership in `admin-api`. Roles `owner/admin/support/viewer` with rank checks; suspended admins rejected.
- Navigation: `AppShell` items, `Protected` route wrapper with minimum role.
- Design: deep dark background `#0a0a12`, electric purple `#a855f7`, glass panels (`hmx-panel`), mobile-first responsive grids.
- API access: `src/lib/api.ts` typed wrapper around `admin-api`; all data server-aggregated (no browser table reads).
- Tests: Vitest for pure utilities and one page; scripts `typecheck`, `test`, `build`. No GitHub Actions workflow in the repo; verification is local typecheck/test/build plus Vercel deployment.
- Reusable UI: `PageHeader`, `ErrorAlert`, `TableLoading`, `EmptyState`, `RoleBadge`, card/grid patterns from `DashboardPage`.

### Conflicts / risks

1. Analytics must be a **new route** (`/analytics`), not folded into Overview, to keep diagnostics and usage metrics clearly separated.
2. No charting library is installed; use lightweight CSS/SVG bars instead of adding a dependency.
3. Role policy: analytics is aggregate-only, so any active admin (`viewer`+) may read it; no write routes are exposed.
4. No raw event or identity exposure: the dashboard receives only aggregates from one server-side RPC.

## 5. Proposed changes (minimum viable, privacy-first)

### Database

1. `analytics_installations` — pseudonymous identity, granted-only; deleted on revoke.
2. `analytics_events` — raw allowlisted events, 90-day retention, unique `(install_id, event_id)`.
3. `analytics_daily_installations` — per-install daily activity for DAU/WAU/MAU and cohorts, 400-day retention.
4. `analytics_daily_aggregates` — identity-free counts per day (active, new, sessions, crashes, per-event totals and unique installs), 400-day retention.
5. `analytics_consent_log` — identity-free register/revoke/delete counts for the privacy panel.
6. Retention settings: `analytics_event_retention_days = 90`, `analytics_aggregate_retention_days = 400`.
7. RPCs: register, revoke/delete, ingest, refresh-day, purge, dashboard aggregate.

### Edge Functions

1. `analytics-consent` — register/revoke with publishable key, rate limits, structured errors.
2. `analytics-ingest` — strict allowlist/schema/timestamp/size/rate validation, idempotent insert.
3. `purge-analytics` — cron-secret retention job (documented, not scheduled).
4. `admin-api` — new `GET /admin/analytics` route using the aggregate RPC.

### Android

1. Dedicated consent (`analytics_enabled`, default false), consent version 1, explanation and explicit enable action.
2. `hmxa-<uuid>` identity generated only after consent; separate prefs from diagnostics.
3. Bounded local queue (200 events, 7 days), session tracking with 30-minute timeout, duplicate protection.
4. Batch upload via existing transport/retry; no heartbeat, no background service.
5. Revoke path: stop collection, discard queue, revoke server identity, verify deletion response.
6. Feature hooks on existing success paths; crash hook chains the default handler.
7. JVM tests for consent default, pre-consent silence, batching, queue bounds, opt-out purge, payload privacy, failure isolation.

### Admin

1. `/analytics` route + nav item, date range 7/30/90 days.
2. Server-aggregated overview, trends, features, reliability, distribution, retention, privacy panels.
3. Metric definitions shown in the page, loading/empty/error states, mobile layout.
4. Vitest coverage for date-range and percentage helpers.

## 6. Explicit non-goals

- No third-party analytics SDK.
- No pre-consent identifiers, buffering or uploads.
- No document content, names, URIs, OCR output, personal data, location or advertising ID.
- No cross-device identity graph.
- No production migration, function deploy, scheduler or release.
- No change to the protected repository.
