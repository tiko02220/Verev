package com.vector.verevcodex.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.usecase.auth.ChangeCurrentPasswordUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveCurrentSecurityConfigUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateCurrentBiometricEnabledUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateCurrentQuickPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PasswordSecurityViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    observeCurrentSecurityConfigUseCase: ObserveCurrentSecurityConfigUseCase,
    private val changeCurrentPasswordUseCase: ChangeCurrentPasswordUseCase,
    private val updateCurrentQuickPinUseCase: UpdateCurrentQuickPinUseCase,
    private val updateCurrentBiometricEnabledUseCase: UpdateCurrentBiometricEnabledUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PasswordSecurityUiState())
    val uiState: StateFlow<PasswordSecurityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeSessionUseCase(),
                observeCurrentSecurityConfigUseCase(),
            ) { session, security -> session to security }.collect { (session, security) ->
                _uiState.update { state ->
                    state.copy(
                        email = session?.user?.email.orEmpty(),
                        hasQuickPin = security != null,
                        biometricEnabled = security?.biometricEnabled == true,
                    )
                }
            }
        }
    }

    fun updateCurrentPassword(value: String) = _uiState.update { it.copy(currentPassword = value, errorRes = null, messageRes = null) }
    fun updateNewPassword(value: String) = _uiState.update { it.copy(newPassword = value, errorRes = null, messageRes = null) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, errorRes = null, messageRes = null) }
    fun updateCurrentPin(value: String) = _uiState.update { it.copy(currentPin = value.filter(Char::isDigit).take(4), errorRes = null, messageRes = null) }
    fun updateNewPin(value: String) = _uiState.update { it.copy(newPin = value.filter(Char::isDigit).take(4), errorRes = null, messageRes = null) }
    fun updateConfirmPin(value: String) = _uiState.update { it.copy(confirmPin = value.filter(Char::isDigit).take(4), errorRes = null, messageRes = null) }
    fun dismissMessage() = _uiState.update { it.copy(messageRes = null, errorRes = null) }

    fun savePassword() {
        val state = _uiState.value
        val error = SettingsValidation.passwordError(
            currentPassword = state.currentPassword,
            newPassword = state.newPassword,
            confirmPassword = state.confirmPassword,
        )
        if (error != null) {
            _uiState.update { it.copy(errorRes = error, messageRes = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPassword = true, errorRes = null, messageRes = null) }
            changeCurrentPasswordUseCase(state.currentPassword, state.newPassword)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            currentPassword = "",
                            newPassword = "",
                            confirmPassword = "",
                            isSavingPassword = false,
                            messageRes = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingPassword = false,
                            errorRes = throwable.toSettingsErrorRes(R.string.merchant_settings_error_password_update_failed),
                        )
                    }
                }
        }
    }

    fun saveQuickPin() {
        val state = _uiState.value
        val error = SettingsValidation.quickPinError(
            hasQuickPin = state.hasQuickPin,
            currentPin = state.currentPin,
            newPin = state.newPin,
            confirmPin = state.confirmPin,
        )
        if (error != null) {
            _uiState.update { it.copy(errorRes = error, messageRes = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPin = true, errorRes = null, messageRes = null) }
            updateCurrentQuickPinUseCase(
                currentPin = state.currentPin,
                newPin = state.newPin,
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        hasQuickPin = true,
                        currentPin = "",
                        newPin = "",
                        confirmPin = "",
                        isSavingPin = false,
                        messageRes = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSavingPin = false,
                        errorRes = throwable.toSettingsErrorRes(R.string.merchant_settings_error_quick_pin_update_failed),
                    )
                }
            }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingBiometric = true, errorRes = null, messageRes = null) }
            updateCurrentBiometricEnabledUseCase(enabled)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            biometricEnabled = enabled,
                            isSavingBiometric = false,
                            messageRes = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingBiometric = false,
                            errorRes = throwable.toSettingsErrorRes(R.string.merchant_settings_error_biometric_update_failed),
                        )
                    }
                }
        }
    }
}
