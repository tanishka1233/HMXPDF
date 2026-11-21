package com.hmx.pdf.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AnnouncementDto(
    val enabled: Boolean = false,
    val message: String = "",
    val url: String = "",
)

@Serializable
data class LimitsDto(
    @SerialName("max_document_mb") val maxDocumentMb: Int = 200,
    @SerialName("max_pages") val maxPages: Int = 500,
)

@Serializable
data class RemoteConfigDto(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    @SerialName("min_supported_app_version_code") val minSupportedVersionCode: Int = 1,
    val features: Map<String, Boolean> = emptyMap(),
    val announcement: AnnouncementDto = AnnouncementDto(),
    val limits: LimitsDto = LimitsDto(),
)

@Serializable
data class ConfigEnvelope(
    val version: Long = 0,
    val config: RemoteConfigDto? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    /** Server verdict for the app version that asked. Absent means "not evaluated". */
    val compatible: Boolean = true,
)

@Serializable
data class ConfigResponse(val data: ConfigEnvelope? = null)

data class RemoteConfigSnapshot(
    val version: Long,
    val config: RemoteConfigDto,
    val compatible: Boolean = true,
)

/** Reads only the published configuration document. Unknown keys are ignored; malformed
 * documents are rejected rather than applied, so the caller can fall back safely. */
object RemoteConfigParser {
    private val json = Json { ignoreUnknownKeys = true }
    private val allowedFeatures = setOf(
        "url_to_pdf", "office_engine", "ocr", "image_editor", "dark_pages", "annotations", "form_fill",
    )

    fun parse(raw: String): RemoteConfigSnapshot? {
        val envelope = try {
            json.decodeFromString(ConfigResponse.serializer(), raw).data
        } catch (e: Exception) {
            null
        } ?: return null
        val config = envelope.config ?: return null
        if (config.schemaVersion != 1) return null
        if (config.minSupportedVersionCode !in 1..1_000_000_000) return null
        if (config.features.keys.any { it !in allowedFeatures }) return null
        return RemoteConfigSnapshot(envelope.version, config, envelope.compatible)
    }

    fun encode(snapshot: RemoteConfigSnapshot): String =
        json.encodeToString(
            ConfigResponse.serializer(),
            ConfigResponse(ConfigEnvelope(snapshot.version, snapshot.config, null, snapshot.compatible)),
        )
}

object FeatureFlags {
    const val URL_TO_PDF = "url_to_pdf"
    const val OFFICE_ENGINE = "office_engine"
    const val OCR = "ocr"
    const val IMAGE_EDITOR = "image_editor"
    const val DARK_PAGES = "dark_pages"
    const val ANNOTATIONS = "annotations"
    const val FORM_FILL = "form_fill"

    fun isEnabled(snapshot: RemoteConfigSnapshot?, flag: String, default: Boolean = false): Boolean {
        if (snapshot == null || !snapshot.compatible) return default
        return snapshot.config.features[flag] ?: default
    }
}

/** No install identifier, no user identity, no device identity. */
object RemoteConfigRequestBuilder {
    fun url(baseUrl: String, appVersionCode: Int): String =
        "$baseUrl/get-app-config?app_version_code=$appVersionCode"
}
