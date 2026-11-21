package com.hmx.pdf.analytics

import kotlinx.serialization.Serializable

const val ANALYTICS_CONSENT_VERSION = 1
const val ANALYTICS_MAX_QUEUE = 200
const val ANALYTICS_MAX_BATCH = 50
const val ANALYTICS_MAX_EVENT_AGE_MS = 7L * 24 * 60 * 60 * 1000
const val ANALYTICS_SESSION_TIMEOUT_MS = 30L * 60 * 1000

/** Strict allowlist. Only these event names and properties can ever leave the device. */
object AnalyticsTaxonomy {
    val EVENTS = setOf(
        "app_open", "session_start", "session_end",
        "pdf_open", "pdf_edit", "pdf_export", "pdf_share",
        "pdf_merge", "pdf_split", "pdf_compress",
        "docx_open", "docx_edit", "docx_export",
        "scan_use", "ocr_use", "signature_use", "url_to_pdf_use",
        "app_crash", "operation_failed",
    )

    val FEATURE_FOR = mapOf(
        "pdf_open" to "pdf", "pdf_edit" to "pdf", "pdf_export" to "pdf", "pdf_share" to "pdf",
        "pdf_merge" to "pdf", "pdf_split" to "pdf", "pdf_compress" to "pdf",
        "docx_open" to "docx", "docx_edit" to "docx", "docx_export" to "docx",
        "scan_use" to "scan", "ocr_use" to "ocr", "signature_use" to "signature",
        "url_to_pdf_use" to "url_to_pdf",
    )

    val ALLOWED_FEATURES = setOf("pdf", "docx", "scan", "ocr", "signature", "url_to_pdf", "general")
    val ERROR_CATEGORIES = setOf("io", "network", "parse", "permission", "memory", "timeout", "unknown")
    private val INSTALL_ID = Regex("^hmxa-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

    fun validInstallId(value: String?): Boolean = value != null && INSTALL_ID.matches(value)

    fun featureFor(name: String, requested: String?): String? {
        val expected = FEATURE_FOR[name] ?: "general"
        return if (requested != null && requested in ALLOWED_FEATURES) requested else expected
    }
}

@Serializable
data class AnalyticsEvent(
    val eventId: String,
    val name: String,
    val occurredAt: Long,
    val sessionId: String? = null,
    val feature: String? = null,
    val errorCategory: String? = null,
    val appVersion: String,
    val appVersionCode: Int,
    val androidMajor: Int,
)

/** Bounded queue semantics — pure, so they can be verified by JVM tests. */
object AnalyticsQueue {
    fun append(
        queue: List<AnalyticsEvent>,
        event: AnalyticsEvent,
        now: Long = System.currentTimeMillis(),
        maxSize: Int = ANALYTICS_MAX_QUEUE,
        maxAgeMs: Long = ANALYTICS_MAX_EVENT_AGE_MS,
    ): List<AnalyticsEvent> {
        if (event.name !in AnalyticsTaxonomy.EVENTS) return queue
        val fresh = queue.filter {
            it.occurredAt > now - maxAgeMs && it.occurredAt <= now + 5 * 60 * 1000 && it.eventId != event.eventId
        }
        return (fresh + event).takeLast(maxSize)
    }

    fun takeBatch(queue: List<AnalyticsEvent>, size: Int = ANALYTICS_MAX_BATCH): List<AnalyticsEvent> =
        queue.take(size)

    fun removeAccepted(
        queue: List<AnalyticsEvent>,
        acceptedIds: Set<String>,
    ): List<AnalyticsEvent> = queue.filterNot { it.eventId in acceptedIds }

    fun shouldStartSession(lastActivityAt: Long?, now: Long): Boolean =
        lastActivityAt == null || now - lastActivityAt > ANALYTICS_SESSION_TIMEOUT_MS
}
