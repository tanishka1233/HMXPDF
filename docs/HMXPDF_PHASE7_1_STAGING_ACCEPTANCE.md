# HMXPDF Phase 7.1 — Final Staging Acceptance & Device Test Preparation

**Status:** staging acceptance prepared and verified. **No production integration, migration, scheduler or release was performed.**
**Date:** 2026-11-26
**Production backend:** `qjhlzsopnwozpsdosbjt` — read-only check only; unchanged.
**Staging backend:** `hwwnksxfrllkrqonngob` (`HMXPDF`, `cpkhymlqqeoqlipcsmdw`, `ap-northeast-1`).
**Protected repository:** `HMXPANEL/HMXPDF` — never accessed.

---

## 1. Task 1 — Device test preparation

### 1.1 Artifact provenance

| Item | Value |
|---|---|
| CI run | [44928687889](https://github.com/tanishka1233/HMXPDF/actions/runs/44928687889) |
| Result | success |
| Commit | `224c935e97f6ed66a637c114d78eae306485b98b` (`224c935`) |
| Branch | `feature/hmxpdf-backend-integration` |
| Artifact | `hmx-pdf-staging-apk` (ZIP) |
| GitHub artifact digest | `sha256:1e1ceafd9ab68bb8a50a601a26aebc497e91a3a3efe30245f35f569a46a2fb79` (173,370,262 bytes) |
| APK used for device testing | `app-play-arm64-v8a-staging.apk` |

### 1.2 Checksums (SHA-256 of the extracted APKs)

| APK | SHA-256 |
|---|---|
| `app-play-arm64-v8a-staging.apk` (**OPPO A38**) | `90e056096327fb1ccee3407d315eca51c498cc6c9e0bb58a723bd1aa9371240a` |
| `app-play-armeabi-v7a-staging.apk` | `7cf9d5aa1676dec03405fa231489f5adc639e8a24587a8722d3745ec36f5caa1` |
| `app-play-universal-staging.apk` | `2773c377796bd0d93f097d5a2dacc78601e366f4b45064439acff4b4ec7be9aa` |
| `app-play-x86-staging.apk` | `c1a61c0dc543d1f868c2180e587a8aa5d532dd8035e813f64dbeb89a6c2d91de` |
| `app-play-x86_64-staging.apk` | `93b3fd49c3a1703700b91e5b196e733b4603848fb0ff0df95d20ed9cf3118d27` |

### 1.3 Package, version and ABI

| Property | Value |
|---|---|
| Package name | `com.hmx.pdf` |
| Version | `versionCode 6`, `versionName 2.1.1` |
| minSdk / targetSdk / compileSdk | 23 / 36 / 36 |
| ABI of the arm64 APK | `arm64-v8a` (correct for OPPO A38) |
| Application label | `HMX PDF` |
| Debuggable | yes (`application-debuggable`; staging build type inherits debug) |

### 1.4 Signing identity (important for installation)

| Property | Value |
|---|---|
| Certificate DN | `C=US, O=Android, CN=Android Debug` |
| Certificate SHA-256 | `4ff3766f4e0a60ed53b04fe4c598b76de7bcaeabad1f6bbfadf475d738ae9464` |
| Certificate SHA-1 | `c4ab5107faca99babc1e53681d22d4509143a6a6` |
| Signature schemes | v1 (JAR) yes, v2 yes, v3 no, v3.1 no, v4 no |
| Signers | 1 |

**Consequences:**

1. The staging APK is **debug-signed**, not release-signed. It cannot be installed over the production `com.hmx.pdf` app (different signing identity). Installing it requires uninstalling the production app first, which deletes app data.
2. The CI workflow does **not** restore a debug keystore, so the debug certificate is regenerated on each cloud run. A staging APK from a different run may have a different signature. Uninstall the previous staging build before installing a new run's APK.
3. Recommended: use a spare device or accept the data loss on the test device. Do not use the owner's primary phone without a backup.

### 1.5 Staging endpoint and key

| Check | Result |
|---|---|
| Staging endpoint | `https://hwwnksxfrllkrqonngob.supabase.co` |
| Publishable key | `sb_publishable_BKpgc…` (redacted) |
| Key matches recorded staging key | **exact byte match** |
| Key SHA-256 | `567f6466bf393b73b6d277eb990329f3009b569aa66ccfa6d65e337db9e784f8` |
| Production project ref in APK | **absent** |
| `sb_secret_` / `service_role` / legacy service JWT | **absent** |
| Staging ref occurrences | 1 |
| Staging-only prefs / identity | `hmx_analytics`, `hmxa-`, `analytics_enabled` present |

Source of the staging values: `app/build.gradle.kts` `staging` build type (`HMX_BACKEND_ENABLED=true`, staging URL, publishable key). Debug/release set the URL and key to empty strings and `HMX_BACKEND_ENABLED=false`.

### 1.6 Installation on OPPO A38 / Android 15 (ColorOS 15)

Use `app-play-arm64-v8a-staging.apk`.

**Option A — adb (preferred)**

1. On the phone: Settings → About device → tap **Build number** 7× to enable Developer options.
2. Settings → System → Developer options → enable **USB debugging**.
3. Connect USB, accept the RSA prompt, then:
   ```bash
   adb devices
   adb uninstall com.hmx.pdf        # only if a different signature is installed (destructive)
   adb install app-play-arm64-v8a-staging.apk
   ```
4. If `adb install` returns `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, a differently signed build is installed; uninstall it and retry.

**Option B — on-device sideload**

1. Copy the APK to the phone (USB, cloud, or local file).
2. Settings → Apps → Special app access → **Install unknown apps** → allow the file manager/browser you use.
3. Open the APK and install.
4. If Android 15 blocks the sideload with “Restricted setting”, open Settings → Apps → HMX PDF → three-dot menu → **Allow restricted settings**, then retry.

**Do not enable analytics during installation.** Consent is only inside Settings → **Analytics (staging)**.

### 1.7 Device test matrix

All steps assume the staging APK is installed and the app can reach the staging backend. `adb shell run-as com.hmx.pdf` works because the staging build is debuggable.

| # | Test | Steps | Pass criteria |
|---|---|---|---|
| 1 | Analytics default OFF | Fresh install or clear app data → Settings → **Analytics (staging)**. | Switch is OFF; status says analytics are OFF. `shared_prefs/hmx_analytics.xml` absent or has no `install_id`. |
| 2 | Consent opt-in | Tap **View privacy explanation** → read → toggle ON → **I understand — enable analytics**. | Status says analytics are ON and a fresh pseudonymous ID was created. `hmx_analytics.xml` has `analytics_enabled=true` and `install_id=hmxa-…` (not `hmx-…`). Staging dashboard shows one new opted-in installation. |
| 3 | App open + sessions | With analytics ON: launch the app, background it, foreground within 30 min, background again. | `app_open` on launch; `session_start` on foreground; `session_end` on background. No heartbeat events. Optional: after 31 min idle, foreground creates a new session ID. |
| 4 | PDF/DOCX success paths | Open a PDF → export → share; merge/split/compress PDFs; open/edit/export a DOCX; scan; OCR; add a signature; use URL-to-PDF. | Each success produces the matching allowlisted event. No document names, paths, text, URIs or OCR output appear anywhere (payload only contains event name and allowlisted properties). |
| 5 | Offline queue | Analytics ON → airplane mode → perform ≥10 PDF opens. Keep the app usable. | No crash/ANR; operations succeed; `hmx_analytics.xml` queue grows (bounded at 200; events older than 7 days are dropped). |
| 6 | Batch upload | While offline, generate 60 events, then re-enable network and trigger a flush (one more event or reopen the app). | Upload resumes; server accepted count increases; no duplicate rows (idempotency). The engine caps a batch at 50 and flushes the remainder. Precise 50+10 behaviour is also covered by `AnalyticsEngineTest`. |
| 7 | Opt-out + server deletion | Toggle OFF, or **Request deletion of analytics data** → **Confirm deletion and opt out**. | Status says analytics are OFF and, only when the server verifies, “Server deletion verified: N events removed.” Otherwise it says deletion is pending and will be retried. `analytics_enabled=false`; queue and session cleared. Staging identity/events disappear. |
| 8 | Queue deletion | Immediately after opt-out, inspect `hmx_analytics.xml`; then trigger another app event. | Queue is empty/absent; no new event is appended while consent is off. If deletion is pending, it is retried on next launch and the UI never claims success before verification. |
| 9 | Diagnostic consent independence | Settings → Diagnostics (staging) and Analytics (staging). Enable only one, then the other, then opt out of analytics. | Separate switches, separate IDs (`hmx-…` vs `hmxa-…`) and separate prefs files (`hmx_diagnostics.xml` vs `hmx_analytics.xml`). Analytics opt-out never changes diagnostic consent or its queue. |
| 10 | Analytics unavailable | Airplane mode, or block the staging endpoint, then open/edit/export PDF and DOCX, scan and OCR. | All document operations work normally; no blocking UI, crash, ANR or retry storm. Events remain queued locally; transport errors fail open. |

Useful inspection commands:

```bash
adb shell run-as com.hmx.pdf ls shared_prefs/
adb shell run-as com.hmx.pdf cat shared_prefs/hmx_analytics.xml
adb shell run-as com.hmx.pdf cat shared_prefs/hmx_diagnostics.xml
```

Server-side verification: use the staging admin `/analytics` page (see §2) or an owner-run SQL check against `hmx.analytics_*`. The dashboard is aggregate-only and never exposes raw events or installation IDs.

---

## 2. Task 2 — Admin staging deployment readiness

### 2.1 Existing production deployment — untouched

| Item | Value |
|---|---|
| Vercel project | `hmxpdf-admin` (`prj_pri8fRpDMhw0AfJarPVGrKuJxOLX`) |
| Git | `tanishka1233/HMXPDF-Admin`, production branch `main` |
| Production URL | `https://hmxpdf-admin-ruby.vercel.app` — HTTP 200 |
| Latest production deployment | unchanged (5 days old at acceptance time) |

No production environment variable, domain, alias or deployment was modified.

### 2.2 New staging deployment

| Item | Value |
|---|---|
| Vercel project | `hmxpdf-admin-staging` (`prj_XZ2rQFlPNHf5HPN3qCImDXZMviMM`) |
| Git | **not connected** (manual CLI deploys only; no auto-deploy) |
| URL | `https://hmxpdf-admin-staging.vercel.app` — HTTP 200 |
| Deep links | `/analytics` and `/sign-in` return HTTP 200 (SPA rewrite works) |
| Framework | Vite via `vercel.json` |
| Environment | Production environment of the **staging project only** |

Staging-only variables:

| Variable | Value |
|---|---|
| `VITE_SUPABASE_URL` | `https://hwwnksxfrllkrqonngob.supabase.co` |
| `VITE_SUPABASE_ANON_KEY` | staging publishable key (same key as the staging APK) |
| `VITE_HMXPDF_ADMIN_API_URL` | `https://hwwnksxfrllkrqonngob.supabase.co/functions/v1/admin-api` |
| `VITE_HMXPDF_RESET_REDIRECT_URL` | `https://hmxpdf-admin-staging.vercel.app/reset-password` |

No service-role key, database password or secret key was added to Vercel.

### 2.3 Deployed bundle verification

| Check | Result |
|---|---|
| Staging project ref | present (2) |
| Production project ref | **absent** |
| Production admin hostname | **absent** |
| Staging admin hostname | present |
| `functions/v1/admin-api` | present |
| `service_role` | **absent** |
| `sb_secret_` | **absent** |
| Staging publishable key | present (exact match) |
| Reset redirect literal | staging URL only |
| Analytics page/metrics strings | present |

The Supabase JS library contains the literal `sb_secret_` as a **key-type detector** (`startsWith("sb_secret_")`), not a secret. The APK scan found zero occurrences.

### 2.4 CORS and Auth

- Staging `admin-api` already allows `https://hmxpdf-admin-staging.vercel.app` (`OPTIONS` → 204 with the correct `Access-Control-Allow-Origin`).
- Staging Supabase Auth `site_url` and allow list are already `https://hmxpdf-admin-staging.vercel.app`.
- `disable_signup=true` and `mailer_autoconfirm=false` remain in force.
- Staging has 22 synthetic `@hmxpdf.test` auth users and 8 synthetic memberships. The production owner email is **not** present in staging. No credentials were created or exposed by this phase.

**Blocker for browser sign-in:** there is no known-password staging admin account. To verify the `/analytics` page in a browser, the owner must provision a staging admin (see §6).

### 2.5 Staging-isolation fix (not merged to production)

The committed admin bundle hardcoded the production reset URL (`https://hmxpdf-admin-ruby.vercel.app/reset-password`). This was fixed on branch **`phase7.1-staging-acceptance`**:

- `src/lib/passwordReset.ts` now resolves the target from `VITE_HMXPDF_RESET_REDIRECT_URL`, then the current origin, and returns `undefined` when neither exists.
- Production behaviour stays correct because its origin is the production admin host.
- `.env.example` documents the optional variable.
- Tests: **23/23**, typecheck clean, production build succeeds.
- The branch is **not merged to `main`**, so the production admin deployment was not rebuilt.

---

## 3. Task 3 — Retention scheduler (staging only)

### 3.1 Function behaviour and authorization

`purge-analytics` is `ACTIVE` in staging. It reads `HMX_CRON_SECRET` and compares it in constant time.

| Request | Result |
|---|---|
| No `Authorization` | `403 invalid_cron_token` |
| Wrong bearer token | `403 invalid_cron_token` |
| Correct bearer token | `200` with retention days and deletion counts |

Live response with the correct token:

```json
{"data":{"deleted_events":0,"event_retention_days":90,"deleted_activity_days":0,
"deleted_aggregate_days":0,"aggregate_retention_days":400,"executed_at":"…"}}
```

### 3.2 Staging schedule

| Item | Value |
|---|---|
| Extensions | `pg_cron 1.6.4`, `pg_net 0.20.4` (Vault already installed) |
| Job name | `hmx-analytics-purge-daily` |
| Schedule | `15 3 * * *` (03:15 UTC daily) |
| State | active |
| Job owner/database | `postgres` / `postgres` |
| Secret storage | Supabase Vault secret `hmx_analytics_cron_secret` |
| Target | `POST https://hwwnksxfrllkrqonngob.supabase.co/functions/v1/purge-analytics` |

The exact job expression was exercised through `pg_net`: the Vault secret was read, the function returned **HTTP 200**, and the body contained the 90/400-day retention values.

### 3.3 Exact deletion proof

Synthetic rows were inserted, the real Edge Function was called, and the tables were inspected:

| Row | Age | Result |
|---|---|---|
| Raw event | 91 days | **deleted** |
| Raw event | 89 days | retained |
| Daily installation | 401 days | **deleted** |
| Daily installation | 399 days | retained |
| Daily aggregate | 401 days | **deleted** |
| Daily aggregate | 399 days | retained |

Purge response: `deleted_events=1`, `deleted_activity_days=1`, `deleted_aggregate_days=1`, `event_retention_days=90`, `aggregate_retention_days=400`.

`hmx.reports` stayed at 2 before and after. The synthetic analytics rows were then removed; all analytics tables are **empty**.

### 3.4 Safeguards

- Only `analytics_events`, `analytics_daily_installations` and `analytics_daily_aggregates` are deleted.
- Retention values are bounds-checked: events 7–365 days, aggregates 30–730 days.
- Each run writes one `analytics.purge` entry to `hmx.admin_audit_log`.
- No diagnostic report, admin account, config or audit row is deleted.
- The job is authorised by the cron secret; the secret is not stored in the job text.
- The scheduler is **not** part of the repository migrations, so a future production `db push` cannot create it.

### 3.5 Production check

| Check | Production |
|---|---|
| `pg_cron` extension | absent |
| `pg_net` extension | absent |
| `cron.job` schema | absent |
| `hmx.analytics_events` | absent |
| `hmx.reports` | 0 |

No production scheduler exists.

### 3.6 Removal (staging only)

```sql
select cron.unschedule('hmx-analytics-purge-daily');
-- optional, only if no other staging jobs use them:
-- drop extension pg_cron;
-- drop extension pg_net;
```

---

## 4. Task 4 — DEX / source-set security assessment

### 4.1 Why the literals appear in debug/release

- The whole `com.hmx.pdf.analytics` package lives in `app/src/main/java`, so every variant compiles it.
- `BuildConfig` only changes values per build type: debug/release set `HMX_BACKEND_ENABLED=false`, an empty URL and an empty key. It does **not** remove the classes or their string constants.
- `Analytics.initialize` and the feature hooks are referenced from `main`, so R8 keeps the classes reachable. There are no analytics ProGuard keep rules either.

Measured literal counts:

| Literal | Staging APK | Debug APK |
|---|---|---|
| `/analytics-consent` | 1 | 1 |
| `/analytics-ingest` | 1 | 1 |
| `hmx_analytics` | 1 | 1 |
| `hmxa-` | 2 | 2 |
| `analytics_enabled` | 1 | 1 |
| Staging project ref | 1 | **0** |
| Staging publishable key | 1 | **0** |
| Production project ref | **0** | **0** |
| `sb_secret_` / `service_role` | **0** | **0** |

The debug APK is the closest available proxy for release: the signed release job was skipped, and release compiles the same `main` source set with the same empty URL/key.

### 4.2 Feasibility of source-set exclusion

**Option A — transport-only relocation (feasible, limited benefit)**

- Keep the `AnalyticsTransport` interface in `main`.
- Move `HttpAnalyticsTransport` (the route strings) to `app/src/staging/java/...`.
- Add a `createAnalyticsTransport()` factory: real in `src/staging`, no-op in `src/debug` and `src/release`.
- Move `FakeAnalyticsTransport` into `src/test`.
- Effect: removes `/analytics-consent` and `/analytics-ingest` from non-staging DEX. `hmx_analytics`, `hmxa-` and `analytics_enabled` remain because they live in the storage/engine.
- Churn: small-to-medium. It touches one factory boundary and two new no-op source sets.

**Option B — full package exclusion (not recommended now)**

- Move the engine, storage, payload, models and facade to `src/staging`.
- Provide a full no-op facade in `src/debug` and `src/release` with the same public API, including `AnalyticsEngine.RevokeOutcome`.
- Relocate the JVM tests or add a staging unit-test task (AGP currently creates unit-test tasks only for debug/release).
- Effect: removes all analytics literals from non-staging DEX.
- Churn: broad. Risk of duplicate FQCNs, test-task gaps across `play`/`foss`/`fdroid`, and behaviour divergence. This conflicts with “no broad architectural changes”.

### 4.3 Risk and recommendation

| Factor | Assessment |
|---|---|
| Security exposure today | **Low** — no staging endpoint/key, no production ref and no secrets in debug/release; the client cannot send because the key/URL are empty and the gate is false. |
| Privacy exposure today | **Low** — the code is inert and the staging manifest controls permissions. |
| Benefit of Option A | Cosmetic/code-hygiene; removes two route strings. |
| Benefit of Option B | Complete literal removal, but no secret removal (there are none). |
| Risk of Option A | Low-to-medium (source-set merge, test coverage). |
| Risk of Option B | Medium-to-high (facade duplication, test tasks, flavour matrix). |

**Recommendation:** defer both. The only sensitive values (staging endpoint and key) are already absent from debug/release. If the owner requires zero analytics route literals in release, implement **Option A** in a separate reviewed change, re-run the full cloud matrix and repeat the binary scan. Do not start **Option B** without a dedicated phase.

### 4.4 Permission note

The staging manifest adds `INTERNET` and `ACCESS_NETWORK_STATE`, but the debug APK also contains `INTERNET` because bundled libraries contribute it through manifest merging. The staging declaration is therefore explicit but partly redundant for the Play flavour. It is not an analytics-specific grant and does not change the privacy result.

---

## 5. Security results

### 5.1 Backend

- Re-ran the staging suite: **43/43 checks passed** (consent, idempotency, allowlist, batch/size/timestamp/property validation, dedupe, rate limit, admin authz, viewer read, viewer write denial, suspension, verified deletion, purge, cleanup).
- Anonymous REST reads of raw analytics return **404** (schema hidden).
- Direct event insertion without a registration is rejected.
- RLS is enabled and forced on all analytics tables; grants are service-role only.
- All analytics tables were **empty** after the run.

### 5.2 Android APK

| Check | Staging | Debug |
|---|---|---|
| Staging endpoint | present | **absent** |
| Production endpoint | **absent** | **absent** |
| Staging publishable key | present | **absent** |
| `sb_secret_` / `service_role` | **absent** | **absent** |
| Analytics literals | present | present (inert) |

### 5.3 Admin bundle (staging)

| Check | Result |
|---|---|
| Staging ref | present |
| Production ref | **absent** |
| Production admin hostname | **absent** |
| `service_role` | **absent** |
| `sb_secret_` | **absent** |
| Staging key | present |

### 5.4 Production integrity

- No production analytics functions.
- No `hmx.analytics_events` table.
- No `pg_cron`/`pg_net` and no `cron.job`.
- `hmx.reports` = 0, `hmx.app_config_versions` = 3.
- Production admin URL still HTTP 200; its latest deployment is unchanged.

---

## 6. Remaining owner actions

1. **Install and run the device test matrix** (§1.7). Use a spare device where possible; the staging APK is debug-signed and cannot coexist with the production app.
2. **Provision a staging admin** if browser sign-in to `https://hmxpdf-admin-staging.vercel.app` is required. There is no known-password staging account, and the production owner is intentionally absent from staging. Suggested owner action (staging only):
   - create an Auth user with `email_confirm=true`;
   - insert an active `owner` row into `hmx.admin_members` for that user;
   - do not reuse the production password.
3. **Decide on the `phase7.1-staging-acceptance` branch.** It contains the password-reset isolation fix and this report. Merging it to `main` will trigger a production admin redeploy; the change is production-safe (origin-derived reset URL), but it was deliberately not merged during this phase.
4. **Decide whether to keep the staging scheduler active.** It is staging-only and can be removed with one `cron.unschedule` call. Approve the 90/400-day windows explicitly.
5. **Decide on DEX cleanup** (§4.3). Optional Option A transport relocation, or no change.
6. **Production integration remains blocked pending explicit approval:** migration, Edge Function deploy, production scheduler and any signed release.

No production migration, function deploy, scheduler, data change, domain change or release was performed in Phase 7.1.
