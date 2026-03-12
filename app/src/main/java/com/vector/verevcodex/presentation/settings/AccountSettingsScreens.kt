package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun PersonalInformationScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: PersonalInformationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SettingsBackRow(onBack = onBack) }
        item {
            SettingsHeroCard(
                title = stringResource(R.string.merchant_settings_personal_information),
                subtitle = stringResource(R.string.merchant_settings_personal_information_subtitle),
                icon = Icons.Default.Person,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
            )
        }
        state.messageRes?.let { messageRes ->
            item { SettingsMessageCard(title = stringResource(messageRes), accent = VerevColors.Moss) }
        }
        state.errorRes?.let { errorRes ->
            item { SettingsMessageCard(title = stringResource(errorRes), accent = Color(0xFFDC2626)) }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_settings_personal_information)) {
                MerchantFormField(
                    value = state.fullName,
                    onValueChange = viewModel::updateFullName,
                    label = stringResource(R.string.auth_full_name),
                    leadingIcon = Icons.Default.Badge,
                )
                MerchantFormField(
                    value = state.email,
                    onValueChange = viewModel::updateEmail,
                    label = stringResource(R.string.auth_email_label),
                    leadingIcon = Icons.Default.AlternateEmail,
                )
                MerchantFormField(
                    value = state.phoneNumber,
                    onValueChange = viewModel::updatePhoneNumber,
                    label = stringResource(R.string.auth_phone),
                    leadingIcon = Icons.Default.Phone,
                )
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
                ) {
                    Text(
                        text = if (state.isSaving) stringResource(R.string.auth_loading) else stringResource(R.string.merchant_settings_save_changes),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordSecurityScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: PasswordSecurityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SettingsBackRow(onBack = onBack) }
        item {
            SettingsHeroCard(
                title = stringResource(R.string.merchant_settings_password_security),
                subtitle = stringResource(R.string.merchant_settings_password_security_subtitle),
                icon = Icons.Default.Shield,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
            )
        }
        state.messageRes?.let { messageRes ->
            item { SettingsMessageCard(title = stringResource(messageRes), accent = VerevColors.Moss) }
        }
        state.errorRes?.let { errorRes ->
            item { SettingsMessageCard(title = stringResource(errorRes), accent = Color(0xFFDC2626)) }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_settings_password_section_title)) {
                Text(
                    text = state.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
                MerchantFormField(
                    value = state.currentPassword,
                    onValueChange = viewModel::updateCurrentPassword,
                    label = stringResource(R.string.merchant_settings_current_password),
                    leadingIcon = Icons.Default.Lock,
                )
                MerchantFormField(
                    value = state.newPassword,
                    onValueChange = viewModel::updateNewPassword,
                    label = stringResource(R.string.auth_new_password_label),
                    leadingIcon = Icons.Default.Password,
                )
                MerchantFormField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    label = stringResource(R.string.auth_confirm_password_label),
                    leadingIcon = Icons.Default.Password,
                )
                Button(
                    onClick = viewModel::savePassword,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSavingPassword,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
                ) {
                    Text(
                        text = if (state.isSavingPassword) stringResource(R.string.auth_loading) else stringResource(R.string.merchant_settings_update_password),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_settings_quick_access_section_title)) {
                if (state.hasQuickPin) {
                    MerchantFormField(
                        value = state.currentPin,
                        onValueChange = viewModel::updateCurrentPin,
                        label = stringResource(R.string.merchant_settings_current_pin),
                        leadingIcon = Icons.Default.Pin,
                    )
                }
                MerchantFormField(
                    value = state.newPin,
                    onValueChange = viewModel::updateNewPin,
                    label = stringResource(R.string.merchant_settings_new_pin),
                    leadingIcon = Icons.Default.Pin,
                )
                MerchantFormField(
                    value = state.confirmPin,
                    onValueChange = viewModel::updateConfirmPin,
                    label = stringResource(R.string.merchant_settings_confirm_new_pin),
                    leadingIcon = Icons.Default.Pin,
                )
                Button(
                    onClick = viewModel::saveQuickPin,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSavingPin,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
                ) {
                    Text(
                        text = if (state.isSavingPin) stringResource(R.string.auth_loading) else stringResource(R.string.merchant_settings_update_quick_pin),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_settings_biometric_login),
                    value = if (state.biometricEnabled) {
                        stringResource(R.string.merchant_settings_enabled)
                    } else {
                        stringResource(R.string.merchant_settings_disabled)
                    },
                    trailing = {
                        Switch(
                            checked = state.biometricEnabled,
                            onCheckedChange = viewModel::toggleBiometric,
                            enabled = !state.isSavingBiometric,
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun EmailNotificationsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: EmailNotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SettingsBackRow(onBack = onBack) }
        item {
            SettingsHeroCard(
                title = stringResource(R.string.merchant_settings_email_notifications),
                subtitle = stringResource(R.string.merchant_settings_email_notifications_subtitle),
                icon = Icons.Default.Email,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
            )
        }
        state.messageRes?.let { messageRes ->
            item { SettingsMessageCard(title = stringResource(messageRes), accent = VerevColors.Moss) }
        }
        state.errorRes?.let { errorRes ->
            item { SettingsMessageCard(title = stringResource(errorRes), accent = Color(0xFFDC2626)) }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_settings_email_notifications)) {
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_email_master),
                    subtitle = stringResource(R.string.merchant_settings_notify_email_master_subtitle),
                    checked = settings.emailEnabled,
                    onCheckedChange = viewModel::setEmailEnabled,
                    enabled = !state.isSaving && !state.isLoading,
                    leadingIcon = Icons.Default.Email,
                    accent = VerevColors.Gold,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_push_master),
                    subtitle = stringResource(R.string.merchant_settings_notify_push_master_subtitle),
                    checked = settings.pushEnabled,
                    onCheckedChange = viewModel::setPushEnabled,
                    enabled = !state.isSaving && !state.isLoading,
                    leadingIcon = Icons.Default.NotificationsActive,
                    accent = VerevColors.Moss,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_sound),
                    subtitle = stringResource(R.string.merchant_settings_notify_sound_subtitle),
                    checked = settings.soundEnabled,
                    onCheckedChange = viewModel::setSoundEnabled,
                    enabled = !state.isSaving && !state.isLoading,
                    leadingIcon = if (settings.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    accent = VerevColors.Gold.copy(alpha = 0.86f),
                )
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_settings_email_preferences_title)) {
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_transaction_emails),
                    subtitle = stringResource(R.string.merchant_settings_notify_transaction_emails_subtitle),
                    checked = settings.transactionEmails,
                    onCheckedChange = viewModel::setTransactionEmails,
                    enabled = !state.isSaving && !state.isLoading && settings.emailEnabled,
                    leadingIcon = Icons.Default.Email,
                    accent = VerevColors.Gold,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_daily_summary),
                    subtitle = stringResource(R.string.merchant_settings_notify_daily_summary_subtitle),
                    checked = settings.dailyBusinessSummary,
                    onCheckedChange = viewModel::setDailyBusinessSummary,
                    enabled = !state.isSaving && !state.isLoading && settings.emailEnabled,
                    leadingIcon = Icons.Default.NotificationsActive,
                    accent = VerevColors.Moss,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_weekly_summary),
                    subtitle = stringResource(R.string.merchant_settings_notify_weekly_summary_subtitle),
                    checked = settings.weeklyBusinessSummary,
                    onCheckedChange = viewModel::setWeeklyBusinessSummary,
                    enabled = !state.isSaving && !state.isLoading && settings.emailEnabled,
                    leadingIcon = Icons.Default.NotificationsActive,
                    accent = VerevColors.Forest,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_marketing_emails),
                    subtitle = stringResource(R.string.merchant_settings_notify_marketing_emails_subtitle),
                    checked = settings.marketingEmails,
                    onCheckedChange = viewModel::setMarketingEmails,
                    enabled = !state.isSaving && !state.isLoading && settings.emailEnabled,
                    leadingIcon = Icons.Default.AlternateEmail,
                    accent = VerevColors.Gold.copy(alpha = 0.86f),
                )
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_settings_push_notifications_title)) {
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_new_customer),
                    subtitle = stringResource(R.string.merchant_settings_notify_new_customer_subtitle),
                    checked = settings.newCustomerPush,
                    onCheckedChange = viewModel::setNewCustomerPush,
                    enabled = !state.isSaving && !state.isLoading && settings.pushEnabled,
                    leadingIcon = Icons.Default.Person,
                    accent = VerevColors.Moss,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_transactions),
                    subtitle = stringResource(R.string.merchant_settings_notify_transactions_subtitle),
                    checked = settings.transactionPush,
                    onCheckedChange = viewModel::setTransactionPush,
                    enabled = !state.isSaving && !state.isLoading && settings.pushEnabled,
                    leadingIcon = Icons.Default.NotificationsActive,
                    accent = VerevColors.Gold,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_reward_redeemed),
                    subtitle = stringResource(R.string.merchant_settings_notify_reward_redeemed_subtitle),
                    checked = settings.rewardRedeemedPush,
                    onCheckedChange = viewModel::setRewardRedeemedPush,
                    enabled = !state.isSaving && !state.isLoading && settings.pushEnabled,
                    leadingIcon = Icons.Default.Badge,
                    accent = VerevColors.Gold.copy(alpha = 0.86f),
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_program_updates),
                    subtitle = stringResource(R.string.merchant_settings_notify_program_updates_subtitle),
                    checked = settings.programUpdatesPush,
                    onCheckedChange = viewModel::setProgramUpdatesPush,
                    enabled = !state.isSaving && !state.isLoading && settings.pushEnabled,
                    leadingIcon = Icons.Default.AlternateEmail,
                    accent = VerevColors.Forest,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_staff_activity),
                    subtitle = stringResource(R.string.merchant_settings_notify_staff_activity_subtitle),
                    checked = settings.staffActivityPush,
                    onCheckedChange = viewModel::setStaffActivityPush,
                    enabled = !state.isSaving && !state.isLoading && settings.pushEnabled,
                    leadingIcon = Icons.Default.Badge,
                    accent = VerevColors.Moss,
                )
                SettingsToggleRow(
                    title = stringResource(R.string.merchant_settings_notify_security_alerts),
                    subtitle = stringResource(R.string.merchant_settings_notify_security_alerts_subtitle),
                    checked = settings.systemAlertsPush,
                    onCheckedChange = viewModel::setSystemAlertsPush,
                    enabled = !state.isSaving && !state.isLoading && settings.pushEnabled,
                    leadingIcon = Icons.Default.Shield,
                    accent = Color(0xFFDC2626),
                )
            }
        }
        item {
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.hasChanges && !state.isSaving && !state.isLoading,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Gold,
                    contentColor = Color.White,
                    disabledContainerColor = VerevColors.Gold.copy(alpha = 0.32f),
                    disabledContentColor = Color.White.copy(alpha = 0.72f),
                ),
            ) {
                Text(
                    text = if (state.isSaving) {
                        stringResource(R.string.auth_loading)
                    } else {
                        stringResource(R.string.merchant_settings_save_preferences)
                    },
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (state.hasChanges) {
            item {
                Text(
                    text = stringResource(R.string.merchant_settings_unsaved_changes_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
        }
        if (state.isLoading) {
            item {
                Text(
                    text = stringResource(R.string.auth_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
        }
    }
}

@Composable
private fun SettingsMessageCard(
    title: String,
    accent: Color,
) {
    MerchantPrimaryCard {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = accent,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    accent: Color = VerevColors.Forest,
) {
    MerchantPrimaryCard(contentPadding = PaddingValues(16.dp)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = accent.copy(alpha = 0.12f),
                                shape = androidx.compose.foundation.shape.CircleShape,
                            ),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = accent,
                        )
                    }
                }
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
                        color = VerevColors.Forest.copy(alpha = 0.64f),
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        }
    }
}
