package com.vector.verevcodex.presentation.settings.account

import com.vector.verevcodex.presentation.settings.*

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons.AutoMirrored
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.MainActivity
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.theme.VerevColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PersonalInformationScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: PersonalInformationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? MainActivity
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.updateProfilePhotoUri(uri.toString())
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCompactHeader(
            title = stringResource(R.string.merchant_settings_personal_information),
            subtitle = stringResource(R.string.merchant_settings_personal_information_subtitle),
            onBack = onBack,
            actionLabel = if (state.isEditing) null else stringResource(R.string.merchant_settings_edit_action),
            onAction = if (state.isEditing) null else viewModel::startEditing,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.messageRes?.let { messageRes ->
                item { SettingsMessageCard(title = stringResource(messageRes), accent = VerevColors.Moss) }
            }
            item {
                PersonalInformationProfileCard(
                    fullName = state.fullName,
                    email = state.email,
                    profilePhotoUri = state.profilePhotoUri,
                    isEditing = state.isEditing,
                    onUploadPhoto = {
                        activity?.suppressRelockForTransientSystemUi()
                        if (!state.isEditing) {
                            viewModel.startEditing()
                        }
                        photoPickerLauncher.launch(arrayOf("image/*"))
                    },
                )
            }
            if (state.isEditing) {
                item {
                    SettingsDetailSection(title = stringResource(R.string.merchant_settings_personal_information_basic_title)) {
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
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                viewModel.cancelEditing()
                                viewModel.dismissMessage()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F4F1), contentColor = VerevColors.Forest),
                        ) {
                            Text(stringResource(R.string.auth_cancel), fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = viewModel::save,
                            modifier = Modifier.weight(1f),
                            enabled = !state.isSaving,
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
                        ) {
                            Text(
                                text = if (state.isSaving) stringResource(R.string.auth_loading) else stringResource(R.string.merchant_settings_save_changes),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            } else {
                item {
                    SettingsDetailSection(title = stringResource(R.string.merchant_settings_personal_information_basic_title)) {
                        SettingsProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = stringResource(R.string.auth_full_name),
                            value = state.fullName,
                        )
                        SettingsProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = stringResource(R.string.auth_email_label),
                            value = state.email,
                        )
                        SettingsProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = stringResource(R.string.auth_phone),
                            value = state.phoneNumber,
                        )
                    }
                }
            }
        }
    }
    state.errorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
fun PasswordSecurityScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: PasswordSecurityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }
    var showPinEditor by rememberSaveable { mutableStateOf(false) }

    SettingsInnerScaffold(
        title = stringResource(R.string.merchant_settings_password_security),
        subtitle = stringResource(R.string.merchant_settings_password_security_subtitle),
        onBack = onBack,
        contentPadding = contentPadding,
    ) {
        item {
            SettingsFeatureCard(
                title = stringResource(R.string.merchant_settings_password_card_title),
                subtitle = stringResource(R.string.merchant_settings_password_card_subtitle),
                icon = Icons.Default.Lock,
                accent = VerevColors.Forest,
            ) {
                SettingsSecureField(
                    value = state.currentPassword,
                    onValueChange = viewModel::updateCurrentPassword,
                    label = stringResource(R.string.merchant_settings_current_password),
                    leadingIcon = Icons.Default.Lock,
                    visible = showCurrentPassword,
                    onToggleVisibility = { showCurrentPassword = !showCurrentPassword },
                )
                SettingsSecureField(
                    value = state.newPassword,
                    onValueChange = viewModel::updateNewPassword,
                    label = stringResource(R.string.auth_new_password_label),
                    leadingIcon = Icons.Default.Password,
                    visible = showNewPassword,
                    onToggleVisibility = { showNewPassword = !showNewPassword },
                    supportingText = stringResource(R.string.merchant_settings_password_requirements),
                )
                SettingsSecureField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    label = stringResource(R.string.auth_confirm_password_label),
                    leadingIcon = Icons.Default.Password,
                    visible = showConfirmPassword,
                    onToggleVisibility = { showConfirmPassword = !showConfirmPassword },
                )
                Button(
                    onClick = viewModel::savePassword,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSavingPassword &&
                        state.currentPassword.isNotBlank() &&
                        state.newPassword.length >= 8 &&
                        state.newPassword == state.confirmPassword,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
                ) {
                    Text(
                        text = if (state.isSavingPassword) stringResource(R.string.auth_loading) else stringResource(R.string.merchant_settings_update_password),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        item {
            SettingsFeatureCard(
                title = stringResource(R.string.merchant_settings_security_options_title),
                subtitle = stringResource(R.string.merchant_settings_security_options_subtitle),
                icon = Icons.Default.Shield,
                accent = VerevColors.Gold,
            ) {
                SettingsSecurityOptionRow(
                    icon = Icons.Default.Pin,
                    accent = VerevColors.Moss,
                    title = stringResource(R.string.merchant_settings_quick_pin_title),
                    subtitle = stringResource(R.string.merchant_settings_quick_pin_subtitle),
                    actionLabel = if (showPinEditor) {
                        stringResource(R.string.merchant_close)
                    } else if (state.hasQuickPin) {
                        stringResource(R.string.merchant_settings_edit_action)
                    } else {
                        stringResource(R.string.merchant_settings_set_quick_pin)
                    },
                    onAction = { showPinEditor = !showPinEditor },
                )
                if (showPinEditor) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (state.hasQuickPin) {
                            MerchantFormField(
                                value = state.currentPin,
                                onValueChange = viewModel::updateCurrentPin,
                                label = stringResource(R.string.merchant_settings_current_pin),
                                leadingIcon = Icons.Default.Pin,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            )
                        }
                        MerchantFormField(
                            value = state.newPin,
                            onValueChange = viewModel::updateNewPin,
                            label = stringResource(R.string.merchant_settings_new_pin),
                            leadingIcon = Icons.Default.Pin,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        )
                        MerchantFormField(
                            value = state.confirmPin,
                            onValueChange = viewModel::updateConfirmPin,
                            label = stringResource(R.string.merchant_settings_confirm_new_pin),
                            leadingIcon = Icons.Default.Pin,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        )
                        Button(
                            onClick = viewModel::saveQuickPin,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSavingPin &&
                                (!state.hasQuickPin || state.currentPin.length == 4) &&
                                state.newPin.length == 4 &&
                                state.newPin == state.confirmPin,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
                        ) {
                            Text(
                                text = if (state.isSavingPin) stringResource(R.string.auth_loading) else stringResource(R.string.merchant_settings_update_quick_pin),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                SettingsSecurityOptionRow(
                    icon = Icons.Default.Fingerprint,
                    accent = VerevColors.Gold,
                    title = stringResource(R.string.merchant_settings_biometric_login),
                    subtitle = stringResource(R.string.merchant_settings_biometric_subtitle),
                    actionLabel = null,
                    onAction = null,
                    trailing = {
                        SettingsInlineToggle(
                            checked = state.biometricEnabled,
                            onCheckedChange = viewModel::toggleBiometric,
                            enabled = state.hasQuickPin && !state.isSavingBiometric,
                            accent = VerevColors.Gold,
                        )
                    },
                )
            }
        }
        item {
            SettingsFeatureCard(
                title = stringResource(R.string.merchant_settings_active_sessions_title),
                subtitle = stringResource(R.string.merchant_settings_active_sessions_subtitle),
                icon = Icons.Default.Devices,
                accent = VerevColors.Forest,
            ) {
                SettingsSessionRow(
                    icon = Icons.Default.Devices,
                    title = stringResource(R.string.merchant_settings_current_device),
                    location = stringResource(R.string.merchant_settings_current_device_location),
                    lastActive = stringResource(R.string.merchant_settings_active_now),
                    statusLabel = stringResource(R.string.merchant_settings_session_current),
                    current = true,
                )
            }
        }
    }
    state.errorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = viewModel::dismissMessage,
        )
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

    SettingsInnerScaffold(
        title = stringResource(R.string.merchant_settings_notifications_title),
        subtitle = stringResource(R.string.merchant_settings_notifications_screen_subtitle),
        onBack = onBack,
        contentPadding = contentPadding,
    ) {
        item {
            SettingsNotificationHero(
                emailEnabled = settings.emailEnabled,
                pushEnabled = settings.pushEnabled,
                soundEnabled = settings.soundEnabled,
            )
        }
        item {
            SettingsFeatureCard(
                title = stringResource(R.string.merchant_settings_master_controls_title),
                subtitle = stringResource(R.string.merchant_settings_master_controls_subtitle),
                icon = Icons.Default.NotificationsActive,
                accent = VerevColors.Forest,
            ) {
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
            SettingsSectionIntro(
                title = stringResource(R.string.merchant_settings_email_preferences_title),
                subtitle = stringResource(R.string.merchant_settings_email_preferences_design_subtitle),
            )
        }
        item {
            SettingsFeatureCard(
                title = stringResource(R.string.merchant_settings_email_preferences_title),
                subtitle = stringResource(R.string.merchant_settings_notify_email_master_subtitle),
                icon = Icons.Default.Email,
                accent = VerevColors.Gold,
            ) {
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
            SettingsSectionIntro(
                title = stringResource(R.string.merchant_settings_push_notifications_title),
                subtitle = stringResource(R.string.merchant_settings_push_notifications_design_subtitle),
            )
        }
        item {
            SettingsFeatureCard(
                title = stringResource(R.string.merchant_settings_push_notifications_title),
                subtitle = stringResource(R.string.merchant_settings_notify_push_master_subtitle),
                icon = Icons.Default.NotificationsActive,
                accent = VerevColors.Moss,
            ) {
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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Gold,
                    contentColor = Color.White,
                    disabledContainerColor = VerevColors.Gold.copy(alpha = 0.32f),
                    disabledContentColor = Color.White.copy(alpha = 0.72f),
                ),
            ) {
                Text(
                    text = if (state.isSaving) stringResource(R.string.auth_loading) else stringResource(R.string.merchant_settings_save_preferences),
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
    state.errorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = viewModel::dismissMessage,
        )
    }
}
