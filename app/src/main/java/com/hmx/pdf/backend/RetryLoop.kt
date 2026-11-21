package com.hmx.pdf.backend

/**
 * Shared bounded-retry loop for the two staging calls.
 *
 * Retries only transport failures, 429 and 5xx. A success with an unusable body
 * is not retried: the server answered, and retrying would not change the answer.
 * [sleep] is injectable so JVM tests do not wait.
 */
internal object RetryLoop {
    fun <T> execute(
        request: () -> HttpResult,
        decode: (String) -> T?,
        fallback: () -> T,
        sleep: (Long) -> Unit = { Thread.sleep(it) },
    ): T {
        var attempt = 1
        while (true) {
            when (val result = request()) {
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
