package com.vector.verevcodex.presentation.settings.account

import com.vector.verevcodex.presentation.settings.*

import androidx.annotation.StringRes

data class PasswordSecurityUiState(
    val email: String = "",
    val hasQuickPin: Boolean = false,
    val biometricEnabled: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPin: String = "",
    val newPin: String = "",
    val confirmPin: String = "",
    val isSavingPassword: Boolean = false,
    val isSavingPin: Boolean = false,
    val isSavingBiometric: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
