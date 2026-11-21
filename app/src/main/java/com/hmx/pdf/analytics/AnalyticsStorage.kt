package com.hmx.pdf.analytics

import android.content.Context

/** Local analytics state. Separate prefs file; never shares diagnostic consent or identity. */
interface AnalyticsStorage {
    fun consent(): Boolean
    fun setConsent(value: Boolean)
    fun installId(): String?
    fun setInstallId(value: String?)
    fun registered(): Boolean
    fun setRegistered(value: Boolean)
    fun pendingDeletionId(): String?
    fun setPendingDeletionId(value: String?)
    fun queue(): List<AnalyticsEvent>
    fun saveQueue(events: List<AnalyticsEvent>, immediate: Boolean = false)
    fun lastActivityAt(): Long?
    fun setLastActivityAt(value: Long)
    fun sessionId(): String?
    fun setSessionId(value: String?)
}

class SharedPreferencesAnalyticsStorage(context: Context) : AnalyticsStorage {
    private val prefs = context.applicationContext.getSharedPreferences("hmx_analytics", Context.MODE_PRIVATE)

    override fun consent(): Boolean = prefs.getBoolean(CONSENT, false)
    override fun setConsent(value: Boolean) = prefs.edit().putBoolean(CONSENT, value).apply().let { Unit }
    override fun installId(): String? = prefs.getString(INSTALL_ID, null)
    override fun setInstallId(value: String?) =
        prefs.edit().let { if (value == null) it.remove(INSTALL_ID) else it.putString(INSTALL_ID, value) }.apply().let { Unit }
    override fun registered(): Boolean = prefs.getBoolean(REGISTERED, false)
    override fun setRegistered(value: Boolean) = prefs.edit().putBoolean(REGISTERED, value).apply().let { Unit }
    override fun pendingDeletionId(): String? = prefs.getString(PENDING_DELETION, null)
    override fun setPendingDeletionId(value: String?) =
        prefs.edit().let { if (value == null) it.remove(PENDING_DELETION) else it.putString(PENDING_DELETION, value) }.apply().let { Unit }
    override fun queue(): List<AnalyticsEvent> = AnalyticsPayload.decodeQueue(prefs.getString(QUEUE, null))
    override fun saveQueue(events: List<AnalyticsEvent>, immediate: Boolean) {
        val edit = prefs.edit().putString(QUEUE, AnalyticsPayload.encodeQueue(events))
        if (immediate) edit.commit() else edit.apply()
    }
    override fun lastActivityAt(): Long? = prefs.getLong(LAST_ACTIVITY, 0L).takeIf { it > 0 }
    override fun setLastActivityAt(value: Long) = prefs.edit().putLong(LAST_ACTIVITY, value).apply().let { Unit }
    override fun sessionId(): String? = prefs.getString(SESSION_ID, null)
    override fun setSessionId(value: String?) =
        prefs.edit().let { if (value == null) it.remove(SESSION_ID) else it.putString(SESSION_ID, value) }.apply().let { Unit }

    private companion object {
        const val CONSENT = "analytics_enabled"
        const val INSTALL_ID = "analytics_install_id"
        const val REGISTERED = "analytics_registered"
        const val PENDING_DELETION = "analytics_pending_deletion"
        const val QUEUE = "analytics_queue"
        const val LAST_ACTIVITY = "analytics_last_activity"
        const val SESSION_ID = "analytics_session_id"
    }
}

/** In-memory storage for JVM tests. */
class MemoryAnalyticsStorage : AnalyticsStorage {
    private var consent = false
    private var installId: String? = null
    private var registered = false
    private var pending: String? = null
    private var events: List<AnalyticsEvent> = emptyList()
    private var lastActivity: Long? = null
    private var session: String? = null

    override fun consent(): Boolean = consent
    override fun setConsent(value: Boolean) { consent = value }
    override fun installId(): String? = installId
    override fun setInstallId(value: String?) { installId = value }
    override fun registered(): Boolean = registered
    override fun setRegistered(value: Boolean) { registered = value }
    override fun pendingDeletionId(): String? = pending
    override fun setPendingDeletionId(value: String?) { pending = value }
    override fun queue(): List<AnalyticsEvent> = events
    override fun saveQueue(events: List<AnalyticsEvent>, immediate: Boolean) { this.events = events }
    override fun lastActivityAt(): Long? = lastActivity
    override fun setLastActivityAt(value: Long) { lastActivity = value }
    override fun sessionId(): String? = session
    override fun setSessionId(value: String?) { session = value }
}
