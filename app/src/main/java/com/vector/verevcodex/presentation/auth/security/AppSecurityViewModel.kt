package com.vector.verevcodex.presentation.auth.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.usecase.auth.LogoutUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveCurrentSecurityConfigUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.VerifyQuickPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class AppSecurityViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    observeCurrentSecurityConfigUseCase: ObserveCurrentSecurityConfigUseCase,
    private val verifyQuickPinUseCase: VerifyQuickPinUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppSecurityUiState())
    val uiState: StateFlow<AppSecurityUiState> = _uiState.asStateFlow()
    private var firstObservationHandled = false
    private var lastBackgroundedAtMillis: Long? = null

    init {
        combine(
            observeSessionUseCase(),
            observeCurrentSecurityConfigUseCase(),
        ) { session, security ->
            session to security
        }.onEach { (session, security) ->
            val current = _uiState.value
            if (!firstObservationHandled) {
                firstObservationHandled = true
                _uiState.value = current.copy(
                    isInitialized = true,
                    session = session,
                    securityConfig = security,
                    authEntryDestination = AuthEntryDestination.LOGIN,
                    requiresUnlock = session != null && security != null,
                    pinDigits = List(4) { "" },
                    pinError = null,
                    pinErrorCount = 0,
                    promptBiometric = session != null && security?.biometricEnabled == true,
                )
            } else {
                _uiState.value = current.copy(
                    isInitialized = true,
                    session = session,
                    securityConfig = security,
                    requiresUnlock = when {
                        current.exitDestination != null -> true
                        session == null || security == null -> false
                        else -> current.requiresUnlock
                    },
                    promptBiometric = if (!current.requiresUnlock) false else current.promptBiometric,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAppBackgrounded(backgroundedAtMillis: Long) {
        val state = _uiState.value
        if (state.session != null && state.securityConfig != null) {
            lastBackgroundedAtMillis = backgroundedAtMillis
        }
    }

    fun onAppForegrounded(foregroundedAtMillis: Long) {
        val state = _uiState.value
        val backgroundedAtMillis = lastBackgroundedAtMillis
        if (state.session == null || state.securityConfig == null || state.requiresUnlock || backgroundedAtMillis == null) {
            return
        }
        if (foregroundedAtMillis - backgroundedAtMillis >= APP_LOCK_TIMEOUT_MS) {
            _uiState.value = state.copy(
                requiresUnlock = true,
                pinDigits = List(4) { "" },
                pinError = null,
                pinErrorCount = 0,
                promptBiometric = state.securityConfig.biometricEnabled,
            )
        }
        lastBackgroundedAtMillis = null
    }

    fun requestBiometric() {
        if (_uiState.value.securityConfig?.biometricEnabled == true) {
            _uiState.value = _uiState.value.copy(promptBiometric = true, pinError = null)
        }
    }

    fun biometricHandled(success: Boolean) {
        _uiState.value = _uiState.value.copy(
            requiresUnlock = if (success) false else _uiState.value.requiresUnlock,
            promptBiometric = false,
            pinDigits = if (success) List(4) { "" } else _uiState.value.pinDigits,
            pinError = if (success) null else "biometric_failed",
            pinErrorCount = if (success) 0 else _uiState.value.pinErrorCount + 1,
        )
    }

    fun updatePinCode(value: String) {
        val sanitized = value.filter(Char::isDigit).take(4)
        _uiState.value = _uiState.value.copy(
            pinDigits = List(4) { index -> sanitized.getOrNull(index)?.toString().orEmpty() },
            pinError = null,
        )
        if (sanitized.length == 4) {
            verifyPin(sanitized)
        }
    }

    fun logoutToLogin() {
        _uiState.value = _uiState.value.copy(
            authEntryDestination = AuthEntryDestination.LOGIN,
            authEntryNonce = _uiState.value.authEntryNonce + 1,
            requiresUnlock = true,
            promptBiometric = false,
            exitDestination = SecurityExitDestination.LOGIN,
        )
    }

    fun recoverAccess() {
        _uiState.value = _uiState.value.copy(
            authEntryDestination = AuthEntryDestination.FORGOT_PIN,
            authEntryNonce = _uiState.value.authEntryNonce + 1,
            requiresUnlock = true,
            promptBiometric = false,
            exitDestination = SecurityExitDestination.FORGOT_PIN,
        )
    }

    fun consumeExitDestination() {
        viewModelScope.launch {
            logoutUseCase()
            lastBackgroundedAtMillis = null
            _uiState.value = _uiState.value.copy(
                requiresUnlock = false,
                exitDestination = null,
                pinDigits = List(4) { "" },
                pinError = null,
                pinErrorCount = 0,
                promptBiometric = false,
            )
        }
    }

    fun showLoginAuthScreen() {
        _uiState.value = _uiState.value.copy(
            authEntryDestination = AuthEntryDestination.LOGIN,
            authEntryNonce = _uiState.value.authEntryNonce + 1,
            requiresUnlock = false,
            exitDestination = null,
            pinDigits = List(4) { "" },
            pinError = null,
            pinErrorCount = 0,
            promptBiometric = false,
        )
    }

    private fun verifyPin(pin: String = _uiState.value.pinDigits.joinToString("")) {
        viewModelScope.launch {
            val isValid = verifyQuickPinUseCase(pin)
            _uiState.value = if (isValid) {
                _uiState.value.copy(
                    requiresUnlock = false,
                    pinError = null,
                    pinErrorCount = 0,
                    pinDigits = List(4) { "" },
                    promptBiometric = false,
                )
            } else {
                _uiState.value.copy(
                    pinError = "auth_pin_invalid",
                    pinErrorCount = _uiState.value.pinErrorCount + 1,
                    pinDigits = List(4) { "" },
                    promptBiometric = false,
                )
            }
        }
    }

    private companion object {
        const val APP_LOCK_TIMEOUT_MS = 30_000L
    }
}

data class AppSecurityUiState(
    val isInitialized: Boolean = false,
    val session: AuthSession? = null,
    val securityConfig: SecurityConfig? = null,
    val authEntryDestination: AuthEntryDestination = AuthEntryDestination.LOGIN,
    val authEntryNonce: Int = 0,
    val requiresUnlock: Boolean = false,
    val pinDigits: List<String> = List(4) { "" },
    val pinError: String? = null,
    val pinErrorCount: Int = 0,
    val promptBiometric: Boolean = false,
    val exitDestination: SecurityExitDestination? = null,
)

enum class SecurityExitDestination {
    LOGIN,
    FORGOT_PIN,
}

enum class AuthEntryDestination {
    LOGIN,
    FORGOT_PIN,
}
