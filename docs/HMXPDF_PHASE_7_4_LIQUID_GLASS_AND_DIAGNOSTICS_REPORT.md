# HMXPDF Phase 7.4 — Liquid Glass Privacy UI + Diagnostics End-to-End Repair (Android)

**Repo:** `tanishka1233/HMXPDF`, branch `feature/hmxpdf-backend-integration`.
**Basis:** Phase 7.3 head `00f8636`, nothing unrelated changed.
**Production `HMXPANEL/HMXPDF`:** never accessed. **Production backend `qjhlzsopnwozpsdosbjt`:** untouched (read-only counts only). **Staging `hwwnksxfrllkrqonngob`:** authorized target.
**Date:** 2026-11-27.

---

## What the owner reported

1. First-launch privacy dialog renders after the 7-page onboarding, but **looks like a plain white Material card**.
2. **Tapping text inside the dialog sometimes shows a temporary grey/black rectangular highlight.**
3. **Diagnostics reports submitted from the real device still don't appear in the Admin Panel**, despite Phase 7.3 claiming it was fixed. Treated as a **P0 until disproven**.

---

## Task A — Liquid Glass privacy dialog

### A1. Existing design system inspected (source, not screenshots)

Reused, not reinvented:

- `GlassDialog` + `GlassDialogAction` (`app/src/main/java/com/hmx/pdf/ui/components/GlassDialog.kt`): in-window dialog sampling the live `backdrop`, translucent `glassDialogSurface`, `drawBackdrop` blur + `lens` + `colorControls` + `vibrancy`, `Highlight.Plain`, `RoundedRectangle(48.dp)`, `GlassDialogField`, `glassDialogPlatter`, `glassDialogInk`, `glassDialogInkSoft`, `glassDialogPlatter`, spring entrance; scrim + panel `clickable(indication = null)`.
- `LiquidButton` / `GlassDialogAction` for actions (`LiquidButton` liquid pills, press deformation, `InteractiveHighlight`).
- **`LiquidToggle`** (`LiquidToggle.kt`): the app's own liquid switch, `Role.Switch`, `toggleableState`, haptic, drag. The dialog previously used Material3 `Switch`; now it uses `LiquidToggle`.
- **`AccentIconTile`** (`ToolKit.kt`): tinted icon tile, `accent.copy(0.14/0.24)`.
- `liquidPressGlow` (`ui/utils/LiquidPressGlow.kt`): soft radial press feedback, **no ripple**.
- `GlassMotion` (`press`, `fade`), `GlassDimens`, `LiquidGlassColors`, `LocalIsDarkMode`, `glassDialogInk*`, `ToolInk`.
- `SettingsScreen.SettingsToggleRow` as the reference row pattern (icon tile + title/desc + toggle, `clickable(indication = null)`, press scale).
- Icons from `material-icons-extended` (`Icons.Rounded.Lock`, `BugReport`, `BarChart`).
- `GlassStyle`, `GlassSettings` via `drawBackdrop` on `GlassDialog` (app's glass tuning still applies).

### A2. Refinement (`PrivacyConsentDialog.kt`)

- Kept `GlassDialog` + `GlassDialogAction`. Added optional `panelShadow: Boolean = false` to `GlassDialog` (default unchanged for other dialogs); privacy passes `panelShadow = true` → soft shadow + glass rim + blur + lens → reads as glass, not a flat white card.
- Hero `AccentIconTile(Lock, Blue)`.
- Each independent choice is a **layered glass card**: `RoundedCornerShape(18.dp)`, hairline border, platter background, **selected tint + accent border** when on, animated `animateColorAsState`.
- Per-choice tinted icon tile (`BugReport`/Blue, `BarChart`/Purple).
- Theme-aware ink via `glassDialogInk()`/`glassDialogInkSoft()`, `LiquidGlassColors.*`; no hardcoded contrast-breaking colors.
- `LiquidToggle` for each independent option.
- Scrollable body (`verticalScroll`, `heightIn(max = screenHeight * 0.52)`), action row stays reachable; `GlassDialog` already `widthIn(max=420.dp)`, `systemBarsPadding`, `imePadding`, scrim `dismissOnScrimTap=false`.
- Optional policy link only when `approvedPrivacyPolicyUrl` non-blank; `APPROVED_PRIVACY_POLICY_URL` is `null`. `TextDecoration.Underline`, no ripple.
- No Brave UI/branding/artwork. Home screen, onboarding, consent logic **unchanged**. Not an Android system overlay; no `SYSTEM_ALERT_WINDOW`.

### A3. Text selection / highlight root cause

**Cause:** `PrivacyChoiceRow`'s informational column used plain `Modifier.clickable { onCheckedChange(!checked) }` **without `indication = null`**, so Compose Material **applies the default `LocalIndication` ripple** to a rectangular `Column`. On tap it paints a temporary bounded rectangular grey/black highlight over the text; on some Android 15 devices it reads as a selection rectangle. (`GlassDialog`'s own scrim/panel already use `indication = null`, which is why only rows flashed.) No native text selection existed; `BasicText` isn't selectable and no `SelectionContainer` wraps the dialog.

**Fix:**
- Informational body text is purely non-interactive; never `clickable`, never inside `SelectionContainer`.
- The only tap target in a choice card is the **header row** (`clickable(interactionSource, indication = null)`) + the `LiquidToggle`; no rectangular ripple.
- Press feedback = designed `liquidPressGlow` + `animateFloatAsState` scale + state color change; accessibility preserved (`Role.Switch`, `toggleableState`, `onClick` semantics on `LiquidToggle`; `paneTitle`; text readable; not disabling interactions).
- Policy link `clickable(indication = null)`.
- Scrolling retained; buttons/switches retained; links retained.

### A4. Theme verification

- Light + dark tokens only; `LocalIsDarkMode`. No dark-on-dark / light-on-light. Reused `glassDialogSurface(isDark)`, `glassDialogPlatter`, `LiquidGlassColors.text/secondary`, `AccentIconTile` theme-aware tint, `LiquidToggle` theme-aware.
- Same staging token verification as Phase 7.3 for **Diagnostics** + **Analytics** + home + settings. No regressions. (See `DiagnosticsSettingsSection`, `AnalyticsSettingsSection`.)
- Local emulator screenshotting is unavailable; **owner device re-check on OPPO A38 remains the final visual gate**. Compile + lint + CI green.

### Task D — Privacy consent behavior (unchanged)

7-page onboarding → Home → in-app `GlassDialog`; independent diagnostics/analytics; **both OFF**; Continue / Not now; prompt-completion stored separately; once per install; Settings can change both independently; no forced consent; nothing persisted by the prompt except a completion flag. 12 JVM `PrivacyPromptPolicyTest` tests.

---

## Task B — Diagnostics submission still didn't reach Admin (P0)

### B1. Exact runtime configuration

**Cannot inspect the owner's installed copy.** From source + CI artifact + **the owner's own persisted rows**:

- **Android build commit:** `aa75bbe` (this change); Phase-7.3 artifact `df1ffb0`. No diff affecting diagnostics/endpoint between `df1ffb0` and `aa75bbe`.
- **Build variant:** `playStaging` (`assemblePlayStaging`), `initWith(debug)`, `isMinifyEnabled=true`, `buildConfig=true`.
- **Version:** `com.hmx.pdf`, `versionName=2.1.1`, `versionCode=6`, `minSdk=23`, `targetSdk=36`, `compileSdk=36`, platform 16.
- **Backend endpoint:** `BuildConfig.HMX_BACKEND_URL=https://hwwnksxfrllkrqonngob.supabase.co/functions/v1`, `submit-report` → `.../submit-report`.
- **Supabase project ref:** `hwwnksxfrllkrqonngob` (staging; verified `production` ref `qjhlzsopnwozpsdosbjt` absent from APK DEX).
- **Publishable key identity:** `sb_publishable_BKpgc4DhPuIM7H4nQWjK4Q_KtYBBjgq`, sent as `apikey`. No service-role key.
- **Build timestamp:** CI run `45976417601`.
- **Owner device evidence proves staging config:** their submissions landed in staging `hmx.reports`, `app_version=2.1.1`, `app_version_code=6`, `android_api_level=35` (Android 15 OPPO A38), `locale=en`. So the installed staging APK **is connected to `hwwnksxfrllkrqonngob`**.

### B2. Android submission path traced + hardened

Diagnostics → consent check (`consentEnabled = BackendConfig.enabled && prefs[diagnostics_consent]`) → type allowlist → non-blank title → `ReportRequestBuilder.build` (only `install_id`, `app_version`, `app_version_code`, `report_type`, `title`, `description`, `diagnostic_payload{android_api_level,locale,error_code?,screen?}`; no document content) → `DiagnosticsRepository.submit` → `RetryLoop` (≤3 attempts; retry only transport/429/5xx; 400/401/403 not retried) → `HttpJson` POST `.../submit-report`, `apikey`, `Content-Type: application/json`, `x-request-id`, **`x-hmx-submission-id`**, 8s connect / 10s read timeout → response parsed → `SubmitResult` → UI.

**Accurate success (no false positive):** previously `decode = { SubmitResult(true, ...) }` on **any** 2xx. Now success only if the body proves persistence: `data.id` non-blank **and** `data.submitted_at` non-blank (`SubmitResponseParser`); unconfirmed 2xx → safe failure message. `HttpResult.Success` now carries `status`. Safe, non-user-content message `Report sent. Reference <id8>. Thank you.` Failure messages are safe static strings.

**Safe structured diagnostics (`DiagnosticsTelemetry`, tag `HMXDiag`):** `report.submit.started`, `report.submit.attempt`, `report.submit.persisted`, `report.submit.failed` with `project`, `submission`, `request`, `attempt`, `http_status`, `category` (`ok/unconfirmed_body/transport/rate_limited/server_error/rejected/http_N`). **Never** logs title, description, document, install id, keys/tokens, or full server body. `project` is the public staging ref already in the APK.

**Duplicate protection:** UI blocks re-tap while `busy` + explicit two-step confirm; stable `submissionId` reused across bounded retries; backend idempotency (below).

### B3. Deployed staging Edge Function verified (not just source)

- `submit-report` **ACTIVE**, redeployed `hwwnksxfrllkrqonngob`. `strings` of downloaded ESZIP shows deployed source matches local (`Unknown report field`, `consume_rate_limit`, `.select("id, submitted_at")`, `apiErrorFromDatabase`).
- Function name `submit-report`, POST only, `requireApiKey`, allowlisted fields, payload validation, **service-role** insert into `hmx.reports`, `.single()`, `error` checked, returns **201** `{data:{id,submitted_at,status}}`, **not** silent success; `apiErrorFromDatabase` non-leaking. `x-request-id`.
- **Not** inserting into wrong schema/table; `hmx.reports`. **RLS + FORCE RLS unchanged.** See `docs/HMXPDF_PHASE_7_4_E2E_VERIFICATION.md`.
- `admin-api` **ACTIVE**, redeployed `hwwnksxfrllkrqonngob`.

### B4. Supabase persistence (staging only)

Real device + synthetic prove a row is actually inserted, then retrievable. No production writes. RLS/FORCE unchanged; no public read. See backend doc.

### B5. Admin Panel traced + fixed

`/diagnostics` route; `GET /admin-api/admin/reports`; `hmx.admin_list_reports` (`security definer`, `require_admin(viewer)`); filters `status/type/q`; keyset pagination; **no accidental filter hiding `new`** (`ALL_FILTER` sentinel); `admin_get_report`; `PATCH /reports/:id` `review_report` (`require_admin(support)`, `dismissed`→admin; `new→triaged|resolved|dismissed`, `triaged→resolved|dismissed`); `hmx.admin_audit_log` `report.status_changed`; panel uses **same staging project** + `hmx` schema; **no local fabricated data**.

**Found stale-view defect:** page loaded once on mount; **no auto-refresh**. A report submitted while the page is open wouldn't appear until reload. Added `useLiveRefresh` + `Refresh` (`src/hooks/useLiveRefresh.ts`, `DiagnosticsPage`, `DashboardPage`): `visibilitychange` + `focus` + 30s poll, silent reload preserving list, `inFlight` guard, `cursorRef`. Admin redeployed `hmxpdf-admin-staging`.

**Found search defect:** `sanitizeSearchQuery` stripped `_`, so `HMXPDF_PHASE_7_4_E2E_TEST_*` couldn't be found by `q`. Preserved `_` (still strips `%`,`*`,`\`,control); still role-checked + parameterized. `admin-api` redeployed.

### B6. Exact root cause (classification)

**Not** wrong APK config, wrong endpoint, wrong Edge Function, headers, body, RLS rejection, wrong schema/table, admin reading another project, or false Android success in practice. **The P0 "not appearing" is explained by:**

1. **(B5, environment) The staging app writes to staging; an Admin Panel reading production shows nothing.** Phase 7.3's "fixed" claim was made without real-device verification. Correct view is **staging Admin Panel `https://hmxpdf-admin-staging.vercel.app`**; production `hmxpdf-admin` reads `qjhlzsopnwozpsdosbjt` and is intentionally untouched. We **cannot** make production show staging data without modifying production (not authorized). The app cannot print the admin URL (no admin URL in APK).
2. **(B5, stale view) Admin didn't refresh.** Fixed with live refresh + manual Refresh.
3. **(B2, robustness) Android treated any 2xx as success.** Fixed to require persisted id. (No in-practice false success observed.)
4. **(duplicate) retry could duplicate.** Fixed with `x-hmx-submission-id` + unique `client_submission_id`.
5. **(B5, search) `_` stripped in admin search.** Fixed.

**Real-device evidence proves persistence + retrieval; we cannot see which Admin URL the owner opened.** We do not claim the owner was never able to find the report for any reason other than (1)+(2); we prove (1)+(2) are real and fixed. If after checking the **staging** panel with a **Refresh** the exact report still isn't there, that's not reproducible here — see limitations.

### B7. Corrected integration

- Android: correct staging endpoint/contract; accurate `SubmitResult`; safe errors; safe retry; duplicate protection.
- Backend: correct persistence; validation; `201` + id; existing rate limits + `require_admin` + `review_report`; idempotency; safe logs.
- Admin: staging project; correct query/filters/pagination; **refresh**; correct detail; `search` underscore.

### B8. Real end-to-end acceptance

**Real device (owner OPPO A38, Android 15, staging APK):** `76575af9-e099-4d16-93a9-cecceef9f9ce` (title `bisjsbs`, `bug`, 2026-11-27T06:18:34Z) and `17056a2c-fec7-4936-bca7-39424df60fb8` (title `bug`, 2026-11-27T06:20:47Z), `app_version=2.1.1`,`app_version_code=6`,`android_api_level=35`. **Persisted in staging; retrieved via Admin API + shown in staging panel; no console errors.** **Not claimed** we re-tested from a real device in this session; we re-verified the persisted rows.

**Cloud synthetic E2E (explicitly NOT real-device):** title `HMXPDF_PHASE_7_4_E2E_TEST_<uuid>`, synthetic id **`835189c4-9ff5-49f2-840a-bdadc79ee50d`**; `201` + id; DB row `status=new`,`client_submission_id=…`; no duplicate; Admin list + detail; **PATCH triaged** `reviewed_by`=owner; audit `report.status_changed` role `owner`; **UI** queue+detail+triaged; production count `0`. Synthetic cleaned up; earlier synthetic rows + a separate idempotency test + a search test removed. Audit entries remain (append-only). Full steps: backend doc.

---

## Task C — Analytics recheck (separate from diagnostics)

Not merged. Re-verified on staging: consent `register`, ingest (`app_open`,`session_start`,`pdf_open`,`pdf_export`,`docx_open`,`scan_use`; no doc content), DB persistence, aggregate RPC, `GET /admin/analytics`, `/analytics` UI; `opted_in_installations` increments; no diagnostic report created; no hardcoded metrics. **Opt-out/delete:** `analytics_revoke_installation`, `verified=true`, `deleted_events`, `identity_deleted`, `refreshed_aggregate_days`; raw + daily + aggregate + `new_installations` withdrawn; aggregate rebuilt; post-revocation ingest rejected; synthetic install removed. Phase 7.3 **52/52** still stands. **Remaining real staging state:** owner's real opted-in install `hmxa-…` (not synthetic; not removed) + `new_installations=1`; our synthetic removed; no synthetic events. Analytics consent independent from diagnostics. Privacy semantics preserved.

---

## Task E — Cloud testing only

No local Gradle/Android build. No emulator. `npm ci` local admin attempts failed on cache; **admin CI on GitHub Actions green**; Android CI green.

| Check | Result |
|---|---|
| Android `build` (JVM unit + lint + debug APK) | success |
| Android `staging` (`assemblePlayStaging`, compile tests, upload, **APK inspection**) | success |
| `release-guard` | success; `release` skipped (not a tag) |
| Android JVM unit tests | **440 tests, 0 failures, 0 errors, 42 skipped** (+8 `SubmitResponseParserTest` vs 432 baseline) |
| Admin typecheck+tests+build | success; **34→35** tests incl `useLiveRefresh.test` |
| Admin `deno check` Edge Functions | success |
| APK inspection | no `qjhlzsopnwozpsdosbjt`, no admin URL/route, no `service_role`/`sb_secret_`/`SUPABASE_SERVICE`/`admin_members`; staging endpoint present |

**Android CI run:** `https://github.com/tanishka1233/HMXPDF/actions/runs/45976417601` (commit `aa75bbe`).
**Admin CI:** `https://github.com/tanishka1233/HMXPDF-Admin/actions/runs/45978340978` (`c7ce5cc`); `45975535186` (`fc10196`).
**APK artifact:** `hmxpdf-staging-apk`. **`app-play-arm64-v8a-staging.apk`** 42,708,211 bytes, SHA-256 `bdada4c7608666871601938a5c84103d0cd9ae1b0f53409eb873349e4a0c45d9`. (4-ABI + universal also in artifact.) Package `com.hmx.pdf` `2.1.1`/`6`. Debug-signed; no production release; signing key/version unchanged.

---

## Task F — Security preserved

No change to `hmx.admin_members`, roles, `require_admin`, `review_report`, `admin_list_reports`, `admin_get_report`, RLS/**FORCE RLS**, audit log, rate limits. `x-hmx-submission-id` is a random UUID, no content; `client_submission_id` server-side; not exposed to admins. **APK contains no** admin URL, admin login, admin routes, credentials, service-role key, management token; only staging endpoint + publishable key. No secrets in CI logs. Safe `DiagnosticsTelemetry`. Migration on **staging only**. Nothing in `HMXPANEL/HMXPDF`.

---

## Source files changed (Android `aa75bbe`, `6fe9fed`, …)

- `app/src/main/java/com/hmx/pdf/ui/components/PrivacyConsentDialog.kt` (rewritten, glass, no ripple, scroll, `LiquidToggle`, `AccentIconTile`, `liquidPressGlow`, `panelShadow`)
- `app/src/main/java/com/hmx/pdf/ui/components/GlassDialog.kt` (+ optional `panelShadow`)
- `app/src/main/java/com/hmx/pdf/backend/HttpJson.kt` (`status`, `x-request-id`, `extraHeaders`)
- `app/src/main/java/com/hmx/pdf/backend/RetryLoop.kt` (+ `observe`, robust `observe`)
- `app/src/main/java/com/hmx/pdf/backend/Repositories.kt` (accurate success, `reportId`, `submissionId`, `requestId`, `x-hmx-submission-id`, telemetry; `SubmitResult`)
- `app/src/main/java/com/hmx/pdf/backend/SubmitResponseParser.kt` (new)
- `app/src/main/java/com/hmx/pdf/backend/DiagnosticsTelemetry.kt` (new, safe)
- `app/src/test/java/com/hmx/pdf/backend/SubmitResponseParserTest.kt` (new, 8 tests)
- `app/src/main/java/com/hmx/pdf/ui/screen/DiagnosticsSettingsSection.kt`, `AnalyticsSettingsSection.kt`, `SettingsScreen.kt`, `DocsApp.kt`, `PrivacyConsentManager.kt`, strings — **unchanged in Phase 7.4** (privacy behavior + dark mode preserved).

---

## Task G — This report. Residual limitations

1. **Can't see which Admin URL the owner opened; can't inspect their installed APK.** We prove persistence + retrieval; **use `https://hmxpdf-admin-staging.vercel.app`** (staging) + **Refresh**.
2. **No local Android runtime / no Compose screenshot test infra.** Visual glass + no-rectangle confirmation on OPPO A38 is owner re-check; code reuses tokens/components; CI + lint green. We don't fabricate screenshots.
3. **Phase 7.3 overclaim corrected:** "diagnostics fixed" wasn't real-device-verified then; now it is.
4. **Admin search `_`** now preserved; still no id-prefix search; not a defect blocking retrieval.
5. Real-device analytics opted-in install remains; not our synthetic.
6. **Production untouched.** No production migration, deployment, scheduler, release, signing, or `HMXPANEL` access. Release tag path unchanged.
