package com.vector.verevcodex.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null, authError = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null, authError = null)
    }

    fun submit() {
        val state = _uiState.value
        var hasError = false
        val emailError = when {
            state.email.isBlank() -> "required_email"
            !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(state.email) -> "invalid_email"
            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> "required_password"
            state.password.length < 8 -> "password_short"
            else -> null
        }
        if (emailError != null || passwordError != null) hasError = true
        if (hasError) {
            _uiState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loginUseCase(state.email, state.password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, authError = "invalid_credentials")
                }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val authError: String? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
)
