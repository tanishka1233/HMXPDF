package com.hmx.pdf.analytics

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * All analytics decisions live here. The engine is deliberately independent of
 * Android Context so consent, queueing and opt-out can be verified by JVM tests.
 */
class AnalyticsEngine(
    private val storage: AnalyticsStorage,
    private val transport: AnalyticsTransport,
    private val enabled: Boolean,
    private val app: AppMeta,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    data class AppMeta(val version: String, val code: Int, val androidMajor: Int)
    data class RevokeOutcome(val ok: Boolean, val pending: Boolean, val verifiedDeleted: Int = 0)
    sealed class Flush {
        object Disabled : Flush()
        object NoEvents : Flush()
        object RegistrationPending : Flush()
        object Failed : Flush()
        data class Sent(val events: Int) : Flush()
    }

    private val appOpened = AtomicBoolean(false)

    // A broken transport must never propagate into a document operation.
    private fun safeRegister(id: String): Boolean = try {
        transport.register(id, app.version, app.code, app.androidMajor)
    } catch (t: Throwable) { false }
    private fun safeIngest(id: String, payload: String): Boolean = try {
        transport.ingest(id, payload)
    } catch (t: Throwable) { false }
    private fun safeRevoke(id: String, action: String): RevokeVerification = try {
        transport.revoke(id, action)
    } catch (t: Throwable) { RevokeVerification(false) }

    fun canCollect(): Boolean = enabled && storage.consent()
    fun consent(): Boolean = storage.consent()
    fun queueSize(): Int = storage.queue().size

    fun newEvent(name: String, feature: String? = null, errorCategory: String? = null): AnalyticsEvent? {
        if (!canCollect() || name !in AnalyticsTaxonomy.EVENTS) return null
        val errorOk = errorCategory == null ||
            (name == "app_crash" || name == "operation_failed") &&
            errorCategory in AnalyticsTaxonomy.ERROR_CATEGORIES
        if (!errorOk) return null
        return AnalyticsEvent(
            eventId = UUID.randomUUID().toString(),
            name = name,
            occurredAt = clock(),
            sessionId = storage.sessionId(),
            feature = AnalyticsTaxonomy.featureFor(name, feature),
            errorCategory = errorCategory,
            appVersion = app.version,
            appVersionCode = app.code,
            androidMajor = app.androidMajor,
        )
    }

    fun track(name: String, feature: String? = null, errorCategory: String? = null): Boolean {
        val event = newEvent(name, feature, errorCategory) ?: return false
        val now = clock()
        storage.saveQueue(AnalyticsQueue.append(storage.queue(), event, now))
        return true
    }

    /** Explicit consent. The identifier is generated here and never before. */
    fun grantConsent(): Boolean {
        if (!enabled) return false
        storage.setConsent(true)
        val existing = storage.installId()
        val id = if (AnalyticsTaxonomy.validInstallId(existing)) {
            existing!!
        } else {
            val generated = "hmxa-${UUID.randomUUID()}"
            storage.setInstallId(generated)
            generated
        }
        val ok = safeRegister(id)
        storage.setRegistered(ok)
        return ok
    }

    /**
     * Opt-out: stop collection and discard the queue immediately. The server
     * deletion is only reported as verified when the endpoint confirms it.
     */
    fun revoke(action: String = "delete"): RevokeOutcome {
        val id = storage.installId()
        storage.setConsent(false)
        storage.saveQueue(emptyList())
        storage.setSessionId(null)
        storage.setRegistered(false)
        if (!AnalyticsTaxonomy.validInstallId(id)) {
            storage.setPendingDeletionId(null)
            return RevokeOutcome(ok = true, pending = false)
        }
        val verification = safeRevoke(id!!, action)
        return if (verification.verified) {
            storage.setInstallId(null)
            storage.setPendingDeletionId(null)
            RevokeOutcome(ok = true, pending = false, verification.deletedEvents)
        } else {
            storage.setPendingDeletionId(id)
            RevokeOutcome(ok = false, pending = true)
        }
    }

    /** Retries a deletion that could not be verified when the user opted out. */
    fun retryPendingDeletion() {
        val pending = storage.pendingDeletionId() ?: return
        if (!AnalyticsTaxonomy.validInstallId(pending)) {
            storage.setPendingDeletionId(null)
            return
        }
        if (safeRevoke(pending, "delete").verified) {
            storage.setInstallId(null)
            storage.setPendingDeletionId(null)
        }
    }

    fun flush(): Flush {
        if (!canCollect()) return Flush.Disabled
        val id = storage.installId() ?: return Flush.Disabled
        if (!storage.registered()) {
            if (!safeRegister(id)) return Flush.RegistrationPending
            storage.setRegistered(true)
        }
        val queue = storage.queue()
        val batch = AnalyticsQueue.takeBatch(queue)
        if (batch.isEmpty()) return Flush.NoEvents
        val payload = try {
            AnalyticsPayload.build(id, batch)
        } catch (e: IllegalArgumentException) {
            storage.saveQueue(emptyList())
            return Flush.Failed
        }
        return if (safeIngest(id, payload)) {
            storage.saveQueue(AnalyticsQueue.removeAccepted(queue, batch.map { it.eventId }.toSet()))
            Flush.Sent(batch.size)
        } else Flush.Failed
    }

    fun onForeground() {
        if (!canCollect()) return
        if (appOpened.compareAndSet(false, true)) track("app_open")
        val now = clock()
        if (AnalyticsQueue.shouldStartSession(storage.lastActivityAt(), now)) {
            storage.setSessionId(UUID.randomUUID().toString())
            track("session_start")
        }
        storage.setLastActivityAt(now)
    }

    fun onBackground() {
        if (!canCollect()) return
        if (storage.sessionId() != null) track("session_end")
        storage.setSessionId(null)
        storage.setLastActivityAt(clock())
    }

    /** Crash path: synchronous, no network, no stack trace, never throws. */
    fun recordCrashNow() {
        try {
            if (!canCollect()) return
            val event = newEvent("app_crash") ?: return
            storage.saveQueue(AnalyticsQueue.append(storage.queue(), event, clock()), immediate = true)
        } catch (t: Throwable) {
            // Analytics must never change crash behaviour.
        }
    }
}
