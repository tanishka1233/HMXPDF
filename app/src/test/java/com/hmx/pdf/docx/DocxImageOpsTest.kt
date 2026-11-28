package com.hmx.pdf.docx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocxImageOpsTest {

    private fun sampleDrawing(): String = DocxImageOps.buildInlineDrawing(
        rid = "rIdHmxImage1",
        widthEmu = 1_000_000L,
        heightEmu = 500_000L,
        drawingId = 42,
        name = "image1.png",
        descr = "",
    )

    @Test
    fun `buildInlineDrawing produces valid DrawingML`() {
        val xml = sampleDrawing()
        assertTrue(xml.contains("<w:drawing"))
        assertTrue(xml.contains("<wp:inline"))
        assertTrue(xml.contains("<wp:extent cx=\"1000000\" cy=\"500000\""))
        assertTrue(xml.contains("<wp:docPr id=\"42\" name=\"image1.png\""))
        assertTrue(xml.contains("<a:blip r:embed=\"rIdHmxImage1\""))
        assertTrue(xml.contains("<a:ext cx=\"1000000\" cy=\"500000\""))
        assertTrue(xml.contains("<a:prstGeom prst=\"rect\""))
        assertTrue(xml.contains("<pic:blipFill"))
        assertTrue(xml.contains("<pic:spPr"))
        assertTrue(DocxImageOps.isImage("<w:r>$xml</w:r>"))
    }

    @Test
    fun `isImage rejects non-pictures`() {
        assertFalse(DocxImageOps.isImage("<w:r><w:t>hello</w:t></w:r>"))
        assertFalse(DocxImageOps.isImage("<w:r><w:drawing><wp:inline/></w:drawing></w:r>")) // no blip
        assertFalse(DocxImageOps.isImage("<w:p/>"))
    }

    @Test
    fun `setExtent rewrites wp extent and a ext preserving other content`() {
        val xml = sampleDrawing()
        val out = DocxImageOps.setExtent(xml, 2_000_000L, 1_000_000L)
        assertTrue(out.contains("<wp:extent cx=\"2000000\" cy=\"1000000\""))
        assertTrue(out.contains("<a:ext cx=\"2000000\" cy=\"1000000\""))
        assertTrue(out.contains("r:embed=\"rIdHmxImage1\""))
        assertTrue(out.contains("name=\"image1.png\""))
    }

    @Test
    fun `invalid dimensions are rejected`() {
        val xml = sampleDrawing()
        assertEquals(xml, DocxImageOps.setExtent(xml, 0L, 100L))
        assertEquals(xml, DocxImageOps.setExtent(xml, -1L, 100L))
        assertEquals(xml, DocxImageOps.setExtent(xml, 100L, 0L))
        assertFalse(DocxImageOps.validDimensions(0L, 100L))
        assertTrue(DocxImageOps.validDimensions(1L, 1L))
        assertEquals(1L to 1L, DocxImageOps.clampDimensions(0L, 0L))
    }

    @Test
    fun `scale preserves aspect ratio by default`() {
        val st = DocxImageOps.style(sampleDrawing())
        val (w, h) = DocxImageOps.scale(st, 2f, preserveAspect = true)
        assertEquals(2_000_000L, w); assertEquals(1_000_000L, h)
        val (w2, h2) = DocxImageOps.scale(st, 0.5f, preserveAspect = true)
        assertEquals(500_000L, w2); assertEquals(250_000L, h2)
    }

    @Test
    fun `rotation normalizes and sets rot`() {
        assertEquals(90f, DocxImageOps.normalizeDegrees(90f))
        assertEquals(-90f, DocxImageOps.normalizeDegrees(-90f))
        assertEquals(0f, DocxImageOps.normalizeDegrees(360f))
        val xml = DocxImageOps.setRotation(sampleDrawing(), 90f)
        assertTrue(xml.contains("rot=\"5400000\""))
        val st = DocxImageOps.style(xml)
        assertEquals(90f, st.rotationDegrees, 0.01f)
        val st2 = DocxImageOps.rotated(st, 90f)
        assertEquals(180f, st2.rotationDegrees, 0.01f)
    }

    @Test
    fun `flip horizontal and vertical persist on a xfrm`() {
        val h = DocxImageOps.setFlip(sampleDrawing(), horizontal = true)
        assertTrue(h.contains("flipH=\"1\""))
        assertTrue(DocxImageOps.style(h).flipH)
        val v = DocxImageOps.setFlip(h, vertical = true)
        assertTrue(v.contains("flipV=\"1\""))
        assertTrue(DocxImageOps.style(v).flipV)
        val off = DocxImageOps.setFlip(v, horizontal = false, vertical = false)
        assertFalse(DocxImageOps.style(off).flipH)
        assertFalse(DocxImageOps.style(off).flipV)
    }

    @Test
    fun `crop writes a srcRect percents`() {
        val out = DocxImageOps.setCrop(sampleDrawing(), 10f, 20f, 5f, 0f)
        assertTrue(out.contains("<a:srcRect l=\"10000\" t=\"20000\" r=\"5000\" b=\"0\"/>"))
        val st = DocxImageOps.style(out)
        assertEquals(10f, st.cropLeftPct, 0.01f); assertEquals(20f, st.cropTopPct, 0.01f); assertEquals(5f, st.cropRightPct, 0.01f)
    }

    @Test
    fun `border enable disable persists a ln`() {
        val on = DocxImageOps.setBorder(sampleDrawing(), enabled = true, colorHex = "#FF0000", widthEmu = 9525L)
        assertTrue(on.contains("<a:ln w=\"9525\""))
        assertTrue(on.contains("<a:srgbClr val=\"FF0000\""))
        assertTrue(DocxImageOps.style(on).borderEnabled)
        val off = DocxImageOps.setBorder(on, enabled = false)
        assertTrue(off.contains("<a:noFill"))
        assertFalse(DocxImageOps.style(off).borderEnabled)
    }

    @Test
    fun `paragraph alignment sets w jc`() {
        val pPr = DocxImageOps.setHAlignParagraph("<w:pPr></w:pPr>", "center")
        assertTrue(pPr.contains("<w:jc w:val=\"center\"/>"))
        val pPr2 = DocxImageOps.setHAlignParagraph(pPr, "right")
        assertTrue(pPr2.contains("<w:jc w:val=\"right\"/>"))
        assertFalse(pPr2.contains("w:val=\"center\""))
        assertTrue(DocxImageOps.setHAlignParagraph("", "left").contains("<w:jc w:val=\"left\"/>"))
    }

    @Test
    fun `setEmbedRid replaces only r embed`() {
        val out = DocxImageOps.setEmbedRid(sampleDrawing(), "rIdNew")
        assertTrue(out.contains("r:embed=\"rIdNew\""))
        assertTrue(out.contains("name=\"image1.png\""))
        assertTrue(out.contains("<wp:extent cx=\"1000000\" cy=\"500000\""))
    }
}
