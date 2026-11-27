package com.hmx.pdf.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmx.pdf.analytics.Analytics
import com.hmx.pdf.ui.theme.LiquidGlassColors
import com.hmx.pdf.ui.theme.LocalIsDarkMode

/** Privacy explanation and explicit consent. Completely separate from diagnostics consent. */
@Composable
fun AnalyticsSettingsSection(modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkMode.current
    val ink = LiquidGlassColors.text(isDark)
    val sub = LiquidGlassColors.secondary(isDark)
    var enabled by remember { mutableStateOf(Analytics.hasConsent()) }
    var showExplanation by remember { mutableStateOf(false) }
    var confirmEnable by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Analytics are OFF. No usage data is collected or sent.") }
    var busy by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Enable privacy-first analytics",
                modifier = Modifier.weight(1f),
                color = ink,
                fontSize = 14.sp,
            )
            Switch(
                checked = enabled,
                onCheckedChange = { value ->
                    if (!value) {
                        enabled = false
                        confirmEnable = false
                        busy = true
                        status = "Turning analytics off and requesting deletion…"
                        Analytics.disable { outcome ->
                            busy = false
                            status = if (outcome.ok) {
                                "Analytics are OFF. Server deletion verified: ${outcome.verifiedDeleted} events removed."
                            } else {
                                "Analytics are OFF and local data is deleted. Server deletion could not be verified; it will be retried."
                            }
                        }
                    } else {
                        confirmEnable = true
                        status = "Review what is collected before enabling analytics."
                    }
                },
                enabled = !busy,
                colors = analyticsSwitchColors(isDark, ink),
            )
        }

        TextButton(
            onClick = { showExplanation = !showExplanation },
            colors = ButtonDefaults.textButtonColors(contentColor = LiquidGlassColors.Blue),
        ) {
            Text(if (showExplanation) "Hide privacy explanation" else "View privacy explanation")
        }

        if (showExplanation || confirmEnable) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Collected only while analytics are ON:", color = ink, fontSize = 13.sp)
                Text(
                    "• App opens and session start/end times\n" +
                        "• Which allowlisted feature was used (PDF open/edit/export/merge/split/compress, DOCX open/edit/export, scan, OCR, signature, URL-to-PDF)\n" +
                        "• App version, Android major version, coarse error category, event time, random session and installation identifiers\n" +
                        "• Crash and operation-failure counts without messages or stack traces",
                    color = sub,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
                Text("Never collected:", color = ink, fontSize = 13.sp)
                Text(
                    "• PDF/DOCX contents, names, paths, URIs, page text or OCR output\n" +
                        "• Device model, serial, Android ID, advertising ID, location, contacts, clipboard\n" +
                        "• Email, phone, passwords, auth tokens, raw network bodies\n" +
                        "• Any diagnostic report data or document content",
                    color = sub,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
                Text(
                    "Why: first-party aggregate counts help prioritise features and reliability work. " +
                        "Opt out any time here; that immediately stops collection, clears queued events and " +
                        "requests deletion of server-side analytics data. Metrics are opted-in installation " +
                        "counts, not total HMXPDF users. Raw events are kept 90 days; aggregates 400 days.",
                    color = sub,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        }

        if (confirmEnable && !enabled) {
            Button(
                onClick = {
                    confirmEnable = false
                    busy = true
                    status = "Enabling analytics…"
                    Analytics.enable { ok ->
                        busy = false
                        enabled = true
                        status = if (ok) {
                            "Analytics are ON. A fresh pseudonymous analytics ID was created for this install."
                        } else {
                            "Analytics are ON locally. Registration could not be confirmed; no data is uploaded until it succeeds."
                        }
                    }
                },
                colors = analyticsPrimaryButtonColors(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("I understand — enable analytics") }
            TextButton(
                onClick = { confirmEnable = false; showExplanation = false },
                colors = ButtonDefaults.textButtonColors(contentColor = LiquidGlassColors.Blue),
            ) {
                Text("Cancel")
            }
        }

        if (enabled || status.contains("verified") || status.contains("retried")) {
            TextButton(
                onClick = { confirmDelete = !confirmDelete },
                colors = ButtonDefaults.textButtonColors(contentColor = LiquidGlassColors.Blue),
            ) {
                Text("Request deletion of analytics data")
            }
            if (confirmDelete) {
                Button(
                    onClick = {
                        confirmDelete = false
                        busy = true
                        status = "Deleting analytics data…"
                        Analytics.disable { outcome ->
                            busy = false
                            enabled = false
                            status = if (outcome.ok) {
                                "Deletion verified: ${outcome.verifiedDeleted} events removed. Analytics are OFF."
                            } else {
                                "Local analytics data deleted. Server deletion is pending and will be retried."
                            }
                        }
                    },
                    colors = analyticsPrimaryButtonColors(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Confirm deletion and opt out") }
            }
        }

        Text(status, color = sub, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun analyticsSwitchColors(isDark: Boolean, ink: Color) = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = LiquidGlassColors.Blue,
    checkedBorderColor = LiquidGlassColors.Blue,
    uncheckedThumbColor = if (isDark) Color(0xFFD1D1D6) else Color(0xFF5E6068),
    uncheckedTrackColor = ink.copy(alpha = 0.14f),
    uncheckedBorderColor = ink.copy(alpha = 0.30f),
)

@Composable
private fun analyticsPrimaryButtonColors() = ButtonDefaults.buttonColors(
    containerColor = LiquidGlassColors.Blue,
    contentColor = Color.White,
    disabledContainerColor = LiquidGlassColors.Blue.copy(alpha = 0.32f),
    disabledContentColor = Color.White.copy(alpha = 0.72f),
)
