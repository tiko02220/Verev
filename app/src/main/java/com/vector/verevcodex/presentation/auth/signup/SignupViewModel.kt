package com.vector.verevcodex.presentation.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.common.defaultPermissions
import com.vector.verevcodex.domain.model.common.defaultPermissionsSummary
import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.usecase.staff.AddStaffMembersUseCase
import com.vector.verevcodex.domain.usecase.auth.ActivateSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.RegisterBusinessUseCase
import com.vector.verevcodex.domain.usecase.auth.SaveSecuritySetupUseCase
import com.vector.verevcodex.domain.usecase.store.SelectStoreUseCase
import com.vector.verevcodex.presentation.auth.common.SignupStep
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val registerBusinessUseCase: RegisterBusinessUseCase,
    private val saveSecuritySetupUseCase: SaveSecuritySetupUseCase,
    private val addStaffMembersUseCase: AddStaffMembersUseCase,
    private val activateSessionUseCase: ActivateSessionUseCase,
    private val selectStoreUseCase: SelectStoreUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun updateBusinessName(value: String) = update { copy(businessName = value, errors = errors - "businessName") }
    fun updateIndustry(value: String) = update { copy(industry = value, errors = errors - "industry") }
    fun updateAddress(value: String) = update { copy(address = value, errors = errors - "address") }
    fun updateCity(value: String) = update { copy(city = value, errors = errors - "city") }
    fun updatePhone(value: String) = update { copy(phone = value, errors = errors - "phone") }
    fun updateFullName(value: String) = update { copy(fullName = value, errors = errors - "fullName") }
    fun updateEmail(value: String) = update { copy(email = value, errors = errors - "email", submissionError = null, showExistingEmailDialog = false) }
    fun updatePassword(value: String) = update { copy(password = value, errors = errors - "password") }
    fun updateConfirmPassword(value: String) = update { copy(confirmPassword = value, errors = errors - "confirmPassword") }
    fun updatePin(value: String) = update {
        val sanitized = value.filter(Char::isDigit).take(4)
        copy(
            pin = sanitized,
            pinError = null,
            pinSetupStep = if (sanitized.length == 4) PinSetupStep.CONFIRM else PinSetupStep.CREATE,
        )
    }

    fun updateConfirmPin(value: String) = update {
        val sanitized = value.filter(Char::isDigit).take(4)
        when {
            sanitized.length < 4 -> copy(confirmPin = sanitized, pinError = null, pinConfirmed = false)
            sanitized == pin -> copy(confirmPin = sanitized, pinError = null, pinConfirmed = true)
            else -> copy(confirmPin = "", pinError = "pin_mismatch", pinConfirmed = false)
        }
    }
    fun setBiometricEnabled(enabled: Boolean) = update { copy(biometricEnabled = enabled) }
    fun requestBiometricPrompt() = update { copy(requestBiometricPrompt = true, submissionError = null) }
    fun biometricPromptHandled(success: Boolean) {
        if (success) {
            completeBiometricSetup(true)
        } else {
            update { copy(requestBiometricPrompt = false, submissionError = "biometric_failed") }
        }
    }
    fun dismissExistingEmailDialog() = update { copy(showExistingEmailDialog = false) }

    fun updateStaffName(value: String) = update { copy(staffName = value, staffError = null) }
    fun updateStaffEmail(value: String) = update { copy(staffEmail = value, staffError = null) }
    fun updateStaffPassword(value: String) = update { copy(staffPassword = value, staffError = null) }
    fun updateStaffRole(role: StaffRole) = update { copy(staffRole = role) }

    fun back() {
        update {
            when (stage) {
                SignupFlowStage.ACCOUNT -> copy(step = SignupStep.BUSINESS, stage = SignupFlowStage.BUSINESS)
                SignupFlowStage.PIN -> copy(step = SignupStep.ACCOUNT, stage = SignupFlowStage.ACCOUNT)
                SignupFlowStage.BIOMETRIC -> copy(stage = SignupFlowStage.PIN)
                SignupFlowStage.STAFF_PROMPT -> copy(stage = SignupFlowStage.BIOMETRIC)
                SignupFlowStage.STAFF_FORM -> copy(stage = SignupFlowStage.STAFF_PROMPT)
                else -> this
            }
        }
    }

    fun continueToAccount() {
        val errors = mutableMapOf<String, String>()
        if (uiState.value.businessName.isBlank()) errors["businessName"] = "required_business_name"
        if (uiState.value.industry.isBlank()) errors["industry"] = "required_industry"
        if (uiState.value.address.isBlank()) errors["address"] = "required_address"
        if (uiState.value.city.isBlank()) errors["city"] = "required_city"
        if (uiState.value.phone.isBlank()) errors["phone"] = "required_phone"
        if (errors.isEmpty()) {
            update { copy(step = SignupStep.ACCOUNT, stage = SignupFlowStage.ACCOUNT, errors = emptyMap()) }
        } else {
            update { copy(errors = errors) }
        }
    }

    fun createAccount() {
        val state = uiState.value
        val errors = mutableMapOf<String, String>()
        if (state.fullName.isBlank()) errors["fullName"] = "required_full_name"
        if (state.email.isBlank()) errors["email"] = "required_email"
        else if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(state.email)) errors["email"] = "invalid_email"
        if (state.password.isBlank()) errors["password"] = "required_password"
        else if (state.password.length < 8) errors["password"] = "password_short"
        if (state.confirmPassword != state.password) errors["confirmPassword"] = "password_confirm"
        if (errors.isNotEmpty()) {
            update { copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            update { copy(isLoading = true, submissionError = null) }
            registerBusinessUseCase(
                business = BusinessRegistration(state.businessName, state.industry, state.address, state.city, "", state.phone),
                account = AccountRegistration(state.fullName, state.email, state.password, state.confirmPassword),
            ).onSuccess { result ->
                update {
                    copy(
                        isLoading = false,
                        stage = SignupFlowStage.PIN,
                        accountId = result.session.user.id,
                        storeId = result.defaultStoreId,
                        shouldNavigateToApp = false,
                    )
                }
            }.onFailure {
                val message = it.message.orEmpty()
                if (message.contains("Email already exists", ignoreCase = true)) {
                    update { copy(isLoading = false, showExistingEmailDialog = true) }
                } else {
                    update { copy(isLoading = false, submissionError = message.ifBlank { "invalid_credentials" }) }
                }
            }
        }
    }

    fun savePinAndContinue() {
        val state = uiState.value
        when {
            state.pin.length != 4 -> update { copy(pinError = "pin_length") }
            state.pin != state.confirmPin -> update { copy(pinError = "pin_mismatch") }
            else -> update { copy(stage = SignupFlowStage.BIOMETRIC, pinError = null, pinConfirmed = false) }
        }
    }

    fun completeBiometricSetup(enabled: Boolean = uiState.value.biometricEnabled) {
        update { copy(biometricEnabled = enabled, requestBiometricPrompt = false, submissionError = null) }
        val state = uiState.value
        val accountId = state.accountId ?: return
        viewModelScope.launch {
            update { copy(isLoading = true) }
            saveSecuritySetupUseCase(
                SecuritySetup(
                    accountId = accountId,
                    pin = state.pin,
                    biometricEnabled = enabled,
                )
            ).onSuccess {
                update { copy(isLoading = false, stage = SignupFlowStage.STAFF_PROMPT) }
            }.onFailure {
                update { copy(isLoading = false, submissionError = it.message ?: "invalid_credentials") }
            }
        }
    }

    fun skipStaffSetup() {
        completeOnboardingAndEnterApp()
    }

    fun startStaffSetup() {
        update { copy(stage = SignupFlowStage.STAFF_FORM, staffError = null) }
    }

    fun addStaffMember() {
        val state = uiState.value
        if (state.staffName.isBlank() || state.staffEmail.isBlank() || state.staffPassword.length < 8) {
            update { copy(staffError = "staff_incomplete") }
            return
        }
        val permissionsSummary = state.staffRole.defaultPermissionsSummary()
        update {
            copy(
                staffMembers = staffMembers + StaffOnboardingMember(
                    fullName = staffName.trim(),
                    email = staffEmail.trim().lowercase(),
                    password = staffPassword,
                    role = staffRole,
                    permissionsSummary = permissionsSummary,
                    permissions = staffRole.defaultPermissions(),
                ),
                staffName = "",
                staffEmail = "",
                staffPassword = "",
                staffRole = StaffRole.STAFF,
                staffError = null,
            )
        }
    }

    fun completeStaffSetup() {
        val state = uiState.value
        val storeId = state.storeId ?: return
        if (state.staffMembers.isEmpty()) {
            completeOnboardingAndEnterApp()
            return
        }
        viewModelScope.launch {
            update { copy(isLoading = true) }
            addStaffMembersUseCase(storeId, state.staffMembers)
                .onSuccess {
                    update { copy(isLoading = false) }
                    completeOnboardingAndEnterApp()
                }
                .onFailure { update { copy(isLoading = false, staffError = it.message ?: "staff_failed") } }
        }
    }

    private fun completeOnboardingAndEnterApp() {
        val accountId = _uiState.value.accountId ?: return
        val storeId = _uiState.value.storeId
        viewModelScope.launch {
            update { copy(isLoading = true) }
            activateSessionUseCase(accountId)
                .onSuccess {
                    if (!storeId.isNullOrBlank()) {
                        selectStoreUseCase(storeId)
                    }
                    update { copy(isLoading = false, shouldNavigateToApp = true) }
                }
                .onFailure { update { copy(isLoading = false, submissionError = it.message ?: "invalid_credentials") } }
        }
    }

    private fun update(block: SignupUiState.() -> SignupUiState) {
        _uiState.value = _uiState.value.block()
    }
}

enum class SignupFlowStage {
    BUSINESS,
    ACCOUNT,
    PIN,
    BIOMETRIC,
    STAFF_PROMPT,
    STAFF_FORM,
}

data class SignupUiState(
    val step: SignupStep = SignupStep.BUSINESS,
    val stage: SignupFlowStage = SignupFlowStage.BUSINESS,
    val businessName: String = "",
    val industry: String = "",
    val address: String = "",
    val city: String = "",
    val phone: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val pinSetupStep: PinSetupStep = PinSetupStep.CREATE,
    val pinConfirmed: Boolean = false,
    val biometricEnabled: Boolean = false,
    val requestBiometricPrompt: Boolean = false,
    val accountId: String? = null,
    val storeId: String? = null,
    val showExistingEmailDialog: Boolean = false,
    val staffMembers: List<StaffOnboardingMember> = emptyList(),
    val staffName: String = "",
    val staffEmail: String = "",
    val staffPassword: String = "",
    val staffRole: StaffRole = StaffRole.STAFF,
    val staffError: String? = null,
    val pinError: String? = null,
    val errors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val submissionError: String? = null,
    val shouldNavigateToApp: Boolean = false,
)

enum class PinSetupStep {
    CREATE,
    CONFIRM,
}
