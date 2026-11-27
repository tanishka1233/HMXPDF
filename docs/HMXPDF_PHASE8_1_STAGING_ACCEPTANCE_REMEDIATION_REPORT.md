# HMXPDF Phase 8.1 — Staging Acceptance Remediation Report (Android)

**Repo:** `tanishka1233/HMXPDF`, `feature/hmxpdf-backend-integration`. **Source tested @ `c19d9b074369d478647b1d8452e20f00ef760c1c`.** No Android source change in 8.1. **Staging `hwwnksxfrllkrqonngob`. Production untouched.** No local Android/Gradle. No signing/version/package change. No production release. **No secrets.**

**Admin remediation source @ `96ba182d5d1436dbf52ac7c22acfa2ad6843e418`.** 

## Android CI + artifact (Task 4/8)
**`https://github.com/tanishka1233/HMXPDF/actions/runs/46035726866`** @ `c19d9b0`. `release-guard`+`build`+`staging` **PASS**; `release` skipped. **440 tests,0,42 skipped.** No `deno`/Android local. 
- Artifact **`hmxpdf-staging-apk`**. Download: `gh run download 46035726866 --repo tanishka1233/HMXPDF --name hmxpdf-staging-apk --dir hmxpdf-staging-apk`. Inside: `app-play-arm64-v8a-staging.apk` + armv7a + x86 + x86_64 + universal. 
- `app-play-arm64-v8a-staging.apk` **42,708,211 B**, SHA-256 `074bf090568ec6926edf1477bc36ef72e15a942da5a57e3598bab3f9a2c66c82`. 
- `com.hmx.pdf`,`versionName=2.1.1`,`versionCode=6`,`minSdk=23`,`targetSdk=36`,`compileSdk=36`,platform16. 
- **Signing:** `staging` `initWith(debug)`, debug-signed; Signer `CN=Android Debug`, `O=Android`,`C=US`; cert SHA-256 `9797ff0cce0d12c632d86375f794fea1d3938dcad733e5b5c6014cdc2725a00e`. Not production-signed. No signing identity changed. 
- **Install warning:** package `com.hmx.pdf`. If an existing **production-signed** app is installed, installing this debug/staging APK will conflict (`INSTALL_FAILED_UPDATE_INCOMPATIBLE` / signature mismatch). Use a spare/second Android device OR uninstall the production app first (uninstalling may delete app data; not guaranteed preserved). Don't downgrade. 
- APK scan **no** production ref/admin URL/`admin_members`/`service_role`/`sb_secret_`/`SUPABASE_SERVICE`/`/admin/`; staging endpoint only. 

### Manual OPPO A38 (Android 15) staging steps
1. Use a spare device or no production HMXPDF installed; don't use owner production device. Download artifact `app-play-arm64-v8a-staging.apk`. Verify SHA-256. Enable "install unknown apps" for your browser/file manager. Install. If production-signed app installed, uninstall it first (owner-controlled; backup data). 
2. Fresh install → 7-page onboarding → Home → first-launch `GlassDialog` privacy; both diagnostics+analytics OFF; Continue/Not now; once; production not used. 
3. Settings → Diagnostics (staging). Enable diagnostics. Confirm no PDF/DOCX content. Enter `bug`, title `HMXPDF_PHASE81_MANUAL_<id>`, description. Review → Confirm and send. App says `Report sent. Reference…`. 
4. Open staging admin `https://hmxpdf-admin-staging.vercel.app` **staging owner** (not production `hmxpdf-admin-ruby.vercel.app`), `/diagnostics`, **Refresh**, find title/id; open detail; submitted payload; `new`. Retry submission → no duplicate. Dashboard counts. 
5. Analytics separate OFF→ON/OFF; deletion `verified`. Sign out. 

## Task 1 security regression (fixed actual defect)
See Admin doc. `requireApiKey` now verifies `apikey` equals `PUBLISHABLE_KEY` (`SUPABASE_PUBLISHABLE_KEY`??`SUPABASE_ANON_KEY`) with constant-time compare; missing/invalid/malformed/`authorization`-only→401; no service-role; no logs; invalid-key zero rows. **`api-key.test.ts`** + `deno test`. 

## Task 2 mobile
`PageHeader`,`ReportDetailPage`,`ConfigPage`,`common`. Light+dark **320/360/390/412** `/diagnostics/:id`,`/config` **fit,0 errors,rendered**. 

## Task 3 remote config
19/19 staging. `min_supported=6` (app versionCode6). 

## Task 4
Password reset SMTP **BLOCKED**; device **BLOCKED**; service-role **PASS source + not exposed**. 

## Production readiness
**PARTIAL / NOT PRODUCTION READY.** No production change. Owner approval required.
