package com.vector.verevcodex.presentation.auth.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.usecase.auth.LogoutUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveAuthBootstrapStateUseCase
import com.vector.verevcodex.domain.usecase.auth.VerifyQuickPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class AppSecurityViewModel @Inject constructor(
    observeAuthBootstrapStateUseCase: ObserveAuthBootstrapStateUseCase,
    private val verifyQuickPinUseCase: VerifyQuickPinUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppSecurityUiState())
    val uiState: StateFlow<AppSecurityUiState> = _uiState.asStateFlow()
    private var hasHandledInitialSnapshot = false
    private var lockSessionId = 0
    private var suppressSignupReentry = false
    private var backgroundedAtMs: Long? = null

    init {
        observeAuthBootstrapStateUseCase().onEach { snapshot ->
            val session = snapshot.session
            val security = snapshot.securityConfig
            val signupOnboardingPending = snapshot.signupOnboardingPending
            if (session == null || !signupOnboardingPending) {
                suppressSignupReentry = false
            }
            val current = _uiState.value
            val authEntryDestination = resolveAuthEntryDestination(
                currentDestination = current.authEntryDestination,
                hasSession = session != null,
                signupOnboardingPending = signupOnboardingPending,
                suppressSignupReentry = suppressSignupReentry,
            )
            val shouldRequireUnlock = when {
                session == null || security == null -> false
                !hasHandledInitialSnapshot -> true
                else -> current.requiresUnlock
            }
            val shouldPromptBiometric = when {
                !shouldRequireUnlock -> false
                authEntryDestination != null -> false
                current.promptBiometric -> true
                !hasHandledInitialSnapshot && security?.biometricEnabled == true -> true
                else -> false
            }
            hasHandledInitialSnapshot = true
            _uiState.value = current.copy(
                isInitialized = true,
                session = session,
                securityConfig = security,
                signupOnboardingPending = signupOnboardingPending,
                authEntryDestination = authEntryDestination,
                requiresUnlock = shouldRequireUnlock,
                pinDigits = if (shouldRequireUnlock) current.pinDigits else List(4) { "" },
                pinError = if (shouldRequireUnlock) current.pinError else null,
                pinErrorCount = if (shouldRequireUnlock) current.pinErrorCount else 0,
                promptBiometric = shouldPromptBiometric,
            )
        }.launchIn(viewModelScope)
    }

    fun onAppBackgrounded(backgroundedAtMs: Long) {
        val state = _uiState.value
        if (state.session == null || state.securityConfig == null || state.authEntryDestination != null) return
        this.backgroundedAtMs = backgroundedAtMs
        _uiState.value = state.copy(
            promptBiometric = false,
        )
    }

    fun onAppForegrounded(foregroundedAtMs: Long, lockDelayMs: Long) {
        val state = _uiState.value
        if (state.session == null || state.securityConfig == null || state.authEntryDestination != null) return
        val backgroundedAt = backgroundedAtMs ?: return
        backgroundedAtMs = null
        if (foregroundedAtMs - backgroundedAt < lockDelayMs) {
            _uiState.value = state.copy(
                requiresUnlock = false,
                promptBiometric = false,
                pinDigits = List(4) { "" },
                pinError = null,
                pinErrorCount = 0,
            )
            return
        }
        lockSessionId += 1
        _uiState.value = state.copy(
            requiresUnlock = true,
            pinDigits = List(4) { "" },
            pinError = null,
            pinErrorCount = 0,
            promptBiometric = state.securityConfig.biometricEnabled,
        )
    }

    fun dismissTransientUnlockForExternalIntent() {
        val state = _uiState.value
        if (state.session == null || state.securityConfig == null || state.authEntryDestination != null) return
        backgroundedAtMs = null
        _uiState.value = state.copy(
            requiresUnlock = false,
            promptBiometric = false,
            pinDigits = List(4) { "" },
            pinError = null,
            pinErrorCount = 0,
        )
    }

    fun requestBiometric() {
        if (_uiState.value.securityConfig?.biometricEnabled == true && _uiState.value.requiresUnlock) {
            _uiState.value = _uiState.value.copy(
                promptBiometric = true,
                pinError = null,
                pinErrorCount = 0,
            )
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
            verifyPin(sanitized, lockSessionId)
        }
    }

    fun logoutToLogin() {
        _uiState.value = _uiState.value.copy(
            session = null,
            securityConfig = null,
            signupOnboardingPending = false,
            authEntryDestination = AuthEntryDestination.LOGIN,
            authEntryNonce = _uiState.value.authEntryNonce + 1,
            requiresUnlock = false,
            promptBiometric = false,
            pinDigits = List(4) { "" },
            pinError = null,
            pinErrorCount = 0,
        )
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun recoverAccess() {
        _uiState.value = _uiState.value.copy(
            authEntryDestination = AuthEntryDestination.FORGOT_PIN,
            authEntryNonce = _uiState.value.authEntryNonce + 1,
            requiresUnlock = false,
            promptBiometric = false,
            pinDigits = List(4) { "" },
            pinError = null,
            pinErrorCount = 0,
        )
    }

    fun exitPinRecovery() {
        val state = _uiState.value
        _uiState.value = state.copy(
            authEntryDestination = null,
            requiresUnlock = state.session != null && state.securityConfig != null,
            promptBiometric = false,
            pinDigits = List(4) { "" },
            pinError = null,
            pinErrorCount = 0,
        )
    }

    fun completeAuthenticatedEntry() {
        val state = _uiState.value
        if (state.session == null) return
        suppressSignupReentry = true
        _uiState.value = state.copy(
            authEntryDestination = null,
            requiresUnlock = false,
            promptBiometric = false,
            pinDigits = List(4) { "" },
            pinError = null,
            pinErrorCount = 0,
        )
    }

    private fun verifyPin(
        pin: String = _uiState.value.pinDigits.joinToString(""),
        expectedLockSessionId: Int = lockSessionId,
    ) {
        viewModelScope.launch {
            val isValid = verifyQuickPinUseCase(pin)
            if (expectedLockSessionId != lockSessionId) return@launch
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
}

internal fun resolveAuthEntryDestination(
    currentDestination: AuthEntryDestination?,
    hasSession: Boolean,
    signupOnboardingPending: Boolean,
    suppressSignupReentry: Boolean = false,
): AuthEntryDestination? = when {
    currentDestination == AuthEntryDestination.FORGOT_PIN -> currentDestination
    suppressSignupReentry && hasSession -> null
    signupOnboardingPending -> AuthEntryDestination.SIGNUP
    hasSession -> null
    currentDestination != null -> currentDestination
    else -> AuthEntryDestination.LOGIN
}
