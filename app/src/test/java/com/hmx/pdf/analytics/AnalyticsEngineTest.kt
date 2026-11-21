package com.hmx.pdf.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsEngineTest {
    private val app = AnalyticsEngine.AppMeta("2.1.1", 21, 34)
    private var now = 1_700_000_000_000L

    private fun engine(
        storage: AnalyticsStorage = MemoryAnalyticsStorage(),
        transport: FakeAnalyticsTransport = FakeAnalyticsTransport(),
        enabled: Boolean = true,
    ) = Triple(AnalyticsEngine(storage, transport, enabled, app) { now }, storage, transport)

    @Test
    fun consentDefaultsOffAndNoEventBeforeOptIn() {
        val (engine, storage, transport) = engine()
        assertFalse(storage.consent())
        assertFalse(engine.canCollect())
        assertFalse(engine.track("pdf_open", "pdf"))
        assertTrue(storage.queue().isEmpty())
        assertTrue(transport.registered.isEmpty())
        assertTrue(transport.batches.isEmpty())
    }

    @Test
    fun disabledBuildNeverCollects() {
        val (engine, storage, _) = engine(enabled = false)
        assertFalse(engine.grantConsent())
        assertNull(storage.installId())
        assertFalse(engine.track("pdf_open", "pdf"))
    }

    @Test
    fun consentCreatesDedicatedIdentityAndRegisters() {
        val (engine, storage, transport) = engine()
        assertTrue(engine.grantConsent())
        val id = storage.installId()
        assertTrue(id!!.startsWith("hmxa-"))
        assertNotEquals("hmx-diagnostic-install-id", id)
        assertEquals(listOf(id), transport.registered)
    }

    @Test
    fun eventsAreCollectedOnlyAfterConsent() {
        val (engine, storage, _) = engine()
        engine.grantConsent()
        assertTrue(engine.track("pdf_open", "pdf"))
        val event = storage.queue().single()
        assertEquals("pdf_open", event.name)
        assertEquals("pdf", event.feature)
        assertEquals(21, event.appVersionCode)
        assertNull(event.errorCategory)
    }

    @Test
    fun batchIsCappedAndSecondFlushSendsRemainder() {
        val (engine, _, transport) = engine()
        engine.grantConsent()
        repeat(60) { engine.track("pdf_open", "pdf") }
        assertEquals(AnalyticsEngine.Flush.Sent(50), engine.flush())
        assertEquals(50, Regex("\"event_id\"").findAll(transport.batches[0]).count())
        assertEquals(AnalyticsEngine.Flush.Sent(10), engine.flush())
        assertEquals(AnalyticsEngine.Flush.NoEvents, engine.flush())
    }

    @Test
    fun networkFailureKeepsQueueUntilSuccess() {
        val (engine, storage, transport) = engine()
        engine.grantConsent()
        engine.track("docx_export", "docx")
        transport.ingestOk = false
        assertEquals(AnalyticsEngine.Flush.Failed, engine.flush())
        assertEquals(1, storage.queue().size)
        transport.ingestOk = true
        assertEquals(AnalyticsEngine.Flush.Sent(1), engine.flush())
        assertTrue(storage.queue().isEmpty())
    }

    @Test
    fun registrationFailurePreventsUploadButKeepsEvents() {
        val (engine, storage, transport) = engine()
        transport.registerOk = false
        engine.grantConsent()
        engine.track("ocr_use", "ocr")
        assertEquals(AnalyticsEngine.Flush.RegistrationPending, engine.flush())
        assertTrue(transport.batches.isEmpty())
        assertEquals(1, storage.queue().size)
        transport.registerOk = true
        assertEquals(AnalyticsEngine.Flush.Sent(1), engine.flush())
    }

    @Test
    fun optOutClearsQueueAndDeletesIdentity() {
        val (engine, storage, transport) = engine()
        engine.grantConsent()
        val id = storage.installId()!!
        engine.track("pdf_merge", "pdf")
        val outcome = engine.revoke()
        assertTrue(outcome.ok)
        assertFalse(outcome.pending)
        assertFalse(storage.consent())
        assertTrue(storage.queue().isEmpty())
        assertNull(storage.installId())
        assertEquals(listOf(id), transport.revoked)
        assertFalse(engine.track("pdf_open", "pdf"))
    }

    @Test
    fun unverifiedDeletionStaysPendingAndDoesNotClaimSuccess() {
        val (engine, storage, transport) = engine()
        engine.grantConsent()
        val id = storage.installId()!!
        transport.revokeVerified = false
        val outcome = engine.revoke()
        assertFalse(outcome.ok)
        assertTrue(outcome.pending)
        assertEquals(id, storage.pendingDeletionId())
        // The ID is retained only for the pending deletion retry; collection stays off.
        assertEquals(id, storage.installId())
        assertTrue(storage.queue().isEmpty())
        transport.revokeVerified = true
        engine.retryPendingDeletion()
        assertNull(storage.pendingDeletionId())
        assertNull(storage.installId())
    }

    @Test
    fun sessionsStartAfterThirtyMinutesAndEndOnBackground() {
        val (engine, storage, _) = engine()
        engine.grantConsent()
        engine.onForeground()
        assertEquals(1, storage.queue().count { it.name == "app_open" })
        assertEquals(1, storage.queue().count { it.name == "session_start" })
        now += 5 * 60 * 1000
        engine.onBackground()
        assertEquals(1, storage.queue().count { it.name == "session_end" })
        engine.onForeground()
        assertEquals(1, storage.queue().count { it.name == "session_start" })
        now += 31 * 60 * 1000
        engine.onForeground()
        assertEquals(2, storage.queue().count { it.name == "session_start" })
    }

    @Test
    fun queueIsBoundedAndDropsStaleEvents() {
        val (engine, storage, _) = engine()
        engine.grantConsent()
        repeat(250) { engine.track("app_open") }
        assertEquals(ANALYTICS_MAX_QUEUE, storage.queue().size)
        now += ANALYTICS_MAX_EVENT_AGE_MS + 1
        engine.track("app_open")
        assertEquals(1, storage.queue().size)
    }

    @Test
    fun crashRecordingIsSafeAndStoresNoMessage() {
        val (engine, storage, _) = engine()
        engine.grantConsent()
        engine.recordCrashNow()
        val crash = storage.queue().single()
        assertEquals("app_crash", crash.name)
        assertNull(crash.errorCategory)
        assertFalse(AnalyticsPayload.encodeQueue(storage.queue()).contains("Exception"))
    }

    @Test
    fun transportExceptionNeverPropagates() {
        val transport = object : AnalyticsTransport {
            override fun register(i: String, v: String, c: Int, a: Int): Boolean = throw RuntimeException("offline")
            override fun ingest(i: String, p: String): Boolean = throw RuntimeException("offline")
            override fun revoke(i: String, a: String): RevokeVerification = throw RuntimeException("offline")
        }
        val storage = MemoryAnalyticsStorage()
        val engine = AnalyticsEngine(storage, transport, true, app)
        engine.grantConsent()
        engine.track("pdf_open", "pdf")
        assertEquals(AnalyticsEngine.Flush.RegistrationPending, engine.flush())
        assertFalse(engine.revoke().ok)
        assertTrue(storage.queue().isEmpty()) // opt-out clears locally even when deletion is unverified
    }
}
