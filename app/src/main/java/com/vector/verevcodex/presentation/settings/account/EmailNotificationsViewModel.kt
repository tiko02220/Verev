package com.vector.verevcodex.presentation.settings.account

import com.vector.verevcodex.presentation.settings.*

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
                        settings = current,
                        savedSettings = current,
                        isLoading = false,
                        hasChanges = false,
                        isSaving = false,
                    )
                }
            }
        }
    }

    fun dismissMessage() = _uiState.update { it.copy(messageRes = null, errorRes = null) }

    fun setEmailEnabled(enabled: Boolean) = updateSettings { copy(emailEnabled = enabled) }

    fun setPushEnabled(enabled: Boolean) = updateSettings { copy(pushEnabled = enabled) }

    fun setSoundEnabled(enabled: Boolean) = updateSettings { copy(soundEnabled = enabled) }

    fun setTransactionEmails(enabled: Boolean) = updateSettings { copy(transactionEmails = enabled) }

    fun setDailyBusinessSummary(enabled: Boolean) = updateSettings { copy(dailyBusinessSummary = enabled) }

    fun setWeeklyBusinessSummary(enabled: Boolean) = updateSettings { copy(weeklyBusinessSummary = enabled) }

    fun setMarketingEmails(enabled: Boolean) = updateSettings { copy(marketingEmails = enabled) }

    fun setNewCustomerPush(enabled: Boolean) = updateSettings { copy(newCustomerPush = enabled) }

    fun setTransactionPush(enabled: Boolean) = updateSettings { copy(transactionPush = enabled) }

    fun setRewardRedeemedPush(enabled: Boolean) = updateSettings { copy(rewardRedeemedPush = enabled) }

    fun setProgramUpdatesPush(enabled: Boolean) = updateSettings { copy(programUpdatesPush = enabled) }

    fun setStaffActivityPush(enabled: Boolean) = updateSettings { copy(staffActivityPush = enabled) }

    fun setSystemAlertsPush(enabled: Boolean) = updateSettings { copy(systemAlertsPush = enabled) }

    fun save() {
        val state = _uiState.value
        if (!state.hasChanges || state.isSaving) return
        val settings = state.settings
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorRes = null, messageRes = null) }
            updateEmailNotificationSettingsUseCase(settings)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            savedSettings = settings,
                            hasChanges = false,
                            isSaving = false,
                            messageRes = null,
                        )
                    }
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

    private fun updateSettings(transform: EmailNotificationSettings.() -> EmailNotificationSettings) {
        _uiState.update { state ->
            val updated = state.settings.transform()
            state.copy(
                settings = updated,
                hasChanges = updated != state.savedSettings,
                errorRes = null,
                messageRes = null,
            )
        }
    }
}
