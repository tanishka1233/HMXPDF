package com.hmx.pdf.docx

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

/** Local-only image loading for DOCX insertion/replacement. No network, no gallery scan, no metadata retention. */
object DocxImageLoader {

    private const val MAX_IMAGE_BYTES = 25L * 1024L * 1024L
    private const val MAX_DIMENSION = 20_000

    val SUPPORTED_MIME = setOf("image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp", "image/jfif")

    data class Loaded(val bytes: ByteArray, val mime: String, val extension: String, val widthPx: Int, val heightPx: Int)

    fun load(context: Context, uri: Uri): Loaded? {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri)?.lowercase()?.trim().orEmpty()
        // Some providers return generic types; sniff from extension when possible, then verify dimensions.
        val extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
        val temp = File.createTempFile("hmx-docx-img-", ".tmp", context.cacheDir)
        try {
            val copied = resolver.openInputStream(uri)?.use { input ->
                temp.outputStream().buffered().use { out -> input.copyTo(out) }
                true
            } ?: false
            if (!copied || !temp.exists() || temp.length() <= 0L || temp.length() > MAX_IMAGE_BYTES) return null

            // Validate dimensions without decoding full bitmap.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            temp.inputStream().buffered().use { BitmapFactory.decodeStream(it, null, bounds) }
            val w = bounds.outWidth
            val h = bounds.outHeight
            if (w <= 0 || h <= 0 || w > MAX_DIMENSION || h > MAX_DIMENSION) return null

            // Sniff actual type; don't trust provider MIME alone.
            val detected = sniff(temp)
            val effectiveMime = when {
                detected != null -> detected
                mime in SUPPORTED_MIME -> if (mime == "image/jpg") "image/jpeg" else mime
                else -> null
            } ?: return null

            val extension = when (effectiveMime) {
                "image/jpeg" -> "jpeg"
                "image/jfif" -> "jpeg"
                "image/png" -> "png"
                "image/gif" -> "gif"
                "image/bmp" -> "bmp"
                "image/webp" -> "webp"
                else -> extFromMime?.lowercase()?.takeIf { it.isNotBlank() } ?: return null
            }
            val bytes = temp.readBytes()
            if (bytes.isEmpty() || bytes.size > MAX_IMAGE_BYTES) return null
            return Loaded(bytes, effectiveMime, extension, w, h)
        } catch (e: Exception) {
            return null
        } finally {
            // No temp leaks.
            try { if (temp.exists()) temp.delete() } catch (_: Exception) { }
        }
    }

    /** Verify actual file signature; rejects unsupported/malformed. */
    private fun sniff(f: File): String? {
        val header = ByteArray(16)
        val n = f.inputStream().buffered().use { it.read(header) }
        if (n < 2) return null
        return when {
            header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() -> "image/jpeg"
            header[0] == 0x89.toByte() && header[1] == 0x50.toByte() && header[2] == 0x4E.toByte() && header[3] == 0x47.toByte() -> "image/png"
            header[0] == 0x47.toByte() && header[1] == 0x49.toByte() && header[2] == 0x46.toByte() -> "image/gif"
            header[0] == 0x42.toByte() && header[1] == 0x4D.toByte() -> "image/bmp"
            n >= 12 && header[0] == 0x52.toByte() && header[1] == 0x49.toByte() && header[2] == 0x46.toByte() &&
                header[3] == 0x46.toByte() && header[8] == 0x57.toByte() && header[9] == 0x45.toByte() &&
                header[10] == 0x42.toByte() && header[11] == 0x50.toByte() -> "image/webp"
            else -> null
        }
    }
}
