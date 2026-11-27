package com.hmx.pdf.backend

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed class HttpResult {
    data class Success(val body: String, val status: Int = 200) : HttpResult()
    data class Failure(val status: Int?, val message: String?) : HttpResult()
}

/** Minimal synchronous JSON transport. No SDK, no background queue, no telemetry. */
internal object HttpJson {
    private const val CONNECT_TIMEOUT_MS = 8_000
    private const val READ_TIMEOUT_MS = 10_000

    fun request(
        url: String,
        method: String,
        apiKey: String,
        body: String? = null,
        /** Correlation id echoed back by the server on errors (x-request-id). Not sensitive. */
        requestId: String? = null,
        /** Extra headers; values are never logged. */
        extraHeaders: Map<String, String> = emptyMap(),
    ): HttpResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("apikey", apiKey)
            if (requestId != null) connection.setRequestProperty("x-request-id", requestId)
            extraHeaders.forEach { (name, value) -> connection.setRequestProperty(name, value) }
            if (body != null) {
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status in 200..299) HttpResult.Success(text, status)
            else HttpResult.Failure(status, text.take(200))
        } catch (e: IOException) {
            HttpResult.Failure(null, e.message?.take(160))
        } catch (e: Exception) {
            HttpResult.Failure(null, e.message?.take(160))
        } finally {
            connection?.disconnect()
        }
    }
}
