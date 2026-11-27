# HMXPDF Phase 7.3 — Privacy Choices, Diagnostics Repair & Analytics Verification

**Status:** staging implementation and verification complete. **No production migration, deployment, scheduler change or release was performed.**
**Date:** 2026-11-27
**Android branch:** `feature/hmxpdf-backend-integration` @ `4f7ca14`
**Admin branch:** `phase7.1-staging-acceptance` @ `f929e6d`
**Production backend:** `qjhlzsopnwozpsdosbjt` — untouched (read-only checks only).
**Staging backend:** `hwwnksxfrllkrqonngob` (`HMXPDF`, `cpkhymlqqeoqlipcsmdw`, `ap-northeast-1`).
**Protected repository:** `HMXPANEL/HMXPDF` — never accessed.

---

## 1. Task 1 — First-launch privacy and feedback dialog

### Implementation

| File | Purpose |
|---|---|
| `app/src/main/java/com/hmx/pdf/data/repository/PrivacyConsentManager.kt` | One-time prompt completion in its own `hmx_privacy_prompt` preferences file, plus pure `PrivacyPromptPolicy` and `PrivacyChoices`. |
| `app/src/main/java/com/hmx/pdf/ui/components/PrivacyConsentDialog.kt` | Compose dialog over the live Home screen with two independent switches, **Continue** and **Not now**. |
| `app/src/main/java/com/hmx/pdf/ui/DocsApp.kt` | Shows the prompt only after onboarding lands on Home; never over onboarding, What's New, a document window or an interrupted-save recovery prompt. |
| `app/src/main/res/values/strings.xml` | Dialog copy. |

Behaviour:

- Both choices default to **OFF** on a fresh install. The dialog seeds its switches from the stored diagnostics and analytics consent, so a returning user sees their real state if the prompt was interrupted.
- The choices are independent: enabling diagnostics never enables analytics and vice versa.
- **Continue** applies only the choices that differ from stored consent. **Not now**, back and scrim dismissal leave both consents unchanged.
- Prompt completion is stored separately from both consents. Completing or dismissing the prompt never grants consent, and changing consent later in Settings never re-opens the prompt.
- No privacy-policy link is rendered because no approved public URL exists in the repository. The required disclosures are written into the dialog. `APPROVED_PRIVACY_POLICY_URL` is `null`; supplying an approved URL later enables the link without changing consent logic.
- The prompt is gated by `BackendConfig.enabled`, which is true only for the staging build type today. Debug/release builds keep both backends disabled, so the prompt does not ask users to opt into inactive programmes. When production backend integration is approved, the prompt becomes active there without further UI work.

### Tests

`app/src/test/java/com/hmx/pdf/data/repository/PrivacyPromptPolicyTest.kt` covers: default-off choices, once-per-install, onboarding gate, document-window/recovery suppression, Home-only routing, both-off Continue, independence of the two switches, unchanged stored consent, and explicit opt-out.

---

## 2. Task 2 — Dark-mode contrast

`DiagnosticsSettingsSection.kt` and `AnalyticsSettingsSection.kt` now take every colour from the existing theme tokens:

- body/label ink: `LiquidGlassColors.text(isDark)`
- secondary/status copy: `LiquidGlassColors.secondary(isDark)`
- field surfaces: `LiquidGlassColors.neutralSurface(isDark)`
- borders: theme ink at 20–30% alpha
- switches and primary buttons: `LiquidGlassColors.Blue` with white content
- text fields: explicit focused/unfocused text, container, border, cursor and label colours via `OutlinedTextFieldDefaults.colors`

Both sections use `fillMaxWidth` columns and weighted rows, with no fixed text widths, so labels wrap instead of clipping on narrow screens. Light and dark tokens are both resolved from `LocalIsDarkMode`. Runtime light/dark screenshots on a physical device remain an owner action because no emulator is available in this environment.

---

## 3. Task 3 — Diagnostics app-to-admin path

### Root cause

Two independent defects explained the owner's report:

1. **Environment mismatch.** The staging Android build submits to staging (`hwwnksxfrllkrqonngob`), but the production Admin Panel is configured against production (`qjhlzsopnwozpsdosbjt`). A report submitted by the staging app can never appear in the production panel. The staging panel is a separate Vercel project and now shows a persistent **Staging environment** banner with the staging project host on the sign-in page.
2. **Latent Admin UI crash.** `DiagnosticsPage.tsx` used empty-string values for the “All statuses” / “All types” Radix `SelectItem`s. Radix forbids empty item values and can throw at render time, blanking the page. The page now uses an explicit `all` sentinel (`src/lib/reportFilters.ts`) and omits the API parameter when no filter is selected.

### Verification evidence

| Step | Result |
|---|---|
| Submit synthetic report through staging `submit-report` | accepted, `2026-11-27T03:56:12Z` |
| Row present in staging `hmx.reports` | yes |
| `admin-api /admin/reports` with synthetic admin token | found exactly the synthetic report |
| Admin UI `/diagnostics` after the fix | synthetic title and owner report `hsiabse` both visible; both filter dropdowns open without console/page errors |
| Synthetic report deleted | deleted by id; Admin API count returned `0` |
| Remaining staging reports | only the real owner report `hsiabse` (`2026-11-26T17:44:00Z`, status `new`) |
| Previously created synthetic staging reports | removed after verification |

The Admin UI was checked with Playwright at 1280×900; the Analytics page was also checked at 390×844. Neither page produced console errors, page errors or horizontal overflow.

---

## 4. Task 4 — Analytics app-to-admin verification

### Backend suite

`/tmp/opencode/hmx-analytics-suite.mjs` against staging: **52/52 checks passed**, including:

- staging identity guard, function health, anonymous/invalid-token rejection
- consent registration and idempotency
- valid event batches, deduplication, reliability events
- allowlist, property, size, timestamp, feature and error-category rejection
- missing registration and missing publishable key rejection
- rate limiting (`429`)
- admin aggregate access for owner/viewer, bounded date range
- viewer cannot write configuration or read audit logs; suspended admin rejected
- support cannot read audit logs, write configuration or list administrators
- admin can read audit logs but cannot invite administrators or change roles (owner only)
- opt-out deletion verified, no raw events remain, post-revocation ingest rejected, repeat revocation safe
- **deletion rebuilds identity-free daily aggregates**
- retention purge rejects a missing cron token and deletes expired raw events

### End-to-end app-event verification

A synthetic staging installation registered consent and submitted `app_open`, `session_start`, `pdf_open`, `pdf_export` and `docx_open`. The Admin API returned real aggregates (`opted_in_installations=1`, `dau=1`, `sessions=1`, feature counts 1 each), and the Admin `/analytics` page rendered those aggregates with no console errors and no overflow at 1280×900 or 390×844.

The synthetic installation was then deleted. The server returned `verified: true`, `deleted_events: 5`, `identity_deleted: true` and `refreshed_aggregate_days: 1`. All analytics tables were empty afterwards.

### Deletion defect found and fixed

The original `analytics_revoke_installation` deleted the identity, raw events and per-install daily rows but left the day's identity-free aggregate counters untouched. A deleted installation's activity therefore survived in dashboard counters. Migration `20261127120000_hmx_analytics_deletion_aggregates.sql`:

- captures every UTC day the installation touched,
- withdraws its `new_installations` credit,
- rebuilds each affected day with `analytics_refresh_day`,
- removes aggregate rows that become empty,
- still verifies that no identity, event or daily activity rows remain.

The migration was applied to staging and recorded in `supabase_migrations.schema_migrations`. Re-testing confirmed the aggregate returns exactly to its pre-test baseline after deletion.

No synthetic events, installations, activity days or consent-log rows remain in staging. No dashboard metric is hardcoded; the empty state explicitly says no opted-in data exists.

---

## 5. Task 5 — Admin security boundaries

| Control | Evidence |
|---|---|
| Android contains no Admin Panel URL, login URL, admin route, management credential or server key | `grep` over `app/src` found no `admin-api`, `hmxpdf-admin`, `vercel.app`, production ref, `service_role`, `sb_secret_`, `SUPABASE_SERVICE` or `/admin/`. Only the staging function base URL and publishable key exist, in the `staging` build type. |
| Admin authorization | Every `admin-api` request calls `requireAdmin`; write routes additionally require `support`, `admin` or `owner`. The role is read server-side from `hmx.admin_members`; JWT claims are never trusted. |
| Suspended administrators | `requireAdmin` rejects `status != 'active'`; the suite verifies a suspended admin receives `403`. |
| RLS | Every `hmx` table has `relrowsecurity` and `relforcerowsecurity` true. `anon` and `authenticated` have no table `SELECT` grants; service-role access is required. |
| Function safety | Security-definer functions set `search_path = hmx, pg_temp`; authorization helpers are not executable by anonymous users. |
| Audit logging | Report review, configuration, member and status changes write to `hmx.admin_audit_log`; the table rejects `UPDATE` and `DELETE`. |
| Role boundaries | Admin Vitest suite asserts owner > admin > support > viewer, support cannot manage configuration or administrators, and only owner can invite/change roles. The staging suite verifies the same boundaries over HTTP. |
| Hidden routes are not a control | The Admin Panel is protected by Supabase Auth + membership + role checks + RLS, not by route secrecy. |

A staging-only owner account was provisioned for the owner's real email so the staging panel can be inspected. Its one-time password is in a local `0600` file (`/tmp/opencode/hmx-staging-owner-credentials.txt`), never printed or committed. The owner should change it after first sign-in. The staging account is separate from production.

---

## 6. Task 6 — Fresh-install and returning-user journey

Static and JVM verification:

1. Fresh install → 7-page onboarding → Home.
2. On Home, `PrivacyPromptPolicy.shouldShow` is true only when backend enabled, onboarding complete, prompt not completed, route is `home`, no document window and no recovery prompt.
3. Dialog appears with both switches off.
4. **Not now** stores prompt completion only; diagnostics and analytics stay off.
5. **Continue** with both off also stores completion only.
6. **Continue** with one or both on applies only those changes.
7. Relaunch: prompt does not reappear; Settings still shows both switches and can change them independently.
8. Returning user with stored consent sees the stored values if the prompt was interrupted mid-session.

A physical-device walkthrough is still required because this environment has no emulator and the staging APK is debug-signed.

---

## 7. Task 7 — Source-first security/privacy review

Reviewed all changed files:

- no document content, file names, paths, URIs, OCR text or contact data added to analytics or diagnostics;
- analytics remains a dedicated `hmxa-<uuid>` identity with its own `hmx_analytics` preferences;
- diagnostics remains a separate `hmx-<uuid>` identity with explicit per-report confirmation;
- the privacy prompt stores no identifier, only a completion flag and timestamp;
- deletion now removes raw events, identity, daily activity **and** aggregate contribution;
- no service-role key, management token or admin credential exists in Android source or the staging APK;
- Admin changes add no secret literals; CI scans for committed credentials.

---

## 8. Task 8 — Tests added/updated

| Area | Tests |
|---|---|
| Android privacy policy | 12 JVM tests (`PrivacyPromptPolicyTest`) |
| Admin report filters | 3 Vitest tests for the `all` sentinel and API parameter mapping |
| Admin role boundaries | 5 Vitest tests for owner/admin/support/viewer and owner-only actions |
| Admin regression suite | 34/34 Vitest tests, typecheck clean, production build succeeds |
| Backend staging suite | 52/52 checks, including aggregate rebuild after deletion and support/admin role boundaries |

---

## 9. Task 9 — Cloud CI and artifact

Android `.github/workflows/android.yml`:

- JVM unit tests, lint and debug build remain unchanged.
- The opt-in staging job builds `assemblePlayStaging` and uploads artifact **`hmxpdf-staging-apk`**.
- A new APK inspection step prints the SHA-256, requires the staging endpoint, and fails if the DEX contains the production ref, an Admin Panel URL, `admin_members`, `service_role`, `sb_secret_`, `SUPABASE_SERVICE` or `/admin/`.
- Release signing, release guard and tag-only production path are unchanged.

Admin `.github/workflows/admin-ci.yml` (new):

- `npm ci`, typecheck, 34 Vitest tests, production build;
- source scan for committed server credentials;
- `deno check` for every Edge Function with npm dependencies installed.

| Evidence | Value |
|---|---|
| Android CI run | `PENDING` |
| Android test count | `PENDING` |
| Staging artifact | `PENDING` |
| Staging APK SHA-256 | `PENDING` |
| Admin CI run | [45947054350](https://github.com/tanishka1233/HMXPDF-Admin/actions/runs/45947054350) — success (web + Edge Function typecheck) |

---

## 10. Residual risks and owner actions

1. **Install the staging APK on a spare device** (or uninstall the production app first). The staging APK is debug-signed, and each CI run regenerates the debug certificate.
2. **Use the staging Admin Panel** for staging reports: `https://hmxpdf-admin-staging.vercel.app`. The production panel intentionally reads production data.
3. **Change the staging owner password** after first sign-in; do not reuse the production password.
4. **Supply an approved privacy-policy URL** to enable the optional link; no URL was invented.
5. **Production remains untouched.** Enabling the privacy prompt and backend in release builds, applying the migration to production, and enabling the production retention scheduler all require separate owner approval.
6. **Device-level light/dark and narrow-width screenshots** are still an owner verification step; the code uses theme tokens and fluid layouts, and the Admin UI was checked at both widths.
