# HMXPDF Phase 8.2 — Functional, Security, Privacy & Performance Audit (Android)

**Repo:** `tanishka1233/HMXPDF`, `feature/hmxpdf-backend-integration`. **Source tested @ `2c21937a58d279f37337f8d8ac00e2a9add78ad2`.** **Staging `hwwnksxfrllkrqonngob`.** Production untouched. No `HMXPANEL/HMXPDF`. No local Android/Gradle. No UI redesign. No production. No secrets. **PASS / FAIL / BLOCKED / NOT TESTED.** Nothing unexecuted marked PASS.

**Admin counterpart:** `tanishka1233/HMXPDF-Admin`, `phase7.1-staging-acceptance` @ `ced7344`. 

**Owner physical-device observations** (provided by user): staging APK installed + tested on **OPPO A38 Android 15**; first-launch privacy after seven-page onboarding; etc. **These are owner observations, not tests run in this session.** No physical device control here. **BLOCKED** items remain.

---

## 1. Executive summary

**What works:** Existing Kotlin+Compose+Liquid Glass app; PDF/DOCX source + JVM; 7-page onboarding; Home; `PrivacyConsentDialog`; diagnostics `submit-report`; `x-hmx-submission-id`; staging `hwwnksxfrllkrqonngob`; no service-role/admin in Android; RLS/FORCE; `submit-report` key fix; `admin-api`; `hmx.admin_members`; `hmx.reports`; analytics opt-in + `hmxa-` + deletion; remote published config + last-known-good; **Android CI 440 PASS**; **Admin CI PASS**; staging artifact. 

**Not claimed:** no local Android; no device control; no OPPO runtime re-test in session; no PDF/DOCX device test; no Macrobenchmark/Perfetto; no FAIL search (source + executed checks clean) doesn't imply runtime. 

**Production readiness:** **PARTIAL / NOT PRODUCTION READY.** No production. Stop before production. 

## 2. Exact commits/branches/CI
- Android `feature/hmxpdf-backend-integration` @ `2c21937a58d279f37337f8d8ac00e2a9add78ad2`. no source change Phase 8.2. 
- **`https://github.com/tanishka1233/HMXPDF/actions/runs/46043879081`** @ `2c21937`: `release-guard`+`build`+`staging` **PASS**, `release` skipped. **440 tests,0,42 skipped.** `hmxpdf-staging-apk`. No local. 
- `46035726866` @ `c19d9b0` Phase8.1 `app-play-arm64-v8a-staging.apk` 42708211 `074bf090…`. `com.hmx.pdf`,`2.1.1`,`6`,`CN=Android Debug`. 
- `46036971435`,`46039236010`. 

## 3. Complete button & interaction inventory
**Source scan total 417 occurrences** (`app/src/main/java`, `Button`,`TextButton`,`IconButton`,`LiquidButton`,`LiquidIconButton`,`GlassDialogAction`,`Switch`,`LiquidToggle`,`Checkbox`,`TextField`,`BasicTextField`,`GlassDialogField`,`OutlinedTextField`,`Dropdown`,`Tab`,`Navigation`,`clickable`,`combinedClickable`,`toggleable`,`selectable`,`rememberLauncherForActivityResult`, dialog actions, back, menus, toolbar, search, file pickers). 

**Result classification:** no source-present control missing handler found in scan; **runtime responsiveness, double-tap behavior, loading, success, error, disabled, restart persistence, a11y touch targets are NOT TESTED on device.** `disabled`, `enabled`, `busy`, `submitting`, `Loading`, `Saving…`, `confirm`, two-step, `Save`, `Cancel`, `Retry`, `Undo`, `Back`, `Navigation`, `Role`, `contentDescription` present in many paths; not complete runtime proof. 

### Summary table
| category | expected | actual/source | status |
|---|---|---|---|
| Buttons (`Button`,`TextButton`,`LiquidButton`,`GlassDialogAction`…) | tap→intended; loading/success/error; no duplicate; disabled | onClick/disabled/busy/confirm; no source defect | NOT TESTED runtime |
| Switches/toggles (`Switch`,`LiquidToggle`, staging diagnostics/analytics) | independent state; persist; no silent consent | `hmx.diagnostics_consent`,`hmx_analytics`,`analytics_enabled`,`Role.Switch`,`toggleableState`,`onCheckedChange` | NOT TESTED runtime |
| Checkboxes | toggle; disabled | source uses `Checkbox` where present; inventory below | NOT TESTED |
| Text fields | input; clear; keyboard; focus; validation | `TextField`,`OutlinedTextField`,`BasicTextField`,`GlassDialogField`,`singleLine`,`keyboardOptions`,`imePadding`,`adjustResize` | NOT TESTED runtime |
| Dropdowns/segmented/menus | open; select; no Radix empty-value | `ALL_FILTER`,`DropdownMenu`,`GlassSegmentedControl`,`LiquidGlassDropdown`,`GlassCapsuleMenu`,`ExposedDropdownMenu`, source | NOT TESTED runtime |
| Tabs/navigation/back | route; retain state; back | `Tab`,`LiquidBottomTab`,`NavigationBar`,`BackHandler`,`enableOnBackInvokedCallback`,`GlassBackButton`,`hmxpdf://`,`shortcuts` | NOT TESTED runtime |
| Dialog actions (`GlassDialog`) | confirm/cancel; scrim rules; no duplicate | `GlassDialog`,`GlassDialogAction`,`AlertDialog`,`confirm`,`dismissOnScrimTap`,`BackHandler` | NOT TESTED runtime |
| Toolbar/search | tap; query; clear; paginated | `GlassSearchHeader`,`GlassToolbar`,`Search`,`ALL_FILTER`,`q`,`limit`,`cursor` | NOT TESTED runtime |
| File pickers | launch; cancel; permission; synthetic only | `rememberLauncherForActivityResult`,`ActivityResultContracts.OpenDocument`,`CreateDocument`,`OpenDocumentTree`,`OpenCamera`,`PickVisualMedia`,`camera`; **no real user docs in testing** | NOT TESTED runtime |
| Confirmations | no duplicate irreversible; destructive labelled | `AlertDialog`,`Confirm`,`submitting`,`pendingStatus`,`Review→Confirm`,`Delete`,`Dismiss`,`Rollback`,`Publish`,`review` | NOT TESTED runtime |
| a11y | label; role; target; contrast | `contentDescription`,`Role`,`semantics`,`paneTitle`,`toggleableState`,`onClick`,`stateDescription`, `text` theme tokens | NOT TESTED runtime; no TalkBack |

**Appended complete occurrence inventory (417 rows)** below. 

| screen | kind | package | line | source | actual result | status |
|---|---|---|---|---|---|---|
| AnalyticsSettingsSection | Button | ui/screen | 116 | `Button(` | source-present; **not runtime-executed** | NOT TESTED |
| AnalyticsSettingsSection | Button | ui/screen | 134 | `TextButton(` | source-present; **not runtime-executed** | NOT TESTED |
| AnalyticsSettingsSection | Button | ui/screen | 143 | `TextButton(` | source-present; **not runtime-executed** | NOT TESTED |
| AnalyticsSettingsSection | Button | ui/screen | 150 | `Button(` | source-present; **not runtime-executed** | NOT TESTED |
| AnalyticsSettingsSection | Button | ui/screen | 74 | `TextButton(` | source-present; **not runtime-executed** | NOT TESTED |
| AnalyticsSettingsSection | Switch | ui/screen | 48 | `Switch(` | source-present; **not runtime-executed** | NOT TESTED |
| CompressPdfScreen | FilePicker | ui/screen | 84 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| CompressPdfScreen | LiquidButton | ui/screen | 107 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| CompressPdfScreen | LiquidButton | ui/screen | 180 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| CompressPdfScreen | LiquidButton | ui/screen | 210 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | Clickable | ui/screen | 329 | `.clickable(enabled = index > 0) { viewModel.moveImage(index, index - 1) },` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | Clickable | ui/screen | 340 | `.clickable(enabled = index < state.selectedImageUris.lastIndex) { viewModel.moveImage(index, index + 1) },` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | FilePicker | ui/screen | 103 | `val imagePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | LiquidButton | ui/screen | 209 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | LiquidButton | ui/screen | 249 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | LiquidButton | ui/screen | 264 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | LiquidButton | ui/screen | 378 | `LiquidButton(onClick = { viewModel.onBlankPageCountChanged(state.blankPageCount - 1) }, backdrop = backdrop) {` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | LiquidButton | ui/screen | 382 | `LiquidButton(onClick = { viewModel.onBlankPageCountChanged(state.blankPageCount + 1) }, backdrop = backdrop, tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | LiquidButton | ui/screen | 429 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | LiquidButton | ui/screen | 465 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| CreatePdfScreen | TextField | ui/screen | 400 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DecryptPdfScreen | FilePicker | ui/screen | 111 | `val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| DecryptPdfScreen | FilePicker | ui/screen | 84 | `val saveLauncher = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| DecryptPdfScreen | LiquidButton | ui/screen | 148 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DecryptPdfScreen | LiquidButton | ui/screen | 164 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DecryptPdfScreen | LiquidButton | ui/screen | 188 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DecryptPdfScreen | LiquidButton | ui/screen | 203 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DecryptPdfScreen | TextField | ui/screen | 173 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DiagnosticsSettingsSection | Button | ui/screen | 160 | `Button(` | source-present; **not runtime-executed** | NOT TESTED |
| DiagnosticsSettingsSection | Switch | ui/screen | 78 | `Switch(` | source-present; **not runtime-executed** | NOT TESTED |
| DiagnosticsSettingsSection | TextField | ui/screen | 134 | `OutlinedTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DiagnosticsSettingsSection | TextField | ui/screen | 143 | `OutlinedTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DiagnosticsSettingsSection | TextField | ui/screen | 152 | `OutlinedTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DocsApp | FilePicker | pdf/ui | 173 | `val recoveryLauncher = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| DocsApp | GlassDialogAction | pdf/ui | 631 | `GlassDialogAction(stringResource(R.string.star_prompt_later), onClick = dismissStar)` | source-present; **not runtime-executed** | NOT TESTED |
| DocsApp | GlassDialogAction | pdf/ui | 632 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| DocsApp | GlassDialogAction | pdf/ui | 680 | `GlassDialogAction(stringResource(R.string.restart_later), onClick = { restartForDirection = false })` | source-present; **not runtime-executed** | NOT TESTED |
| DocsApp | GlassDialogAction | pdf/ui | 681 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| DocsApp | GlassDialogAction | pdf/ui | 712 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| DocsApp | GlassDialogAction | pdf/ui | 716 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| DocsApp | GlassDialogAction | pdf/ui | 727 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| DocsBottomTabs | Navigation | ui/components | 36 | `LiquidBottomTab(onClick = { onTabSelected(0) }) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocsBottomTabs | Navigation | ui/components | 41 | `LiquidBottomTab(onClick = { onTabSelected(1) }) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocsBottomTabs | Navigation | ui/components | 46 | `LiquidBottomTab(onClick = { onTabSelected(2) }) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocsBottomTabs | Tab | ui/components | 36 | `LiquidBottomTab(onClick = { onTabSelected(0) }) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocsBottomTabs | Tab | ui/components | 41 | `LiquidBottomTab(onClick = { onTabSelected(1) }) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocsBottomTabs | Tab | ui/components | 46 | `LiquidBottomTab(onClick = { onTabSelected(2) }) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocsNavGraph | FilePicker | ui/navigation | 402 | `val openDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1333 | `Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) { panel = DocPanel.NONE }) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1355 | `.clickable(remember { MutableInteractionSource() }, indication = null) {}` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1563 | `Modifier.size(20.dp).clickable(remember { MutableInteractionSource() }, indication = null) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1570 | `Modifier.size(20.dp).clickable(remember { MutableInteractionSource() }, indication = null) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1620 | `modifier = Modifier.clickable(remember { MutableInteractionSource() }, indication = null) { sel = i }` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1662 | `.clickable(remember { MutableInteractionSource() }, indication = null, enabled = live) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1723 | `.clickable(remember { MutableInteractionSource() }, indication = null, enabled = pid != null) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1740 | `.clickable(remember { MutableInteractionSource() }, indication = null, enabled = sbid != null) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1789 | `.clickable(remember { MutableInteractionSource() }, indication = null, enabled = live) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 1803 | `.clickable(remember { MutableInteractionSource() }, indication = null, enabled = live) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 2513 | `if (env.editMode && !readOnly) Modifier.clickable(remember { MutableInteractionSource() }, indication = null) { env.onReadOnlyTap() } else Modifier` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 2831 | `.clickable(enabled = enabled) { onClick() }` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Clickable | ui/screen | 2863 | `.clickable(enabled = enabled) { onClick() }` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Dropdown | ui/screen | 2375 | `LiquidGlassDropdown(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Dropdown | ui/screen | 2386 | `LiquidGlassDropdown(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | FilePicker | ui/screen | 803 | `val saveAsLauncher = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | GlassDialogAction | ui/screen | 1373 | `GlassDialogAction(stringResource(R.string.save_recovery_discard), onClick = { viewModel.discardPendingCheckpoint() })` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | GlassDialogAction | ui/screen | 1374 | `GlassDialogAction(stringResource(R.string.docx_checkpoint_recover), onClick = { viewModel.recoverPendingCheckpoint() }, primary = true)` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | GlassDialogAction | ui/screen | 1404 | `GlassDialogAction(stringResource(R.string.docx_fit_width), onClick = { zoomTarget = 1f; showGoTo = false })` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | GlassDialogAction | ui/screen | 1405 | `GlassDialogAction(stringResource(R.string.docx_go), onClick = { go() }, primary = true, enabled = pageInput.toIntOrNull() != null)` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | GlassDialogAction | ui/screen | 1453 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | GlassDialogAction | ui/screen | 1516 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | GlassDialogAction | ui/screen | 1522 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 1227 | `LiquidIconButton(onClick = { print() }, backdrop = screenBackdrop.glass, modifier = printInk.modifier.size(44.dp)) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 1230 | `LiquidIconButton(onClick = { openAsPdf() }, backdrop = screenBackdrop.glass, modifier = pdfInk.modifier.size(44.dp)) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 1233 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 1283 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 1304 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 1311 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 924 | `LiquidIconButton(onClick = { if (!meta.saving) save(false) }, backdrop = headerBackdrop) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 929 | `LiquidIconButton(onClick = { panel = DocPanel.COMMENTS }, backdrop = headerBackdrop, modifier = findInk.modifier) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 932 | `LiquidIconButton(onClick = onTogglePaper, backdrop = headerBackdrop, modifier = paperInk.modifier) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 942 | `LiquidIconButton(onClick = { showSearch = true }, backdrop = headerBackdrop, modifier = findInk.modifier) {` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidIconButton | ui/screen | 947 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidToggle | ui/screen | 1537 | `LiquidToggle(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidToggle | ui/screen | 1638 | `LiquidToggle(selected = { cur.landscape }, onSelect = { v ->` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidToggle | ui/screen | 1755 | `LiquidToggle(selected = { DocxPageLayout.titlePg(section.sectPr) }, onSelect = { v ->` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | LiquidToggle | ui/screen | 1764 | `LiquidToggle(selected = { viewModel.evenOddHeaders() }, onSelect = { v ->` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | Navigation | ui/screen | 902 | `GlassBackButton(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | TextField | ui/screen | 1416 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | TextField | ui/screen | 1483 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | TextField | ui/screen | 1690 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | TextField | ui/screen | 2073 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | TextField | ui/screen | 2267 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| DocxEditorScreen | TextField | ui/screen | 2542 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | Clickable | screen/imageeditor | 138 | `.clickable(interaction, null, role = Role.Button) {` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | Clickable | screen/imageeditor | 192 | `.clickable(role = Role.Button, onClick = onClick)` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | Clickable | screen/imageeditor | 339 | `.clickable(role = Role.Button) {` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | Clickable | screen/imageeditor | 531 | `.clickable(role = Role.Button, onClick = onClick),` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | Dropdown | screen/imageeditor | 584 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | LiquidButton | screen/imageeditor | 244 | `LiquidButton(onClick = { vm.triggerCrop() }, backdrop = backdrop, tint = LiquidGlassColors.Blue, modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | LiquidButton | screen/imageeditor | 500 | `LiquidButton(onClick = { vm.applyText() }, backdrop = backdrop, tint = LiquidGlassColors.Blue) {` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | LiquidButton | screen/imageeditor | 565 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | LiquidButton | screen/imageeditor | 594 | `LiquidButton(onClick = { vm.closeMore() }, backdrop = backdrop, surfaceColor = Color.White.copy(0.08f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | Tab | screen/imageeditor | 584 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorPanels | TextField | screen/imageeditor | 491 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | Dropdown | screen/imageeditor | 168 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | Dropdown | screen/imageeditor | 180 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | Dropdown | screen/imageeditor | 207 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | LiquidButton | screen/imageeditor | 55 | `LiquidButton(onClick = onClick, backdrop = backdrop, tint = LiquidGlassColors.Blue, modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | LiquidToggle | screen/imageeditor | 146 | `LiquidToggle(selected = { keep }, onSelect = { keep = it }, backdrop = backdrop)` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | Tab | screen/imageeditor | 168 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | Tab | screen/imageeditor | 180 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | Tab | screen/imageeditor | 207 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| EditorSheets | TextField | screen/imageeditor | 70 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| EncryptPdfScreen | FilePicker | ui/screen | 123 | `val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| EncryptPdfScreen | FilePicker | ui/screen | 86 | `val saveLauncher = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| EncryptPdfScreen | LiquidButton | ui/screen | 165 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| EncryptPdfScreen | LiquidButton | ui/screen | 181 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| EncryptPdfScreen | LiquidButton | ui/screen | 206 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| EncryptPdfScreen | LiquidButton | ui/screen | 234 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| EncryptPdfScreen | TextField | ui/screen | 261 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractPagesScreen | FilePicker | ui/screen | 73 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractPagesScreen | LiquidButton | ui/screen | 129 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractPagesScreen | LiquidButton | ui/screen | 150 | `LiquidButton(onClick = { onViewOutput(outUri) }, backdrop = backdrop, tint = ExtractAccent, modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractPagesScreen | LiquidButton | ui/screen | 90 | `LiquidButton(onClick = { filePicker.launch(arrayOf("application/pdf")) }, backdrop = backdrop, tint = ExtractAccent) {` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractPagesScreen | TextField | ui/screen | 111 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractTextScreen | FilePicker | ui/screen | 76 | `val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractTextScreen | LiquidButton | ui/screen | 131 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractTextScreen | LiquidButton | ui/screen | 141 | `LiquidButton(onClick = { clipboard.setText(AnnotatedString(state.text)) }, backdrop = backdrop) {` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractTextScreen | LiquidButton | ui/screen | 168 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractTextScreen | LiquidButton | ui/screen | 178 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ExtractTextScreen | LiquidIconButton | ui/screen | 145 | `com.hmx.pdf.ui.components.LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| FillFormScreen | FilePicker | ui/screen | 86 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| FillFormScreen | LiquidButton | ui/screen | 103 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| FlattenPdfScreen | FilePicker | ui/screen | 66 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| FlattenPdfScreen | LiquidButton | ui/screen | 100 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| FlattenPdfScreen | LiquidButton | ui/screen | 120 | `LiquidButton(onClick = { onViewOutput(outUri) }, backdrop = backdrop, tint = FlattenAccent, modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| FlattenPdfScreen | LiquidButton | ui/screen | 83 | `LiquidButton(onClick = { filePicker.launch(arrayOf("application/pdf")) }, backdrop = backdrop, tint = FlattenAccent) {` | source-present; **not runtime-executed** | NOT TESTED |
| GlassBead | Clickable | ui/components | 87 | `.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),` | source-present; **not runtime-executed** | NOT TESTED |
| GlassBottomSheet | Clickable | ui/components | 119 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassBottomSheet | Clickable | ui/components | 158 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassCapsuleMenu | Clickable | ui/components | 129 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassCapsuleMenu | Dropdown | ui/components | 65 | `fun GlassCapsuleMenu(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassColorPicker | Clickable | ui/components | 232 | `.clickable(role = Role.Button) {` | source-present; **not runtime-executed** | NOT TESTED |
| GlassColorPicker | Clickable | ui/components | 281 | `.clickable(role = Role.Button) { customOpen = !customOpen }` | source-present; **not runtime-executed** | NOT TESTED |
| GlassColorPicker | TextField | ui/components | 468 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassDialog | Clickable | ui/components | 153 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassDialog | Clickable | ui/components | 198 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassDialog | GlassDialogAction | ui/components | 243 | `fun RowScope.GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassDialog | LiquidButton | ui/components | 261 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassDialog | TextField | ui/components | 312 | `fun GlassDialogField(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassDialog | TextField | ui/components | 344 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassGuideCallout | LiquidButton | ui/components | 98 | `LiquidButton(onClick = onNext, backdrop = backdrop, tint = LiquidGlassColors.Blue) {` | source-present; **not runtime-executed** | NOT TESTED |
| GlassPrimitives | LiquidButton | ui/components | 168 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassPrimitives | LiquidIconButton | ui/components | 64 | `// Natural glass by default, same as every sibling LiquidIconButton (search, edit, save) in a` | source-present; **not runtime-executed** | NOT TESTED |
| GlassPrimitives | LiquidIconButton | ui/components | 80 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassPrimitives | Navigation | ui/components | 256 | `GlassBackButton(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassPrimitives | Navigation | ui/components | 59 | `fun GlassBackButton(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassScreenHeader | Navigation | ui/components | 153 | `GlassBackButton(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassSearchHeader | Clickable | ui/components | 402 | `.clickable(interactionSource = null, indication = null) { onQueryChange("") },` | source-present; **not runtime-executed** | NOT TESTED |
| GlassSearchHeader | LiquidIconButton | ui/components | 251 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassSearchHeader | TextField | ui/components | 382 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassSegmentedControl | Dropdown | ui/components | 72 | `fun GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassSegmentedControl | Tab | ui/components | 72 | `fun GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassTitlePill | LiquidButton | ui/components | 82 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| GlassToolbar | Clickable | ui/components | 153 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| HomeScreen | Clickable | ui/screen | 1183 | `.combinedClickable(` | source-present; **not runtime-executed** | NOT TESTED |
| HomeScreen | Clickable | ui/screen | 930 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| HomeScreen | Clickable | ui/screen | 965 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| HomeScreen | Dropdown | ui/screen | 949 | `GlassCapsuleMenu(` | source-present; **not runtime-executed** | NOT TESTED |
| HomeScreen | GlassDialogAction | ui/screen | 629 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| HomeScreen | LiquidIconButton | ui/screen | 365 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| HomeScreen | LiquidIconButton | ui/screen | 399 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| HtmlToPdfScreen | FilePicker | ui/screen | 77 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| HtmlToPdfScreen | LiquidButton | ui/screen | 122 | `LiquidButton(onClick = { filePicker.launch("text/html") }, backdrop = backdrop, tint = HtmlAccent, modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| HtmlToPdfScreen | LiquidButton | ui/screen | 147 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| HtmlToPdfScreen | LiquidButton | ui/screen | 166 | `LiquidButton(onClick = { onViewOutput(outUri) }, backdrop = backdrop, tint = HtmlAccent, modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| HtmlToPdfScreen | LiquidButton | ui/screen | 185 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| HtmlToPdfScreen | TextField | ui/screen | 107 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| HtmlToPdfScreen | TextField | ui/screen | 129 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| ImageEditorScreen | FilePicker | ui/screen | 150 | `val pickWatermarkImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| ImageEditorScreen | LiquidButton | ui/screen | 461 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ImageEditorScreen | LiquidIconButton | ui/screen | 223 | `LiquidIconButton(onClick = { sheet = MoreTool.Export }, backdrop = glass, tint = LiquidGlassColors.Blue) {` | source-present; **not runtime-executed** | NOT TESTED |
| ImageEditorScreen | LiquidIconButton | ui/screen | 241 | `LiquidIconButton(onClick = {}, backdrop = glass, isInteractive = false, surfaceColor = Color.White.copy(0.10f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| ImageEditorScreen | LiquidIconButton | ui/screen | 428 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ImageToolsScreen | FilePicker | ui/screen | 71 | `val imagePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| ImageToolsScreen | LiquidButton | ui/screen | 144 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ImageToolsScreen | LiquidButton | ui/screen | 165 | `LiquidButton(onClick = { viewModel.saveToGallery(context) }, backdrop = backdrop, tint = ImageAccent, modifier = Modifier.weight(1f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| ImageToolsScreen | LiquidButton | ui/screen | 171 | `LiquidButton(onClick = { viewModel.share(context) }, backdrop = backdrop, tint = ImageAccent, modifier = Modifier.weight(1f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| ImageToolsScreen | LiquidButton | ui/screen | 200 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ImageToolsScreen | LiquidButton | ui/screen | 88 | `LiquidButton(onClick = { imagePicker.launch("image/*") }, backdrop = backdrop, tint = ImageAccent) {` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | Clickable | ui/screen | 505 | `.clickable(onClick = onClick),` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | Clickable | ui/screen | 522 | `.clickable(onClick = onClick)` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | Clickable | ui/screen | 542 | `.clickable(onClick = onClick)` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | FilePicker | ui/screen | 126 | `val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | LiquidButton | ui/screen | 178 | `LiquidButton(onClick = { picker.launch("image/*") }, backdrop = backdrop, tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | LiquidButton | ui/screen | 196 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | LiquidButton | ui/screen | 200 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | LiquidButton | ui/screen | 266 | `LiquidButton(onClick = { onViewOutput(uri) }, backdrop = backdrop, tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| ImagesToPdfScreen | LiquidButton | ui/screen | 273 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| InteractiveHighlight | LiquidIconButton | ui/utils | 222 | `* The Liquid Glass press transform shared by LiquidButton and LiquidIconButton (call it from` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidBottomTab | Clickable | ui/components | 32 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidBottomTab | Navigation | ui/components | 23 | `fun RowScope.LiquidBottomTab(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidBottomTab | Tab | ui/components | 23 | `fun RowScope.LiquidBottomTab(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidButton | Clickable | ui/components | 126 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidButton | LiquidButton | ui/components | 67 | `fun LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidGlassCard | LiquidIconButton | ui/components | 95 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidGlassDropdown | Clickable | ui/components | 249 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidGlassDropdown | Dropdown | ui/components | 117 | `fun <T> LiquidGlassDropdown(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidGlassDropdown | LiquidButton | ui/components | 145 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidIconButton | Clickable | ui/components | 109 | `Modifier.combinedClickable(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidIconButton | Clickable | ui/components | 118 | `Modifier.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidIconButton | LiquidIconButton | ui/components | 38 | `fun LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidRowClick | Clickable | ui/components | 37 | `.clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button) {` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidSaveDialog | FilePicker | ui/components | 163 | `val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidSaveDialog | LiquidButton | ui/components | 204 | `LiquidButton(onClick = { folderPicker.launch(null) }, backdrop = backdrop, surfaceColor = folderAccent.copy(0.16f), modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidSaveDialog | LiquidButton | ui/components | 220 | `LiquidButton(onClick = onDismiss, backdrop = backdrop, surfaceColor = field) {` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidSaveDialog | LiquidButton | ui/components | 223 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidSaveDialog | TextField | ui/components | 190 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| LiquidToggle | LiquidToggle | ui/components | 52 | `fun LiquidToggle(` | source-present; **not runtime-executed** | NOT TESTED |
| MergePdfScreen | Clickable | ui/screen | 184 | `.clickable(enabled = index > 0) { viewModel.moveFile(index, index - 1) },` | source-present; **not runtime-executed** | NOT TESTED |
| MergePdfScreen | Clickable | ui/screen | 191 | `.clickable(enabled = index < state.selectedFiles.lastIndex) { viewModel.moveFile(index, index + 1) },` | source-present; **not runtime-executed** | NOT TESTED |
| MergePdfScreen | FilePicker | ui/screen | 89 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| MergePdfScreen | LiquidButton | ui/screen | 151 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| MergePdfScreen | LiquidButton | ui/screen | 212 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| MergePdfScreen | LiquidButton | ui/screen | 250 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| MergePdfScreen | LiquidIconButton | ui/screen | 194 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OfficeEngineHint | LiquidButton | ui/screen | 86 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OfficeEngineHint | LiquidIconButton | ui/screen | 97 | `LiquidIconButton(onClick = onDismiss, backdrop = backdrop) {` | source-present; **not runtime-executed** | NOT TESTED |
| OfficeEngineSettingsSection | FilePicker | ui/screen | 112 | `val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {` | source-present; **not runtime-executed** | NOT TESTED |
| OfficeEngineSettingsSection | LiquidButton | ui/screen | 355 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OfficeEngineSettingsSection | LiquidButton | ui/screen | 367 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OfficeEngineSettingsSection | LiquidToggle | ui/screen | 300 | `LiquidToggle(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingDemos | Dropdown | ui/components | 655 | `GlassCapsuleMenu(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | Dropdown | ui/screen | 465 | `LiquidGlassDropdown(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | FilePicker | ui/screen | 508 | `val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | LiquidButton | ui/screen | 221 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | LiquidButton | ui/screen | 268 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | LiquidButton | ui/screen | 547 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | LiquidButton | ui/screen | 603 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | LiquidButton | ui/screen | 638 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | LiquidButton | ui/screen | 693 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | LiquidIconButton | ui/screen | 714 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| OnboardingScreen | LiquidToggle | ui/screen | 676 | `LiquidToggle(` | source-present; **not runtime-executed** | NOT TESTED |
| PageNumbersScreen | FilePicker | ui/screen | 69 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| PageNumbersScreen | LiquidButton | ui/screen | 133 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PageNumbersScreen | LiquidButton | ui/screen | 153 | `LiquidButton(onClick = { onViewOutput(outUri) }, backdrop = backdrop, tint = PageNumAccent, modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageNumbersScreen | LiquidButton | ui/screen | 172 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PageNumbersScreen | LiquidButton | ui/screen | 86 | `LiquidButton(onClick = { filePicker.launch(arrayOf("application/pdf")) }, backdrop = backdrop, tint = PageNumAccent) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageNumbersScreen | LiquidToggle | ui/screen | 129 | `LiquidToggle(selected = { state.includeTotal }, onSelect = viewModel::onIncludeTotalChange, backdrop = backdrop)` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | FilePicker | ui/screen | 151 | `val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidButton | ui/screen | 199 | `LiquidButton(onClick = { picker.launch(arrayOf("application/pdf")) }, backdrop = backdrop, tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidButton | ui/screen | 240 | `LiquidButton(onClick = { viewModel.rotateSelected() }, backdrop = backdrop) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidButton | ui/screen | 243 | `LiquidButton(onClick = { viewModel.deleteSelected(context) }, backdrop = backdrop, tint = Color(0xFFEF5350)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidButton | ui/screen | 246 | `LiquidButton(onClick = { viewModel.clearSelection() }, backdrop = backdrop) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidButton | ui/screen | 250 | `LiquidButton(onClick = { viewModel.selectAll() }, backdrop = backdrop) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidButton | ui/screen | 415 | `LiquidButton(onClick = { onViewOutput(uri) }, backdrop = backdrop, tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidButton | ui/screen | 422 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidIconButton | ui/screen | 348 | `LiquidIconButton(onClick = { viewModel.rotatePage(index) }, backdrop = backdrop, surfaceColor = Color.White.copy(0.10f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PageOrganizerScreen | LiquidIconButton | ui/screen | 351 | `LiquidIconButton(onClick = { viewModel.deletePage(index) }, backdrop = backdrop, surfaceColor = Color(0xFFD32F2F).copy(0.18f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfContinuousPage | Clickable | ui/screen | 1278 | `.clickable(interactionSource = interaction, indication = null, onClick = onClick)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfFormFill | LiquidButton | ui/screen | 485 | `LiquidButton(onClick = onDone, backdrop = backdrop, tint = FormTint, modifier = Modifier.padding(end = 2.dp)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfFormFill | LiquidIconButton | ui/screen | 470 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfFormFill | LiquidIconButton | ui/screen | 474 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfFormFill | TextField | ui/screen | 407 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfSearchBar | LiquidIconButton | ui/screen | 110 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfSearchBar | LiquidIconButton | ui/screen | 117 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfSearchBar | LiquidIconButton | ui/screen | 125 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfSelectionToolbar | Clickable | ui/selection | 499 | `.combinedClickable(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfSelectionToolbar | Clickable | ui/selection | 551 | `.combinedClickable(interactionSource = interaction, indication = null, onLongClick = item.onLongClick, onClick = item.onClick)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfSelectionToolbar | Clickable | ui/selection | 582 | `.combinedClickable(interactionSource = interaction, indication = null, onClick = onClick),` | source-present; **not runtime-executed** | NOT TESTED |
| PdfToImagesScreen | FilePicker | ui/screen | 95 | `val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| PdfToImagesScreen | LiquidButton | ui/screen | 134 | `LiquidButton(onClick = { filePicker.launch(arrayOf("application/pdf")) }, backdrop = backdrop, tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfToImagesScreen | LiquidButton | ui/screen | 154 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfToImagesScreen | LiquidButton | ui/screen | 171 | `LiquidButton(onClick = { viewModel.run(context) }, backdrop = backdrop, tint = accent, isInteractive = !state.isProcessing, modifier = Modifier.fillMaxWidth()) ` | source-present; **not runtime-executed** | NOT TESTED |
| PdfToImagesScreen | LiquidButton | ui/screen | 211 | `LiquidButton(onClick = { viewModel.saveToGallery(context) }, backdrop = backdrop, tint = Color(0xFF43A047), modifier = Modifier.weight(1f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfToImagesScreen | LiquidButton | ui/screen | 217 | `LiquidButton(onClick = { viewModel.shareAll(context) }, backdrop = backdrop, surfaceColor = Color.White.copy(0.1f), modifier = Modifier.weight(1f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 387 | `LiquidButton(onClick = onImageDone, backdrop = backdrop, tint = Color(0xFF00C853)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 398 | `LiquidButton(onClick = onReplaceImage, backdrop = backdrop, tint = LiquidGlassColors.Blue) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 412 | `LiquidButton(onClick = onDeleteImage, backdrop = backdrop, tint = Color(0xFFEF5350)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 447 | `LiquidButton(onClick = { onSetStrokeWidth(w) }, backdrop = backdrop, surfaceColor = surf) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 486 | `LiquidButton(onClick = onDismissExportFeedback, backdrop = backdrop, surfaceColor = chip) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 493 | `LiquidButton(onClick = onOpenExportedFile, backdrop = backdrop, tint = LiquidGlassColors.Blue) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 575 | `LiquidButton(onClick = { onSetActiveTool(if (hlOn) PdfEditTool.None else PdfEditTool.Highlight) }, backdrop = backdrop, tint = Color(0xFFF9A825), surfaceColor =` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 581 | `LiquidButton(onClick = onToggleFindBar, backdrop = backdrop, tint = Color(0xFF0277BD), surfaceColor = wDim(showFindBar)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 614 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 621 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 633 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 640 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 647 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 654 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 660 | `LiquidButton(onClick = onShowSignaturePad, backdrop = backdrop, tint = Color(0xFF5E35B1), surfaceColor = toolDim(false)) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 667 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 682 | `LiquidButton(onClick = onUndo, backdrop = backdrop, surfaceColor = chip) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 714 | `LiquidButton(onClick = onResetZoom, backdrop = backdrop, surfaceColor = chip) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 721 | `LiquidButton(onClick = { onZoomStep(0.5f) }, backdrop = backdrop, surfaceColor = chip) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 730 | `LiquidButton(onClick = onShowSaveDialog, backdrop = backdrop, tint = LiquidGlassColors.Blue) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidButton | ui/screen | 793 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 312 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 360 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 368 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 373 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 458 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 691 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 700 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 707 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 759 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerBottomToolbar | LiquidIconButton | ui/screen | 773 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | Clickable | ui/screen | 225 | `.clickable(onClick = onClick)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | Dropdown | ui/screen | 461 | `LiquidGlassDropdown(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 151 | `GlassDialogAction(stringResource(R.string.cancel), onDismiss)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 152 | `GlassDialogAction(stringResource(R.string.viewer_go), { go() }, primary = true)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 271 | `GlassDialogAction(stringResource(R.string.delete), onDelete, destructive = true)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 272 | `GlassDialogAction(stringResource(R.string.cancel), onDismiss)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 273 | `GlassDialogAction(stringResource(R.string.anno_save), { onSave(text, color, fontSize) }, primary = true)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 332 | `if (canRevert) GlassDialogAction(stringResource(R.string.text_edit_revert), onRevert, destructive = true)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 333 | `GlassDialogAction(stringResource(R.string.cancel), onDismiss)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 334 | `GlassDialogAction(stringResource(R.string.viewer_done), { onSave(text) }, primary = true)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 425 | `GlassDialogAction(stringResource(R.string.cancel), onDismiss)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 426 | `if (onPrint != null) GlassDialogAction(stringResource(R.string.docx_print), onPrint)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 427 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 512 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 602 | `GlassDialogAction(stringResource(R.string.delete), onDelete, destructive = true)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | GlassDialogAction | ui/screen | 603 | `GlassDialogAction(stringResource(R.string.viewer_done), onDismiss, primary = true)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | LiquidIconButton | ui/screen | 560 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | TextField | ui/screen | 160 | `GlassDialogField(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | TextField | ui/screen | 276 | `GlassDialogField(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | TextField | ui/screen | 344 | `GlassDialogField(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerDialogs | TextField | ui/screen | 496 | `GlassDialogField(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | FilePicker | ui/screen | 2669 | `val saveCopyLauncher = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | FilePicker | ui/screen | 748 | `val pdfPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | FilePicker | ui/screen | 752 | `val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | LiquidButton | ui/screen | 1949 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | LiquidButton | ui/screen | 1966 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | LiquidButton | ui/screen | 905 | `LiquidButton(onClick = { pdfPickerLauncher.launch(arrayOf("*/*")) }, backdrop = backdrop, tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | LiquidButton | ui/screen | 971 | `LiquidButton(onClick = { state.passwordUri?.let { viewModel.openPdf(context, it, passwordText) } }, backdrop = backdrop, tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | LiquidIconButton | ui/screen | 1861 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | LiquidIconButton | ui/screen | 1872 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | Navigation | ui/screen | 1830 | `GlassBackButton(` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | Navigation | ui/screen | 887 | `GlassBackButton(onBack = onBack, backdrop = backdrop, foreground = text)` | source-present; **not runtime-executed** | NOT TESTED |
| PdfViewerScreen | TextField | ui/screen | 956 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| PrivacyConsentDialog | Clickable | ui/components | 154 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| PrivacyConsentDialog | Clickable | ui/components | 231 | `.clickable(interactionSource = interaction, indication = null) { onCheckedChange(!checked) }` | source-present; **not runtime-executed** | NOT TESTED |
| PrivacyConsentDialog | GlassDialogAction | ui/components | 93 | `GlassDialogAction(stringResource(R.string.privacy_prompt_not_now), onClick = onDismiss)` | source-present; **not runtime-executed** | NOT TESTED |
| PrivacyConsentDialog | GlassDialogAction | ui/components | 94 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| PrivacyConsentDialog | LiquidToggle | ui/components | 244 | `LiquidToggle(selected = { checked }, onSelect = onCheckedChange, backdrop = backdrop)` | source-present; **not runtime-executed** | NOT TESTED |
| RecentContextMenu | Clickable | ui/screen | 252 | `.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}` | source-present; **not runtime-executed** | NOT TESTED |
| RecentContextMenu | Clickable | ui/screen | 343 | `.clickable(interactionSource = interaction, indication = null, role = Role.Button) {` | source-present; **not runtime-executed** | NOT TESTED |
| ScanDocumentScreen | FilePicker | ui/screen | 165 | `val galleryLauncher = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| ScanDocumentScreen | LiquidButton | ui/screen | 262 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ScanDocumentScreen | LiquidButton | ui/screen | 281 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ScanDocumentScreen | LiquidButton | ui/screen | 529 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | Clickable | ui/screen | 1056 | `modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { openExternalLink(context, url) }` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | Clickable | ui/screen | 912 | `.clickable(interactionSource = interaction, indication = null, onClick = onClick)` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | Clickable | ui/screen | 998 | `.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onCheckedChange(!checked) }` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | Dropdown | ui/screen | 405 | `LiquidGlassDropdown(` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | FilePicker | ui/screen | 177 | `val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | FilePicker | ui/screen | 186 | `val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | GlassDialogAction | ui/screen | 652 | `GlassDialogAction(text = stringResource(R.string.office_engine_cancel), onClick = { officeEngineDeleteSize = null })` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | GlassDialogAction | ui/screen | 653 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | LiquidButton | ui/screen | 734 | `LiquidButton(onClick = {}, backdrop = previewBackdrop, surfaceColor = if (isLight) Color(0xFFFAFAFA).copy(0.35f) else Color(0xFF1E1E1E).copy(0.35f)) {` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | LiquidButton | ui/screen | 844 | `LiquidButton(onClick = {}, backdrop = backdrop, tint = LiquidGlassColors.Purple) {` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | LiquidIconButton | ui/screen | 567 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SettingsScreen | LiquidToggle | ui/screen | 1008 | `LiquidToggle(selected = { checked }, onSelect = onCheckedChange, backdrop = backdrop)` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | GlassDialogAction | ui/screen | 637 | `GlassDialogAction(stringResource(R.string.cancel), onClick = { signatureToDelete = null })` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | GlassDialogAction | ui/screen | 638 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | GlassDialogAction | ui/screen | 691 | `GlassDialogAction(stringResource(R.string.cancel), onClick = onDismiss)` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | GlassDialogAction | ui/screen | 692 | `GlassDialogAction(` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | LiquidButton | ui/screen | 549 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | LiquidButton | ui/screen | 562 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | LiquidIconButton | ui/screen | 260 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | LiquidIconButton | ui/screen | 280 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | LiquidIconButton | ui/screen | 323 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SignaturePadDialog | TextField | ui/screen | 718 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| SplitPdfScreen | Clickable | ui/screen | 378 | `.clickable(onClick = onClick)` | source-present; **not runtime-executed** | NOT TESTED |
| SplitPdfScreen | FilePicker | ui/screen | 97 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| SplitPdfScreen | LiquidButton | ui/screen | 162 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SplitPdfScreen | LiquidButton | ui/screen | 187 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SplitPdfScreen | LiquidButton | ui/screen | 200 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SplitPdfScreen | LiquidButton | ui/screen | 317 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SplitPdfScreen | LiquidButton | ui/screen | 352 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Clickable | ui/screen | 796 | `Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) { dropdown = null }` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Clickable | ui/screen | 821 | `Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) { panel = Panel.NONE }) {` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Clickable | ui/screen | 852 | `.clickable(remember { MutableInteractionSource() }, indication = null) {}` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Dropdown | ui/screen | 1123 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Dropdown | ui/screen | 1132 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | FilePicker | ui/screen | 437 | `val saveAsLauncher = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | LiquidIconButton | ui/screen | 1002 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | LiquidIconButton | ui/screen | 1011 | `LiquidIconButton(onClick = onCommit, backdrop = backdrop, modifier = Modifier.size(34.dp), tint = accent) {` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | LiquidIconButton | ui/screen | 537 | `LiquidIconButton(onClick = { if (!state.saving) save(false) }, backdrop = headerBackdrop) {` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | LiquidIconButton | ui/screen | 545 | `LiquidIconButton(onClick = { showSearch = true }, backdrop = headerBackdrop, modifier = findInk.modifier) {` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | LiquidIconButton | ui/screen | 548 | `LiquidIconButton(onClick = onToggleSheet, backdrop = headerBackdrop, modifier = sheetInk.modifier) {` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | LiquidIconButton | ui/screen | 557 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | LiquidIconButton | ui/screen | 749 | `LiquidIconButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | LiquidToggle | ui/screen | 1142 | `LiquidToggle(selected = { wrap }, onSelect = onWrap, backdrop = backdrop)` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Navigation | ui/screen | 1049 | `LiquidBottomTab(onClick = { onSelect(sheetIndex) }) {` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Navigation | ui/screen | 512 | `GlassBackButton(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Tab | ui/screen | 1049 | `LiquidBottomTab(onClick = { onSelect(sheetIndex) }) {` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Tab | ui/screen | 1123 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | Tab | ui/screen | 1132 | `GlassSegmentedControl(` | source-present; **not runtime-executed** | NOT TESTED |
| SpreadsheetViewerScreen | TextField | ui/screen | 982 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| ToolKit | LiquidButton | ui/components | 135 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ToolKit | LiquidButton | ui/components | 175 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| ToolTile | Clickable | ui/components | 150 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| ToolTile | Clickable | ui/components | 93 | `.clickable(` | source-present; **not runtime-executed** | NOT TESTED |
| UnsavedChangesDialog | GlassDialogAction | ui/components | 42 | `GlassDialogAction(stringResource(R.string.unsaved_discard), onClick = onDiscard, destructive = true)` | source-present; **not runtime-executed** | NOT TESTED |
| UnsavedChangesDialog | GlassDialogAction | ui/components | 43 | `GlassDialogAction(stringResource(R.string.cancel), onClick = onCancel)` | source-present; **not runtime-executed** | NOT TESTED |
| UnsavedChangesDialog | GlassDialogAction | ui/components | 44 | `GlassDialogAction(saveLabel, onClick = onSave, primary = true, tint = accent)` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | FilePicker | ui/screen | 78 | `val filePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | FilePicker | ui/screen | 82 | `val imagePicker = rememberLauncherForActivityResult(` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | LiquidButton | ui/screen | 100 | `LiquidButton(onClick = { filePicker.launch(arrayOf("application/pdf")) }, backdrop = backdrop, tint = WatermarkAccent) {` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | LiquidButton | ui/screen | 152 | `LiquidButton(onClick = { imagePicker.launch("image/*") }, backdrop = backdrop, tint = WatermarkAccent, modifier = Modifier.fillMaxWidth()) {` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | LiquidButton | ui/screen | 187 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | LiquidButton | ui/screen | 217 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | LiquidButton | ui/screen | 240 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | LiquidToggle | ui/screen | 182 | `LiquidToggle(selected = { state.diagonal }, onSelect = viewModel::onDiagonalChange, backdrop = backdrop)` | source-present; **not runtime-executed** | NOT TESTED |
| WatermarkPdfScreen | TextField | ui/screen | 136 | `BasicTextField(` | source-present; **not runtime-executed** | NOT TESTED |
| WhatsNewScreen | LiquidButton | ui/screen | 191 | `LiquidButton(` | source-present; **not runtime-executed** | NOT TESTED |

---

## 4. Privacy dialog (Task 4) — **source+JVM PASS; runtime NOT TESTED**

Implementation `PrivacyConsentDialog`,`PrivacyConsentManager`,`PrivacyPromptPolicy`,`PrivacyChoices`,`PrivacyConsentManager`,`DocsApp`. `GlassDialog` `dismissOnScrimTap=false`; `panelShadow`; `LiquidToggle`; no system overlay; `SYSTEM_ALERT_WINDOW` absent. 12 `PrivacyPromptPolicyTest`. 

### Crash reports / diagnostics
- **Default OFF** `hmx_diagnostics` `diagnostics_consent=false`; `BackendConfig.enabled`; nothing sent. 
- Enable/disable independent; `Continue` applies only changed; **Continue OFF** no consent; **Not now** no consent; `markPromptCompleted` separate; `hmx_privacy_prompt`. once per install; no prompt if not backend. 
- **Explicit user confirmation** `Review report`→`Confirm and send`; `busy`; no silent; consent toggles don't submit. 
- `Not now`/back/dismiss unchanged; no forced. Reopen Settings. `PrivacyChoiceCard` no unwanted selection; no ripple; `indication=null`; `liquidPressGlow`; no `SelectionContainer`; a11y `Role.Switch`. 
- **Runtime restart/rotation/recreate, light/dark/widths, text-selection overlay NOT TESTED in session; owner OPPO observation + Phase8.1 Playwright.** 

### Anonymous product insights
- **Default OFF** `hmx_analytics`; `analytics_enabled=false`; no id/event before consent; `hmxa-<uuid>` only after; `hmx-` diagnostics separate. 
- `Continue` independent; `Analytics.enable`/`disable`; `AnalyticsEngine.revoke`. 
- events only after consent + registered; **opt-out stops future**; clears queue; `setRegistered=false`; `setPendingDeletionId`; **pending events cleared**. 
- **deletion `action=delete` `analytics_revoke_installation` `verified=true` `deleted_events`,`identity_deleted`,`refreshed_aggregate_days`, pending retry; `no success unless verified`**; staging verified. 
- **runtime NOT TESTED.** 

### Dialog actions (`Continue`,`Not now`,`Reopen Settings`,`Navigate away/return`,`rotate`,`light/dark`,`widths`) 
`PASS source + JVM + Phase8.1`; **device runtime NOT TESTED; no black overlay/selection/clipped/hidden source issue; owner PASS observation.** 

## 5. PDF + DOCX functionality (Task 5) — **source PASS; runtime NOT TESTED**

**No real user documents used; synthetic only.** No local Android. No device. 

| area | source evidence | status |
|---|---|---|
| PDF open | `DocumentActivity`,`MainActivity`,`PdfRenderer`,`pdf-core`, content/file VIEW, synthetic generated `PdfRenderInstrumentationTest` | NOT TESTED device |
| Scan | Camera + `POST_NOTIFICATIONS`, ML Kit Document Scanner, `scan`, `scan_images`, `CAMERA`, permission denial handling | NOT TESTED device |
| View/navigate pages | `pdf-core`, `PdfContinuousPage`,`PdfRenderer`,`PdfSelectionOverlay`, page controls | NOT TESTED device |
| Search | PDF text search + `GlassSearchHeader`,`q`,`FORM_FILL`, on-device `SpellEngine` | NOT TESTED device |
| Annotate/edit | `AnnotationEditorDialog`,`PdfAnnotation`,`pdf_edit`,`pdf_export`,`edit`,`save` | NOT TESTED device |
| Save/reopen saved | `save_recovery`,`save_copy_recovery`,`PdfSave`,`SaveLocationManager`,`CreateDocument`, reopen | NOT TESTED device |
| Export/share/rename/delete | `pdf_export`,`pdf_share`,`Share`,`FileProvider`,`shared_exports`,`Rename`,`Delete`, document provider `grantUriPermissions` | NOT TESTED device |
| DOCX open | `docx-open`,`docx-preview`,`office_engine`, OOXML, `office_engine`, `docx_open`, `DocumentActivity` | NOT TESTED device |
| DOCX edit text/format | `DocxEditorScreen`,`docx_edit`,`docx_export`,`word`,`format`, rich text, `Page Setup`, `pdf` preserved, `office_engine` separate `:office` | NOT TESTED device |
| DOCX Page Setup/save/reopen/export | `Page Setup`,`Save`,`Export`,`docx_export`,`Reopen`, OOXML `docx-preview`, `LibreOffice`, `OOXML`, `docx` | NOT TESTED device |
| formatting/integrity | DOCX `word/document.xml`,`styles`,`numbering`,`header/footer`, tables, images, `PDF`, no schema fake; tests `XlsxWriter`,`XlsxRefs`,`Docx`, `Pdf` | NOT TESTED device + JVM |
| File picker cancellation | `rememberLauncherForActivityResult`, `ActivityResultContracts`, `OpenDocument`, `CreateDocument`,`OpenDocumentTree`, null result handling | source PASS; **runtime NOT TESTED** |
| invalid file types | extension/mime + `PdfSecurityService`,`OfficeEngine`,`reportType`, `Invalid`,`Unsupported`,`Could not`, `runCatching`,`catch`; `PdfRenderInstrumentationTest` synthetic valid | source PASS; **invalid runtime NOT TESTED** |
| empty files | `isEmpty`,`isBlank`,`0`,`Pdf`,`DOCX`, `Could not`, `empty`, error handling | source PASS; **runtime NOT TESTED** |
| large files | `MAX`,`maxDocumentMb`,`max_pages`, `Dispatchers.IO`,`withContext`, `OfficeEngine` `:office`, `WorkManager`, foreground, `memory`, `Pdf` | source PASS; **large-file runtime NOT TESTED** |
| password-protected PDFs | `PdfSecurityService`,`PdfPassword`,`DecryptedSession`,`encrypt`,`decrypt`,`password`,`secure`, `PdfSecurityServiceStartupTest`,`DecryptedSessionTest` | source+JVM; **device encrypted doc NOT TESTED** |
| permission denial | `CAMERA`,`READ_EXTERNAL`,`WRITE`,`POST`,`OpenDocument`,`grant`,`denied`,`SecurityException`,`runCatching`,`permission` | source; **runtime NOT TESTED** |
| insufficient storage | `SaveLocation`,`CreateDocument`,`WRITE`,`cache`,`Disk`,`IOException`,`OutOfMemory`,`catch`,`recover`, `insufficient`, `Save` | source; **runtime NOT TESTED** |
| interrupted operations | `save_recovery`,`save_copy_recovery`,`OfficeEngine`,`WorkManager`,`Lifecycle`,`AutoSave`,`recover`,`journal`,`try/finally`, `Dispatchers.IO`, `app background/resume` | source; **runtime NOT TESTED** |
| app background/resume | `MainActivity`,`DocumentActivity`,`Lifecycle`,`onResume`,`savedState`,`configChanges`,`AutoRemove`,`WorkManager`,`OfficeEngine` | source; **runtime NOT TESTED** |

**No test failure; no synthetic doc left; no real user doc overwritten.** 

## 6. Security audit (Task 6) — source-first

**Nothing weakened.** Staging only. No service-role. No production. 

### AndroidManifest / components / permissions
- `com.hmx.pdf`, `minSdk23`,`target36`,`compileSdk36`. `MainActivity` exported launcher + `hmxpdf://`, `app shortcuts`. `DocumentActivity` exported VIEW/SEND `application/pdf`,`docx`,`msword`,`xls`,`xlsx`,`ppt`,`pptx`, `content`,`file`,`BROWSABLE`,`documentLaunchMode=intoExisting`,`autoRemoveFromRecents`. `PDF` `external`. `PROCESS_TEXT`. `OfficeEngineService` `not exported`,`:office`,`FOREGROUND_SERVICE_DATA_SYNC`. `SystemForegroundService` WorkManager. `FileProvider` `not exported`,`grantUriPermissions`,`${applicationId}.provider`, `cache-path scan_images`,`shared`,`external-files`. `no insecure exported components`. 
- `CAMERA`,`READ_EXTERNAL_STORAGE` max32,`WRITE_EXTERNAL_STORAGE` max28,`POST_NOTIFICATIONS`,`FOREGROUND_SERVICE`,`FOREGROUND_SERVICE_DATA_SYNC`,`INTERNET`,`ACCESS_NETWORK_STATE`,`READ/WRITE`. **No `SYSTEM_ALERT_WINDOW`, no location, no contacts, no SMS, no background location, no accessibility, no install packages, no `WRITE_SECURE_SETTINGS`.** FOSS `INTERNET`; merged libs. `allowClearTextTraffic` not set (secure default); no `networkSecurityConfig` file. 
- `allowBackup=true`, `dataExtractionRules`,`fullBackupContent`,`backup_rules`,`data_extraction_rules`, `cloud-backup`,`device-transfer`, excludes `cache/decrypted_pdfs`,`cache/shared`,`save_copy_recovery`,`save_recovery`,`converted_pdfs`,`save_recovery`. **not weakened; runtime backup NOT TESTED.** 
- **WebView** local HTML/DOCX block external; URL-to-PDF intentionally user address; no `addJavascriptInterface`; `WebView.EnableSafeBrowsing=false`; `WebView.MetricsOptOut=true`. `WebView` `PDF` not. 
- `no networkSecurityConfig`; staging HTTPS. 

### Security findings
| severity | finding | evidence | impact | recommendation |
|---|---|---|---|---|
| **Medium** | `android.webkit.WebView.EnableSafeBrowsing` **disabled** `false`; `WebView.MetricsOptOut=true`. | `AndroidManifest.xml`. local external blocked; URL-to-PDF user URL. | less protection for user-entered web pages in URL-to-PDF; no doc upload; pre-existing. | **Not changed in 8.2**; owner/product should approve `EnableSafeBrowsing=true` and/or explicit `networkSecurityConfig`, separate privacy. |
| **Medium** | `DocumentActivity` exported with `scheme=file` VIEW intent (plus `content`,`application/*`,`SEND`). | `AndroidManifest.xml`, `DocumentActivity`, `FileProvider`. `target36`. | another app may `file://`/`content://` a document to view; must only read granted content; no weakened write; **runtime NOT TESTED**. | keep `FileProvider`,`grantUriPermissions`, read-only, `ContentResolver`, `takePersistableUriPermission`, `scheme=content`; consider removing `file` scheme after verifying. **Not changed**. |
| **Low** | `allowBackup=true`. | backup exclusions `backup_rules`,`data_extraction_rules`. `allowBackup`. | residual backup risk outside exclusions; **not tested**. | keep exclusions; verify `dumpsys` backup on device; don't weaken. |
| **Low** | `networkSecurityConfig` absent. | `allowClearTextTraffic` not set; staging HTTPS. | future `http` no explicit policy. | add `@xml/network_security_config` `cleartextTrafficPermitted=false` (not changed). |
| **Informational** | no `networkSecurityConfig` but secure default; `no admin URL`; `no service-role`; `no native-code download` except FOSS engine pinned SHA-256; `no production` staging. | source + APK scan. |  |  |
| **PASS** | `submit-report` `requireApiKey` `PUBLISHABLE_KEY`, constant-time, no `authorization` substitute; `SUPABASE_SERVICE_ROLE_KEY` server. | Phase8.1 `_shared/api-key`. | `garbage`→401 zero rows; valid→201. |  |

### Source scan
`git grep` no `sbp_`,`sb_secret`,`service_role`,`SUPABASE_SERVICE_ROLE_KEY`,`SUPABASE_ANON_KEY` in app; no admin URL/routes/credentials; only staging + `sb_publishable...`. `PUBLISHABLE_KEY`. `SERVICE_ROLE_KEY`. no `Crashlytics`,`Sentry`,`FirebaseCrash`,`ACRA`. `no token logging`. 

**No Critical/High.** 

### Backend security
`submit-report`,`admin-api`,`analytics-consent`,`analytics-ingest`. `requireApiKey` `PUBLISHABLE_KEY`. `requireAdmin` `hmx.admin_members`,`hmx.require_admin`,`require_admin`, `role`,`status`,`suspended`, `viewer`,`support`,`admin`,`owner`. `Supabase Auth.getUser`. no JWT trust. `RLS`+`FORCE RLS` `hmx` `no anon/authenticated`; `security definer search_path=hmx,pg_temp`; `service-role`. `hmx.admin_audit_log` `hmx.log_admin_action`, `report.status_changed`,`config.*`, no edit admin route; service-role DB note. `hmx.submit_rate_limits`,`analytics` rate. `purge-expired-reports`,`purge-analytics` `HMX_CRON_SECRET`. `HMXPDF_PHASE8_1` verified. 

### Logging / temp / cache / storage
No `service-role` logs; `DiagnosticsTelemetry` safe; `Log`; no `printStackTrace`; no doc content. `cache`,`decrypted_pdfs`,`shared`,`scan`,`save_recovery`, `cache` cleared? `DecryptedSession`,`Save`,`Pdf`. `no private user document contents in logs/analytics`; `submit-report` no title/desc/install in logs. `no crash doc`. `app_crash` no message. 

### Dependency vulnerabilities
`npm audit`,`gradle`,`lint`,`deno check`,`OWASP`, `CI`; no local. **Dependency vulnerability run NOT TESTED exhaustive; no fabricated.** 

## 7. Privacy-policy & data-handling comparison (Task 7)

**Actual policy locations:** `PRIVACY.md`, `hmxwebsite/privacy.html`, `docs/release-intelligence/PRIVACY_AND_DATA_FLOW.md`, `docs/HMXPDF_ANALYTICS_PRIVACY_AND_DATA_RETENTION.md`, `docs/fresh-audit/HMXPDF_SECURITY_PRIVACY_AUDIT.md`, in-app `APPROVED_PRIVACY_POLICY_URL=null`, `privacy_prompt`. **No approved public URL; no policy auto-published.** 

### Comparison vs implementation
| claim | implementation | verdict |
|---|---|---|
| offline-capable document app | `Document operations on-device`; PDF/DOCX local; `BackendConfig` only optional; no doc upload | **PASS production**, but `PRIVACY` says no HMXPDF server; **staging build has optional backend + diagnostics + analytics**; production `HMX_BACKEND_ENABLED=false`. |
| no account/no personal info app | app no account; **Admin backend Supabase Auth + `hmx.admin_members` separate; app no account**; analytics pseudonymous not verified human | app PASS; policy needs `no user account` not `no personal info at all`. |
| no analytics SDK | no third-party; `Analytics` first-party `AnalyticsTransport`,`analytics-consent`,`analytics-ingest`,`hmxa`,`hmx_analytics` | no SDK PASS; `no analytics at all` **inaccurate**. |
| no crash-reporting service | `diagnostics` optional `submit-report`,`hmx.reports`,`crash`,`app_crash` `Analytics` `app_crash`,`error_category`,`operation_failed`, no content | **inaccurate**; distinguish no third-party crash service vs optional first-party. |
| no HMXPDF server / no server stores usage history | `hmx.analytics_*`,`hmx.reports`,`hmx.admin_*`,`Supabase`,`hmx-analytics`, `hmx.admin_audit_log`; no doc server | no doc server PASS; `no usage history` **inaccurate when opted-in/staging**. |
| no server-side copy to request deletion | `analytics_revoke_installation`,`hmx.reports`, `hmx.report retention`, `hmx-analytics-purge`, `hmx-analytics` `hmx.admin`; `report_retention_days=30`, `hmx.purge_expired_reports`, `purge-expired-reports`; **no doc server** | no doc deletion server; `no server-side copy` **incomplete**; analytics deletion + report retention. |
| no PDF/DOCX contents/extracted text/OCR sent | `ReportRequestBuilder`, `AnalyticsTaxonomy`, no `content`,`ocr`,`filename`,`path`,`uri`,`device model`,`location`; only `app_version`,`app_version_code`,`android_major`,`locale`,`android_api_level`,`error_code`,`screen` `error_category`, `feature`, `session`, `event` `occurred_at` | **PASS**. no file name/path. `install_id`. |
| no file names/paths/device/location | `Recent` local only; `document name` local; `hmx` no; `no location`; `no device model`; `no advertising ID`; `no hardware serial`; `no Android ID` | **PASS**. `screen` (not doc name), `error_code`, `android_major`. |
| no contacts/clipboard/passwords/tokens | no; `REPORT` no; `Analytics` no; `no email`; `no password`; `hmx.admin` separate; `no contact` | **PASS**. `admin membership` not in app. |
| offline no background `hmx` | `app offline`; no `ReceiveBoot`,`BACKGROUND`,`heartbeat`; **30-min session; no heartbeat**; `RetryLoop`,`WorkManager`,`network`; `Analytics` `track` only active; `no hidden tracking` | `no hidden tracking/background heartbeat` **PASS**; network only features/consent/admin-not-app; `OfficeEngine`,`ML Kit`,`Play`,`URL-to-PDF`,`scanner`,`remote config`,`Supabase` `staging`. |
| `app offline when diagnostics/analytics off` | `BackendConfig`,`Analytics`,`Diagnostics`; off no `hmx`, no silent | **PASS**; optional `get-app-config`,`OfficeEngine`,`URL-to-PDF`,`Play`,`scanner`,`Google`. |
| third-party services | Google Play services/ML Kit, ML Kit OCR/Tesseract, LibreOffice `office_engine`, `docx-preview`, `Supabase`, `WebView`, `URL-to-PDF`, `FOSS` `GitHub`, `no ads`, no analytics provider | policy needs **Supabase + Google + optional engines** list. `WebView.SafeBrowsing` disabled. `Play`. |
| crash handling | `app_crash` `no message`, no stack; `default handler`; no crash service | source `hmx` `app_crash`; `app_crash` if analytics on; diagnostics explicit `crash`; `no automatic diagnostic submission`. |
| `no sharing with advertisers/analytics providers` | no ads; first-party staging; `service-role` server; `no third-party analytics`. `no sharing`. | `no advertisers`; `analytics providers` no. `Supabase` `HMXPDF`. |

### Retention & deletion evidence
- `hmx.system_settings` staging `analytics_event_retention_days=90`,`analytics_aggregate_retention_days=400`,`analytics_consent_version=1`,`report_retention_days=30`. `hmx.analytics_purge`,`hmx.purge_expired_reports`,`purge-expired-reports`,`purge-analytics`. 
- raw `hmx.analytics_events` 90; `hmx.analytics_daily_installations` + `hmx.analytics_daily_aggregates` + `hmx.analytics_consent_log` 400; `analytics_installations` until opt-out; `hmx.reports` 30; `hmx.admin_audit_log`; `hmx.app_config_versions`. 
- **staging `hmx-analytics-purge-daily` `15 3 * * *` active** `purge-analytics` + vault secret; **production scheduler absent**; `purge-expired-reports`; `hmx.admin` `hmx`. `no scheduler enabled` doc inaccurate for staging. 
- analytics **no raw event browser**; `admin_list` `hmx.admin_analytics`; `hmx`. `admin-api`. 
- `hmx.analytics_revoke_installation`, `analytics_consent_log`, `delete`,`revoke`,`verified`,`deleted_events`,`identity_deleted`,`refreshed_aggregate_days`,`new_installations`, `aggregate rebuild`, `hmx`. **uninstall without opt-out no callback** noted. 
- **no production changes.** 

### Proposed policy corrections (not published; owner/legal approval)
1. Replace `PRIVACY.md` + `hmxwebsite/privacy.html` `no analytics SDK`,`no crash-reporting`,`no server usage history`,`no server-side copy` with: no third-party analytics/crash SDK; **production no HMXPDF backend by default; staging and any production `BackendConfig` optional first-party `hmx.analytics`,`hmx.reports`,`hmx.admin`; no documents; no hidden tracking; analytics OFF + diagnostics OFF + explicit; `hmxa`; `hmx`; retention/deletion**. 
2. Add **Supabase** `hmx`, `hmx.reports`, `hmx.analytics`, `hmx.admin`, optional **Google Play/ML Kit**, **LibreOffice** `office_engine`, `docx-preview`, `Supabase Auth`, `URL-to-PDF`, `OfficeEngine`, `PDF` provider. `WebView.SafeBrowsing` disabled note. 
3. Clarify `not analytics SDK` means no third-party; `not crash` optional first-party diagnostics; `not cloud sync`; `no accounts` app. 
4. `retention 90/400/30`; `hmx.analytics-purge-daily` staging; production no scheduler; `hmx.analytics` production no. `report_retention_days=30`. 
5. `no doc content`; `no filename/path`; `no location`; `no device model`; `no Android ID`; `no email`; `no account`; `no contacts`. 
6. `hmxwebsite` `no scheduler` outdated; `no server-side copy` `no server stores usage` outdated. 
7. `no claim `no personal info`; `not verified human`; `installation id`. 

### Privacy classification
Privacy source **PASS**; policy accuracy **MEDIUM**; proposed corrections **not auto-published**. 

## 8. Performance & smoothness (Task 8) — **no measured numbers**

**No Android device/emulator; no local; no Macrobenchmark/Perfetto/FrameTimeline/simpleperf; no cloud device infra; no OPPO re-test.** **Do not invent.** 

| metric | measured | status |
|---|---|---|
| cold startup | — | BLOCKED / NOT TESTED |
| warm startup | — | NOT TESTED |
| time to first usable screen | — | NOT TESTED |
| transition latency | — | NOT TESTED |
| scroll smoothness / frame drops / jank | — | NOT TESTED |
| memory/CPU | — | NOT TESTED |
| background activity | source no hidden heartbeat; WorkManager/OfficeEngine; no numbers | source PASS; **runtime NOT TESTED** |
| network latency | no measure; `RetryLoop`,`connectTimeout=8000`,`readTimeout=10000`,`hmx`,`RetryPolicy`,`WorkManager`,`hmx.admin` | NOT TESTED |
| PDF rendering time | `PdfRenderInstrumentationTest` source valid synthetic; no timing | **PDF device test NOT TESTED**; JVM `Pdf...` PASS |
| DOCX open/save time | `office_engine`,`OOXML`,`LibreOffice`; no timing | NOT TESTED |
| large-file handling | `maxDocumentMb`,`max_pages`,`Dispatchers.IO`,`:office`,`WorkManager`,`memory`,`catch`,`save_recovery`; no measure | source PASS; **large device NOT TESTED** |
| battery | no measure; no background heartbeat; `hmx` `30-min session`; `WorkManager`,`OfficeEngine`,`Google` | NOT TESTED |
| responsiveness background/resume | `Lifecycle`,`WorkManager`,`savedState`,`save_recovery`,`Office`,`AutoSave`; no measure | source; **NOT TESTED** |

**OPPO A38 steps are in remaining manual list.** `androidTest` no perf. `Macrobenchmark` not added. **No performance claim smooth-enough-for-release.** 

### Smoothness source considerations
`hardwareAccelerated=true`, `Dispatchers.IO`, `kotlinx.coroutines`, `withContext`, `LaunchedEffect`, `animateFloat`, `GlassMotion`, `ReducedGlassMotion`, `MotionSettings`, `LiquidGlass`, `no heartbeat`, `configChanges`, `savedState`, `OfficeEngine` `:office`, `WorkManager`, `Pdf`, `Docx`. `glass` blur/lens `Performance` `GlassStyle.Performance`, `LocalReducedGlassMotion`. `not measured`. 

## 9. Admin/backend integration (Task 9) — staging PASS + BLOCKED

`submit-report`,`hmx.reports`,`admin-api`,`hmx.admin_list_reports`,`hmx.admin_get_report`,`hmx.review_report`,`hmx.admin_members`,`hmx.require_admin`,`hmx.admin_audit_log`,`hmx.analytics`,`hmx.admin_analytics`. `hmxpdf-admin-staging`. `hwwnksxfrllkrqonngob`. no production. 

- Android diagnostic `submit-report` staging + `x-hmx-submission-id` + `client_submission_id`; no duplicate. **Real-device owner evidence + Phase8 synthetic + Phase8.1 invalid key.** **New Phase8.2 actual-Android synthetic BLOCKED no device.** 
- `GET /admin/diagnostics`,`/admin/reports`,`/admin/reports/:id`; report detail; filters; pagination; `review`. `PATCH`. `audit`. `hmx`. `hmx.reports`. `hmx`. `hmx`. 
- analytics only after `register`; `analytics-ingest`; `hmx.analytics`; `admin/analytics`; no raw; `delete` verified; dashboard aggregate accuracy `hmx.analytics_dashboard` `admin`; no hardcoded; no doc; no fake. `hmx.analytics` `new_installations`,`DAU`,`WAU`,`MAU`,`sessions`,`features`,`crash`,`distribution`,`retention`. 
- unauthorized `401/403`; `viewer`,`support`,`admin`,`owner`; `suspended`; RLS; `service-role` not browser. 
- remote `get-app-config`,`hmx`. 
- no production. 

**PASS staging + Admin CI; device new synthetic BLOCKED; no unexecuted PASS.** 

## 10. Automated testing & cloud builds (Task 10)
- Android `testPlayDebugUnitTest` `testPlayStaging`? `staging` compile; `assemblePlayStaging`; `hmxpdf-staging-apk`. `46043879081`. **440,0,42.** 
- `androidTest` `AppLaunchSmokeTest`,`PdfRenderInstrumentationTest`; **not run connected; no infra/device**. 
- lint; `release-guard`; `staging` inspection; `deno check`; `deno test`; `vitest`; `vite build`; `tsc`; no local. no signing/package/version. 

## 11. Fixed defects
**No new defects verified in Phase 8.2.** Previous verified fixes retained: `submit-report` `requireApiKey` key equality; `admin-api` invalid report UUID 400; `config_*`→400; `vite`; `deno test`; mobile. **No unrelated refactor; no removed functionality; no test bypass; no production.** 

## 12. Remaining blockers
**BLOCKED / NOT TESTED / BLOCKED**; no unexecuted PASS. 

- No Android device/emulator; no OPPO A38 runtime; no connected instrumentation; no Macrobenchmark/Perfetto/FrameTimeline; no TalkBack; no light/dark screenshots; no runtime double-tap/disabled/restart/rotation; no PDF/DOCX large/password/permission/storage/interrupted/format-integrity; no `continued...`. 
- No production credentials/infra; no SMTP; no production. No `HMXPANEL`. 
- `WebView Safe Browsing disabled`, exported `file`, backup, network config are **review findings**; not weakened; remediation requires separate approval. 
- Privacy policy inaccurate `no analytics/crash/no server usage`; proposed corrections not published. no `APPROVED_PRIVACY_POLICY_URL`. 
- `service_role` server-only; note `service_role` DB grants not exposed. 
- No full dependency scan. no `FAIL` newly fixed. 

## 13. Exact manual tests owner must perform (OPPO A38 Android15; staging only)
Use synthetic docs; don't use production. `https://hmxpdf-admin-staging.vercel.app`. No local build. `gh run download 46043879081 --repo tanishka1233/HMXPDF --name hmxpdf-staging-apk`. staging signed vs production conflict. 

1. Fresh install; seven-page onboarding; Home; privacy `GlassDialog`; both OFF; `Continue` OFF/ON independent; `Not now`; no silent; reopen Settings; navigate return; rotate; light/dark; 320/360/390/412; no grey/black rectangle/clipping/hidden buttons/touch intercept; TalkBack. 
2. Diagnostics staging `bug` `HMXPDF_PHASE8_2_MANUAL_<id>` Review→Confirm; app success; no `apikey`; `hmx.reports`; admin `/diagnostics` `/admin/reports`; `Refresh`; `new`; detail; `triaged`,`resolved`,`dismissed`,`new→...`,invalid; `note`; `reviewed_by`; `hmx.admin_audit_log`; no edit audit; no duplicate; no production. 
3. Analytics OFF→ON→OFF; pending clear; `hmxa` separate; event; `admin/analytics`; `deletion` `verified`; no doc; no hidden. 
4. **PDF synthetic** open/scan/view/navigate/search/annotate/edit/save/reopen/export/share/rename/delete; invalid/empty/large/password; picker cancel; permissions; storage; interrupt; background/resume. 
5. **DOCX synthetic** open/edit/format/Page Setup/save/reopen/export; formatting/integrity; invalid/empty/large/permission/interrupted. 
6. **Performance** cold/warm/TTID/transitions/scroll/frame drops/memory/CPU/background/network/PDF/DOCX/large/battery; rapid nav; opening/closing docs; long PDF; light/dark; low-mem safe; freezes/ANR/crash; no invented. 
7. **Security/privacy** no exported; no `file`; no backup excluded; no service-role; no admin; no doc logs; no clear text; `WebView` local; `URL-to-PDF` user address; `SafeBrowsing` finding. 

## Production readiness verdict
**PARTIAL / NOT PRODUCTION READY.** No production. **Smoothness not established; security/privacy findings + blocked runtime + policy corrections.** Stop before production; owner approval.
