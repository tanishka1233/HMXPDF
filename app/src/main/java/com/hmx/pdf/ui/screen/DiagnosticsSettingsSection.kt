package com.hmx.pdf.ui.screen

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmx.pdf.backend.DiagnosticsRepository
import com.hmx.pdf.backend.FeatureFlags
import com.hmx.pdf.backend.RemoteConfigSnapshot
import com.hmx.pdf.backend.RemoteFlags
import com.hmx.pdf.backend.ReportDraft
import com.hmx.pdf.backend.ReportRequestBuilder
import com.hmx.pdf.ui.theme.LiquidGlassColors
import com.hmx.pdf.ui.theme.LocalIsDarkMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/** Staging-only diagnostics consent UI. Compiled in every variant but inert when disabled. */
@Composable
fun DiagnosticsSettingsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isDark = LocalIsDarkMode.current
    val ink = LiquidGlassColors.text(isDark)
    val sub = LiquidGlassColors.secondary(isDark)
    val fieldSurface = LiquidGlassColors.neutralSurface(isDark)
    val diagnostics = remember(context) { DiagnosticsRepository(context) }
    var consent by remember { mutableStateOf(diagnostics.consentEnabled) }
    var snapshot by remember { mutableStateOf<RemoteConfigSnapshot?>(null) }
    var reportType by remember { mutableStateOf(ReportRequestBuilder.ALLOWED_TYPES.first()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Diagnostics are off. Nothing is sent.") }
    var busy by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        snapshot = withContext(Dispatchers.IO) { RemoteFlags.refresh(context) }
            ?: RemoteFlags.current()
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Enable diagnostics",
                modifier = Modifier.weight(1f),
                color = ink,
                fontSize = 14.sp,
            )
            Switch(
                checked = consent,
                onCheckedChange = { value ->
                    consent = value
                    diagnostics.setConsent(value)
                    status = if (value) {
                        "Off by default. Each report needs a separate confirmation."
                    } else {
                        "Diagnostics are off. Nothing is sent."
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = LiquidGlassColors.Blue,
                    checkedBorderColor = LiquidGlassColors.Blue,
                    uncheckedThumbColor = if (isDark) Color(0xFFD1D1D6) else Color(0xFF5E6068),
                    uncheckedTrackColor = ink.copy(alpha = 0.14f),
                    uncheckedBorderColor = ink.copy(alpha = 0.30f),
                ),
            )
        }
        Text(
            if (snapshot == null) "Published configuration: unavailable (using built-in defaults)"
            else if (!snapshot!!.compatible) "Published configuration: v${snapshot!!.version} (update required)"
            else "Published configuration: v${snapshot!!.version}",
            color = sub,
            fontSize = 12.sp,
        )
        val announcement = snapshot?.config?.announcement
        if (announcement != null && announcement.enabled && announcement.message.isNotBlank()) {
            Text("Announcement: ${announcement.message}", color = sub, fontSize = 12.sp)
        }
        val flags = listOf(
            FeatureFlags.URL_TO_PDF,
            FeatureFlags.OFFICE_ENGINE,
            FeatureFlags.OCR,
            FeatureFlags.IMAGE_EDITOR,
            FeatureFlags.DARK_PAGES,
            FeatureFlags.ANNOTATIONS,
            FeatureFlags.FORM_FILL,
        )
        Text(
            "Remote feature flags: " + flags.joinToString { flag ->
                "$flag=" + if (RemoteFlags.isEnabled(flag)) "on" else "off"
            },
            color = sub,
            fontSize = 12.sp,
        )
        Text(
            "Each confirmed report includes: install id (report request only), app version, " +
                "your title and description, Android API level and locale. " +
                "No PDF/DOCX content, device model or location is collected.",
            color = sub,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
        OutlinedTextField(
            value = reportType,
            onValueChange = { reportType = it; confirm = false },
            label = { Text("Report type (bug, crash, performance, ui, other)") },
            singleLine = true,
            textStyle = TextStyle(color = ink, fontSize = 14.sp),
            colors = diagnosticsFieldColors(isDark, ink, sub, fieldSurface),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it; confirm = false },
            label = { Text("Title") },
            singleLine = true,
            textStyle = TextStyle(color = ink, fontSize = 14.sp),
            colors = diagnosticsFieldColors(isDark, ink, sub, fieldSurface),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it; confirm = false },
            label = { Text("Description (optional)") },
            textStyle = TextStyle(color = ink, fontSize = 14.sp),
            colors = diagnosticsFieldColors(isDark, ink, sub, fieldSurface),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                if (!confirm) {
                    confirm = true
                    status = "Review the fields above, then confirm to send."
                } else {
                    confirm = false
                    busy = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            diagnostics.submit(
                                ReportDraft(
                                    reportType = reportType.trim(),
                                    title = title.trim(),
                                    description = description.trim(),
                                    androidApiLevel = Build.VERSION.SDK_INT,
                                    locale = Locale.getDefault().toString(),
                                ),
                            )
                        }
                        status = result.message
                        busy = false
                    }
                }
            },
            enabled = consent && !busy && title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = LiquidGlassColors.Blue,
                contentColor = Color.White,
                disabledContainerColor = LiquidGlassColors.Blue.copy(alpha = 0.32f),
                disabledContentColor = Color.White.copy(alpha = 0.72f),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (busy) "Sending…" else if (confirm) "Confirm and send" else "Review report")
        }
        if (busy) CircularProgressIndicator(color = LiquidGlassColors.Blue)
        Text(status, color = sub, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun diagnosticsFieldColors(
    isDark: Boolean,
    ink: Color,
    sub: Color,
    fieldSurface: Color,
) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ink,
    unfocusedTextColor = ink,
    focusedContainerColor = fieldSurface,
    unfocusedContainerColor = fieldSurface,
    cursorColor = LiquidGlassColors.Blue,
    focusedBorderColor = LiquidGlassColors.Blue,
    unfocusedBorderColor = ink.copy(alpha = if (isDark) 0.28f else 0.20f),
    focusedLabelColor = LiquidGlassColors.Blue,
    unfocusedLabelColor = sub,
)
