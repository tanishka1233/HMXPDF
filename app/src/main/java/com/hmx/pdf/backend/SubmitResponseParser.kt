package com.hmx.pdf.backend

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses the `submit-report` success contract:
 * `{ "data": { "id": <uuid>, "submitted_at": <timestamptz>, "status": "new" } }`.
 *
 * A 2xx response is NOT proof of persistence on its own — a success is only recognised when the
 * server returns a non-blank report id. Anything else is treated as "unconfirmed", never success.
 */
object SubmitResponseParser {
    /** Returns the persisted report id, or null if the body doesn't prove a saved row. */
    fun parseReportId(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val root = Json.parseToJsonElement(body)
            if (root !is JsonObject) return null
            val data = root["data"] as? JsonObject ?: return null
            val id = data["id"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
            // The server inserts a fresh row and returns it; a missing submitted_at means we can't
            // trust that the row exists as returned.
            data["submitted_at"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
            id
        } catch (e: Exception) {
            null
        }
    }
}
