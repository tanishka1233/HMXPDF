# HMXPDF Android → Staging Backend Integration (Phase 6)

**Status:** staging integration complete, awaiting owner approval before production integration or release.

- **Date:** 2026-11-21
- **Android repo:** `https://github.com/tanishka1233/HMXPDF.git`
- **Branch:** `feature/hmxpdf-backend-integration`
- **HEAD:** `232542e3afecae743c3ce736960020204fbb199f`
- **Staging project:** `hwwnksxfrllkrqonngob` (`ap-northeast-1`)
- **Production project:** `qjhlzsopnwozpsdosbjt` — **not accessed or modified in this phase**
- **Protected repo:** `https://github.com/HMXPANEL/HMXPDF.git` — **not accessed**

---

## 1. Scope delivered

| Objective | Result |
|---|---|
| Audit existing architecture and network layer | Done (§2) |
| Preserve Kotlin / Compose / Room / Hilt / existing architecture | Done — app has no Hilt or Room; existing Compose + SharedPreferences architecture preserved (§2) |
| Staging-only diagnostic report submission | Done, explicit two-step consent (§5) |
| Remote configuration fetching | Done with last-known-good cache (§5.1) |
| Feature-flag consumption | Done — URL-to-PDF kill switch + resolved flag display (§5.3) |
| Server-side RLS / Edge Function authorization respected | Done — publishable key only, no privileged keys (§6) |
| Network errors, retries, offline behaviour | Done — bounded retries, disk cache, silent fallback (§5.4) |
| App usable when diagnostics fail | Done — submission never blocks document work (§5.4) |
| No PDF/DOCX content uploaded | Enforced client-side and rejected server-side (§6) |
| Only approved diagnostics collected | Allowlisted payload, verified against the live function (§6) |

---

## 2. Architecture audit

- **Language/UI:** Kotlin, Jetpack Compose, Material 3, custom liquid-glass UI. No XML screens for the affected surfaces.
- **DI/state:** no Hilt and no Room in this codebase. State is `ViewModel` + `StateFlow`, persistence is `SharedPreferences`-backed manager objects (`AppSettingsManager`, `RecentFilesManager`, etc.). Introducing Hilt/Room would have been a new architecture, not preservation, so it was not added.
- **Serialization:** `kotlinx.serialization` was already a dependency and is used by the existing code, so the new DTOs reuse it.
- **Network layer:** none existed. There were no Retrofit/OkHttp/Ktor dependencies and no backend client. `HttpURLConnection` is now used directly to avoid adding libraries or increasing APK size.
- **Flavors/build types:** `distribution` flavors `play`, `foss`, `fdroid`; build types `debug`, `release`. Main manifest deliberately does not request `INTERNET`; the FOSS manifest does.
- **Existing CI:** `.github/workflows/android.yml` runs Play-debug unit tests, lint and a debug APK on GitHub-hosted runners, plus a release-signing guard.
- **Privacy posture already present:** no analytics SDK, no tracking, no advertising identifier.

---

## 3. Architecture decisions

1. **New `staging` build type**, inheriting `debug`, is the only variant with a backend endpoint. Production `debug`/`release` compile `HMX_BACKEND_ENABLED = false` and empty URL/key strings.
2. **`matchingFallbacks += listOf("debug")`** lets `staging` consume the debug variants of `:backdrop`, `:pdf-core`, `:ocr-core`, `:office_engine`, which do not define a staging build type.
3. **No new third-party libraries.** Transport is `HttpURLConnection`; JSON is `kotlinx.serialization`; retries are a small injectable loop.
4. **No background work.** Config fetch runs once per launch on `Dispatchers.IO`; report submission only runs from an explicit user action. No WorkManager, no boot receiver, no queue that could upload later without consent.
5. **Install ID is created lazily at first submission**, stored in a private `SharedPreferences` file, and sent only in `/submit-report`. Config requests contain only `app_version_code`.
6. **Fail-open for features, fail-closed for privacy.** Missing config means built-in defaults; missing consent or a failed submission means nothing is sent and the app continues normally.

### BuildConfig values

| Field | staging | debug | release |
|---|---|---|---|
| `HMX_BACKEND_ENABLED` | `true` | `false` | `false` |
| `HMX_BACKEND_URL` | `https://hwwnksxfrllkrqonngob.supabase.co/functions/v1` | `""` | `""` |
| `HMX_BACKEND_PUBLISHABLE_KEY` | staging publishable key (public by design) | `""` | `""` |

The production project ref appears nowhere in Android source or in any built artifact (verified in §8).

---

## 4. Files changed

All changes are on `feature/hmxpdf-backend-integration`; no release branch, tag, or release artifact was created.

| File | Change |
|---|---|
| `app/build.gradle.kts` | `staging` build type, BuildConfig fields, `buildConfig = true`, variant fallbacks, signing-guard exemption |
| `app/src/staging/AndroidManifest.xml` | new; explicit `INTERNET` + `ACCESS_NETWORK_STATE` for the staging variant |
| `app/src/main/java/com/hmx/pdf/backend/BackendConfig.kt` | new; build-gated config + app version helpers |
| `app/src/main/java/com/hmx/pdf/backend/RemoteConfigModels.kt` | new; DTOs, strict parser, allowlisted feature keys |
| `app/src/main/java/com/hmx/pdf/backend/RemoteFlags.kt` | new; process-wide cached flags + test hook |
| `app/src/main/java/com/hmx/pdf/backend/ReportRequestBuilder.kt` | new; allowlisted report JSON, retry policy |
| `app/src/main/java/com/hmx/pdf/backend/RetryLoop.kt` | new; injectable bounded retry executor |
| `app/src/main/java/com/hmx/pdf/backend/HttpJson.kt` | new; minimal JSON transport, timeouts, no redirects |
| `app/src/main/java/com/hmx/pdf/backend/Repositories.kt` | new; config cache + explicit diagnostics submission |
| `app/src/main/java/com/hmx/pdf/ui/screen/DiagnosticsSettingsSection.kt` | new; consent toggle, two-step report form, privacy text |
| `app/src/main/java/com/hmx/pdf/ui/screen/SettingsScreen.kt` | new "Diagnostics (staging)" page, visible only when backend enabled |
| `app/src/main/java/com/hmx/pdf/MainActivity.kt` | one config refresh per launch in staging builds |
| `app/src/main/java/com/hmx/pdf/ui/viewmodel/HtmlToPdfViewModel.kt` | consumes `url_to_pdf` flag before URL conversion |
| `app/src/test/java/com/hmx/pdf/backend/RemoteConfigParserTest.kt` | new; 8 tests incl. live staging document |
| `app/src/test/java/com/hmx/pdf/backend/ReportRequestBuilderTest.kt` | new; 7 tests incl. forbidden-field scan |
| `app/src/test/java/com/hmx/pdf/backend/RetryPolicyTest.kt` | new; 3 tests |
| `app/src/test/java/com/hmx/pdf/backend/RetryLoopTest.kt` | new; 6 tests |
| `.github/workflows/android.yml` | opt-in `verify_staging` job; unit-test summary and result artifact |

No production source file, package name, signing identity, `versionCode` or `versionName` was changed.

---

## 5. API integration

Base URL (staging build type only): `https://hwwnksxfrllkrqonngob.supabase.co/functions/v1`

### 5.1 Published configuration

```
GET /get-app-config?app_version_code=21
apikey: <staging publishable key>
```

- Response: `{ data: { version, config, published_at, compatible, min_supported_app_version_code } }`.
- Client accepts only `schema_version == 1`, rejects unknown feature keys, and rejects malformed bodies.
- On success the raw body is cached as last-known-good; on any failure the cache (or built-in defaults) is used.
- The client does not yet send `If-None-Match`; the server already emits an ETag, so this is a future bandwidth optimisation.
- No install ID, user ID, device model or location is sent.

### 5.2 Diagnostic report

```
POST /submit-report
apikey: <staging publishable key>
Content-Type: application/json
```

```json
{
  "install_id": "hmx-<uuid>",
  "app_version": "2.1.1",
  "app_version_code": 21,
  "report_type": "bug",
  "title": "user-written title",
  "description": "user-written description",
  "diagnostic_payload": {
    "android_api_level": 34,
    "locale": "en_IN",
    "error_code": "…optional…",
    "screen": "…optional…"
  }
}
```

- Success: `201` with `{ data: { id, submitted_at, status: "new" } }`.
- `install_id` is validated client-side against the server's allowlist `^[A-Za-z0-9._:-]{8,128}$`.
- The UI requires an explicit consent toggle **and** a separate "Confirm and send" step; editing any field cancels the confirmation.

### 5.3 Feature-flag consumption

- `RemoteFlags` exposes the last fetched snapshot process-wide.
- `url_to_pdf` gates URL→PDF conversion in `HtmlToPdfViewModel`; the HTML→PDF path is unaffected.
- If no configuration exists, or the app is below `min_supported_app_version_code` (`compatible = false`), flags fall back to built-in defaults — the server is never able to silently disable features offline.
- The Diagnostics page lists the resolved state of every allowlisted flag and shows `update required` when incompatible.

### 5.4 Retries, errors, offline

| Condition | Behaviour |
|---|---|
| Transport failure, `429`, `5xx` | up to 3 attempts, backoff 400 ms then 800 ms |
| `400`, `401`, `403`, `404` | no retry |
| Success with unparseable body | no retry; fallback |
| Config offline | cached last-known-good, else defaults |
| Report offline | clear failure message; no queue, no background retry; documents unaffected |
| Timeouts | 8 s connect, 10 s read |

Configuration flags fetched but not yet enforced: `limits.max_document_mb`, `limits.max_pages`, `announcement`, and the `office_engine`/`ocr`/`image_editor`/`dark_pages`/`annotations`/`form_fill` flags. They are parsed and displayed, but changing them is a product decision that would alter existing user workflows; only the URL-to-PDF kill switch is wired to behaviour.

---

## 6. Privacy review

### Collected, only after explicit consent and confirmation

| Field | Source | Purpose |
|---|---|---|
| `install_id` | random UUID generated on first submission | rate limiting / de-duplication; report requests only |
| `app_version`, `app_version_code` | package manager | reproduce on the right build |
| `report_type` | user choice from a fixed list | triage |
| `title`, `description` | user-written | reproduce the issue |
| `android_api_level` | `Build.VERSION.SDK_INT` | platform compatibility |
| `locale` | default locale | layout/text issues |
| `error_code`, `screen` | optional app-set values | locate the failing surface |

### Never collected

Document bytes, file names, URIs, page text, OCR output, device model/manufacturer, serial/IMEI, advertising ID, location, contacts, accounts, clipboard, analytics events, crash breadcrumbs, or background telemetry.

### Enforcement

- Server-side `submit-report` rejects any top-level or payload key outside the allowlist with `400 invalid_field`; verified live with `device_model`.
- Server-side retention default is **30 days** (`hmx.system_settings.report_retention_days`), purged by `purge-expired-reports`.
- No PDF/DOCX code path is referenced from the backend package; the report form takes only user text.
- Diagnostics are off by default; the consent switch starts off on every install.

---

## 7. Cloud build and test results

All builds ran on GitHub-hosted runners. No Gradle, APK build, or test ran on the owner's device.

Final run: **[44917795687](https://github.com/tanishka1233/HMXPDF/actions/runs/44917795687)** — **success**

| Job | Result |
|---|---|
| `build` (unit tests, lint, debug APK) | success |
| `staging` (opt-in: compile `playStaging` + APK) | success |
| `release-guard` (missing signing must fail) | success |
| `release` (signed candidate) | skipped (`build_signed_release=false`) |

### Unit tests

```
com.hmx.pdf.backend.RemoteConfigParserTest: tests=8 failures=0 errors=0 skipped=0
com.hmx.pdf.backend.ReportRequestBuilderTest: tests=7 failures=0 errors=0 skipped=0
com.hmx.pdf.backend.RetryLoopTest: tests=6 failures=0 errors=0 skipped=0
com.hmx.pdf.backend.RetryPolicyTest: tests=3 failures=0 errors=0 skipped=0
TOTAL tests=399 failures=0 errors=0 skipped=42
```

24 new backend tests; 399 app tests total, zero failures. The 42 skips are pre-existing.

What the tests cover:

- **Configuration parsing:** live staging document (v3), malformed JSON, wrong schema, unknown feature key, compatibility fallback, cache encode/decode round trip.
- **Diagnostic submission payload:** allowlisted fields present; device/document/analytics field names absent; report-type allowlist; install-ID format; text truncation; config URL contains no install identifier.
- **Retry behaviour:** transient recovery, 429 retry, no retry on 4xx, attempt cap, no retry on unusable success body, backoff progression.
- **Privacy boundaries:** the forbidden-field scan plus the live server rejection described in §8.

AGP only creates unit-test tasks for debug/release, so the staging variant is verified by compiling it (`compilePlayStagingKotlin` + `assemblePlayStaging`). Its Kotlin logic is the same code exercised by the Play-debug JVM tests.

### Binary verification

Downloaded the CI artifacts and inspected the DEX and manifest:

| Check | Staging APK | Debug APK (production flavour) |
|---|---|---|
| Staging project ref | present | absent |
| Production project ref | absent | absent |
| `/functions/v1` string | present | absent |
| `sb_publishable_` key | present | absent |
| `sb_secret_` / `service_role` | absent | absent |
| Cron secret name / DB password value | absent | absent |
| `android.permission.INTERNET` | present | present |

`INTERNET` is merged into all variants by an existing dependency, not added by this change; the staging manifest now declares it explicitly for the feature that uses it.

---

## 8. Live staging backend verification

Executed against `hwwnksxfrllkrqonngob` with the staging publishable key. A synthetic report was created and then deleted; its rate-limit bucket was reset. No production endpoint was called.

| Check | Result |
|---|---|
| `GET /get-app-config?app_version_code=21` | `200`, version 3, all 7 flags, `compatible: true` |
| `GET …app_version_code=abc` | `400 invalid_query` |
| `POST /submit-report` (valid synthetic report) | `201`, id + `status: "new"` |
| `POST /submit-report` with `device_model` | `400 invalid_field`, field named in error |
| `POST /submit-report` with `report_type: tracking` | `400 invalid_field`, allowlist returned |
| `POST /submit-report` without `apikey` | `401 unauthenticated` |
| Anonymous PostgREST access to `hmx` | denied (verified in Phase 5) |

The valid submission exercised the same JSON the Android builder emits, including `android_api_level`, `locale`, `error_code` and `screen`. Cleanup removed the report and the `install:hmx-phase6-contract-0001` rate-limit row.

Production integrity was not touched during Phase 6: no request reached the production data plane or any production Edge Function. The only production call was a read-only Management API listing of secret *names* (to confirm `HMX_CRON_SECRET` exists); no value was read or printed.

---

## 9. Retention scheduler (documented, not enabled)

`purge-expired-reports` already exists and is deployed in both projects, with `HMX_CRON_SECRET` present in both.

Behaviour:

- `POST /functions/v1/purge-expired-reports`
- Requires `Authorization: Bearer $HMX_CRON_SECRET`; constant-time comparison. Missing secret → `503 cron_unconfigured`; wrong token → `403 invalid_cron_token`.
- Reads `hmx.system_settings.report_retention_days`; default **30** days when absent or invalid.
- Deletes in batches of up to 500, at most 100 batches per invocation, using `submitted_at` UTC.
- Returns `{ data: { deleted, batches, executed_at } }`; each deletion writes an audit row.

### How to enable daily (owner approval required)

**Option A — Supabase scheduled function (dashboard):**
Project → Integrations → Cron → new job, schedule `15 3 * * *` (03:15 UTC), HTTP request to the function URL with header `Authorization: Bearer <HMX_CRON_SECRET>`.

**Option B — `pg_cron` + `pg_net` inside the database:**

```sql
select cron.schedule(
  'hmx-purge-reports',
  '15 3 * * *',
  $$
  select net.http_post(
    url     := 'https://<project-ref>.supabase.co/functions/v1/purge-expired-reports',
    headers := jsonb_build_object('Authorization', 'Bearer ' || current_setting('hmx.cron_secret'))
  );
  $$
);
```

Store the secret with `alter database postgres set hmx.cron_secret = '…';` rather than in the job body.

**Option C — external scheduler (GitHub Actions, cron service):** daily `curl -X POST` with the bearer header, alerting on non-200.

Recommended schedule: daily 03:15 UTC. Enable in **staging first**, confirm the response and audit rows, then request production approval. Nothing was scheduled in either project during this phase.

---

## 10. Remaining blockers and risks

1. **No device-level end-to-end run.** This environment has no Android SDK/emulator and the owner's device has limited RAM, so runtime UI, real offline transitions, and notification/lifecycle behaviour were not exercised. Installing the staging APK on a test device is the remaining verification step.
2. **Real password reset / SMTP** remains unverified (from Phase 5) and is unrelated to the Android client.
3. **`limits` and most feature flags are parsed but not enforced.** Only `url_to_pdf` is wired; enforcement of the rest needs a product decision.
4. **ETag caching is not used** by the client; each fetch downloads the full small document.
5. **Staging publishable key is embedded in the staging APK** — expected and acceptable for a publishable key, but it means the staging APK must not be distributed publicly.
6. **No production scheduler** and no production deploy were performed. Production integration remains intentionally untouched.
7. **Package/signing/version untouched.** The staging APK is debug-signed by CI and is not a release artifact.

---

## 11. Rollback

- Android: `git revert` the Phase 6 commits or delete `feature/hmxpdf-backend-integration`. Production variants contain no backend code paths at runtime; the client is compiled out.
- Backend: no migration, function or secret change was made in this phase, so there is nothing to roll back server-side.
- Staging data: the synthetic report and its rate-limit bucket were deleted; staging config remains at published version 3.

---

## 12. Stop point

Phase 6 ends here. No production release, no production Supabase change, no protected-repository access, and no package/signing/version change occurred. Production integration and any release require explicit owner approval.
