# HMXPDF Phase 8 — Staging Acceptance Report (Android)

**Repo:** `tanishka1233/HMXPDF`, branch `feature/hmxpdf-backend-integration`.
**Commit:** `ea79da7610236a7011c9db807291359568fd1526` (verified head).
**Staging backend:** Supabase `hwwnksxfrllkrqonngob`. **Production `qjhlzsopnwozpsdosbjt` untouched (read-only counts only).** **Protected `HMXPANEL/HMXPDF` never accessed.** **No production deploy / release / signing change.**
**Admin counterpart:** `tanishka1233/HMXPDF-Admin` `phase7.1-staging-acceptance` @ `11e42d0` `docs/HMXPDF_PHASE8_STAGING_ACCEPTANCE_REPORT.md`.
**Date:** 2026-11-27. Classifications: **PASS / FAIL / BLOCKED / NOT TESTED.** Nothing unexecuted marked PASS.

---

## Android CI (cloud only; no local Gradle/APK)

**Run `https://github.com/tanishka1233/HMXPDF/actions/runs/46016094584`** (`feature/hmxpdf-backend-integration`, `ea79da7`):
- `release-guard` **PASS**; `build` **PASS** (JVM + lint + debug); `staging` **PASS** (`assemblePlayStaging`, compile tests, upload, **APK inspection**); `release` **skipped** (not a tag).
- **JVM unit tests: 440 total, 0 failures, 0 errors, 42 skipped** (pre-existing skips). (PASS)
- Artifact **`hmxpdf-staging-apk`** (staging only). **`app-play-arm64-v8a-staging.apk`** 42,708,210 bytes, SHA-256 `c85b6fb1f98007f88e661c710d944c1fae38da76cea414c8577433c9274c5da7`. (4-ABI + universal in artifact.)
- **`com.hmx.pdf`, `versionName=2.1.1`, `versionCode=6`, `minSdk=23`, `targetSdk=36`, `compileSdk=36`, platform 16.** No change. Debug/staging-signed; not production.
- **APK inspection PASS:** no `qjhlzsopnwozpsdosbjt`, no admin URL, no `admin_members`, no `service_role`, no `sb_secret_`, no `SUPABASE_SERVICE`, no `/admin/`; staging `hwwnksxfrllkrqonngob.supabase.co` present; no service-role key. 

### Task 8 classification
| Check | Result |
|---|---|
| complete relevant test suite | PASS (440,0,42) |
| staging compilation | PASS |
| release guard | PASS (`release` skipped, no tag) |
| staging artifact only | PASS (`hmxpdf-staging-apk`) |
| APK production refs/secrets inspection | PASS |
| package name / signing / versionCode / versionName unchanged | PASS |

---

## Android architecture preserved

Kotlin + Compose + existing PDF/DOCX engines + onboarding + Home + Supabase + remote config + diagnostics + analytics. Liquid Glass unchanged (Phase 7.4). No new design system.

### Diagnostics submission path (traced)
`DiagnosticsSettingsSection` → consent → type/title validation → `ReportDraft` → `DiagnosticsRepository.submit` → `ReportRequestBuilder` (only `install_id`,`app_version`,`app_version_code`,`report_type`,`title`,`description`,`diagnostic_payload{android_api_level,locale,error_code?,screen?}`; **no PDF/DOCX content**) → `RetryLoop` (≤3; transport/429/5xx only) → `HttpJson` POST `.../functions/v1/submit-report`, `apikey`, `x-request-id`, `x-hmx-submission-id`, `Content-Type`, timeouts → `SubmitResponseParser` (`data.id`+`data.submitted_at`) → `SubmitResult`. Staging `hwwnksxfrllkrqonngob`. No production. No admin URL in app. No service-role.
`DiagnosticsTelemetry` safe (`project`,`submission`,`request`,`attempt`,`http_status`,`category`; no content/secrets/tokens/install/full id).

### Task 3 — Android-to-admin integration

| Check | Result | Evidence |
|---|---|---|
| full app→staging Edge→DB→admin-api→Admin path | PASS (existing real-device + app contract + admin) | `ReportRequestBuilder`, `DiagnosticsRepository`, `submit-report`, `admin-api`, staging `hmx.reports`, `admin_list_reports`. |
| **new clearly-labelled synthetic staging diagnostic report using the staging Android app** | **BLOCKED** | No Android device/emulator; no local Android builds; cannot operate owner's phone. Not claimed. |
| app→`submit-report`→DB→admin API→Admin queue/detail→dashboard count→no duplicate→no production endpoint | **PASS where executed; full app part BLOCKED** | Existing real-device reports + separate **staging synthetic via same `submit-report` contract** in Admin/Backend doc. `x-hmx-submission-id` idempotent; `client_submission_id` unique. No production endpoint in APK. |

**Existing real-device reports (Phase 7.4, also inspected Phase 8; not recreated):** `76575af9-e099-4d16-93a9-cecceef9f9ce` `bisjsbs`, `17056a2c-fec7-4936-bca7-39424df60fb8` `bug`, app 2.1.1/6, `android_api_level=35`, `locale=en`. **Phase 8 synthetic via app contract:** `HMXPDF_PHASE8_SYNTHETIC_*` created + cleaned. 

---

## Task 5 — Privacy verification

Classifications + source/tests. **No local Android runtime; not claiming device runtime.** JVM `PrivacyPromptPolicyTest`, `Analytics*` tests + CI + staging backend E2E.

| Requirement | Result |
|---|---|
| diagnostics default OFF | PASS (`hmx_diagnostics` `diagnostics_consent=false`; `BackendConfig.enabled` staging gate; nothing sent; privacy dialog independent) |
| analytics default OFF | PASS (`hmx_analytics`, no auto enable; `AnalyticsTaxonomy`; `AnalyticsEngine`) |
| separate consent + identities | PASS (`hmx-` `hmx_diagnostics` vs `hmxa-` `hmx_analytics`; `hmxpdf.privacy...` prompt completion separate) |
| no PDF/DOCX contents collected | PASS (`ReportRequestBuilder`, `AnalyticsTaxonomy`, no doc fields; diagnostics only allowlisted payload) |
| opt-out clears pending analytics events | PASS (`AnalyticsEngine.revoke`: `saveQueue(emptyList)`, `setRegistered(false)`, `setPendingDeletionId`, retry verified deletion; JVM + staging) |
| diagnostic explicit user confirmation | PASS (two-step Review→Confirm + `busy` double-tap guard + consent; no silent submit) |
| analytics deletion works + verified accurately | PASS (staging `analytics_revoke_installation`, `verified`,`deleted_events`,`identity_deleted`,`refreshed_aggregate_days`, rebuild; post-revocation rejected; synthetic removed) |
| no hidden tracking / background heartbeat | PASS (session start/end from Activity lifecycle; 30-min idle `session_end`; no heartbeat; no doc collection; remote config sends no install id; `BackendConfig.enabled` staging only) |
| Phase 8 real-device runtime re-check on OPPO A38 | NOT TESTED here (no device control); source + CI + staging evidence |

---

## Task 4 — Security verification (staging; nothing weakened)

Backend `hmx-phase8-security`: **23/23**. 

| Check | Result |
|---|---|
| anonymous admin-api | PASS (401) |
| invalid authentication | PASS admin-api (401 invalid token/apikey); **FAIL `submit-report` invalid `apikey`** (see findings) |
| authenticated non-admin | PASS (403; `require_admin`; no member) |
| suspended administrator | PASS (403 `admin_suspended`) |
| viewer | PASS read; cannot mutate (403); no audit (403) |
| support | PASS read; can `triaged/resolved`; `dismissed`→admin; no audit (403); no member mgmt |
| admin | PASS; audit (admin); no member invite (owner); no production |
| owner | PASS; `/admin/me` owner active |
| unauthorized report access / mutation | PASS (401/403; non-owner can't read other data; no anonymous read) |
| **RLS + FORCE RLS** | PASS (9/11 `hmx` tables; `submit_rate_limits`+`system_settings` no RLS; no `anon`/`authenticated` grants; `security definer` `search_path=hmx,pg_temp`; service-role server-side only) |
| no service-role in Android APK / browser / source / CI logs | PASS (APK scan + `git grep` + source; `VITE_*` no service role; no `sb_secret_`/`sbp_`; no history match) |
| no secrets in Git history / frontend source maps | PASS source; no service-role; no printed secrets. (Admin `service_role` string in SQL grants to DB role, not a key.) |
| audit not editable via normal admin | PASS (`PATCH/DELETE /admin/audit-logs` 404; no route; `hmx.log_admin_action`; no `anon`/`authenticated`; append-only API). **Note:** server `service_role` has DB `UPDATE/DELETE` privilege on `hmx.admin_audit_log`; not exposed; not "normal admin". |

**Findings:**
1. **`submit-report` `requireApiKey` checks only presence, not key match.** `apikey:garbage` → 201. `submit-report` is anonymous + rate-limited + no read + no production; but task says invalid authentication. Classify **FAIL**. (Not weakened; existing behavior; recommend validating key against `SUPABASE_ANON_KEY`.)
2. `hmx.admin_audit_log` `service_role` DB `UPDATE/DELETE` `has_table_privilege=true`; not exposed via admin-api. Normal-admin **PASS**; service-role **note**.
3. `hmx` 11 tables; 9 RLS+FORCE; `submit_rate_limits`/`system_settings` no RLS (no anon/auth grant).

---

## Production preservation

`qjhlzsopnwozpsdosbjt` read-only: `hmx.reports=0`, `hmx.admin_members=1`, no `analytics_%`, no retention fn; **no production write, migration, Edge deploy, analytics, scheduler, secret, RLS, membership, or `HMXPANEL` access.** No production release. Production Admin `hmxpdf-admin` untouched. Staging `submit-report`/`admin-api` redeployed **staging only**; `submit-report` Phase-8 invalid-UUID? (that fix is admin-api `11e42d0`, staging). No production.

---

## Remaining defects / NOT TESTED

- **`submit-report` invalid `apikey` accepted** (FAIL, above).
- **`/diagnostics/:reportId` horizontal overflow at 320/360/390** (not 412); no console error. **FAIL layout.** 
- **`/config` at 320 rendered short/empty** (not rendered>150; no error). **noted**.
- **Android synthetic via actual staging app BLOCKED** (no device/emulator). **App→staging synthetic via contract PASS; real-device PASS.**
- **Remote rollback not executed** (not mutate config); `POST /admin/config/rollback`, `hmx.rollback_config`, `target_version`, invalid `target_version`→400; `POST /admin/config/validate` invalid→400; `get-app-config` published-only. **Published PASS; invalid PASS; rollback NOT TESTED; draft-created not tested**; Android offline last-known-good **source+JVM PASS, device NOT TESTED**.
- **Password reset email delivery NOT TESTED** (no SMTP); `/forgot-password`/`/reset-password` route render; staging reset redirect set.
- **Full CI secret scan NOT exhaustive**; source/APK/history scanned.
- no screenshots captured; Playwright text checks.

## Production readiness verdict

**STAGING ACCEPTANCE: PARTIAL — production readiness NOT READY.** Core staging Android→Supabase→admin-api→Admin + diagnostics + analytics + privacy + remote-config-published + RLS/RBAC/audit + CI + artifact verified. Blockers/FAILs: `submit-report` key-presence-only; invalid-UUID→fixed staging; `/diagnostics/:id` mobile overflow; `/config` 320; **Task 3 actual-Android synthetic BLOCKED**; reset email NOT TESTED; rollback NOT TESTED. **Production integration requires explicit owner approval.** No production change made.
