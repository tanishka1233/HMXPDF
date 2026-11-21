# HMXPDF Analytics — Privacy and Data Retention

**Status:** implemented and verified in staging only. Production is unchanged.
**Consent default:** `analytics_enabled = false`. No identifier, event or upload exists before explicit opt-in.

## 1. What is collected (only after explicit opt-in)

| Field | Where | Purpose |
|---|---|---|
| `install_id` | random `hmxa-<uuid>` generated at consent | pseudonymous, per-installation attribution |
| `consent_version` | constant `1` | schema version for consent rules |
| `event name` | strict allowlist (19 names) | which feature/reliability event occurred |
| `feature` | allowlisted identifier derived from event | feature roll-up |
| `error_category` | allowlisted coarse category | reliability roll-up; only on `app_crash` / `operation_failed` |
| `session_id` | random UUID per active session, rotated after 30 minutes idle | session denominator |
| `occurred_at` | device timestamp, server window-checked | trends/cohorts |
| `app_version`, `app_version_code` | package metadata | version insights |
| `android_major` | `Build.VERSION.SDK_INT` | coarse Android distribution |

### Event taxonomy

`app_open`, `session_start`, `session_end`,
`pdf_open`, `pdf_edit`, `pdf_export`, `pdf_share`, `pdf_merge`, `pdf_split`, `pdf_compress`,
`docx_open`, `docx_edit`, `docx_export`,
`scan_use`, `ocr_use`, `signature_use`, `url_to_pdf_use`,
`app_crash`, `operation_failed`.

Every property is validated twice: in the Edge Function and again in the database function/constraints. Unknown keys are rejected.

## 2. What is never collected

PDF/DOCX contents, document names, file paths or URIs, page text, OCR output, file metadata, email, phone, passwords, auth tokens, clipboard, contacts, location, advertising ID, Android ID, hardware serial, device model, raw HTTP bodies, exception messages or stack traces.

The crash handler writes only `app_crash` with no message; the existing default handler still receives the throwable.

## 3. Consent lifecycle

### Opt-in

1. Settings → **Analytics (staging)** → **View privacy explanation**.
2. The user must press **I understand — enable analytics**.
3. Only then: create `hmxa-<uuid>` in a private prefs file (`hmx_analytics`), set `analytics_enabled = true`, and POST `/analytics-consent` with `action=register`.
4. If registration fails, consent is recorded locally but no event is uploaded until registration succeeds; the UI says so explicitly.

This is a separate screen, flag, identity and prefs file from diagnostic reporting. There is no bundled consent.

### Opt-out

1. Switch off, or **Request deletion of analytics data** → confirm.
2. Immediately: `analytics_enabled = false`, the in-memory/queue JSON is cleared, the session ID is cleared, and no further `track()` call can append an event.
3. POST `/analytics-consent` with `action=revoke` or `action=delete`.
4. The database function deletes identity, raw events and daily activity **and then verifies** no row remains. Only a `verified: true` response is reported as success.
5. If verification cannot be obtained, the local ID is kept only in `analytics_pending_deletion` for a retry on next launch; the UI reports "pending", never "deleted".

A declining user never has an analytics ID. The app functions normally with analytics on, off, online or offline.

### Identifier rules

- Generated only after consent; not derived from any device identifier.
- Never the diagnostic install ID (`hmx-…`); the analytics prefix is `hmxa-…`.
- Reinstall after opt-out generates a new ID (new consent required). Consent reset via app data clear behaves the same.
- No cross-device identity graph, no account linking, no email/phone.
- **Limitation:** one ID = one app installation that opted in, not a verified human. Uninstall without opt-out leaves no ID but also no deletion callback; therefore all counts are labelled *opted-in installations*, never users.

## 4. Data model and retention

| Table | Contents | Retention |
|---|---|---|
| `hmx.analytics_installations` | pseudonymous ID, consent version, app/Android version, timestamps | until opt-out/deletion |
| `hmx.analytics_events` | allowlisted raw events, idempotency key | **90 days** (configurable) |
| `hmx.analytics_daily_installations` | `(day, install_id)` activity for DAU/WAU/MAU and cohorts | **400 days** |
| `hmx.analytics_daily_aggregates` | identity-free daily counters and per-event totals | **400 days** |
| `hmx.analytics_consent_log` | identity-free register/revoke/delete counters | 400 days (same cleanup job) |

Settings keys: `analytics_event_retention_days = 90`, `analytics_aggregate_retention_days = 400`, `analytics_consent_version = 1` in `hmx.system_settings`.

`hmx.analytics_purge()` deletes only expired analytics rows and writes one audit entry. `purge-analytics` is the cron-secret Edge Function wrapper. **No scheduler has been enabled in either project.** To enable later (owner approval required): daily `POST /functions/v1/purge-analytics` with `Authorization: Bearer <HMX_CRON_SECRET>`, e.g. `15 3 * * *`. Enable in staging first, then production only with explicit approval.

Deletion never touches `hmx.reports`, `hmx.admin_audit_log` or any diagnostic record.

## 5. Metric definitions

- **DAU** — unique opted-in installations with at least one eligible event on the selected UTC day.
- **WAU / MAU** — unique installations active in the rolling 7 / 30 days ending on the selected day.
- **New opted-in installations** — first successful consent registrations in the range.
- **Returning installations** — active in the range and also active before `from`.
- **Retention** — cohort by first observed active day; day 1/7/30 return activity; blank when the window has not elapsed.
- **Feature usage** — per event: count and unique opted-in installations (unique counts come from the 90-day raw window).
- **Crash metrics** — crash events, affected installations, `crash_free_session_pct = 1 − crashes ÷ session_start` shown only when the denominator is > 0.
- **Distribution** — active installations grouped by app version and coarse Android major version.

Not included anywhere: total APK downloads, Play Store install counts, or "total HMXPDF users". Those numbers are not available from this system and are not inferred.

## 6. Server-side enforcement

- All five analytics tables have RLS enabled and FORCEd with **no policies**; `anon`/`authenticated` have no schema usage or table grants.
- Ingestion requires a publishable key plus a server-side registration row. Browser/APK never receive a service-role or secret key.
- `analytics-ingest` rejects unknown fields, invalid event names, wrong feature/error categories, out-of-window timestamps, batches outside 1–50, bodies over 64 KiB (gateway may return 503) and rate-limit violations; `hmx.analytics_ingest` repeats validation in SQL.
- Idempotency: unique `(install_id, event_id)`; retries return `duplicates` rather than duplicate rows.
- Admin reads go only through `/admin/analytics`, which calls a `SECURITY DEFINER` aggregate RPC after the existing JWT + membership + role + status checks. No raw event row reaches the browser.

## 7. Owner decisions still required

1. Approve proposed 90-day raw and 400-day aggregate retention (currently staging settings).
2. Approve SMTP/password-reset, retention schedulers for both reports and analytics, and any production deployment.
3. Confirm whether `pdf_share`, `signature_use` and all DOCX edit variants should be instrumented more deeply (currently wired at the main success paths).
