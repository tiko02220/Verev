package com.vector.verevcodex.domain.model.auth

data class EmailNotificationSettings(
    val promotionsAndCampaigns: Boolean = true,
    val loyaltyActivity: Boolean = true,
    val weeklyBusinessSummary: Boolean = true,
    val securityAlerts: Boolean = true,
)
