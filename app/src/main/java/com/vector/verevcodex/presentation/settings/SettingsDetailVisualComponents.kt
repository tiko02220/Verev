package com.vector.verevcodex.presentation.settings

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.theme.VerevColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PRIVACY_SECTION_MAIN_ID = "privacy_main"
private const val PRIVACY_SECTION_DATA_ID = "privacy_data"
private const val TERMS_SECTION_MAIN_ID = "terms_main"
private const val TERMS_SECTION_SECURITY_ID = "terms_security"

internal const val PRIVACY_EXPORT_MIME_TYPE = "text/plain"
internal const val PRIVACY_POLICY_EXPORT_FILE_NAME = "privacy-policy.txt"
internal const val TERMS_EXPORT_FILE_NAME = "terms-of-service.txt"

@Composable
internal fun PaymentMethodCard(
    method: PaymentMethodUi,
    onMakeDefault: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (method.isDefault) VerevColors.Moss.copy(alpha = 0.24f) else VerevColors.Inactive.copy(alpha = 0.1f),
                    RoundedCornerShape(22.dp),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    if (method.brand.equals("visa", ignoreCase = true)) {
                                        listOf(Color(0xFF1434CB), Color(0xFF2563EB))
                                    } else {
                                        listOf(Color(0xFFEB001B), Color(0xFFFF5F00))
                                    },
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.merchant_payment_methods_card_label, method.brand, method.last4),
                            style = MaterialTheme.typography.titleMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(R.string.merchant_payment_methods_card_expiry, method.expiry),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.56f),
                        )
                    }
                }
                SettingsSectionBadge(
                    text = if (method.isDefault) {
                        stringResource(R.string.merchant_payment_methods_default)
                    } else {
                        stringResource(R.string.merchant_payment_methods_saved_label)
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!method.isDefault) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = VerevColors.Moss.copy(alpha = 0.12f),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onMakeDefault)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.merchant_payment_methods_set_default),
                                style = MaterialTheme.typography.labelLarge,
                                color = VerevColors.Forest,
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier.weight(if (method.isDefault) 1f else 0.72f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEE2E2),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onRemove)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.merchant_payment_methods_remove),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFB91C1C),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingsPageIntro(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.62f),
        )
    }
}

@Composable
internal fun SettingsBranchCard(
    name: String,
    address: String,
    isSelected: Boolean,
    isMain: Boolean,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .size(width = 264.dp, height = 144.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isSelected) {
                    Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest))
                } else {
                    Brush.linearGradient(listOf(Color.White, Color.White))
                },
            )
            .border(
                1.dp,
                if (isSelected) Color.Transparent else VerevColors.Inactive.copy(alpha = 0.18f),
                RoundedCornerShape(22.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.18f) else VerevColors.Gold.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else VerevColors.Gold,
                )
            }
            SettingsSectionBadge(
                text = when {
                    isMain -> stringResource(R.string.merchant_main_location)
                    active -> stringResource(R.string.merchant_store_active)
                    else -> stringResource(R.string.merchant_store_disabled)
                },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) Color.White else VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.White.copy(alpha = 0.78f) else VerevColors.Forest.copy(alpha = 0.62f),
                maxLines = 2,
            )
        }
    }
}

@Composable
internal fun SettingsActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, VerevColors.Inactive.copy(alpha = 0.16f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = accent)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
internal fun BillingHistoryCard(
    title: String,
    subtitle: String,
    amount: String,
    status: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VerevColors.Forest.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = VerevColors.Forest)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.62f),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                SettingsSectionBadge(text = status)
            }
        }
    }
}

@Composable
internal fun SettingsPrivacyCard(
    title: String,
    subtitle: String,
    accentColors: List<Color>,
    expanded: Boolean,
    onClick: () -> Unit,
    tab: PrivacyTabUi,
    bullets: List<String>,
) {
    SettingsDetailSection(title = title) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(accentColors.map { it.copy(alpha = 0.12f) })),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (tab == PrivacyTabUi.PRIVACY) Icons.Default.Shield else Icons.Default.Description,
                        contentDescription = null,
                        tint = VerevColors.Forest,
                    )
                }
                Column {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.72f),
                    )
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = VerevColors.Inactive,
            )
        }
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                bullets.forEach { bullet ->
                    SettingsCheckBullet(text = bullet)
                }
            }
        }
    }
}

@Composable
internal fun PrivacyTabButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) Brush.horizontalGradient(listOf(VerevColors.Forest, VerevColors.Moss)) else Brush.horizontalGradient(listOf(Color.White, Color.White)),
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = VerevColors.Inactive.copy(alpha = 0.22f),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else VerevColors.Forest)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) Color.White else VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
internal fun rememberPrivacySections(tab: PrivacyTabUi): List<PrivacySectionUi> = when (tab) {
    PrivacyTabUi.PRIVACY -> listOf(
        PrivacySectionUi(
            id = PRIVACY_SECTION_MAIN_ID,
            title = stringResource(R.string.merchant_privacy_section_privacy_title),
            subtitle = stringResource(R.string.merchant_privacy_section_privacy_subtitle),
            bullets = listOf(
                stringResource(R.string.merchant_privacy_privacy_point_one),
                stringResource(R.string.merchant_privacy_privacy_point_two),
                stringResource(R.string.merchant_privacy_privacy_point_three),
            ),
            accentColors = listOf(VerevColors.Moss, VerevColors.Forest),
        ),
        PrivacySectionUi(
            id = PRIVACY_SECTION_DATA_ID,
            title = stringResource(R.string.merchant_privacy_section_data_title),
            subtitle = stringResource(R.string.merchant_privacy_section_data_subtitle),
            bullets = listOf(
                stringResource(R.string.merchant_privacy_data_point_one),
                stringResource(R.string.merchant_privacy_data_point_two),
                stringResource(R.string.merchant_privacy_data_point_three),
            ),
            accentColors = listOf(VerevColors.Gold, VerevColors.Tan),
        ),
    )
    PrivacyTabUi.TERMS -> listOf(
        PrivacySectionUi(
            id = TERMS_SECTION_MAIN_ID,
            title = stringResource(R.string.merchant_terms_section_service_title),
            subtitle = stringResource(R.string.merchant_terms_section_service_subtitle),
            bullets = listOf(
                stringResource(R.string.merchant_terms_service_point_one),
                stringResource(R.string.merchant_terms_service_point_two),
                stringResource(R.string.merchant_terms_service_point_three),
            ),
            accentColors = listOf(VerevColors.Forest, VerevColors.Moss),
        ),
        PrivacySectionUi(
            id = TERMS_SECTION_SECURITY_ID,
            title = stringResource(R.string.merchant_terms_section_security_title),
            subtitle = stringResource(R.string.merchant_terms_section_security_subtitle),
            bullets = listOf(
                stringResource(R.string.merchant_terms_security_point_one),
                stringResource(R.string.merchant_terms_security_point_two),
                stringResource(R.string.merchant_terms_security_point_three),
            ),
            accentColors = listOf(VerevColors.Gold, VerevColors.Tan),
        ),
    )
}

internal fun defaultPrivacySectionId(tab: PrivacyTabUi): String = when (tab) {
    PrivacyTabUi.PRIVACY -> PRIVACY_SECTION_MAIN_ID
    PrivacyTabUi.TERMS -> TERMS_SECTION_MAIN_ID
}

internal fun privacyExportFileName(tab: PrivacyTabUi): String = when (tab) {
    PrivacyTabUi.PRIVACY -> PRIVACY_POLICY_EXPORT_FILE_NAME
    PrivacyTabUi.TERMS -> TERMS_EXPORT_FILE_NAME
}

internal fun parseHexColor(raw: String?, fallback: Color): Color = try {
    raw?.removePrefix("#")?.takeIf { it.length == 6 }?.let { value ->
        Color(android.graphics.Color.parseColor("#$value"))
    } ?: fallback
} catch (_: IllegalArgumentException) {
    fallback
}

@Composable
internal fun rememberSettingsImageBitmap(uriString: String): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, uriString) {
        value = if (uriString.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(android.net.Uri.parse(uriString))?.use { input ->
                        BitmapFactory.decodeStream(input)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }.value
}

internal fun buildPrivacyExportText(
    title: String,
    lastUpdatedLabel: String,
    sections: List<PrivacySectionUi>,
): String = buildString {
    appendLine(title)
    appendLine(lastUpdatedLabel)
    appendLine()
    sections.forEach { section ->
        appendLine(section.title)
        appendLine(section.subtitle)
        section.bullets.forEach { bullet ->
            appendLine("- $bullet")
        }
        appendLine()
    }
}

@Composable
internal fun SettingsLogoPreview(
    logoUri: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val imageBitmap = rememberSettingsImageBitmap(logoUri)
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.88f),
            )
        }
    }
}
