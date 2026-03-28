package com.vector.verevcodex.presentation.auth.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.usecase.auth.ActivateInvitedPasswordUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

@HiltViewModel
class ForcePasswordSetupViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    private val activateInvitedPasswordUseCase: ActivateInvitedPasswordUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForcePasswordSetupUiState())
    val uiState: StateFlow<ForcePasswordSetupUiState> = _uiState.asStateFlow()

    init {
        observeSessionUseCase().onEach { session ->
            _uiState.value = _uiState.value.copy(email = session?.user?.email.orEmpty())
        }.launchIn(viewModelScope)
    }

    fun updateNewPassword(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, errorKey = null)
    }

    fun updateConfirmPassword(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorKey = null)
    }

    fun submit() {
        val state = _uiState.value
        val errorKey = when {
            state.newPassword.length < 10 -> "password_short"
            state.newPassword != state.confirmPassword -> "password_confirm"
            else -> null
        }
        if (errorKey != null) {
            _uiState.value = state.copy(errorKey = errorKey)
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorKey = null)
            activateInvitedPasswordUseCase(state.newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isCompleted = true)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorKey = throwable.message?.takeIf { it.isNotBlank() } ?: "activation_failed",
                    )
                }
        }
    }
}

data class ForcePasswordSetupUiState(
    val email: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val errorKey: String? = null,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
)
