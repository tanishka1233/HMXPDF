package com.hmx.pdf.analytics

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Builds the exact, allowlisted JSON body accepted by analytics-ingest. */
object AnalyticsPayload {
    private val json = Json { ignoreUnknownKeys = true }
    private val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    @Synchronized
    fun timestamp(epochMillis: Long): String = iso.format(java.util.Date(epochMillis))

    fun build(installId: String, events: List<AnalyticsEvent>): String {
        require(AnalyticsTaxonomy.validInstallId(installId)) { "invalid analytics install id" }
        require(events.isNotEmpty()) { "empty analytics batch" }
        require(events.size <= ANALYTICS_MAX_BATCH) { "analytics batch too large" }
        return buildString {
            append("{\"install_id\":\"").append(installId)
            append("\",\"consent_version\":").append(ANALYTICS_CONSENT_VERSION)
            append(",\"events\":[")
            events.forEachIndexed { index, event ->
                if (index > 0) append(',')
                require(event.name in AnalyticsTaxonomy.EVENTS) { "event not allowlisted" }
                append("{\"event_id\":\"").append(event.eventId)
                append("\",\"name\":\"").append(event.name)
                append("\",\"occurred_at\":\"").append(timestamp(event.occurredAt)).append('"')
                if (event.sessionId != null) append(",\"session_id\":\"").append(event.sessionId).append('"')
                if (event.feature != null) {
                    require(event.feature in AnalyticsTaxonomy.ALLOWED_FEATURES) { "feature not allowlisted" }
                    append(",\"feature\":\"").append(event.feature).append('"')
                }
                if (event.errorCategory != null) {
                    require(event.errorCategory in AnalyticsTaxonomy.ERROR_CATEGORIES) { "error category not allowlisted" }
                    append(",\"error_category\":\"").append(event.errorCategory).append('"')
                }
                append(",\"app_version\":")
                append(json.encodeToString(event.appVersion))
                append(",\"app_version_code\":").append(event.appVersionCode)
                append(",\"android_major\":").append(event.androidMajor)
                append('}')
            }
            append("]}")
        }
    }

    fun encodeQueue(queue: List<AnalyticsEvent>): String =
        json.encodeToString(ListSerializer(AnalyticsEvent.serializer()), queue)

    fun decodeQueue(raw: String?): List<AnalyticsEvent> = try {
        if (raw.isNullOrBlank()) emptyList()
        else json.decodeFromString(ListSerializer(AnalyticsEvent.serializer()), raw)
    } catch (e: Exception) {
        emptyList()
    }
}
