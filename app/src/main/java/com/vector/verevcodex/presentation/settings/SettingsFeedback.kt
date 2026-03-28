package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.common.errors.AppStateException
import com.vector.verevcodex.data.remote.core.RemoteException

internal sealed interface SettingsFeedback {
    data class Message(@StringRes val resId: Int) : SettingsFeedback
    data class Error(@StringRes val resId: Int) : SettingsFeedback
}

@StringRes
internal fun Throwable.toSettingsErrorRes(defaultRes: Int): Int {
    val remoteCode = (this as? RemoteException)?.backendCode
    val appStateReason = (this as? AppStateException)?.reason
    return when {
        remoteCode == "CONFLICT_USER_EMAIL" -> R.string.merchant_settings_error_email_exists
        remoteCode == "AUTH_PASSWORD_MISMATCH" -> R.string.merchant_settings_error_current_password
        remoteCode == "AUTH_PIN_MISMATCH" -> R.string.merchant_settings_error_current_pin
        appStateReason == AppStateException.Reason.NoActiveAccount -> R.string.merchant_settings_error_no_active_account
        appStateReason == AppStateException.Reason.NoActiveSession -> R.string.merchant_settings_error_no_active_account
        else -> defaultRes
    }
}
