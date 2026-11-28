package com.hmx.pdf.docx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocxImageInsertionIntegrationTest {

    private val png = byteArrayOf(
        0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(),
        0, 0, 0, 13, 'I'.code.toByte(), 'H'.code.toByte(), 'D'.code.toByte(), 'R'.code.toByte(),
    )

    @Test
    fun `inserted image persists media relationship drawing and surrounding text`() {
        val src = DocxFixtures.docx("<w:p><w:r><w:t>Hello</w:t></w:r></w:p><w:p><w:r><w:t>World</w:t></w:r></w:p>")
        val (pkg, state) = DocxFixtures.read(src)
        val p = state.blocks.filterIsInstance<DocPara>().first()
        val drawing = DocxImageOps.buildInlineDrawing("rIdHmxImage1", 1_000_000L, 500_000L, 1, "image1.png", "")
        val obj = DocxImageOps.insertImageRun(drawing)
        val newP = p.edited(text = p.text + OBJ_CHAR, fmts = p.fmts + CharFmt("", obj = obj))
        val newState = DocState(listOf(newP) + state.blocks.drop(1))
        val mediaPath = "word/media/hmx-image-1.png"
        val pkg2 = pkg.withImageAdded("rIdHmxImage1", mediaPath, png)
        val extras = DocxWriter.Extras(
            mediaAdd = mapOf(mediaPath to png),
            relsAdd = listOf(DocxWriter.RelAdd("rIdHmxImage1", DocxFixtures.REL + "/image", "media/hmx-image-1.png")),
            contentTypeAdds = listOf(DocxWriter.ContentTypeAdd("png", null, "image/png", true)),
        )
        val saved = DocxFixtures.save(src, pkg2, newState, extras)

        // media added
        assertTrue(DocxFixtures.entry(saved, mediaPath)!!.toByteArray(Charsets.ISO_8859_1).contentEquals(png) ||
            DocxFixtures.entry(saved, mediaPath)!!.length == png.size)
        // relationship + content type
        val rels = DocxFixtures.entry(saved, "word/_rels/document.xml.rels")!!
        assertTrue(rels.contains("Id=\"rIdHmxImage1\""))
        assertTrue(rels.contains("Type=\"${DocxFixtures.REL}/image\""))
        assertTrue(rels.contains("Target=\"media/hmx-image-1.png\""))
        val types = DocxFixtures.entry(saved, "[Content_Types].xml")!!
        assertTrue(types.contains("Extension=\"png\""))
        assertTrue(types.contains("image/png"))
        // drawing + surrounding text preserved
        val docXml = DocxFixtures.entry(saved, "word/document.xml")!!
        assertTrue(docXml.contains("<w:t xml:space=\"preserve\">Hello</w:t>"))
        assertTrue(docXml.contains("<w:t xml:space=\"preserve\">World</w:t>"))
        assertTrue(docXml.contains("<a:blip r:embed=\"rIdHmxImage1\""))

        // reopen
        val (rpkg, rstate) = DocxFixtures.read(saved)
        assertEquals(mediaPath, rpkg.rels["rIdHmxImage1"])
        assertTrue(rpkg.image("rIdHmxImage1")!!.isNotEmpty())
        assertTrue(rpkg.image("rIdHmxImage1")!!.size == png.size)
    }
}
