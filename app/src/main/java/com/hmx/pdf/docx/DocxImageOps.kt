package com.hmx.pdf.docx

import kotlin.math.roundToInt

/** Style of a DrawingML picture, read from `wp:extent`, `a:xfrm`, `a:srcRect`, `a:ln`, `wp:wrap*`, `wp:anchor`. */
data class DocxImageStyle(
    val widthEmu: Long = 0L,
    val heightEmu: Long = 0L,
    val rotationDegrees: Float = 0f,
    val flipH: Boolean = false,
    val flipV: Boolean = false,
    val cropLeftPct: Float = 0f,
    val cropTopPct: Float = 0f,
    val cropRightPct: Float = 0f,
    val cropBottomPct: Float = 0f,
    val borderEnabled: Boolean = false,
    val borderColor: String = "000000",
    val borderWidthEmu: Long = 0L,
    val hAlign: String? = null,
    val wrapping: String = "inLine",
) {
    val widthPt: Float get() = widthEmu / 12700f
    val heightPt: Float get() = heightEmu / 12700f
}

/**
 * Byte-preserving DOCX image transforms on a single object run (`CharFmt.obj`).
 * Only the targeted element is rewritten; all other XML is left verbatim.
 * Works for `wp:inline`, `wp:anchor`, and unmodeled images safely (no-op + caller falls back).
 */
object DocxImageOps {

    private const val EMU_PER_INCH = 914400L
    private const val EMU_PER_PT = 12700L

    // ── read ──────────────────────────────────────────────────────────────────

    fun style(obj: String): DocxImageStyle {
        val extent = Regex("<wp:extent\\s+([^>]*)>").find(obj)?.groupValues?.get(1).orEmpty()
        val cx = attr(extent, "cx")?.toLongOrNull() ?: attr(obj, "cx")?.toLongOrNull() ?: 0L
        val cy = attr(extent, "cy")?.toLongOrNull() ?: attr(obj, "cy")?.toLongOrNull() ?: 0L
        val xfrm = Regex("<a:xfrm\\s*([^>]*)>(.*?)</a:xfrm>", RegexOption.DOT_MATCHES_ALL).find(obj)?.value.orEmpty()
        val rot = attr(xfrm, "rot")?.toFloatOrNull()?.div(60000f) ?: 0f
        val flipH = attr(xfrm, "flipH").equals("1", true) || attr(xfrm, "flipH").equals("true", true)
        val flipV = attr(xfrm, "flipV").equals("1", true) || attr(xfrm, "flipV").equals("true", true)
        val src = Regex("<a:srcRect\\s+([^>]*)>").find(obj)?.groupValues?.get(1).orEmpty()
        fun pct(key: String) = (attr(src, key)?.toFloatOrNull() ?: 0f) / 1000f
        val ln = Regex("<a:ln\\s+([^>]*)>.*?</a:ln>", RegexOption.DOT_MATCHES_ALL).find(obj)?.value.orEmpty()
        val borderW = attr(ln, "w")?.toLongOrNull() ?: 0L
        val borderColor = Regex("<a:srgbClr val=\"([0-9A-Fa-f]{6})\"").find(ln)?.groupValues?.get(1) ?: "000000"
        val borderEnabled = ln.isNotEmpty() && !ln.contains("<a:noFill") && borderW > 0
        val anchored = obj.contains("<wp:anchor")
        val wrapping = when {
            !anchored -> "inLine"
            obj.contains("<wp:wrapSquare") -> "square"
            obj.contains("<wp:wrapTight") -> "tight"
            obj.contains("<wp:wrapThrough") -> "through"
            obj.contains("<wp:wrapTopAndBottom") -> "topAndBottom"
            obj.contains("<wp:wrapNone") -> if (obj.contains("behindDoc=\"1\"") || obj.contains("behindDoc=\"true\"")) "behindText" else "inFrontOfText"
            else -> "square"
        }
        val hAlign = Regex("<wp:positionH[^>]*>\\s*<wp:align>([^<]+)</wp:align>").find(obj)?.groupValues?.get(1)?.trim()
            ?: Regex("<wp:align>([^<]+)</wp:align>").find(obj)?.groupValues?.get(1)?.trim()
        return DocxImageStyle(
            widthEmu = cx, heightEmu = cy,
            rotationDegrees = rot,
            flipH = flipH, flipV = flipV,
            cropLeftPct = pct("l"), cropTopPct = pct("t"), cropRightPct = pct("r"), cropBottomPct = pct("b"),
            borderEnabled = borderEnabled, borderColor = borderColor, borderWidthEmu = borderW,
            hAlign = hAlign, wrapping = wrapping,
        )
    }

    fun isImage(obj: String): Boolean = obj.contains("<w:drawing") &&
        (obj.contains("<a:blip") || obj.contains("<v:imagedata")) &&
        (obj.contains("embed=\"") || obj.contains("r:id=\"") || obj.contains("id=\""))

    // ── safe helpers ──────────────────────────────────────────────────────────

    fun validDimensions(widthEmu: Long, heightEmu: Long): Boolean = widthEmu in 1L..1_000_000_000_000L && heightEmu in 1L..1_000_000_000_000L

    fun clampDimensions(widthEmu: Long, heightEmu: Long): Pair<Long, Long> {
        val w = widthEmu.coerceIn(1L, 1_000_000_000_000L)
        val h = heightEmu.coerceIn(1L, 1_000_000_000_000L)
        return w to h
    }

    fun scale(style: DocxImageStyle, factor: Float, preserveAspect: Boolean = true): Pair<Long, Long> {
        if (style.widthEmu <= 0 || style.heightEmu <= 0) return style.widthEmu to style.heightEmu
        val f = if (factor.isFinite() && factor > 0f) factor else 1f
        val w = (style.widthEmu * f).roundToInt().toLong().coerceAtLeast(1L)
        val h = if (preserveAspect) (style.heightEmu * f).roundToInt().toLong().coerceAtLeast(1L) else style.heightEmu
        return clampDimensions(w, h)
    }

    fun rotated(style: DocxImageStyle, degrees: Float): DocxImageStyle =
        style.copy(rotationDegrees = normalizeDegrees(style.rotationDegrees + degrees))

    fun normalizeDegrees(d: Float): Float {
        if (!d.isFinite()) return 0f
        var x = d % 360f
        if (x > 180f) x -= 360f
        if (x < -180f) x += 360f
        return x
    }

    // ── OOXML transforms on `obj` ─────────────────────────────────────────────

    fun setExtent(obj: String, widthEmu: Long, heightEmu: Long): String {
        if (!validDimensions(widthEmu, heightEmu)) return obj
        var out = obj
        // wp:extent (wordprocessingDrawing)
        out = out.replace(Regex("(<wp:extent\\s+)cx=\"\\d+\"\\s+cy=\"\\d+\"")) { "${it.groupValues[1]}cx=\"$widthEmu\" cy=\"$heightEmu\"" }
        if (!Regex("<wp:extent\\s+cx=\"$widthEmu\"\\s+cy=\"$heightEmu\"").containsMatchIn(out)) {
            out = out.replace(Regex("<wp:extent\\s+([^>]*?)cx=\"\\d+\"\\s+cy=\"\\d+\"([^>]*)>")) { m ->
                "<wp:extent ${m.groupValues[1]}cx=\"$widthEmu\" cy=\"$heightEmu\"${m.groupValues[2]}>"
            }
        }
        // a:ext inside a:xfrm (drawingml main)
        out = out.replace(Regex("(<a:ext\\s+)cx=\"\\d+\"\\s+cy=\"\\d+\"")) { "${it.groupValues[1]}cx=\"$widthEmu\" cy=\"$heightEmu\"" }
        return out
    }

    fun setRotation(obj: String, degrees: Float): String {
        val rot = (normalizeDegrees(degrees) * 60000f).roundToInt()
        return if (Regex("<a:xfrm\\s[^>]*rot=").containsMatchIn(obj)) {
            obj.replace(Regex("(<a:xfrm\\s[^>]*\\srot=\")[-\\d.]+\"", RegexOption.DOT_MATCHES_ALL)) { "${it.groupValues[1]}$rot\"" }
                .replace(Regex("(<a:xfrm\\s[^>]*)rot=\"[-\\d.]+\"")) { "${it.groupValues[1]}rot=\"$rot\"" }
        } else {
            obj.replace(Regex("<a:xfrm(\\s[^>]*)?>")) { m -> "<a:xfrm${m.groupValues[1].orEmpty()} rot=\"$rot\">" }
        }
    }

    fun setFlip(obj: String, horizontal: Boolean? = null, vertical: Boolean? = null): String {
        var out = obj
        val cur = style(obj)
        val fh = horizontal ?: cur.flipH
        val fv = vertical ?: cur.flipV
        // set/remove flipH / flipV on a:xfrm
        out = out.replace(Regex("(<a:xfrm[^>]*?)\\sflipH=\"[^\"]*\"")) { it.groupValues[1] }
            .replace(Regex("(<a:xfrm[^>]*?)\\sflipV=\"[^\"]*\"")) { it.groupValues[1] }
        out = out.replace(Regex("<a:xfrm(\\s[^>]*)?>")) { m ->
            val attrs = (m.groupValues[1].orEmpty()).trim()
            val add = (if (fh) " flipH=\"1\"" else "") + (if (fv) " flipV=\"1\"" else "")
            "<a:xfrm" + (if (attrs.isEmpty()) "" else " $attrs") + add + ">"
        }
        return out
    }

    fun setCrop(obj: String, leftPct: Float, topPct: Float, rightPct: Float, bottomPct: Float): String {
        fun pct1000(v: Float) = (v.coerceIn(0f, 99f) * 1000f).roundToInt()
        val l = pct1000(leftPct); val t = pct1000(topPct); val r = pct1000(rightPct); val b = pct1000(bottomPct)
        val rect = "<a:srcRect l=\"$l\" t=\"$t\" r=\"$r\" b=\"$b\"/>"
        var out = obj.replace(Regex("<a:srcRect\\s+[^>]*/>"), rect).replace(Regex("<a:srcRect\\s+[^>]*>.*?</a:srcRect>", RegexOption.DOT_MATCHES_ALL), rect)
        if (!out.contains("<a:srcRect")) {
            // insert into blipFill before stretch
            out = out.replace(Regex("(<pic:blipFill[^>]*>\\s*<a:blip[^>]*?(?:/>|>.*?</a:blip>)\\s*)", RegexOption.DOT_MATCHES_ALL)) { it.value + rect + "" }
            if (!out.contains("<a:srcRect")) out = out.replace("</pic:blipFill>", rect + "</pic:blipFill>")
            if (!out.contains("<a:srcRect")) out = out.replace("<a:stretch>", rect + "<a:stretch>")
        }
        return out
    }

    fun setBorder(obj: String, enabled: Boolean, colorHex: String = "000000", widthEmu: Long = 9525L): String {
        val color = colorHex.removePrefix("#").take(6).uppercase().let { if (Regex("[0-9A-F]{6}").matches(it)) it else "000000" }
        val ln = if (enabled) "<a:ln w=\"${widthEmu.coerceAtLeast(1L)}\"><a:solidFill><a:srgbClr val=\"$color\"/></a:solidFill></a:ln>"
        else "<a:ln><a:noFill/></a:ln>"
        var out = obj.replace(Regex("<a:ln\\s+[^>]*>.*?</a:ln>", RegexOption.DOT_MATCHES_ALL), ln)
            .replace(Regex("<a:ln\\s+[^>]*/>"), ln)
        if (!out.contains("<a:ln")) {
            // insert into pic:spPr after xfrm / prstGeom
            out = out.replace(Regex("(<pic:spPr[^>]*>.*?<a:prstGeom[^>]*>.*?</a:prstGeom>)", RegexOption.DOT_MATCHES_ALL)) { it.value + ln }
            if (!out.contains("<a:ln")) out = out.replace("</pic:spPr>", ln + "</pic:spPr>")
        }
        return out
    }

    fun setHAlignParagraph(pPr: String, align: String): String {
        val jc = when (align.lowercase()) { "center", "centre" -> "center"; "right", "end" -> "right"; else -> "left" }
        val existing = Regex("<w:jc\\s+w:val=\"[^\"]*\"\\s*/>").find(pPr)
        return if (existing != null) pPr.replace(existing.value, "<w:jc w:val=\"$jc\"/>")
        else {
            // insert w:jc at end of pPr (schema order loose; word tolerates) 
            if (pPr.isBlank()) "<w:pPr><w:jc w:val=\"$jc\"/></w:pPr>" else pPr.replace("</w:pPr>", "<w:jc w:val=\"$jc\"/></w:pPr>")
        }
    }

    fun insertImageRun(drawingXml: String): String = "<w:r>$drawingXml</w:r>"

    /** Build `wp:inline` DrawingML for a new picture. */
    fun buildInlineDrawing(
        rid: String,
        widthEmu: Long,
        heightEmu: Long,
        drawingId: Int,
        name: String,
        descr: String = "",
    ): String {
        val w = widthEmu.coerceAtLeast(1L); val h = heightEmu.coerceAtLeast(1L)
        val esc = XmlScan.escape(name); val desc = XmlScan.escape(descr)
        val base = esc.lowercase().substringBeforeLast('.').ifEmpty { "image" }
        return """<w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"><wp:extent cx="$w" cy="$h"/><wp:effectExtent l="0" t="0" r="0" b="0"/><wp:docPr id="$drawingId" name="$esc" descr="$desc"/><wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect="1" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"/></wp:cNvGraphicFramePr><a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><pic:nvPicPr><pic:cNvPr id="$drawingId" name="$esc"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="$rid"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="$w" cy="$h"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing>"""
    }

    fun setEmbedRid(obj: String, newRid: String): String =
        obj.replace(Regex("(<a:blip[^>]*\\sr:embed=\")[^\"]*(\")")) { "${it.groupValues[1]}$newRid${it.groupValues[2]}" }
            .replace(Regex("(<a:blip[^>]*\\sembed=\")[^\"]*(\")")) { "${it.groupValues[1]}$newRid${it.groupValues[2]}" }

    /** Delete drawing object; caller removes the object character from paragraph. */
    fun removeDrawing(obj: String): String? = null

    private fun attr(xml: String, name: String): String? =
        Regex("(?:^|\\s)$name=\"([^\"]*)\"").find(xml)?.groupValues?.get(1)

    fun emuFromPx(px: Int, densityDpi: Int = 96): Long = (px.toLong() * EMU_PER_INCH / densityDpi.coerceAtLeast(1)).coerceAtLeast(1L)
    fun emuFromPt(pt: Float): Long = (pt * EMU_PER_PT).roundToInt().toLong().coerceAtLeast(1L)
    fun ptToEmu(pt: Float): Long = (pt * EMU_PER_PT).roundToInt().toLong()
}
