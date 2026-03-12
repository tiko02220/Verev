package com.vector.verevcodex.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.auth.EmailNotificationSettings
import com.vector.verevcodex.domain.usecase.auth.ObserveEmailNotificationSettingsUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateEmailNotificationSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EmailNotificationsViewModel @Inject constructor(
    observeEmailNotificationSettingsUseCase: ObserveEmailNotificationSettingsUseCase,
    private val updateEmailNotificationSettingsUseCase: UpdateEmailNotificationSettingsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmailNotificationsUiState())
    val uiState: StateFlow<EmailNotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeEmailNotificationSettingsUseCase().collect { settings ->
                val current = settings ?: EmailNotificationSettings()
                _uiState.update { state ->
                    state.copy(
                        promotionsAndCampaigns = current.promotionsAndCampaigns,
                        loyaltyActivity = current.loyaltyActivity,
                        weeklyBusinessSummary = current.weeklyBusinessSummary,
                        securityAlerts = current.securityAlerts,
                        isSaving = false,
                    )
                }
            }
        }
    }

    fun dismissMessage() = _uiState.update { it.copy(messageRes = null, errorRes = null) }

    fun setPromotionsAndCampaigns(enabled: Boolean) = save(
        EmailNotificationSettings(
            promotionsAndCampaigns = enabled,
            loyaltyActivity = _uiState.value.loyaltyActivity,
            weeklyBusinessSummary = _uiState.value.weeklyBusinessSummary,
            securityAlerts = _uiState.value.securityAlerts,
        )
    )

    fun setLoyaltyActivity(enabled: Boolean) = save(
        EmailNotificationSettings(
            promotionsAndCampaigns = _uiState.value.promotionsAndCampaigns,
            loyaltyActivity = enabled,
            weeklyBusinessSummary = _uiState.value.weeklyBusinessSummary,
            securityAlerts = _uiState.value.securityAlerts,
        )
    )

    fun setWeeklyBusinessSummary(enabled: Boolean) = save(
        EmailNotificationSettings(
            promotionsAndCampaigns = _uiState.value.promotionsAndCampaigns,
            loyaltyActivity = _uiState.value.loyaltyActivity,
            weeklyBusinessSummary = enabled,
            securityAlerts = _uiState.value.securityAlerts,
        )
    )

    fun setSecurityAlerts(enabled: Boolean) = save(
        EmailNotificationSettings(
            promotionsAndCampaigns = _uiState.value.promotionsAndCampaigns,
            loyaltyActivity = _uiState.value.loyaltyActivity,
            weeklyBusinessSummary = _uiState.value.weeklyBusinessSummary,
            securityAlerts = enabled,
        )
    )

    private fun save(settings: EmailNotificationSettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorRes = null, messageRes = null) }
            updateEmailNotificationSettingsUseCase(settings)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, messageRes = R.string.merchant_settings_message_notifications_updated) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorRes = throwable.toSettingsErrorRes(R.string.merchant_settings_error_notifications_update_failed),
                        )
                    }
                }
        }
    }
}
