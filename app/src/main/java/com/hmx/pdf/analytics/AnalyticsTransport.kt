package com.hmx.pdf.analytics

import com.hmx.pdf.backend.BackendConfig
import com.hmx.pdf.backend.HttpJson
import com.hmx.pdf.backend.HttpResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class RevokeVerification(val verified: Boolean, val deletedEvents: Int = 0)

interface AnalyticsTransport {
    fun register(installId: String, appVersion: String, appVersionCode: Int, androidMajor: Int): Boolean
    fun ingest(installId: String, payload: String): Boolean
    fun revoke(installId: String, action: String): RevokeVerification
}

/** Production/staging HTTP transport. Never used when BackendConfig is disabled. */
class HttpAnalyticsTransport : AnalyticsTransport {

    override fun register(installId: String, appVersion: String, appVersionCode: Int, androidMajor: Int): Boolean {
        val body = buildString {
            append("{\"action\":\"register\",\"install_id\":\"").append(installId)
            append("\",\"consent_version\":").append(ANALYTICS_CONSENT_VERSION)
            append(",\"app_version\":").append(Json.encodeToString(appVersion))
            append(",\"app_version_code\":").append(appVersionCode)
            append(",\"android_major\":").append(androidMajor)
            append('}')
        }
        return HttpJson.request(
            "${BackendConfig.functionsBaseUrl}/analytics-consent", "POST", BackendConfig.publishableKey, body,
        ) is HttpResult.Success
    }

    override fun ingest(installId: String, payload: String): Boolean =
        HttpJson.request(
            "${BackendConfig.functionsBaseUrl}/analytics-ingest", "POST", BackendConfig.publishableKey, payload,
        ) is HttpResult.Success

    override fun revoke(installId: String, action: String): RevokeVerification {
        val body = "{\"action\":\"$action\",\"install_id\":\"$installId\"}"
        return when (val result = HttpJson.request(
            "${BackendConfig.functionsBaseUrl}/analytics-consent", "POST", BackendConfig.publishableKey, body,
        )) {
            is HttpResult.Success -> {
                val verified = Regex("\"verified\"\\s*:\\s*true").containsMatchIn(result.body)
                val deleted = Regex("\"deleted_events\"\\s*:\\s*(\\d+)")
                    .find(result.body)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                RevokeVerification(verified, deleted)
            }
            is HttpResult.Failure -> RevokeVerification(false)
        }
    }
}

/** Test transport: records calls and never touches the network. */
class FakeAnalyticsTransport : AnalyticsTransport {
    var registered = mutableListOf<String>()
    var batches = mutableListOf<String>()
    var revoked = mutableListOf<String>()
    var registerOk = true
    var ingestOk = true
    var revokeVerified = true

    override fun register(installId: String, appVersion: String, appVersionCode: Int, androidMajor: Int): Boolean {
        registered.add(installId)
        return registerOk
    }

    override fun ingest(installId: String, payload: String): Boolean {
        batches.add(payload)
        return ingestOk
    }

    override fun revoke(installId: String, action: String): RevokeVerification {
        revoked.add(installId)
        return RevokeVerification(revokeVerified, 3)
    }
}
