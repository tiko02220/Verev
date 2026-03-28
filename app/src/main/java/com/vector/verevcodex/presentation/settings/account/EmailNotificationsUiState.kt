package com.vector.verevcodex.presentation.settings.account

import com.vector.verevcodex.presentation.settings.*

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.auth.EmailNotificationSettings

data class EmailNotificationsUiState(
    val settings: EmailNotificationSettings = EmailNotificationSettings(),
    val savedSettings: EmailNotificationSettings = EmailNotificationSettings(),
    val isLoading: Boolean = true,
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
