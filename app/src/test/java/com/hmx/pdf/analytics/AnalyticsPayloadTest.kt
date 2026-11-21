package com.hmx.pdf.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsPayloadTest {
    private val id = "hmxa-11111111-1111-4111-8111-111111111111"
    private val event = AnalyticsEvent(
        eventId = "22222222-2222-4222-8222-222222222222",
        name = "pdf_open",
        occurredAt = 1_700_000_000_000L,
        sessionId = "33333333-3333-4333-8333-333333333333",
        feature = "pdf",
        appVersion = "2.1.1",
        appVersionCode = 21,
        androidMajor = 34,
    )

    @Test
    fun payloadContainsOnlyAllowlistedFields() {
        val json = AnalyticsPayload.build(id, listOf(event))
        for (field in listOf(
            "install_id", "consent_version", "events", "event_id", "name", "occurred_at",
            "session_id", "feature", "app_version", "app_version_code", "android_major",
        )) assertTrue("missing $field", json.contains("\"$field\""))
        assertTrue(json.startsWith("{\"install_id\":\"" + id + "\""))
        assertEquals(1, AnalyticsTaxonomy.EVENTS.count { it == event.name })
    }

    @Test
    fun payloadNeverContainsDocumentOrPersonalFields() {
        val json = AnalyticsPayload.build(id, listOf(event))
        for (forbidden in listOf(
            "file_name", "file_uri", "path", "uri", "document", "page_text", "ocr",
            "email", "phone", "password", "token", "location", "latitude", "longitude",
            "advertising_id", "android_id", "device_model", "stack", "exception", "message",
        )) assertFalse("forbidden field: $forbidden", json.contains("\"$forbidden\"", ignoreCase = true))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEventOutsideTaxonomy() {
        AnalyticsPayload.build(id, listOf(event.copy(name = "tracking")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDiagnosticStyleInstallId() {
        AnalyticsPayload.build("hmx-11111111-1111-4111-8111-111111111111", listOf(event))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOversizedBatch() {
        AnalyticsPayload.build(id, List(ANALYTICS_MAX_BATCH + 1) { event })
    }

    @Test
    fun timestampIsIsoUtc() {
        assertEquals("2023-11-14T22:13:20.000Z", AnalyticsPayload.timestamp(1_700_000_000_000L))
    }

    @Test
    fun queueSurvivesSerialization() {
        val encoded = AnalyticsPayload.encodeQueue(listOf(event, event.copy(eventId = "44444444-4444-4444-8444-444444444444")))
        assertEquals(2, AnalyticsPayload.decodeQueue(encoded).size)
        assertTrue(AnalyticsPayload.decodeQueue("corrupt").isEmpty())
    }

    @Test
    fun errorCategoryAllowedOnlyForReliabilityEvents() {
        val engine = AnalyticsEngine(
            MemoryAnalyticsStorage(), FakeAnalyticsTransport(), true,
            AnalyticsEngine.AppMeta("2.1.1", 21, 34),
        )
        engine.grantConsent()
        assertFalse(engine.track("pdf_open", "pdf", "io"))
        assertTrue(engine.track("operation_failed", "pdf", "io"))
    }
}
