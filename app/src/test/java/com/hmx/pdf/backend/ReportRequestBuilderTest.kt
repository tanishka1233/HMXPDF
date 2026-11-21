package com.hmx.pdf.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportRequestBuilderTest {
    private val draft = ReportDraft(
        reportType = "bug",
        title = "Viewer crashed",
        description = "Only user-written text",
        errorCode = "PDF_OPEN_FAILED",
        screen = "viewer",
        androidApiLevel = 34,
        locale = "en-IN",
    )

    @Test
    fun payloadContainsOnlyAllowlistedFields() {
        val json = ReportRequestBuilder.build(draft, "hmx-install-1234", "2.1.1", 21)
        for (field in listOf(
            "install_id", "app_version", "app_version_code", "report_type", "title",
            "description", "diagnostic_payload",
        )) {
            assertTrue("missing $field", json.contains("\"$field\""))
        }
        val payload = json.substringAfter("\"diagnostic_payload\"")
        for (field in listOf("android_api_level", "locale", "error_code", "screen")) {
            assertTrue("missing payload field $field", payload.contains("\"$field\""))
        }
    }

    @Test
    fun payloadNeverContainsDocumentsOrDeviceIdentity() {
        val json = ReportRequestBuilder.build(draft, "hmx-install-1234", "2.1.1", 21)
        // Check JSON field names, not substrings of user text: an error_code may
        // legitimately contain "PDF_OPEN_FAILED" without carrying document content.
        for (forbidden in listOf(
            "device_model", "manufacturer", "imei", "location", "latitude", "longitude",
            "file_uri", "document", "content_uri", "advertising_id", "file_content", "page_text",
        )) {
            assertFalse("forbidden field present: $forbidden", json.contains("\"" + forbidden + "\"", ignoreCase = true))
        }
        val payload = json.substringAfter("\"diagnostic_payload\"")
        for (allowed in listOf("android_api_level", "locale", "error_code", "screen")) {
            assertTrue("missing allowlisted payload key $allowed", payload.contains("\"" + allowed + "\""))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonAllowlistedReportType() {
        ReportRequestBuilder.build(
            draft.copy(reportType = "evil"), "hmx-install-1234", "2.1.1", 21,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTooShortInstallId() {
        ReportRequestBuilder.build(draft, "short", "2.1.1", 21)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInstallIdWithUnsupportedCharacters() {
        ReportRequestBuilder.build(draft, "hmx install id with spaces", "2.1.1", 21)
    }

    @Test
    fun truncatesUserText() {
        val long = "x".repeat(9000)
        val json = ReportRequestBuilder.build(
            draft.copy(title = long, description = long), "hmx-install-1234", "2.1.1", 21,
        )
        assertEquals(200, json.substringAfter("\"title\":\"").substringBefore("\"").length)
        assertTrue(json.length < 12000)
    }

    @Test
    fun configRequestHasNoInstallIdentifier() {
        val url = RemoteConfigRequestBuilder.url("https://staging.example/functions/v1", 21)
        assertEquals("https://staging.example/functions/v1/get-app-config?app_version_code=21", url)
        assertFalse(url.contains("install", ignoreCase = true))
    }
}
