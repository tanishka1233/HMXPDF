package com.hmx.pdf.analytics

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.hmx.pdf.backend.AppInfo
import com.hmx.pdf.backend.BackendConfig
import java.util.concurrent.Executors

/**
 * Android entry point. Inert in debug/release builds. All work is executed on a
 * single daemon thread; no timer, no WorkManager, no continuous background work.
 */
object Analytics {
    @Volatile
    private var engine: AnalyticsEngine? = null
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "hmx-analytics").apply { isDaemon = true }
    }
    private val mainHandler = Handler(Looper.getMainLooper())
    private var crashHandlerInstalled = false

    fun initialize(context: Context) {
        if (!BackendConfig.enabled) return
        val app = context.applicationContext
        val storage = SharedPreferencesAnalyticsStorage(app)
        val engine = AnalyticsEngine(
            storage = storage,
            transport = HttpAnalyticsTransport(),
            enabled = true,
            app = AnalyticsEngine.AppMeta(
                version = AppInfo.versionName(app),
                code = AppInfo.versionCode(app),
                androidMajor = Build.VERSION.SDK_INT,
            ),
        )
        this.engine = engine
        executor.execute { engine.retryPendingDeletion() }
        installCrashHandler(engine)
    }

    fun isEnabled(): Boolean = engine?.canCollect() ?: false
    fun hasConsent(): Boolean = engine?.consent() ?: false

    fun track(name: String, feature: String? = null, errorCategory: String? = null) {
        val current = engine ?: return
        executor.execute {
            current.track(name, feature, errorCategory)
            if (current.queueSize() >= 10) current.flush()
        }
    }

    fun onForeground() {
        val current = engine ?: return
        executor.execute { current.onForeground() }
    }

    fun onBackground() {
        val current = engine ?: return
        executor.execute { current.onBackground() }
    }

    fun flush() {
        val current = engine ?: return
        executor.execute { current.flush() }
    }

    fun enable(onResult: (Boolean) -> Unit) {
        val current = engine
        if (current == null) {
            mainHandler.post { onResult(false) }
            return
        }
        executor.execute {
            val ok = current.grantConsent()
            mainHandler.post { onResult(ok) }
        }
    }

    fun disable(onResult: (AnalyticsEngine.RevokeOutcome) -> Unit) {
        val current = engine
        if (current == null) {
            mainHandler.post { onResult(AnalyticsEngine.RevokeOutcome(ok = false, pending = false)) }
            return
        }
        executor.execute {
            val outcome = current.revoke()
            mainHandler.post { onResult(outcome) }
        }
    }

    @Synchronized
    private fun installCrashHandler(engine: AnalyticsEngine) {
        if (crashHandlerInstalled) return
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try { engine.recordCrashNow() } catch (_: Throwable) {}
            if (previous != null) previous.uncaughtException(thread, throwable)
            else System.exit(1)
        }
        crashHandlerInstalled = true
    }
}
