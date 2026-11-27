package com.hmx.pdf.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmx.pdf.R
import com.hmx.pdf.ui.theme.LiquidGlassColors
import com.hmx.pdf.ui.theme.LocalIsDarkMode
import com.hmx.pdf.ui.utils.liquidPressGlow
import com.kyant.backdrop.Backdrop

/**
 * One-time privacy and feedback choices, shown over the live Home screen after onboarding.
 *
 * A member of the app's Liquid Glass dialog family ([GlassDialog], [GlassDialogAction],
 * [LiquidToggle], [AccentIconTile]): theme-aware ink on a translucent glass panel, tinted glass
 * icon tiles, glass switches, a pressed *glow* instead of a rectangular ripple, and a scrollable
 * body so it holds up on small screens and large font scales. No control layers a system ripple
 * over text, and informational text is never selectable, so tapping it can't leave a grey/black
 * selection rectangle.
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

    // Keep the action row reachable; scroll a long body rather than letting it push actions off.
    val maxBodyHeight = (LocalConfiguration.current.screenHeightDp * 0.52f).dp

    GlassDialog(
        visible = visible,
        onDismiss = onDismiss,
        backdrop = backdrop,
        title = stringResource(R.string.privacy_prompt_title),
        dismissOnScrimTap = false,
        panelShadow = true,
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
            Modifier
                .fillMaxWidth()
                .heightIn(max = maxBodyHeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Hero tile, same tinted icon-tile language as Settings rows.
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AccentIconTile(Icons.Rounded.Lock, LiquidGlassColors.Blue, size = 44, iconSize = 22)
                BasicText(
                    stringResource(R.string.privacy_prompt_intro),
                    style = TextStyle(soft, 14.sp, lineHeight = 20.sp),
                    modifier = Modifier.weight(1f),
                )
            }
            PrivacyChoiceCard(
                title = stringResource(R.string.privacy_prompt_diagnostics_title),
                body = stringResource(R.string.privacy_prompt_diagnostics_body),
                icon = Icons.Rounded.BugReport,
                accent = LiquidGlassColors.Blue,
                checked = diagnostics,
                onCheckedChange = { diagnostics = it },
                backdrop = backdrop,
                ink = ink,
                soft = soft,
                isDark = isDark,
            )
            PrivacyChoiceCard(
                title = stringResource(R.string.privacy_prompt_analytics_title),
                body = stringResource(R.string.privacy_prompt_analytics_body),
                icon = Icons.Rounded.BarChart,
                accent = LiquidGlassColors.Purple,
                checked = analytics,
                onCheckedChange = { analytics = it },
                backdrop = backdrop,
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
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { uriHandler.openUri(approvedPrivacyPolicyUrl) }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    style = TextStyle(
                        LiquidGlassColors.Blue,
                        13.sp,
                        FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                    ),
                )
            }
        }
    }
}

/**
 * One independent choice as a layered glass card. Only the header (icon + title + switch) is a
 * tap target; the body copy is purely informational, so normal taps can't select or highlight it.
 * Press feedback is a soft glass glow + slight scale, never a rectangular ripple/selection.
 */
@Composable
private fun PrivacyChoiceCard(
    title: String,
    body: String,
    icon: ImageVector,
    accent: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: Backdrop,
    ink: Color,
    soft: Color,
    isDark: Boolean,
) {
    val shape = RoundedCornerShape(18.dp)
    val platter = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.035f)
    val uncheckedBg by animateColorAsState(
        targetValue = platter,
        animationSpec = GlassMotion.fade(),
        label = "privacyChoiceBgOff",
    )
    val checkedBg by animateColorAsState(
        targetValue = accent.copy(alpha = if (isDark) 0.16f else 0.10f),
        animationSpec = GlassMotion.fade(),
        label = "privacyChoiceBgOn",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) accent.copy(alpha = if (isDark) 0.55f else 0.45f)
        else ink.copy(alpha = if (isDark) 0.12f else 0.10f),
        animationSpec = GlassMotion.fade(),
        label = "privacyChoiceBorder",
    )

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (checked) checkedBg else uncheckedBg)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.985f else 1f,
            animationSpec = GlassMotion.press(),
            label = "privacyChoicePress",
        )
        Row(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(RoundedCornerShape(12.dp))
                .liquidPressGlow(interaction, onLight = !isDark)
                // No system ripple: the designed glow is the only feedback.
                .clickable(interactionSource = interaction, indication = null) { onCheckedChange(!checked) }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AccentIconTile(icon, accent, size = 34, iconSize = 18)
            BasicText(
                title,
                modifier = Modifier.weight(1f),
                style = TextStyle(ink, 15.sp, FontWeight.SemiBold, lineHeight = 20.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            LiquidToggle(selected = { checked }, onSelect = onCheckedChange, backdrop = backdrop)
        }
        BasicText(
            body,
            style = TextStyle(soft, 12.sp, lineHeight = 17.sp),
        )
    }
}
