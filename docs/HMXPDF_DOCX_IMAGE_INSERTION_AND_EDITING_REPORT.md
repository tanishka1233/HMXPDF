# HMXPDF — DOCX Image Insertion & Editing Report

**Repo:** `tanishka1233/HMXPDF`, `feature/hmxpdf-backend-integration`. **Source commit tested:** `b8c363357b4db8070c997816455514648df877e3`. **Package `com.hmx.pdf`, version `2.1.1`, `versionCode=6`, signing unchanged, no production.** **DOCX only. PDF editor untouched.** No local Android/Gradle. No production. No secrets.

**CI:** `https://github.com/tanishka1233/HMXPDF/actions/runs/46128233814` `release-guard`+`build`+`staging` **PASS**, `release` skipped. **452 tests, 0 failures, 42 skipped.** `hmxpdf-staging-apk`. (prior `46126399054`.) 

**Staging artifact** generated cloud. No Microsoft Word verification claimed. 

---

## 1. Architecture audit (before implementation)

- **DOCX editor entry:** `app/src/main/java/com/hmx/pdf/ui/screen/DocxEditorScreen.kt`, `DocxEditorViewModel.kt`; `DocxEditorScreen(...)`; `DocumentActivity`; `MainActivity`. `com.hmx.pdf.ui.screen.DocxEditorScreen`. 
- **Compose/toolbar:** `DocxEditorScreen`, `GlassToolButton`, `GlassDialog`, `GlassDialogAction`, `GlassToolbar`, `DocPanel`, `DocxLook`, `Liquid*`. Existing `text formatting toolbar` (`FormatBold`,`FormatItalic`,`FormatUnderlined`,`FormatStrikethrough`,`BorderColor`,`Comments`,`Revisions`,`Layout`,`Table`,`Notes`,`Spell`,`Read`,`Undo`,`Redo`,`Align`,`Bullets`,`Numbering`,`Superscript`,`FormatClear`,`SaveAs`). 
- **Rendering/editing:** `DocxReader`, `DocxWriter`, `DocxModel`, `DocxObjects`, `DocxXml`, `XmlScan`, `DocPara`, `CharFmt`, `OBJ_CHAR`, `DocxEdits`, `DocxTables`, `DocxRevisions`, `DocxComments`, `DocxNotes`, `DocxProps`, `DocxPageLayout`, `DocxStyles`, `DocxTheme`, `DocxNumbering`. `DocxWebRenderer` `docx-preview`. `DocxPdfWriter`. `DocPicture` `DocImages`. 
- **Load/save:** `DocxReader.read`; `DocxPackage`; `DocState`; `DocxEditorViewModel`; `saveCapture`; `DocxWriter.write`; `source`; `buildPatched`; `writeWithRecovery`; `recordSaved`; `extras`; `DocxWriter.Extras`. `Dispatchers.IO`; temp cache; backups; checkpoints. `SaveAs`. 
- **Image-related existing:** `DocxObjects`, `DocPicture`, `DocImages`, `ObjInfo`, `wp:inline`,`wp:anchor`,`a:blip`,`v:imagedata`,`wp:extent`,`positionH`,`hAlign`, `DocxReader.media`, `DocxReader.rels`, `DocxReader.image`, `DocxReader.isPicture`, `word/media`, `Content_Types`, `wp:docPr`, `a:xfrm`, `a:srcRect`,`a:ext`,`a:off`,`a:stretch`,`a:fillRect`,`a:prstGeom`, `origin`. `DocxWriter`. 
- **Existing image editing before Phase:** read/render only; no insert/select/edit/replace/delete. 
- **PDF editor protection:** `app/src/main/java/com/hmx/pdf/ui/screen/imageeditor/*`, `Pdf*`, `PdfEditor*`, `PdfWriter`, `PdfAnnotation`, `PdfPageRenderer`, `PdfRenderer`, `pdf-core`. **No PDF file changed.** Only `com.hmx.pdf.docx.*`, `DocxEditorScreen`, `DocxEditorViewModel`. No shared PDF. PDF compile + tests CI. 
- **DocxWriter sole writer:** preserved. No second DOCX package writer. New `DocxImageOps` only mutates object-run strings + `DocxWriter.Extras`; `DocxWriter.write` remains sole zip writer. `DocxPackage.withImageAdded` copy. 

## 2. Feature A — Insert images from device

**Android Photo Picker** `ActivityResultContracts.PickVisualMedia`, `PickVisualMediaRequest`, `PickVisualMedia.ImageOnly`, `rememberLauncherForActivityResult`, existing Compose. Gallery/Photos/providers. No broad `READ_EXTERNAL_STORAGE`. No `READ_MEDIA*`. `content` `Uri`. `ContentResolver.getType`, `openInputStream`, `content` `file`; `persistable` not needed. `cancelled` `null` no-op. `inaccessible/deleted` `null` `catch`. 

**Validation:** `DocxImageLoader`, `SUPPORTED_MIME`, `image/jpeg`,`jpg`,`png`,`gif`,`bmp`,`webp`,`jfif`, `sniff` header, `BitmapFactory.inJustDecodeBounds`, `MAX_IMAGE_BYTES=25MiB`, `MAX_DIMENSION=20000`, `outWidth/outHeight`, `unsupported`, `malformed`. No unnecessary full decode for bounds. `temp` `cache`, `copyTo`, `readBytes`, `finally temp.delete`. 

**Insertion:** `DocxEditorViewModel.insertImageFromUri`, `DocxImageLoader`, `DocxImageOps.buildInlineDrawing`, `wp:inline`, `wp:extent`, `wp:effectExtent`,`wp:docPr`,`wp:cNvGraphicFramePr`,`a:graphic`,`a:graphicData`,`pic`,`a:blip`,`a:stretch`,`a:xfrm`,`a:off`,`a:ext`,`a:prstGeom`,`a:avLst`, unique `rIdHmxImageN`, `drawingId`, `media`, `w:r`, `OBJ_CHAR`, `wp`. 
**Media:** `word/media/hmx-image-N.ext`, `[Content_Types].xml`, `word/_rels/document.xml.rels`, `Content_Types` default, `DocxWriter.Extras.mediaAdd`, `relsAdd`, `contentTypeAdds`, `addRelWithId`, `addDefault`, `addOverride`, `rewrite`. `pkg.withImageAdded`. **Copied into DOCX**; original URI not retained. `word/document.xml`. `hmx.` no. `no remote`. **Preserves** `origin`, `pPr`, `fmts`, `marks`, `revs`, `markers`, `DocRaw`, `DocTable`, `DocxReader`. `DocxWriter`. 
**After reopen** `DocxReader.read` `media`, `rels`, `DocxObjects`, `DocPicture`, `pkg.image`. 

## 3. Feature B — Image selection

`DocxEditorScreen.SelectedDocxImage`, `selectedImage`, `DocxObjects.ObjInfo.obj`, `imageStyle`, `DocxImageOps.style`, `DocPicture.onClick`, `Role.Button`, `onClickLabel`, no text selection interference, `activeId=null`, `DocPanel.IMAGE`, `Image Options`. selection outline `Modifier.border`, `clip`, `rounded`, `accent`. resize handles not separate (not claimed). Existing text editing preserved. no full-screen editor. 

## 4. Feature C — Editing options

All via `DocxEditorViewModel.editImage`, `DocxImageOps`, `commit`, `DocState`, `DocxWriter`. Only target object run; preserve other XML. 

| Feature | Implementation | Persisted | UI |
|---|---|---|---|
| Rotate left/right 90 | `rotateImageLeft/Right`, `DocxImageOps.setRotation`, `a:xfrm rot` | yes `rot` | `Rotate left/right` |
| custom rotation | `setImageRotation`, `DocxImageOps.normalizeDegrees`, `setRotation` 60000ths | yes `a:xfrm rot`; `rotationDegrees`; preview `Modifier.rotate`; not `wp` `rot` for wp.anchor | VM; no custom numeric UI (not claimed) |
| Resize inc/dec + width/height + aspect | `resizeImage`, `setImageExtent`, `DocxImageOps.scale`, `validDimensions`, `clampDimensions`, `wp:extent`,`a:ext`, aspect default | yes; zero/neg rejected; no aspect-disable UI | `Resize +`; `setImageExtent` VM; no numeric fields (not claimed) |
| Crop | `cropImage`, `DocxImageOps.setCrop`, `a:srcRect l/t/r/b`, `a:blipFill`, preview `ContentScale.Crop`; fixed `cropImage(10,10,10,10)` UI | `a:srcRect` persisted; original media preserved; no `a:srcRect` removal/reset? reset restores | `Crop`; no full visual crop editor (not claimed); OOXML-compatible crop |
| Flip horizontal/vertical | `flipImageHorizontal/Vertical`, `setFlip`, `a:xfrm flipH/flipV`, `graphicsLayer scaleX/scaleY` | yes | buttons |
| Alignment left/centre/right | `setImageAlignment`, `DocxImageOps.setHAlignParagraph`, `w:jc`, `wp:align` read; `DocxObjects.hAlign`, paragraph alignment | `w:jc`; `wp:anchor` `wp:align` not rewritten (not claimed) | `FormatAlign*`; no wrapping |
| Text wrapping in-line/square/tight/top+bottom/behind/front | `DocxImageStyle.wrapping`, `wp:anchor`, `wp:inline`, `wp:wrapSquare/Tight/Through/TopAndBottom/None`, `behindDoc`, read | existing preserved; **new wrapping not exposed**; renderer simplified; not claimed | NOT TESTED/NOT SUPPORTED safe limitation |
| Replace image | `replaceImageFromUri`, `DocxImageOps.setEmbedRid`, `a:blip`, `mediaAdd`, `pkg.withImageAdded`, preserve `wp:extent`,`a:xfrm`,`a:srcRect`,`a:ln`,`wp`,`w`,`pPr`, preserve `rid` + `mediaPath`, `Content_Types`, `DocxWriter`. no `setEmbedRid` UI uses `replaceImagePicker` | media replaced at same path on save; no duplicate `rId`; `replace` zip; old shared media not removed; orphan not created | `Replace` |
| Border enable/disable + colour + thickness | `setImageBorder`, `a:ln`,`a:solidFill`,`a:srgbClr`,`a:noFill`,`w`, preview `border`, color | `a:ln`; `setImageBorder` UI only enable black; colour/thickness VM; not claimed UI | `Border`; `setImageBorder` VM |
| Delete | `deleteImage`, remove object run + `OBJ_CHAR`, `fmt`, `replacePara`, preserve surrounding `text`, `w:t`, `w:p`, `w:r`, `DocTable`, `DocRaw`, `w:pPr`, `wp`. `DocxWriter`. no para delete | yes; relationship may remain if shared; no orphan claim | `Delete` |
| Reset | `resetImage`, `SelectedDocxImage.originalObjXml`, `original`, restore original `obj`; preserve pPr `w:jc` not original para `pPr`! **Note:** reset restores image object, not paragraph alignment changes; not claimed full | image object reset; `wp`,`a`,`w`,`wp`. `originalObjXml`. no reset of `w:jc`, `a:srcRect`? original obj. 

**not claimed:** resize handles, custom numeric UI, visual crop editor, wrapping UI, full Reset, reopening removal. `DocxImageOps` `wrapping` read only. 

## 5. OOXML & document integrity

Preserve `[Content_Types].xml`, `word/document.xml`, `word/_rels/document.xml.rels`, `word/media/`, `wp:inline`,`wp:anchor`,`a:blip`,`a:srcRect`,`a:xfrm`,`a:ext`,`a:off`,`a:stretch`,`a:fillRect`,`a:prstGeom`,`a:ln`,`wp:docPr`,`wp:cNvGraphicFramePr`,`pic`,`r:embed`, `w:pPr`,`w:rPr`,`w:jc`,`w:id`. `DocxWriter`. `origin`. `DocxReader`. `wp`. 

Avoid duplicate `rId` `addRelWithId`, `id`, `drawingId`, broken target `relTarget`, `addDefault`, missing `Content_Types`, `word/media`, `XmlScan.escape`, `isImage`, `null` `catch`. `DocxWriter`. `add`. `seen`. `rewrite`. `replace`. `add`. `pkg`. `RLS` not. 

Multiple images: `picIndex`, `nextDrawingId`, `imageIndex`, `editImage`, `DocxImageOps`, `pkg`. shared media: `replace`, `rid`, `old not delete`, `origin`, `a:blip`. anchored/inline: `wp:anchor`,`wp:inline`, `ObjInfo`, `wp`, `set...`, not rebuild `wp:anchor`; unusual `fail` `null`, `origin`, `readOnly`, `DocxWriter`. 

## 6. Save/reopen persistence

`editImage` `DocPara.edited(origin=null)`; `commit`; `saveCapture`; `extras`; `DocxWriter`. `insert`. `mediaAdd`; `relsAdd`. `replace`. 

**JVM tests added:** `DocxImageOpsTest` (dimension, aspect, rotation, crop, flip, border, alignment, `setEmbedRid`, `buildInlineDrawing`, invalid, `setExtent`); `DocxImageInsertionIntegrationTest` (insert→save→reopen media + `rId` + `Content_Types` + `a:blip` + `World` + `Hello` + `media`). 

**CI `46128233814`, 452 tests 0 fail.** 

**Not claimed Microsoft Word / external DOCX readers.** **No device-level save/reopen on OPPO A38.** `DocxWriter` JVM persistence. 

### Required test matrix status
PASS source + JVM + CI for listed; **device NOT TESTED**; no real docs. `insert` `rotate` `resize` `crop` `replace` `delete` `multi` `one vs another` `surrounding` `tables` `headers/footers` `hyperlinks` `formatting` `existing DOCX`. `headers/footers`,`comments`,`bookmarks`,`revisions`,`origin`,`DocxNotes`,`DocxComments`. `CI`. Not claim Word. 

## 7. Feature D/E — toolbar UI

`Insert Image`, `Image Options`, `Rotate`, `Crop`, `Resize`, `Flip`, `Align`, `Replace`, `Border`, `Delete`, `Reset`, `Wrap Text` not exposed. `DocPanel.IMAGE`. `GlassToolButton`, `Icons.Rounded.*`, `Accent`, `LiquidGlass`, existing light/dark, `BorderColor`, no redesign, horizontal scroll, `320`, `narrow`, `Role`, `contentDescription`, `onClickLabel`. `no permanent image toolbar`; `selectedImage`. `DocPanel`. 

`Insert Image` `imagePicker`, `Replace` `replaceImagePicker`, no silent. `Save`. `DocxEditorViewModel`. 

## 8. Security/privacy/reliability

No remote image upload; no gallery scan; no image metadata retention; no analytics; no file names/paths in logs; no doc content logs; `DocxImageLoader` no print; `catch`. `temp.delete`; `cache`; `DocxWriter` `out.delete`; no service-role; no production; no PDF. `DocxImageLoader`. `ContentResolver`. `Content_Types`. `catch`. `malformed`, `oversized`, `corrupted DOCX`. `DocxReader` `null`. `DocxWriter` `catch`. `no excessive`. 

**No new permissions.** `PickVisualMedia`. no `SYSTEM`. no storage. `manifest` unchanged. `PDF` unchanged. 

## 9. Performance

`DocxImageLoader` `inJustDecodeBounds`, `copyTo`, `temp`, `readBytes`, `DocxImages` `LruCache`, `decodeScaled`, `ImageDecoder`, `Dispatchers.IO`, `produceState`, `remember`, `key`, `bucket`, `DocxWriter` `origin`, `commit`, `replacePara`, `mapContainerOf`, `DocxWriter` `Dispatchers.IO` `save`, `DocxImageOps` string patch. `pkg.media`. `DocxPackage.withImageAdded`. 

**No measured benchmarks/no claim.** Not all docs decoded. `DocxReader` existing `MEDIA_BUDGET`. `DocxWriter` `read`. `DocxImages`. `no main-thread`? **Note image-loading `DocxImageLoader.load` in currently invoked from Activity Result callback on main; DOCX save is `Dispatchers.IO`.** `no measured`. `DocxEditorScreen`. `viewModel`. 

## 10. Fixed defects

- `DocxWriter` additive `Extras` + `mediaAdd`,`relsAdd`,`contentTypeAdds`,`addDefault`; no duplicate `rels`; no second writer. 
- `DocxPackage.withImageAdded`; `image` `emptyMedia`. 
- `DocxObjects` `DocxImageStyle` `imageStyle`,`obj`; `DocxImageOps`. 
- `DocxEditorViewModel` image `commit`,`recordSaved`,`extras`,`DocxWriter`. `DocxImageOps`. 
- `DocxEditorScreen` `Insert Image`, `PickVisualMedia`, `DocPicture`, `DocPanel.IMAGE`, `Role`, `border`,`rotate`,`flip`,`crop`,`ContentScale`, no text selection. 
- Icons. 
- Regression `DocxImageOpsTest`,`DocxImageInsertionIntegrationTest`. 

## 11. Known limitations / not supported

- `Wrap Text` not exposed; `wp:wrap...` preserved, no new wrapping; renderer doesn't reflow around floating images; `wp:anchor` `wp:align` read; not claiming `wp:anchor` `wp:wrap...` created. 
- No visual crop editor; `a:srcRect` + `ContentScale.Crop`; no original `a:srcRect` custom UI; crop fixed `10%` UI. 
- No resize handles; no width/height numeric fields in Compose; `setImageExtent` VM. 
- custom rotation VM; no numeric UI; `wp` `rot` `wp:anchor` not rewrite. 
- Reset restores image object not `w:jc`/paragraph `pPr`. 
- Replace `rid` + `mediaPath`; old media not removed; no orphan cleanup claim. 
- No full `wp:anchor` rebuild; no `a:srcRect` `wp`. 
- No `word/media` `EMF/WMF/PICT` new insertion (existing rendered? `DocxReader.isPicture` `DocPicture` `DocImages` existing; `DocxWriter`. not claimed). `docx-preview`. 
- no `Word` `LO` `external`. no `wp`. 
- `DocxEditorScreen`. `DocxEditorViewModel`. 

## 12. PDF regression

No `PDF` changes. `git diff` `app/src/main/java/com/hmx/pdf/ui/screen/imageeditor`, `pdf`, `Pdf`, `PDF`, `pdf-core`. `Docx...`. `Android CI` `testPlayDebugUnitTest`, `testPlayStaging`, `assemblePlayStaging`, `lint`,`release-guard`. `PDF`. `no local`. 

## Remaining manual tests

OPPO A38: insert JPEG/PNG/WEBP; cancel; invalid; huge; select; rotate; resize; crop; flip; align; replace; border; delete; reset; save/reopen; multiple; shared media; anchored; existing tables/headers/footers/hyperlinks; light/dark; 320; `hmxpdf-staging-apk`. `gh run download 46128233814 ...`. no production. `Microsoft Word`. 

No production.
