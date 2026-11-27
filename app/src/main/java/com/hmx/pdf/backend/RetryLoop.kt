package com.hmx.pdf.backend

/**
 * Shared bounded-retry loop for the two staging calls.
 *
 * Retries only transport failures, 429 and 5xx. A success with an unusable body
 * is not retried: the server answered, and retrying would not change the answer.
 * [sleep] is injectable so JVM tests do not wait.
 * [observe] is invoked once per attempt with the raw result; it must not throw.
 */
internal object RetryLoop {
    fun <T> execute(
        request: () -> HttpResult,
        decode: (String) -> T?,
        fallback: () -> T,
        sleep: (Long) -> Unit = { Thread.sleep(it) },
        observe: ((attempt: Int, result: HttpResult) -> Unit)? = null,
    ): T {
        var attempt = 1
        while (true) {
            val result = request()
            try { observe?.invoke(attempt, result) } catch (_: Exception) { }
            when (result) {
                is HttpResult.Success -> return decode(result.body) ?: fallback()
                is HttpResult.Failure -> {
                    if (!RetryPolicy.shouldRetry(attempt, result.status)) return fallback()
                    sleep(RetryPolicy.backoffMillis(attempt))
                    attempt += 1
                }
            }
        }
    }
}
