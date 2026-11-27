package com.hmx.pdf.data.repository

import android.content.Context

/**
 * First-launch privacy and feedback choices.
 *
 * Prompt completion lives in its own preferences file so it can never be confused with either
 * consent: dismissing the prompt must not grant or revoke diagnostics or analytics. Both choices
 * are independent and default to off.
 */
object PrivacyConsentManager {
    private const val PREFS_NAME = "hmx_privacy_prompt"
    private const val KEY_PROMPT_COMPLETED = "privacy_prompt_completed"
    private const val KEY_COMPLETED_AT = "privacy_prompt_completed_at"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasCompletedPrompt(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PROMPT_COMPLETED, false)

    fun markPromptCompleted(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_PROMPT_COMPLETED, true)
            .putLong(KEY_COMPLETED_AT, System.currentTimeMillis())
            .apply()
    }

    /** True while the one-time prompt still needs to be shown. Never reads either consent. */
    fun isPromptPending(context: Context): Boolean = !hasCompletedPrompt(context)
}

/** The two independent opt-ins shown by the first-launch prompt. Both default to off. */
data class PrivacyChoices(
    val diagnostics: Boolean = false,
    val analytics: Boolean = false,
)

/** Only the choices that actually differ from stored consent; null means "leave unchanged". */
data class PrivacyConsentChanges(
    val diagnostics: Boolean? = null,
    val analytics: Boolean? = null,
)

/**
 * Pure decision logic for the first-launch prompt, kept free of Android types so it can be tested
 * on the JVM. The prompt appears once per install, only on Home, and never on top of onboarding,
 * a document window, What's New or an interrupted-save recovery prompt.
 */
object PrivacyPromptPolicy {
    const val HOME_ROUTE = "home"

    fun shouldShow(
        backendEnabled: Boolean,
        onboardingCompleted: Boolean,
        promptCompleted: Boolean,
        documentWindow: Boolean,
        route: String?,
        recoveryPending: Boolean,
    ): Boolean =
        backendEnabled &&
            onboardingCompleted &&
            !promptCompleted &&
            !documentWindow &&
            route == HOME_ROUTE &&
            !recoveryPending

    fun changes(
        choices: PrivacyChoices,
        currentDiagnostics: Boolean,
        currentAnalytics: Boolean,
    ): PrivacyConsentChanges = PrivacyConsentChanges(
        diagnostics = choices.diagnostics.takeIf { it != currentDiagnostics },
        analytics = choices.analytics.takeIf { it != currentAnalytics },
    )
}
