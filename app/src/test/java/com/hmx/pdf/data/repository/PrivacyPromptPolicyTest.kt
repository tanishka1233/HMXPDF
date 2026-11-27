package com.hmx.pdf.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPromptPolicyTest {

    @Test
    fun `both choices default to off`() {
        val choices = PrivacyChoices()
        assertFalse(choices.diagnostics)
        assertFalse(choices.analytics)
    }

    @Test
    fun `prompt shows once on home after onboarding in an enabled build`() {
        assertTrue(
            PrivacyPromptPolicy.shouldShow(
                backendEnabled = true,
                onboardingCompleted = true,
                promptCompleted = false,
                documentWindow = false,
                route = PrivacyPromptPolicy.HOME_ROUTE,
                recoveryPending = false,
            )
        )
    }

    @Test
    fun `prompt never shows before onboarding completes`() {
        assertFalse(show(onboardingCompleted = false))
    }

    @Test
    fun `prompt never shows twice per install`() {
        assertFalse(show(promptCompleted = true))
    }

    @Test
    fun `prompt does not show in disabled production builds`() {
        assertFalse(show(backendEnabled = false))
    }

    @Test
    fun `prompt does not cover a document window`() {
        assertFalse(show(documentWindow = true))
    }

    @Test
    fun `prompt only appears on home`() {
        assertFalse(show(route = "tools"))
        assertFalse(show(route = "settings"))
        assertFalse(show(route = "onboarding"))
        assertFalse(show(route = null))
    }

    @Test
    fun `prompt yields to an interrupted save recovery prompt`() {
        assertFalse(show(recoveryPending = true))
    }

    @Test
    fun `continue with both off changes nothing`() {
        val changes = PrivacyPromptPolicy.changes(
            choices = PrivacyChoices(diagnostics = false, analytics = false),
            currentDiagnostics = false,
            currentAnalytics = false,
        )
        assertNull(changes.diagnostics)
        assertNull(changes.analytics)
    }

    @Test
    fun `choices are independent`() {
        val diagnosticsOnly = PrivacyPromptPolicy.changes(
            choices = PrivacyChoices(diagnostics = true, analytics = false),
            currentDiagnostics = false,
            currentAnalytics = false,
        )
        assertEquals(true, diagnosticsOnly.diagnostics)
        assertNull(diagnosticsOnly.analytics)

        val analyticsOnly = PrivacyPromptPolicy.changes(
            choices = PrivacyChoices(diagnostics = false, analytics = true),
            currentDiagnostics = false,
            currentAnalytics = false,
        )
        assertNull(analyticsOnly.diagnostics)
        assertEquals(true, analyticsOnly.analytics)
    }

    @Test
    fun `unchanged stored consent is left alone`() {
        val changes = PrivacyPromptPolicy.changes(
            choices = PrivacyChoices(diagnostics = true, analytics = true),
            currentDiagnostics = true,
            currentAnalytics = true,
        )
        assertNull(changes.diagnostics)
        assertNull(changes.analytics)
    }

    @Test
    fun `explicitly turning a stored choice off is reported separately`() {
        val changes = PrivacyPromptPolicy.changes(
            choices = PrivacyChoices(diagnostics = false, analytics = false),
            currentDiagnostics = true,
            currentAnalytics = true,
        )
        assertEquals(false, changes.diagnostics)
        assertEquals(false, changes.analytics)
    }

    private fun show(
        backendEnabled: Boolean = true,
        onboardingCompleted: Boolean = true,
        promptCompleted: Boolean = false,
        documentWindow: Boolean = false,
        route: String? = PrivacyPromptPolicy.HOME_ROUTE,
        recoveryPending: Boolean = false,
    ) = PrivacyPromptPolicy.shouldShow(
        backendEnabled = backendEnabled,
        onboardingCompleted = onboardingCompleted,
        promptCompleted = promptCompleted,
        documentWindow = documentWindow,
        route = route,
        recoveryPending = recoveryPending,
    )
}
