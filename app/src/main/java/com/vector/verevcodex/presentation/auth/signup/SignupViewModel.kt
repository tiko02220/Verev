package com.vector.verevcodex.presentation.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.common.phone.isValidPhoneNumber
import com.vector.verevcodex.common.phone.normalizePhoneNumber
import com.vector.verevcodex.common.phone.sanitizePhoneNumberInput
import com.vector.verevcodex.common.phone.defaultPhoneNumberInput
import com.vector.verevcodex.common.validation.isValidStaffPassword
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.common.defaultPermissions
import com.vector.verevcodex.domain.model.common.summary
import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.model.auth.SignupOnboardingProgress
import com.vector.verevcodex.domain.model.auth.SignupOnboardingStage
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.usecase.staff.AddStaffMembersUseCase
import com.vector.verevcodex.domain.usecase.auth.ActivateSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveAuthBootstrapStateUseCase
import com.vector.verevcodex.domain.usecase.auth.RegisterBusinessUseCase
import com.vector.verevcodex.domain.usecase.auth.SaveSecuritySetupUseCase
import com.vector.verevcodex.domain.usecase.auth.SetSignupOnboardingPendingUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateSignupOnboardingProgressUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.store.SelectStoreUseCase
import com.vector.verevcodex.presentation.auth.common.SignupStep
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class SignupViewModel @Inject constructor(
    observeAuthBootstrapStateUseCase: ObserveAuthBootstrapStateUseCase,
    private val registerBusinessUseCase: RegisterBusinessUseCase,
    private val saveSecuritySetupUseCase: SaveSecuritySetupUseCase,
    private val addStaffMembersUseCase: AddStaffMembersUseCase,
    private val activateSessionUseCase: ActivateSessionUseCase,
    private val setSignupOnboardingPendingUseCase: SetSignupOnboardingPendingUseCase,
    private val updateSignupOnboardingProgressUseCase: UpdateSignupOnboardingProgressUseCase,
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val selectStoreUseCase: SelectStoreUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    init {
        observeAuthBootstrapStateUseCase()
            .onEach { bootstrap ->
                if (!bootstrap.signupOnboardingPending || bootstrap.session == null) return@onEach
                val current = _uiState.value
                val progress = bootstrap.signupProgress
                    ?: SignupOnboardingProgress(
                        accountId = bootstrap.session.user.id,
                        storeId = current.storeId,
                        stage = SignupOnboardingStage.PIN,
                        pinSetupSkipped = false,
                    )
                if (
                    current.accountId == progress.accountId &&
                    current.stage == progress.stage.toUiStage() &&
                    current.storeId == (progress.storeId ?: current.storeId) &&
                    current.pinSetupSkipped == progress.pinSetupSkipped
                ) {
                    return@onEach
                }
                if (current.accountId != null && current.accountId == progress.accountId && current.stage != SignupFlowStage.BUSINESS) {
                    return@onEach
                }
                update {
                    copy(
                        step = SignupStep.ACCOUNT,
                        stage = progress.stage.toUiStage(),
                        accountId = progress.accountId,
                        storeId = progress.storeId ?: current.storeId,
                        pinSetupSkipped = progress.pinSetupSkipped,
                    )
                }
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .onEach { store ->
                val storeId = store?.id ?: return@onEach
                val current = _uiState.value
                if (current.storeId == storeId) return@onEach
                update { copy(storeId = storeId) }
                if (current.accountId != null && current.stage != SignupFlowStage.BUSINESS && current.stage != SignupFlowStage.ACCOUNT) {
                    persistSignupProgress(
                        stage = current.stage.toDomainStage(),
                        storeId = storeId,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateBusinessName(value: String) = update { copy(businessName = value, errors = errors - "businessName") }
    fun updateIndustry(value: String) = update { copy(industry = value, errors = errors - "industry") }
    fun updateAddress(value: String) = update { copy(address = value, errors = errors - "address") }
    fun updateCity(value: String) = update { copy(city = value, errors = errors - "city") }
    fun updateBusinessEmail(value: String) = update { copy(businessEmail = value, errors = errors - "businessEmail") }
    fun updatePhone(value: String) = update { copy(phone = sanitizePhoneNumberInput(value), errors = errors - "phone") }
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
    fun dismissSubmissionError() = update { copy(submissionError = null) }
    fun navigationHandled() = update { copy(shouldNavigateToApp = false) }

    fun updateStaffName(value: String) = update { copy(staffName = value, staffError = null) }
    fun updateStaffEmail(value: String) = update { copy(staffEmail = value, staffError = null) }
    fun updateStaffPhone(value: String) = update { copy(staffPhone = sanitizePhoneNumberInput(value), staffError = null) }
    fun updateStaffPassword(value: String) = update { copy(staffPassword = value, staffError = null) }
    fun updateStaffRole(role: StaffRole) = update {
        copy(
            staffRole = role,
            staffPermissions = role.defaultPermissions(),
            staffError = null,
        )
    }
    fun updateStaffPermissions(permissions: com.vector.verevcodex.domain.model.common.StaffPermissions) = update {
        copy(staffPermissions = permissions, staffError = null)
    }

    fun back() {
        update {
            when (stage) {
                SignupFlowStage.ACCOUNT -> copy(step = SignupStep.BUSINESS, stage = SignupFlowStage.BUSINESS)
                SignupFlowStage.PIN -> copy(step = SignupStep.ACCOUNT, stage = SignupFlowStage.ACCOUNT)
                SignupFlowStage.BIOMETRIC -> copy(stage = SignupFlowStage.PIN)
                SignupFlowStage.STAFF_PROMPT -> copy(
                    stage = if (pinSetupSkipped) SignupFlowStage.PIN else SignupFlowStage.BIOMETRIC,
                )
                SignupFlowStage.STAFF_FORM -> copy(stage = SignupFlowStage.STAFF_PROMPT)
                SignupFlowStage.COMPLETE -> copy(stage = SignupFlowStage.STAFF_PROMPT)
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
        if (uiState.value.businessEmail.isBlank()) errors["businessEmail"] = "required_email"
        else if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(uiState.value.businessEmail)) errors["businessEmail"] = "invalid_email"
        if (!isValidPhoneNumber(uiState.value.phone)) errors["phone"] = "required_phone"
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
                business = BusinessRegistration(
                    state.businessName,
                    state.industry,
                    state.address,
                    state.city,
                    "",
                    normalizePhoneNumber(state.phone),
                    state.businessEmail.trim().lowercase(),
                ),
                account = AccountRegistration(state.fullName, state.email, state.password, state.confirmPassword),
            ).onSuccess { result ->
                update {
                    copy(
                        isLoading = false,
                        stage = SignupFlowStage.PIN,
                        accountId = result.session.user.id,
                        storeId = result.defaultStoreId,
                        pinSetupSkipped = false,
                    )
                }
                persistSignupProgress(
                    stage = SignupOnboardingStage.PIN,
                    accountId = result.session.user.id,
                    storeId = result.defaultStoreId,
                    pinSetupSkipped = false,
                )
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
            else -> {
                update { copy(stage = SignupFlowStage.BIOMETRIC, pinError = null, pinConfirmed = false) }
                persistSignupProgress(stage = SignupOnboardingStage.BIOMETRIC, pinSetupSkipped = false)
            }
        }
    }

    fun skipPinSetup() {
        update {
            copy(
                pin = "",
                confirmPin = "",
                pinError = null,
                pinConfirmed = false,
                biometricEnabled = false,
                requestBiometricPrompt = false,
                pinSetupSkipped = true,
                stage = SignupFlowStage.STAFF_PROMPT,
            )
        }
        persistSignupProgress(stage = SignupOnboardingStage.STAFF_PROMPT, pinSetupSkipped = true)
    }

    fun completeBiometricSetup(enabled: Boolean = uiState.value.biometricEnabled) {
        update {
            copy(
                biometricEnabled = enabled,
                requestBiometricPrompt = false,
                submissionError = null,
                pinSetupSkipped = false,
            )
        }
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
                persistSignupProgress(stage = SignupOnboardingStage.STAFF_PROMPT, pinSetupSkipped = false)
            }.onFailure {
                update { copy(isLoading = false, submissionError = it.message ?: "invalid_credentials") }
            }
        }
    }

    fun skipStaffSetup() {
        finalizeOnboarding()
    }

    fun startStaffSetup() {
        update { copy(stage = SignupFlowStage.STAFF_FORM, staffError = null) }
        persistSignupProgress(stage = SignupOnboardingStage.STAFF_FORM)
    }

    fun createStaffMember() {
        val state = uiState.value
        val storeId = state.storeId
        if (storeId.isNullOrBlank()) {
            update { copy(staffError = "staff_missing_store") }
            return
        }
        val draftMember = validateAndBuildDraftMember(state) ?: run {
            update { copy(staffError = validateStaffDraftError(state)) }
            return
        }
        viewModelScope.launch {
            update { copy(isLoading = true) }
            addStaffMembersUseCase(storeId, listOf(draftMember))
                .onSuccess {
                    update {
                        copy(
                            isLoading = false,
                            stage = SignupFlowStage.STAFF_PROMPT,
                            staffMembers = staffMembers + draftMember,
                            staffName = "",
                            staffEmail = "",
                            staffPhone = defaultPhoneNumberInput(),
                            staffPassword = "",
                            staffRole = StaffRole.STAFF,
                            staffPermissions = StaffRole.STAFF.defaultPermissions(),
                            staffError = null,
                        )
                    }
                    persistSignupProgress(stage = SignupOnboardingStage.STAFF_PROMPT)
                }
                .onFailure {
                    update {
                        copy(
                            isLoading = false,
                            staffError = it.message?.takeIf(String::isNotBlank) ?: "staff_failed",
                        )
                    }
                }
        }
    }

    fun enterApp() {
        viewModelScope.launch {
            update { copy(isLoading = true, submissionError = null) }
            runCatching { setSignupOnboardingPendingUseCase(false) }
                .onSuccess {
                    update { copy(isLoading = false, shouldNavigateToApp = true) }
                }
                .onFailure {
                    update { copy(isLoading = false, submissionError = it.message ?: "invalid_credentials") }
                }
        }
    }

    private fun validateAndBuildDraftMember(state: SignupUiState): StaffOnboardingMember? {
        if (
            state.staffName.isBlank() ||
            state.staffEmail.isBlank() ||
            !isValidPhoneNumber(state.staffPhone) ||
            !isValidStaffPassword(state.staffPassword)
        ) {
            return null
        }
        return StaffOnboardingMember(
            fullName = state.staffName.trim(),
            email = state.staffEmail.trim().lowercase(),
            phoneNumber = normalizePhoneNumber(state.staffPhone),
            password = state.staffPassword,
            role = state.staffRole,
            permissionsSummary = state.staffPermissions.summary(),
            permissions = state.staffPermissions,
        )
    }

    private fun validateStaffDraftError(state: SignupUiState): String = when {
        state.staffName.isBlank() || state.staffEmail.isBlank() -> "staff_incomplete"
        !isValidPhoneNumber(state.staffPhone) -> "staff_phone_invalid"
        !isValidStaffPassword(state.staffPassword) -> "staff_password_short"
        else -> "staff_incomplete"
    }

    private fun finalizeOnboarding() {
        val accountId = _uiState.value.accountId ?: return
        val storeId = _uiState.value.storeId
        viewModelScope.launch {
            update { copy(isLoading = true) }
            activateSessionUseCase(accountId)
                .onSuccess {
                    if (!storeId.isNullOrBlank()) {
                        selectStoreUseCase(storeId)
                    }
                    update {
                        copy(
                            isLoading = false,
                            stage = SignupFlowStage.COMPLETE,
                        )
                    }
                    persistSignupProgress(stage = SignupOnboardingStage.COMPLETE)
                }
                .onFailure { update { copy(isLoading = false, submissionError = it.message ?: "invalid_credentials") } }
        }
    }

    private fun persistSignupProgress(
        stage: SignupOnboardingStage,
        accountId: String? = _uiState.value.accountId,
        storeId: String? = _uiState.value.storeId,
        pinSetupSkipped: Boolean = _uiState.value.pinSetupSkipped,
    ) {
        val resolvedAccountId = accountId ?: return
        viewModelScope.launch {
            updateSignupOnboardingProgressUseCase(
                SignupOnboardingProgress(
                    accountId = resolvedAccountId,
                    storeId = storeId,
                    stage = stage,
                    pinSetupSkipped = pinSetupSkipped,
                ),
            )
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
    COMPLETE,
}

private fun SignupOnboardingStage.toUiStage(): SignupFlowStage = when (this) {
    SignupOnboardingStage.PIN -> SignupFlowStage.PIN
    SignupOnboardingStage.BIOMETRIC -> SignupFlowStage.BIOMETRIC
    SignupOnboardingStage.STAFF_PROMPT -> SignupFlowStage.STAFF_PROMPT
    SignupOnboardingStage.STAFF_FORM -> SignupFlowStage.STAFF_FORM
    SignupOnboardingStage.COMPLETE -> SignupFlowStage.COMPLETE
}

private fun SignupFlowStage.toDomainStage(): SignupOnboardingStage = when (this) {
    SignupFlowStage.PIN -> SignupOnboardingStage.PIN
    SignupFlowStage.BIOMETRIC -> SignupOnboardingStage.BIOMETRIC
    SignupFlowStage.STAFF_PROMPT -> SignupOnboardingStage.STAFF_PROMPT
    SignupFlowStage.STAFF_FORM -> SignupOnboardingStage.STAFF_FORM
    SignupFlowStage.COMPLETE -> SignupOnboardingStage.COMPLETE
    SignupFlowStage.BUSINESS,
    SignupFlowStage.ACCOUNT,
    -> SignupOnboardingStage.PIN
}

data class SignupUiState(
    val step: SignupStep = SignupStep.BUSINESS,
    val stage: SignupFlowStage = SignupFlowStage.BUSINESS,
    val businessName: String = "",
    val industry: String = "",
    val address: String = "",
    val city: String = "",
    val businessEmail: String = "",
    val phone: String = defaultPhoneNumberInput(),
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
    val pinSetupSkipped: Boolean = false,
    val accountId: String? = null,
    val storeId: String? = null,
    val showExistingEmailDialog: Boolean = false,
    val staffMembers: List<StaffOnboardingMember> = emptyList(),
    val staffName: String = "",
    val staffEmail: String = "",
    val staffPhone: String = defaultPhoneNumberInput(),
    val staffPassword: String = "",
    val staffRole: StaffRole = StaffRole.STAFF,
    val staffPermissions: com.vector.verevcodex.domain.model.common.StaffPermissions = StaffRole.STAFF.defaultPermissions(),
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
