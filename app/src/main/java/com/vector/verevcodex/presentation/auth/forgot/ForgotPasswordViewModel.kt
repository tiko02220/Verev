package com.vector.verevcodex.presentation.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.usecase.auth.ResetPasswordUseCase
import com.vector.verevcodex.domain.usecase.auth.ResetQuickPinUseCase
import com.vector.verevcodex.domain.usecase.auth.SendPasswordResetCodeUseCase
import com.vector.verevcodex.domain.usecase.auth.VerifyPasswordResetCodeUseCase
import com.vector.verevcodex.presentation.auth.common.ForgotPasswordStep
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetCodeUseCase: SendPasswordResetCodeUseCase,
    private val verifyPasswordResetCodeUseCase: VerifyPasswordResetCodeUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val resetQuickPinUseCase: ResetQuickPinUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()
    private var resendCountdownJob: Job? = null

    fun updateEmail(value: String) = update { copy(email = value.trim(), error = null) }
    fun updateOtp(index: Int, value: String) = update {
        val list = otp.toMutableList()
        list[index] = value.take(1)
        copy(otp = list, error = null)
    }
    fun updateNewPassword(value: String) = update { copy(newPassword = value.replace("\n", ""), error = null) }
    fun updateConfirmPassword(value: String) = update { copy(confirmPassword = value.replace("\n", ""), error = null) }

    fun back() {
        update {
            copy(
                step = when (step) {
                    ForgotPasswordStep.OTP -> ForgotPasswordStep.EMAIL
                    ForgotPasswordStep.NEW_PASSWORD -> ForgotPasswordStep.OTP
                    ForgotPasswordStep.SUCCESS -> ForgotPasswordStep.EMAIL
                    ForgotPasswordStep.EMAIL -> ForgotPasswordStep.EMAIL
                },
                error = null,
            )
        }
    }

    fun sendCode() {
        requestResetCode()
    }

    fun resendCode() {
        if (_uiState.value.resendCountdownSeconds > 0) return
        requestResetCode()
    }

    private fun requestResetCode() {
        if (uiState.value.email.isBlank()) {
            update { copy(error = "required_email") }
            return
        }
        viewModelScope.launch {
            update { copy(isLoading = true, error = null) }
            sendPasswordResetCodeUseCase(uiState.value.email)
                .onSuccess {
                    update {
                        copy(
                            isLoading = false,
                            step = ForgotPasswordStep.OTP,
                            otp = List(6) { "" },
                            resendCountdownSeconds = RESEND_COUNTDOWN_SECONDS,
                        )
                    }
                    startResendCountdown()
                }
                .onFailure { update { copy(isLoading = false, error = "invalid_email") } }
        }
    }

    fun verifyCode() {
        val code = uiState.value.otp.joinToString(separator = "")
        if (code.length != 6) {
            update { copy(error = "code_incomplete") }
            return
        }
        viewModelScope.launch {
            update { copy(isLoading = true, error = null) }
            verifyPasswordResetCodeUseCase(uiState.value.email, code)
                .onSuccess { update { copy(isLoading = false, step = ForgotPasswordStep.NEW_PASSWORD) } }
                .onFailure { update { copy(isLoading = false, error = "code_invalid") } }
        }
    }

    fun submitReset(mode: RecoveryMode) {
        val state = uiState.value
        when {
            state.newPassword.isBlank() -> update { copy(error = "required_password") }
            mode == RecoveryMode.PIN && state.newPassword.length < 4 -> update { copy(error = "pin_length") }
            mode == RecoveryMode.PASSWORD && state.newPassword.length < 8 -> update { copy(error = "password_short") }
            state.newPassword != state.confirmPassword -> update {
                copy(error = if (mode == RecoveryMode.PIN) "pin_mismatch" else "password_confirm")
            }
            else -> viewModelScope.launch {
                update { copy(isLoading = true, error = null) }
                val result = when (mode) {
                    RecoveryMode.PASSWORD -> resetPasswordUseCase(state.email, state.newPassword)
                    RecoveryMode.PIN -> resetQuickPinUseCase(state.email, state.newPassword)
                }
                result
                    .onSuccess { update { copy(isLoading = false, step = ForgotPasswordStep.SUCCESS) } }
                    .onFailure { update { copy(isLoading = false, error = "reset_failed") } }
            }
        }
    }

    private fun startResendCountdown() {
        resendCountdownJob?.cancel()
        resendCountdownJob = viewModelScope.launch {
            while (_uiState.value.resendCountdownSeconds > 0) {
                delay(1_000)
                update {
                    copy(resendCountdownSeconds = (resendCountdownSeconds - 1).coerceAtLeast(0))
                }
            }
        }
    }

    private fun update(block: ForgotPasswordUiState.() -> ForgotPasswordUiState) {
        _uiState.value = _uiState.value.block()
    }

    private companion object {
        const val RESEND_COUNTDOWN_SECONDS = 30
    }
}

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.EMAIL,
    val email: String = "",
    val otp: List<String> = List(6) { "" },
    val newPassword: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val resendCountdownSeconds: Int = 0,
)
