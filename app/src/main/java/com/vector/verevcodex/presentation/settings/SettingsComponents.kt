package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.auth.AuthUser
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.presentation.merchant.common.MerchantConfirmationDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantSkeletonBlock
import com.vector.verevcodex.presentation.merchant.common.MerchantSkeletonCard
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun SettingsHeader(roleSubtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.merchant_settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = roleSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = VerevColors.Forest.copy(alpha = 0.7f),
        )
    }
}

@Composable
internal fun SettingsScreenSkeleton(
    isStaffFocusedRole: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSkeletonBlock(
            modifier = Modifier.fillMaxWidth(0.42f).height(30.dp),
        )
        MerchantSkeletonBlock(
            modifier = Modifier.fillMaxWidth(0.68f).height(18.dp),
        )
        MerchantSkeletonCard(shape = RoundedCornerShape(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MerchantSkeletonBlock(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MerchantSkeletonBlock(
                        modifier = Modifier.fillMaxWidth(0.46f).height(22.dp),
                    )
                    MerchantSkeletonBlock(
                        modifier = Modifier.fillMaxWidth(0.58f).height(16.dp),
                    )
                    MerchantSkeletonBlock(
                        modifier = Modifier.fillMaxWidth(0.38f).height(14.dp),
                    )
                }
                MerchantSkeletonBlock(
                    modifier = Modifier.size(22.dp),
                    shape = CircleShape,
                )
            }
        }
        if (isStaffFocusedRole) {
            MerchantSkeletonCard(shape = RoundedCornerShape(24.dp)) {
                MerchantSkeletonBlock(
                    modifier = Modifier.fillMaxWidth(0.34f).height(20.dp),
                )
                MerchantSkeletonBlock(
                    modifier = Modifier.fillMaxWidth(0.44f).height(24.dp),
                )
                repeat(3) {
                    MerchantSkeletonBlock(
                        modifier = Modifier.fillMaxWidth().height(16.dp),
                    )
                }
            }
        }
        repeat(3) {
            MerchantSkeletonBlock(
                modifier = Modifier.fillMaxWidth(0.28f).height(14.dp),
            )
            MerchantSkeletonCard(shape = RoundedCornerShape(24.dp)) {
                repeat(2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MerchantSkeletonBlock(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MerchantSkeletonBlock(
                                modifier = Modifier.fillMaxWidth(0.38f).height(18.dp),
                            )
                            MerchantSkeletonBlock(
                                modifier = Modifier.fillMaxWidth(0.66f).height(14.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingsBusinessCard(
    store: Store?,
    currentUser: AuthUser?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(VerevColors.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = VerevColors.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = store?.name ?: stringResource(R.string.merchant_select_store),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.White,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = buildString {
                        append(store?.category ?: stringResource(R.string.merchant_business_location))
                        append(" • ")
                        append(
                            if (store?.active == false) {
                                stringResource(R.string.merchant_store_disabled)
                            } else {
                                stringResource(R.string.merchant_store_active)
                            }
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.White.copy(alpha = 0.82f),
                )
                currentUser?.email?.takeIf { it.isNotBlank() }?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.White.copy(alpha = 0.68f),
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = VerevColors.White.copy(alpha = 0.8f),
            )
        }
    }
}

internal data class SettingsMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun SettingsGroupLabel(
    title: String,
    subtitle: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = VerevColors.Forest.copy(alpha = 0.55f),
            fontWeight = FontWeight.Medium,
        )
        subtitle?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun SettingsMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VerevColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                VerevColors.Moss.copy(alpha = 0.12f),
                                VerevColors.Forest.copy(alpha = 0.12f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VerevColors.Forest,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = VerevColors.Forest.copy(alpha = 0.34f),
            )
        }
    }
}

@Composable
internal fun SettingsMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailingLabel: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VerevColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(VerevColors.AppBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = VerevColors.Forest, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                )
            }
            if (trailingLabel.isNotBlank()) {
                Text(
                    text = trailingLabel,
                    color = VerevColors.Moss,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = VerevColors.Forest.copy(alpha = 0.34f),
                )
            }
        }
    }
}

@Composable
internal fun SettingsGroup(
    group: SettingsMenuGroup,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsGroupLabel(title = group.title, subtitle = group.subtitle)
        group.items.forEach { item ->
            SettingsMenuCard(
                title = item.title,
                subtitle = item.subtitle,
                icon = item.icon,
                onClick = item.onClick,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsCurrentStoreCard(
    store: Store?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VerevColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_settings_my_store_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.merchant_settings_my_store_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                )
            }
            Text(
                text = store?.name ?: stringResource(R.string.merchant_select_store),
                style = MaterialTheme.typography.titleLarge,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SettingsSectionBadge(
                    text = store?.category ?: stringResource(R.string.merchant_business_location),
                )
                SettingsSectionBadge(
                    text = if (store?.active == false) {
                        stringResource(R.string.merchant_store_disabled)
                    } else {
                        stringResource(R.string.merchant_store_active)
                    },
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_address_label),
                    value = store?.address ?: stringResource(R.string.merchant_settings_value_unavailable),
                )
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_contact_label),
                    value = store?.contactInfo ?: stringResource(R.string.merchant_settings_value_unavailable),
                )
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_hours_label),
                    value = store?.workingHours ?: stringResource(R.string.merchant_settings_value_unavailable),
                )
            }
        }
    }
}

@Composable
internal fun SettingsLogoutButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VerevColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = VerevColors.Danger,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.merchant_logout),
                color = VerevColors.Danger,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
internal fun SettingsLogoutDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    MerchantConfirmationDialog(
        title = stringResource(R.string.merchant_logout_confirm_title),
        message = stringResource(R.string.merchant_logout_confirm_subtitle),
        onConfirm = onConfirm,
        onDismiss = onCancel,
    )
}
