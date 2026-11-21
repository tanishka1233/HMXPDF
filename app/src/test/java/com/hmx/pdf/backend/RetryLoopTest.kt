package com.hmx.pdf.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryLoopTest {
    private val sleeps = mutableListOf<Long>()

    private fun run(requests: List<HttpResult>, decodeFails: Boolean = false): Pair<String?, Int> {
        var calls = 0
        val value = RetryLoop.execute(
            request = { requests[calls++.coerceAtMost(requests.lastIndex)] },
            decode = { body -> if (decodeFails) null else body.uppercase() },
            fallback = { null },
            sleep = { sleeps.add(it) },
        )
        return value to calls
    }

    @Test
    fun succeedsAfterTransientFailures() {
        val (value, calls) = run(
            listOf(
                HttpResult.Failure(null, "offline"),
                HttpResult.Failure(503, "unavailable"),
                HttpResult.Success("ok"),
            ),
        )
        assertEquals("OK", value)
        assertEquals(3, calls)
        assertEquals(listOf(400L, 800L), sleeps)
    }

    @Test
    fun givesUpAfterThreeAttempts() {
        val (value, calls) = run(List(5) { HttpResult.Failure(500, "boom") })
        assertNull(value)
        assertEquals(3, calls)
        assertEquals(2, sleeps.size)
    }

    @Test
    fun doesNotRetryClientErrors() {
        val (value, calls) = run(listOf(HttpResult.Failure(400, "bad request"), HttpResult.Success("ok")))
        assertNull(value)
        assertEquals(1, calls)
        assertTrue(sleeps.isEmpty())
    }

    @Test
    fun rateLimitIsRetried() {
        val (value, calls) = run(listOf(HttpResult.Failure(429, "slow down"), HttpResult.Success("ok")))
        assertEquals("OK", value)
        assertEquals(2, calls)
    }

    @Test
    fun unparseableSuccessIsNotRetried() {
        val (value, calls) = run(listOf(HttpResult.Success("garbage")), decodeFails = true)
        assertNull(value)
        assertEquals(1, calls)
        assertTrue(sleeps.isEmpty())
    }

    @Test
    fun retryNeverExceedsPolicyAttempts() {
        val (_, calls) = run(List(10) { HttpResult.Failure(502, "bad gateway") })
        assertFalse(calls > RetryPolicy.MAX_ATTEMPTS)
    }
}
