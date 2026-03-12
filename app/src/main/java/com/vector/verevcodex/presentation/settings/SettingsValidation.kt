package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import com.vector.verevcodex.R

internal object SettingsValidation {
    @StringRes
    fun personalInformationError(
        fullName: String,
        email: String,
        phoneNumber: String,
        isEmailValid: Boolean,
    ): Int? = when {
        fullName.isBlank() -> R.string.merchant_settings_error_full_name_required
        email.isBlank() -> R.string.auth_error_required_email
        !isEmailValid -> R.string.auth_error_invalid_email
        phoneNumber.isBlank() -> R.string.merchant_settings_error_phone_required
        else -> null
    }

    @StringRes
    fun passwordError(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
    ): Int? = when {
        currentPassword.isBlank() -> R.string.merchant_settings_error_current_password_required
        newPassword.length < 8 -> R.string.auth_error_password_short
        newPassword != confirmPassword -> R.string.auth_error_password_confirm
        else -> null
    }

    @StringRes
    fun quickPinError(
        hasQuickPin: Boolean,
        currentPin: String,
        newPin: String,
        confirmPin: String,
    ): Int? = when {
        hasQuickPin && currentPin.length != 4 -> R.string.merchant_settings_error_current_pin_required
        newPin.length != 4 -> R.string.auth_pin_error_length
        newPin != confirmPin -> R.string.auth_pin_error_mismatch
        else -> null
    }
}
