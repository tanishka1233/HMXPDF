package com.hmx.pdf.backend

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** Only these fields are ever sent, and only after the user confirms a report. */
data class ReportDraft(
    val reportType: String,
    val title: String,
    val description: String,
    val errorCode: String? = null,
    val screen: String? = null,
    val androidApiLevel: Int = 0,
    val locale: String = "",
)

object ReportRequestBuilder {
    val ALLOWED_TYPES = setOf("bug", "crash", "performance", "ui", "other")

    fun build(
        draft: ReportDraft,
        installId: String,
        appVersion: String,
        appVersionCode: Int,
    ): String {
        require(draft.reportType in ALLOWED_TYPES) { "report_type is not allowlisted" }
        // Mirrors the server's install_id rule so a tampered preference fails locally.
        require(installId.matches(Regex("^[A-Za-z0-9._:-]{8,128}$"))) { "install id has an invalid format" }
        return buildJsonObject {
            put("install_id", installId)
            put("app_version", appVersion.take(40))
            put("app_version_code", appVersionCode)
            put("report_type", draft.reportType)
            put("title", draft.title.take(200))
            put("description", draft.description.take(5000))
            putJsonObject("diagnostic_payload") {
                put("android_api_level", draft.androidApiLevel)
                put("locale", draft.locale.take(40))
                if (!draft.errorCode.isNullOrBlank()) put("error_code", draft.errorCode.take(80))
                if (!draft.screen.isNullOrBlank()) put("screen", draft.screen.take(60))
            }
        }.toString()
    }
}

/** Bounded retry policy: transport failures, 429 and 5xx only. No retry on validation errors. */
object RetryPolicy {
    const val MAX_ATTEMPTS = 3

    fun shouldRetry(attempt: Int, httpStatus: Int?): Boolean {
        if (attempt >= MAX_ATTEMPTS) return false
        return httpStatus == null || httpStatus == 429 || httpStatus in 500..599
    }

    fun backoffMillis(attempt: Int): Long = 400L * (1L shl (attempt - 1))
}
