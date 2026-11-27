package com.hmx.pdf.backend

import android.util.Log

/**
 * Safe structured diagnostics for the diagnostics-submission pipeline.
 *
 * Privacy invariant: nothing a user typed, no document content, no install id, no API key or token,
 * no report id in full, and no server response body is ever logged. Only opaque ids the client
 * generated, the (already public) backend project reference, HTTP status, and coarse categories.
 */
object DiagnosticsTelemetry {
    const val TAG = "HMXDiag"

    fun started(project: String, submissionId: String, requestId: String, reportType: String) {
        log(
            "report.submit.started",
            mapOf("project" to project, "submission" to shortId(submissionId), "request" to shortId(requestId), "type" to reportType),
        )
    }

    fun attempt(project: String, submissionId: String, requestId: String, attempt: Int, httpStatus: Int?, category: String) {
        log(
            "report.submit.attempt",
            mapOf(
                "project" to project,
                "submission" to shortId(submissionId),
                "request" to shortId(requestId),
                "attempt" to attempt.toString(),
                "http_status" to (httpStatus?.toString() ?: "transport"),
                "category" to category,
            ),
        )
    }

    fun persisted(project: String, submissionId: String, requestId: String, reportId: String) {
        log(
            "report.submit.persisted",
            mapOf("project" to project, "submission" to shortId(submissionId), "request" to shortId(requestId), "report" to shortId(reportId)),
        )
    }

    fun failed(project: String, submissionId: String, requestId: String, category: String) {
        log("report.submit.failed", mapOf("project" to project, "submission" to shortId(submissionId), "request" to shortId(requestId), "category" to category))
    }

    /** First 8 hex chars only — enough to correlate, not enough to matter if logs leak. */
    fun shortId(id: String): String = id.take(8)

    /** Supabase project reference from the public backend URL (already present in the APK). */
    fun projectRef(baseUrl: String): String = try {
        val host = java.net.URI(baseUrl).host.orEmpty()
        host.removeSuffix(".supabase.co").removeSuffix(".supabase.in")
    } catch (e: Exception) {
        "unknown"
    }

    private fun log(event: String, fields: Map<String, String>) {
        val line = buildString {
            append(event)
            for ((k, v) in fields) { append(' ').append(k).append('=').append(v) }
        }
        runCatching { Log.i(TAG, line) }
    }
}
