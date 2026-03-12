package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import com.vector.verevcodex.R

internal sealed interface SettingsFeedback {
    data class Message(@StringRes val resId: Int) : SettingsFeedback
    data class Error(@StringRes val resId: Int) : SettingsFeedback
}

@StringRes
internal fun Throwable.toSettingsErrorRes(defaultRes: Int): Int = when (message) {
    "Email already exists" -> R.string.merchant_settings_error_email_exists
    "Current password is incorrect" -> R.string.merchant_settings_error_current_password
    "Current PIN is incorrect" -> R.string.merchant_settings_error_current_pin
    "No active account" -> R.string.merchant_settings_error_no_active_account
    "No active session" -> R.string.merchant_settings_error_no_active_account
    else -> defaultRes
}
