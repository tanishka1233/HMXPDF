# HMXPDF Android — Opt-in Analytics Implementation Report

**Status:** implemented and verified in staging code/tests; awaiting owner approval for production integration.
**Date:** 2026-11-21
**Branch:** `feature/hmxpdf-backend-integration`
**Verified code commit:** `224c935` (docs-only commits may follow)
**Backend target:** staging `hwwnksxfrllkrqonngob` only. Production `qjhlzsopnwozpsdosbjt` untouched.

## 1. Files changed

| File | Purpose |
|---|---|
| `analytics/AnalyticsModels.kt` | event taxonomy, bounded queue rules, session timeout |
| `analytics/AnalyticsPayload.kt` | exact allowlisted JSON body, ISO UTC timestamps, queue serialization |
| `analytics/AnalyticsStorage.kt` | dedicated `hmx_analytics` prefs + in-memory test storage |
| `analytics/AnalyticsTransport.kt` | HTTP transport (backend publishable key only) + fake transport |
| `analytics/AnalyticsEngine.kt` | consent, collection, batching, verification, sessions, crash recording |
| `analytics/Analytics.kt` | Android entry point; single daemon executor; crash handler chaining |
| `ui/screen/AnalyticsSettingsSection.kt` | explanation, explicit enable confirmation, disable and deletion requests |
| `ui/screen/SettingsScreen.kt` | new "Analytics (staging)" page |
| `MainActivity.kt` | initialize analytics; onStart/onForeground, onStop/onBackground |
| `OpenPdfUseCase.kt`, `PdfViewerViewModel.kt`, `PdfViewerScreen.kt` | pdf_open, pdf_export, pdf_share, operation_failed |
| `MergePdfViewModel.kt`, `SplitPdfViewModel.kt`, `CompressPdfViewModel.kt` | pdf_merge, pdf_split, pdf_compress, operation_failed |
| `DocxEditorViewModel.kt` | docx_open, docx_edit, docx_export |
| `ScanViewModel.kt`, `ExtractTextViewModel.kt`, `HtmlToPdfViewModel.kt` | scan, OCR, URL-to-PDF |
| `SignaturePadDialog.kt` | signature_use |
| `docs/HMXPDF_ANALYTICS_ARCHITECTURE_AUDIT.md` | audit (copied from admin repo) |
| `docs/HMXPDF_ANALYTICS_PRIVACY_AND_DATA_RETENTION.md` | privacy/retention rules (copied) |

No Gradle dependency, package name, signing identity, `versionCode` or `versionName` was changed. No Hilt/Room was introduced (neither exists in this app).

## 2. Architecture decisions

- **Backed by existing Phase 6 client**: `HttpJson`, `RetryLoop`/`RetryPolicy` (capped 3 attempts), `BackendConfig`, `AppInfo`. No third-party SDK.
- **Separate identity and consent**: `hmx_analytics` prefs, `analytics_enabled` default false, `hmxa-<uuid>` generated only after the explicit confirmation press. Diagnostics uses `hmx_diagnostics` / `hmx-<uuid>` and is untouched.
- **Bounded queue**: max 200 events, max 7 days old; batches of 50; no heartbeat, no WorkManager, no background service; one daemon executor thread only when events are recorded.
- **Session rule**: `session_start` on first foreground or after 30 minutes idle, `session_end` on background. No continuous timing events.
- **Fail open**: transport errors never propagate into document code; queue stays local; upload is retried on the next event/flush.
- **Deletion honesty**: opt-out clears consent and queue immediately; a pending flag remains only if server verification fails, and the UI says "pending", not "deleted".

## 3. API integration

- `POST /analytics-consent` — `action=register|revoke|delete`, `install_id`, `consent_version=1`, app metadata. Registration is required server-side before any event is accepted.
- `POST /analytics-ingest` — `{ install_id, consent_version, events[1..50] }`; only allowlisted names/properties; idempotent via `event_id`.
- Publishable key from `BuildConfig.HMX_BACKEND_PUBLISHABLE_KEY`; empty in debug/release. No service-role or secret key anywhere in the app.
- All analytics work is compiled into `main` but gated at runtime by `BackendConfig.enabled` (false in debug/release). The staging endpoint and key exist only in the staging build type's BuildConfig.

## 4. Privacy review

Collected fields and never-collected fields are enumerated in `docs/HMXPDF_ANALYTICS_PRIVACY_AND_DATA_RETENTION.md`. Highlights:

- No PDF/DOCX content, names, paths, URIs, text, OCR output, device IDs, location, contacts, clipboard, credentials or stack traces.
- Crash handler chains the previous default handler and stores only `app_crash` with no message.
- Event ID format is allowlisted; the queue/pruning logic drops stale and duplicate events.
- Consent screen lists all collected data, reasons, opt-out and deletion paths.

## 5. Cloud build results

Final run: **[44928687889](https://github.com/tanishka1233/HMXPDF/actions/runs/44928687889)** — **success**

| Job | Result |
|---|---|
| `build` (unit tests, lint, debug APK) | success |
| `staging` (compile `playStaging` + unsigned APK) | success |
| `release-guard` | success |
| `release` (signed candidate) | skipped |

```
com.hmx.pdf.analytics.AnalyticsEngineTest: tests=13 failures=0 errors=0 skipped=0
com.hmx.pdf.analytics.AnalyticsPayloadTest: tests=8 failures=0 errors=0 skipped=0
TOTAL tests=420 failures=0 errors=0 skipped=42
```

21 new analytics tests cover: consent default OFF, no event before opt-in, disabled builds, dedicated ID, allowlisted payload, batch caps, offline queue, retry/registration failure, opt-out deletion verification, pending deletion, session timeout, queue bounds/age, crash safety, transport exception isolation.

Earlier runs failed on two compile issues and three incorrect test expectations; each was fixed and re-verified. No release job was ever enabled.

### APK binary inspection (CI artifacts, arm64 staging + debug)

| Check | Staging APK | Debug APK |
|---|---|---|
| Staging project ref | present | absent |
| Production project ref | absent | absent |
| Staging publishable key | present | absent |
| `sb_secret_` / `service_role` | absent | absent |
| Analytics route literals | present | present (inert: base URL and key are empty) |
| `hmx_analytics` / `hmxa-` literals | present | present (inert) |

The analytics classes are compiled in all variants but cannot send anything in debug/release because `BackendConfig.enabled` is false and the base URL/key are empty strings. If the owner wants the route literals physically stripped from non-staging APKs, that requires moving the transport into a build-type source set with no-op stubs; it was not done here to avoid changing the existing source-set structure.

## 6. Backend verification (staging, 43/43)

Executed by `/tmp/opencode/hmx-analytics-suite.mjs` against `hwwnksxfrllkrqonngob`; all synthetic identities, events, rate buckets and admin users were deleted afterwards. Highlights: consent registration/idempotency, valid and invalid batches, dedupe, four kinds of rejection, rate limiting, exact aggregate correctness (DAU 2, crashes 1), viewer/suspended-role matrix, verified deletion, post-revoke rejection, retention purge, empty final tables.

## 7. Known limitations

1. No device-level runtime test (no SDK/emulator here; owner device RAM-limited). Cloud compilation + JVM tests + binary inspection are the evidence.
2. `limits` from remote config remain display-only (unchanged from Phase 6).
3. The opt-out retry keeps the local analytics ID (with consent off) until deletion is verified; no event can be attributed in that state.
4. Route literals remain in non-staging DEX as inert constants (see §5).

## 8. Remaining owner decisions

- Approve the 90/400-day retention windows and a daily `purge-analytics` schedule (staging first).
- Approve deploying the admin panel analytics page to a staging Vercel target for browser verification.
- Decide whether to physically strip analytics transport classes from non-staging APKs.
- Approve production migration/function deploy and any release in a separate phase.
