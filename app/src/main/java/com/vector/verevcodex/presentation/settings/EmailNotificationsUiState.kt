package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes

data class EmailNotificationsUiState(
    val promotionsAndCampaigns: Boolean = true,
    val loyaltyActivity: Boolean = true,
    val weeklyBusinessSummary: Boolean = true,
    val securityAlerts: Boolean = true,
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
