package com.hmx.pdf.backend

import android.content.Context

/** Published configuration with a last-known-good cache. Never sends an install id. */
class RemoteConfigRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("hmx_remote_config", Context.MODE_PRIVATE)

    fun fetch(appVersionCode: Int): RemoteConfigSnapshot? {
        if (!BackendConfig.enabled) return null
        val url = RemoteConfigRequestBuilder.url(BackendConfig.functionsBaseUrl, appVersionCode)
        return RetryLoop.execute(
            request = { HttpJson.request(url, "GET", BackendConfig.publishableKey) },
            decode = { raw ->
                RemoteConfigParser.parse(raw)?.let { snapshot ->
                    prefs.edit().putString(LAST_GOOD, raw).apply()
                    snapshot
                }
            },
            fallback = { lastKnownGood() },
        )
    }

    fun lastKnownGood(): RemoteConfigSnapshot? =
        prefs.getString(LAST_GOOD, null)?.let { RemoteConfigParser.parse(it) }

    fun clearCache() {
        prefs.edit().remove(LAST_GOOD).apply()
    }

    private companion object {
        const val LAST_GOOD = "last_good"
    }
}

/** Explicit submission only. Generates the install id on first submission, never for config. */
class DiagnosticsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("hmx_diagnostics", Context.MODE_PRIVATE)

    val consentEnabled: Boolean
        get() = BackendConfig.enabled && prefs.getBoolean(CONSENT_KEY, false)

    fun setConsent(enabled: Boolean) {
        prefs.edit().putBoolean(CONSENT_KEY, enabled).apply()
    }

    fun installId(): String {
        val stored = prefs.getString(INSTALL_KEY, null)
        if (!stored.isNullOrBlank()) return stored
        val generated = "hmx-" + java.util.UUID.randomUUID().toString()
        prefs.edit().putString(INSTALL_KEY, generated).apply()
        return generated
    }

    fun submit(draft: ReportDraft): SubmitResult {
        if (!consentEnabled) return SubmitResult(false, "Diagnostics are off.")
        if (draft.reportType !in ReportRequestBuilder.ALLOWED_TYPES) {
            return SubmitResult(false, "Choose a supported report type.")
        }
        if (draft.title.isBlank()) return SubmitResult(false, "A title is required.")
        if (!BackendConfig.enabled) return SubmitResult(false, "Backend is not configured for this build.")

        val body = try {
            ReportRequestBuilder.build(
                draft = draft,
                installId = installId(),
                appVersion = AppInfo.versionName(context),
                appVersionCode = AppInfo.versionCode(context),
            )
        } catch (e: IllegalArgumentException) {
            return SubmitResult(false, e.message ?: "The report could not be prepared.")
        }

        return RetryLoop.execute(
            request = {
                HttpJson.request(
                    url = "${BackendConfig.functionsBaseUrl}/submit-report",
                    method = "POST",
                    apiKey = BackendConfig.publishableKey,
                    body = body,
                )
            },
            decode = { SubmitResult(true, "Report sent. Thank you.") },
            fallback = {
                SubmitResult(false, "Report could not be sent. Your documents are unaffected.")
            },
        )
    }

    private companion object {
        const val CONSENT_KEY = "diagnostics_consent"
        const val INSTALL_KEY = "diagnostics_install_id"
    }
}

data class SubmitResult(val ok: Boolean, val message: String)
