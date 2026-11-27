package com.hmx.pdf.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubmitResponseParserTest {

    @Test
    fun `persisted 201 contract yields report id`() {
        val body = """{"data":{"id":"17056a2c-fec7-4936-bca7-39424df60fb8","submitted_at":"2026-11-27T06:20:47.813025+00:00","status":"new"}}"""
        assertEquals("17056a2c-fec7-4936-bca7-39424df60fb8", SubmitResponseParser.parseReportId(body))
    }

    @Test
    fun `idempotent replay contract yields the same report id`() {
        val body = """{"data":{"id":"382505ed-0a46-4819-be6c-4731976e00d5","submitted_at":"2026-11-27T08:55:33.148479+00:00","status":"new"}}"""
        assertEquals("382505ed-0a46-4819-be6c-4731976e00d5", SubmitResponseParser.parseReportId(body))
    }

    @Test
    fun `2xx without a report id is not success`() {
        assertNull(SubmitResponseParser.parseReportId("""{"data":{"status":"new"}}"""))
        assertNull(SubmitResponseParser.parseReportId("""{"data":{}}"""))
        assertNull(SubmitResponseParser.parseReportId("{}"))
    }

    @Test
    fun `missing submitted_at is not success`() {
        assertNull(SubmitResponseParser.parseReportId("""{"data":{"id":"x"}}"""))
        assertNull(SubmitResponseParser.parseReportId("""{"data":{"id":"17056a2c-fec7-4936-bca7-39424df60fb8","submitted_at":""}}"""))
    }

    @Test
    fun `blank or malformed bodies are not success`() {
        assertNull(SubmitResponseParser.parseReportId(""))
        assertNull(SubmitResponseParser.parseReportId("   "))
        assertNull(SubmitResponseParser.parseReportId("not json"))
        assertNull(SubmitResponseParser.parseReportId("[]"))
        assertNull(SubmitResponseParser.parseReportId("""{"data":null}"""))
        assertNull(SubmitResponseParser.parseReportId("""{"data":{"id":""}}"""))
    }

    @Test
    fun `error contract is never parsed as success`() {
        assertNull(SubmitResponseParser.parseReportId("""{"error":{"code":"invalid_field","message":"Unknown report field","request_id":"x"}}"""))
    }

    @Test
    fun `project ref derived from public backend url`() {
        assertEquals("hwwnksxfrllkrqonngob", DiagnosticsTelemetry.projectRef("https://hwwnksxfrllkrqonngob.supabase.co/functions/v1"))
        assertEquals("unknown", DiagnosticsTelemetry.projectRef("not a url"))
    }

    @Test
    fun `short id truncation`() {
        assertEquals("17056a2c", DiagnosticsTelemetry.shortId("17056a2c-fec7-4936-bca7-39424df60fb8"))
    }
}
