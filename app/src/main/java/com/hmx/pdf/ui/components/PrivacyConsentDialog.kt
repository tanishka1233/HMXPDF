package com.hmx.pdf.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmx.pdf.R
import com.hmx.pdf.ui.theme.LiquidGlassColors
import com.hmx.pdf.ui.theme.LocalIsDarkMode
import com.kyant.backdrop.Backdrop

/**
 * One-time privacy and feedback choices, shown over the live Home screen after onboarding.
 *
 * Both switches start from the stored consent values (off on a fresh install), and are independent:
 * toggling one never changes the other. "Not now", back and scrim dismissal all leave consent
 * untouched. The optional privacy-policy link is only rendered when an approved URL is supplied.
 */
@Composable
fun PrivacyConsentDialog(
    visible: Boolean,
    initialDiagnostics: Boolean,
    initialAnalytics: Boolean,
    backdrop: Backdrop,
    onDismiss: () -> Unit,
    onContinue: (diagnostics: Boolean, analytics: Boolean) -> Unit,
    approvedPrivacyPolicyUrl: String? = null,
) {
    val isDark = LocalIsDarkMode.current
    val ink = glassDialogInk()
    val soft = glassDialogInkSoft()
    val uriHandler = LocalUriHandler.current
    var diagnostics by remember(visible, initialDiagnostics) { mutableStateOf(initialDiagnostics) }
    var analytics by remember(visible, initialAnalytics) { mutableStateOf(initialAnalytics) }

    GlassDialog(
        visible = visible,
        onDismiss = onDismiss,
        backdrop = backdrop,
        title = stringResource(R.string.privacy_prompt_title),
        dismissOnScrimTap = false,
        actions = {
            GlassDialogAction(stringResource(R.string.privacy_prompt_not_now), onClick = onDismiss)
            GlassDialogAction(
                stringResource(R.string.privacy_prompt_continue),
                onClick = { onContinue(diagnostics, analytics) },
                primary = true,
            )
        },
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BasicText(
                stringResource(R.string.privacy_prompt_intro),
                style = TextStyle(soft, 14.sp, lineHeight = 20.sp),
            )
            PrivacyChoiceRow(
                title = stringResource(R.string.privacy_prompt_diagnostics_title),
                body = stringResource(R.string.privacy_prompt_diagnostics_body),
                checked = diagnostics,
                onCheckedChange = { diagnostics = it },
                ink = ink,
                soft = soft,
                isDark = isDark,
            )
            PrivacyChoiceRow(
                title = stringResource(R.string.privacy_prompt_analytics_title),
                body = stringResource(R.string.privacy_prompt_analytics_body),
                checked = analytics,
                onCheckedChange = { analytics = it },
                ink = ink,
                soft = soft,
                isDark = isDark,
            )
            BasicText(
                stringResource(R.string.privacy_prompt_footnote),
                style = TextStyle(soft.copy(alpha = 0.9f), 12.sp, lineHeight = 17.sp),
            )
            if (!approvedPrivacyPolicyUrl.isNullOrBlank()) {
                BasicText(
                    stringResource(R.string.privacy_prompt_policy),
                    modifier = Modifier
                        .clickable { uriHandler.openUri(approvedPrivacyPolicyUrl) }
                        .padding(vertical = 4.dp),
                    style = TextStyle(
                        LiquidGlassColors.Blue,
                        13.sp,
                        FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PrivacyChoiceRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    ink: Color,
    soft: Color,
    isDark: Boolean,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            Modifier
                .weight(1f)
                .clickable { onCheckedChange(!checked) }
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            BasicText(title, style = TextStyle(ink, 14.sp, FontWeight.SemiBold))
            BasicText(body, style = TextStyle(soft, 12.sp, lineHeight = 17.sp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
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
}
