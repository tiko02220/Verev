package com.vector.verevcodex.presentation.auth.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveCurrentSecurityConfigUseCase
import com.vector.verevcodex.domain.usecase.auth.SaveSecuritySetupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class FirstSecuritySetupViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    observeCurrentSecurityConfigUseCase: ObserveCurrentSecurityConfigUseCase,
    private val saveSecuritySetupUseCase: SaveSecuritySetupUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FirstSecuritySetupUiState())
    val uiState: StateFlow<FirstSecuritySetupUiState> = _uiState.asStateFlow()
    private var accountId: String? = null

    init {
        observeSessionUseCase().onEach { session ->
            accountId = session?.user?.id
        }.launchIn(viewModelScope)
        observeCurrentSecurityConfigUseCase().onEach { config ->
            if (config?.hasQuickPin == true) {
                _uiState.value = _uiState.value.copy(isCompleted = true)
            }
        }.launchIn(viewModelScope)
    }

    fun updatePin(value: String) {
        val sanitized = value.filter(Char::isDigit).take(4)
        _uiState.value = _uiState.value.copy(
            pin = sanitized,
            pinError = null,
            pinConfirmed = false,
        )
    }

    fun updateConfirmPin(value: String) {
        val sanitized = value.filter(Char::isDigit).take(4)
        val current = _uiState.value
        if (sanitized.length < 4) {
            _uiState.value = current.copy(confirmPin = sanitized, pinError = null, pinConfirmed = false)
            return
        }
        if (sanitized != current.pin) {
            _uiState.value = current.copy(confirmPin = "", pinError = "auth_pin_mismatch", pinConfirmed = false)
            return
        }
        _uiState.value = current.copy(confirmPin = sanitized, pinError = null, pinConfirmed = true)
        viewModelScope.launch {
            delay(400)
            _uiState.value = _uiState.value.copy(stage = FirstSecuritySetupStage.BIOMETRIC, pinConfirmed = false)
        }
    }

    fun requestBiometricPrompt() {
        _uiState.value = _uiState.value.copy(requestBiometricPrompt = true, submissionError = null)
    }

    fun biometricPromptHandled(success: Boolean) {
        if (success) {
            completeBiometricSetup(true)
        } else {
            _uiState.value = _uiState.value.copy(requestBiometricPrompt = false, submissionError = "biometric_failed")
        }
    }

    fun completeBiometricSetup(enabled: Boolean) {
        val state = _uiState.value
        if (state.pin.length != 4) {
            _uiState.value = state.copy(stage = FirstSecuritySetupStage.PIN, pinError = "auth_pin_invalid")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, biometricEnabled = enabled, requestBiometricPrompt = false, submissionError = null)
            val resolvedAccountId = accountId
            if (resolvedAccountId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    submissionError = "security_setup_failed",
                )
                return@launch
            }
            saveSecuritySetupUseCase(SecuritySetup(accountId = resolvedAccountId, pin = state.pin, biometricEnabled = enabled))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        biometricEnabled = enabled,
                        requestBiometricPrompt = false,
                        isCompleted = true,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        requestBiometricPrompt = false,
                        submissionError = throwable.message?.takeIf { it.isNotBlank() } ?: "security_setup_failed",
                    )
                }
        }
    }
}

data class FirstSecuritySetupUiState(
    val stage: FirstSecuritySetupStage = FirstSecuritySetupStage.PIN,
    val pin: String = "",
    val confirmPin: String = "",
    val pinError: String? = null,
    val pinConfirmed: Boolean = false,
    val biometricEnabled: Boolean = false,
    val requestBiometricPrompt: Boolean = false,
    val submissionError: String? = null,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
)

enum class FirstSecuritySetupStage {
    PIN,
    BIOMETRIC,
}
