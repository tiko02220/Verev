package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.auth.AuthUser
import com.vector.verevcodex.presentation.merchant.common.MerchantMenuRow
import com.vector.verevcodex.presentation.merchant.common.MerchantPageHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun SettingsHeader(roleSubtitle: String) {
    MerchantPageHeader(
        title = stringResource(R.string.merchant_settings_title),
        subtitle = roleSubtitle,
    )
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
                Icon(Icons.Default.Storefront, contentDescription = null, tint = VerevColors.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = store?.name ?: stringResource(R.string.merchant_select_store),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.White,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.size(2.dp))
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
                    color = VerevColors.White.copy(alpha = 0.8f),
                )
                currentUser?.email?.takeIf { it.isNotBlank() }?.let { email ->
                    Spacer(Modifier.size(2.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.White.copy(alpha = 0.68f),
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VerevColors.White.copy(alpha = 0.8f))
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
internal fun SettingsMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailingLabel: String,
    onClick: () -> Unit,
) {
    MerchantMenuRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick,
        trailing = {
            Text(
                text = trailingLabel,
                color = VerevColors.Moss,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        },
    )
}

@Composable
internal fun SettingsGroup(
    title: String,
    items: List<SettingsMenuItem>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = title)
        items.forEach { item ->
            MerchantMenuRow(
                title = item.title,
                subtitle = item.subtitle,
                icon = item.icon,
                onClick = item.onClick,
                trailing = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VerevColors.Inactive)
                },
            )
        }
    }
}

@Composable
internal fun SettingsLogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerevColors.White, contentColor = VerevColors.Danger),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Text(text = stringResource(R.string.merchant_logout), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun SettingsLogoutDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VerevColors.Scrim),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(VerevColors.DangerContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = VerevColors.Danger)
                }
                Text(
                    text = stringResource(R.string.merchant_logout_confirm_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.merchant_logout_confirm_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerevColors.DangerStrong,
                        contentColor = VerevColors.White,
                    ),
                ) {
                    Text(stringResource(R.string.merchant_logout))
                }
                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerevColors.White, contentColor = VerevColors.Moss),
                ) {
                    Text(stringResource(R.string.auth_cancel))
                }
            }
        }
    }
}
