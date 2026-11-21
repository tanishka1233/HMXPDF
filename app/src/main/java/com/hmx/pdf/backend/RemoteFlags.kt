package com.hmx.pdf.backend

import android.content.Context

/**
 * Process-wide view of the published remote configuration.
 *
 * Startup refreshes this once (staging builds only). Feature code asks
 * [isEnabled] and keeps its existing behaviour whenever no published
 * configuration is available, so an offline device is never degraded.
 */
object RemoteFlags {
    @Volatile
    private var snapshot: RemoteConfigSnapshot? = null

    @Volatile
    private var loadedFromDisk = false

    fun refresh(context: Context): RemoteConfigSnapshot? {
        if (!BackendConfig.enabled) return null
        val app = context.applicationContext
        val repository = RemoteConfigRepository(app)
        val fetched = repository.fetch(AppInfo.versionCode(app))
        if (fetched != null) {
            snapshot = fetched
            loadedFromDisk = true
            return fetched
        }
        if (!loadedFromDisk) {
            snapshot = repository.lastKnownGood()
            loadedFromDisk = true
        }
        return snapshot
    }

    fun current(): RemoteConfigSnapshot? = snapshot

    /** True when the flag is absent, no config exists, or the app must update first. */
    fun isEnabled(flag: String, default: Boolean = true): Boolean =
        FeatureFlags.isEnabled(snapshot, flag, default)

    /** Test hook: lets JVM tests inject a snapshot without Android storage. */
    fun setForTest(value: RemoteConfigSnapshot?) {
        snapshot = value
        loadedFromDisk = true
    }
}
