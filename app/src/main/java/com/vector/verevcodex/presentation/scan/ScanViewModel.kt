package com.vector.verevcodex.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerBonusActionsUseCase
import com.vector.verevcodex.domain.usecase.customer.FindCustomerByLoyaltyIdUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveActiveScanActionsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.scan.ClearScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.scan.ObserveScanPreferencesUseCase
import com.vector.verevcodex.domain.usecase.scan.SaveScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.scan.ExecuteScanActionUseCase
import com.vector.verevcodex.domain.usecase.scan.ValidateScanActionUseCase
import com.vector.verevcodex.domain.usecase.scan.ScanValidationError
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val findCustomerByLoyaltyIdUseCase: FindCustomerByLoyaltyIdUseCase,
    private val observeCustomerBonusActionsUseCase: ObserveCustomerBonusActionsUseCase,
    private val executeScanActionUseCase: ExecuteScanActionUseCase,
    private val validateScanActionUseCase: ValidateScanActionUseCase,
    private val observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val observeProgramsUseCase: ObserveProgramsUseCase,
    private val observeScanPreferencesUseCase: ObserveScanPreferencesUseCase,
    private val observeActiveScanActionsUseCase: ObserveActiveScanActionsUseCase,
    private val saveScanPreferenceUseCase: SaveScanPreferenceUseCase,
    private val clearScanPreferenceUseCase: ClearScanPreferenceUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    private val scannedCustomerId = MutableStateFlow<String?>(null)

    init {
        observeSelectedStoreUseCase()
            .onEach { store ->
                _uiState.value = _uiState.value.copy(
                    selectedStoreId = store?.id,
                    selectedStoreName = store?.name.orEmpty(),
                )
            }
            .launchIn(viewModelScope)

        observeSessionUseCase()
            .onEach { session ->
                _uiState.value = _uiState.value.copy(
                    currencyCode = session?.user?.defaultCurrencyCode?.ifBlank { "AMD" } ?: "AMD",
                )
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .flatMapLatest { store -> observeActiveScanActionsUseCase(store?.id) }
            .onEach { remoteActions ->
                val current = _uiState.value
                val resolvedActions = resolveAvailableActions(
                    remoteActions = remoteActions,
                    activePrograms = current.activePrograms,
                )
                _uiState.value = current.copy(
                    availableActions = resolvedActions,
                    selectedAction = current.selectedAction?.takeIf { it in resolvedActions } ?: resolvedActions.firstOrNull(),
                )
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .flatMapLatest { store -> observeProgramsUseCase(store?.id) }
            .onEach { programs ->
                val current = _uiState.value
                val activePrograms = programs.filter { it.active }
                val resolvedActions = resolveAvailableActions(
                    remoteActions = current.availableActions,
                    activePrograms = activePrograms,
                )
                _uiState.value = current.copy(
                    activePrograms = activePrograms,
                    availableActions = resolvedActions,
                    selectedAction = current.selectedAction?.takeIf { it in resolvedActions } ?: resolvedActions.firstOrNull(),
                )
            }
            .launchIn(viewModelScope)

        observeScanPreferencesUseCase()
            .onEach { preferences ->
                _uiState.value = _uiState.value.copy(scanPreferences = preferences)
            }
            .launchIn(viewModelScope)

        scannedCustomerId
            .flatMapLatest { customerId ->
                customerId?.let { observeCustomerBonusActionsUseCase(it) } ?: flowOf(emptyList())
            }
            .onEach { bonusActions ->
                _uiState.value = _uiState.value.copy(
                    customerRewardHighlights = bonusActions.filterRewardHighlights(),
                )
            }
            .launchIn(viewModelScope)
    }

    fun prepareScanEntry(initialMethod: ScanMethod? = null) {
        val current = _uiState.value
        if (
            current.activeScanMethod != null ||
            current.contentMode != ScanContentMode.ACTIVE_SCAN ||
            current.customer != null ||
            !current.scannedLoyaltyId.isNullOrBlank() ||
            current.isSearching ||
            current.isSubmitting
        ) {
            return
        }
        val resolvedActions = resolveAvailableActions(
            remoteActions = current.availableActions,
            activePrograms = current.activePrograms,
        )
        val nextAction = current.selectedAction?.takeIf { it in resolvedActions } ?: resolvedActions.firstOrNull()
        _uiState.value = current.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            customer = null,
            customerRewardHighlights = emptyList(),
            scannedLoyaltyId = null,
            visitCountedForCurrentScan = false,
            availableActions = resolvedActions,
            selectedAction = nextAction,
            isSearching = false,
            isSubmitting = false,
            fieldErrors = emptyMap(),
            successDialogMessageRes = null,
            errorRes = null,
            messageRes = null,
        )
        scannedCustomerId.value = null
        enterScan(initialMethod)
    }

    fun enterScan(method: ScanMethod? = null, rememberChoice: Boolean = false) {
        viewModelScope.launch {
            val current = _uiState.value
            val resolvedActions = resolveAvailableActions(
                remoteActions = current.availableActions,
                activePrograms = current.activePrograms,
            )
            val nextAction = current.selectedAction?.takeIf { it in resolvedActions } ?: resolvedActions.firstOrNull()
            _uiState.value = current.copy(
                contentMode = ScanContentMode.ACTIVE_SCAN,
                customer = null,
                customerRewardHighlights = emptyList(),
                scannedLoyaltyId = null,
                visitCountedForCurrentScan = false,
                availableActions = resolvedActions,
                selectedAction = nextAction,
                isSearching = false,
                isSubmitting = false,
                fieldErrors = emptyMap(),
                successDialogMessageRes = null,
                errorRes = null,
                messageRes = null,
            )
            scannedCustomerId.value = null
            val resolvedMethod = method
                ?: _uiState.value.scanPreferences.preferredMethod
                ?: ScanMethod.NFC
            if (rememberChoice) {
                saveScanPreferenceUseCase(resolvedMethod, true)
            }
            startScan(resolvedMethod)
        }
    }

    fun requestScan() {
        val current = _uiState.value
        val resolvedActions = resolveAvailableActions(
            remoteActions = current.availableActions,
            activePrograms = current.activePrograms,
        )
        val nextAction = current.selectedAction?.takeIf { it in resolvedActions } ?: resolvedActions.firstOrNull()
        _uiState.value = current.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            customer = null,
            customerRewardHighlights = emptyList(),
            scannedLoyaltyId = null,
            visitCountedForCurrentScan = false,
            availableActions = resolvedActions,
            selectedAction = nextAction,
            isSearching = false,
            isSubmitting = false,
            fieldErrors = emptyMap(),
            successDialogMessageRes = null,
            errorRes = null,
            messageRes = null,
        )
        scannedCustomerId.value = null
        val resolvedMethod = current.activeScanMethod
            ?: current.scanPreferences.preferredMethod
            ?: ScanMethod.NFC
        startScan(resolvedMethod)
    }

    fun clearSavedScanChoiceAndRequest() {
        viewModelScope.launch {
            clearScanPreferenceUseCase()
            _uiState.value = _uiState.value.copy(
                contentMode = ScanContentMode.ACTIVE_SCAN,
                customerRewardHighlights = emptyList(),
                scannedLoyaltyId = null,
                visitCountedForCurrentScan = false,
                successDialogMessageRes = null,
                errorRes = null,
                messageRes = null,
                fieldErrors = emptyMap(),
            )
            scannedCustomerId.value = null
            startScan(ScanMethod.NFC)
        }
    }

    fun switchScanMethod(method: ScanMethod) {
        viewModelScope.launch {
            startScan(method)
        }
    }

    fun onExternalNfcScan(rawValue: String) {
        handleResolvedScan(ScanMethod.NFC, rawValue)
    }

    fun onNfcScanFailed() {
        _uiState.value = _uiState.value.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            activeScanMethod = ScanMethod.NFC,
            errorRes = R.string.merchant_scan_error_nfc_failed,
            messageRes = null,
            isSearching = false,
            fieldErrors = emptyMap(),
        )
    }

    fun onBarcodeScanned(rawValue: String) {
        handleResolvedScan(ScanMethod.BARCODE, rawValue)
    }

    fun onBarcodeScanFailed(reason: BarcodeScanFailureReason) {
        val errorRes = when (reason) {
            BarcodeScanFailureReason.PERMISSION_DENIED -> R.string.merchant_scan_error_camera_permission_denied
            BarcodeScanFailureReason.CAMERA_UNAVAILABLE -> R.string.merchant_scan_error_camera_unavailable
            BarcodeScanFailureReason.UNSUPPORTED_CODE -> R.string.merchant_scan_error_unsupported_barcode
            BarcodeScanFailureReason.GENERIC -> R.string.merchant_scan_error_barcode_failed
            BarcodeScanFailureReason.CANCELLED -> null
        }
        val messageRes = if (reason == BarcodeScanFailureReason.CANCELLED) {
            R.string.merchant_scan_message_barcode_cancelled
        } else {
            null
        }
        _uiState.value = _uiState.value.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            activeScanMethod = ScanMethod.BARCODE,
            errorRes = errorRes,
            messageRes = messageRes,
            isSearching = false,
            fieldErrors = emptyMap(),
        )
    }

    fun selectAction(action: RewardProgramScanAction) {
        _uiState.value = _uiState.value.copy(
            selectedAction = action,
            successDialogMessageRes = null,
            errorRes = null,
            messageRes = null,
            fieldErrors = emptyMap(),
        )
    }

    fun clearFieldErrors(vararg fieldKeys: String) {
        if (fieldKeys.isEmpty()) return
        val updatedErrors = _uiState.value.fieldErrors - fieldKeys.toSet()
        _uiState.value = _uiState.value.copy(
            fieldErrors = updatedErrors,
            errorRes = if (updatedErrors.isEmpty()) null else _uiState.value.errorRes,
        )
    }

    private fun resolveAvailableActions(
        remoteActions: List<RewardProgramScanAction>,
        activePrograms: List<com.vector.verevcodex.domain.model.loyalty.RewardProgram>,
    ): List<RewardProgramScanAction> {
        val activeProgramActions = activePrograms
            .asSequence()
            .filter { it.active }
            .flatMap { it.configuration.scanActions.asSequence() }
            .distinct()
            .toList()
        val resolvedActions = (remoteActions + activeProgramActions)
            .distinct()
            .filterNot { action ->
                action == RewardProgramScanAction.TRACK_TIER_PROGRESS
            }
        val actionableWithoutCheckIn = resolvedActions.filterNot { it == RewardProgramScanAction.CHECK_IN }
        val visibleActions = if (
            RewardProgramScanAction.CHECK_IN in resolvedActions &&
            actionableWithoutCheckIn.isNotEmpty()
        ) {
            actionableWithoutCheckIn
        } else {
            resolvedActions
        }
        return visibleActions.sortedBy { action ->
            when (action) {
                RewardProgramScanAction.EARN_POINTS -> 0
                RewardProgramScanAction.REDEEM_REWARDS -> 1
                RewardProgramScanAction.CHECK_IN -> 2
                RewardProgramScanAction.TRACK_TIER_PROGRESS -> 3
            }
        }
    }

    fun submitAction(amountInput: String, pointsInput: String) {
        val state = _uiState.value
        val storeId = state.selectedStoreId
        val customer = state.customer
        val selectedAction = state.selectedAction
        if (storeId.isNullOrBlank() || customer == null || selectedAction == null) {
            publishError(R.string.merchant_scan_error_missing_customer)
            return
        }

        val validationError = validateScanActionUseCase(
            action = selectedAction,
            amount = amountInput.toDoubleOrNull(),
            points = pointsInput.toIntOrNull(),
            customerPoints = customer.currentPoints,
            activePrograms = state.activePrograms
        )

        if (validationError != null) {
            val (fieldKey, errorRes) = when (validationError) {
                ScanValidationError.InvalidAmount -> SCAN_FIELD_AMOUNT to R.string.merchant_scan_error_amount
                ScanValidationError.InvalidPoints -> SCAN_FIELD_POINTS to R.string.merchant_scan_error_points
                ScanValidationError.PointsExceedBalance -> SCAN_FIELD_POINTS to R.string.merchant_scan_error_points_exceeds_balance
                is ScanValidationError.MinimumPointsRequired -> SCAN_FIELD_POINTS to R.string.merchant_scan_error_points_minimum_required
                is ScanValidationError.MinimumSpendRequired -> SCAN_FIELD_AMOUNT to R.string.merchant_scan_error_amount
            }
            _uiState.value = state.copy(
                contentMode = ScanContentMode.CUSTOMER,
                isSubmitting = false,
                errorRes = errorRes,
                messageRes = null,
                fieldErrors = mapOf(fieldKey to errorRes),
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                contentMode = ScanContentMode.CUSTOMER,
                isSubmitting = true,
                errorRes = null,
                messageRes = null,
                fieldErrors = emptyMap(),
            )
            val session = observeSessionUseCase().first()
            val staffId = session?.user?.relatedEntityId ?: session?.user?.id
            if (staffId.isNullOrBlank()) {
                publishError(R.string.merchant_scan_error_missing_staff_session)
                return@launch
            }
            
            executeScanActionUseCase(
                action = selectedAction,
                customerId = customer.id,
                storeId = storeId,
                staffId = staffId,
                amount = amountInput.toDoubleOrNull(),
                points = pointsInput.toIntOrNull(),
                customer = customer,
                activePrograms = state.activePrograms,
                visitAlreadyCounted = state.visitCountedForCurrentScan
            ).onSuccess {
                if (selectedAction.countsVisitFor(state.activePrograms)) {
                    markVisitCountedForCurrentScan()
                }
                refreshCustomerAfterAction(customer.loyaltyId, getSuccessMessage(selectedAction))
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    contentMode = ScanContentMode.CUSTOMER,
                    isSubmitting = false,
                    successDialogMessageRes = null,
                    errorRes = R.string.merchant_scan_error_action_failed,
                    messageRes = null,
                )
            }
        }
    }

    private fun getSuccessMessage(action: RewardProgramScanAction): Int = when (action) {
        RewardProgramScanAction.EARN_POINTS -> R.string.merchant_scan_message_points_added
        RewardProgramScanAction.REDEEM_REWARDS -> R.string.merchant_scan_message_points_redeemed
        RewardProgramScanAction.CHECK_IN -> R.string.merchant_scan_message_check_in
        else -> R.string.merchant_scan_message_member_found
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorRes = null, messageRes = null, successDialogMessageRes = null)
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(successDialogMessageRes = null)
    }

    private fun startScan(method: ScanMethod) {
        _uiState.value = _uiState.value.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            scanSessionToken = _uiState.value.scanSessionToken + 1,
            activeScanMethod = method,
            customer = null,
            customerRewardHighlights = emptyList(),
            scannedLoyaltyId = null,
            visitCountedForCurrentScan = false,
            isSearching = false,
            isSubmitting = false,
            fieldErrors = emptyMap(),
            successDialogMessageRes = null,
            errorRes = null,
            messageRes = when (method) {
                ScanMethod.NFC -> R.string.merchant_scan_message_nfc_ready
                ScanMethod.BARCODE -> R.string.merchant_scan_message_barcode_ready
            },
        )
        scannedCustomerId.value = null
    }

    private fun handleResolvedScan(method: ScanMethod, rawValue: String) {
        val loyaltyId = LoyaltyIdCodec.normalize(rawValue)
        if (loyaltyId.isBlank()) {
            publishError(R.string.merchant_scan_error_invalid_identifier)
            return
        }
        _uiState.value = _uiState.value.copy(activeScanMethod = method, fieldErrors = emptyMap())
        viewModelScope.launch {
            performLookup(loyaltyId)
        }
    }

    private suspend fun performLookup(loyaltyId: String, successMessageRes: Int? = null) {
        val previousState = _uiState.value
        _uiState.value = _uiState.value.copy(
            contentMode = ScanContentMode.LOOKUP,
            scannedLoyaltyId = loyaltyId,
            isSearching = true,
            isSubmitting = false,
            customer = null,
            customerRewardHighlights = emptyList(),
            fieldErrors = emptyMap(),
            successDialogMessageRes = null,
            errorRes = null,
            messageRes = successMessageRes,
        )
        runCatching { findCustomerByLoyaltyIdUseCase(loyaltyId, _uiState.value.selectedStoreId) }
            .onSuccess { customer ->
                scannedCustomerId.value = customer?.id
                val preserveVisitCount =
                    customer != null &&
                        previousState.visitCountedForCurrentScan &&
                        previousState.scannedLoyaltyId == loyaltyId &&
                        previousState.customer?.id == customer.id
                _uiState.value = _uiState.value.copy(
                    contentMode = if (customer == null) ScanContentMode.ACTIVE_SCAN else ScanContentMode.CUSTOMER,
                    isSearching = false,
                    isSubmitting = false,
                    customer = customer,
                    visitCountedForCurrentScan = preserveVisitCount,
                    successDialogMessageRes = null,
                    errorRes = if (customer == null) R.string.merchant_scan_error_not_found else null,
                    messageRes = successMessageRes ?: if (customer != null) {
                        R.string.merchant_scan_message_member_found
                    } else {
                        null
                    },
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    contentMode = ScanContentMode.ACTIVE_SCAN,
                    isSearching = false,
                    isSubmitting = false,
                    customer = null,
                    customerRewardHighlights = emptyList(),
                    successDialogMessageRes = null,
                    errorRes = R.string.merchant_scan_error_lookup_failed,
                    messageRes = null,
                )
                scannedCustomerId.value = null
            }
    }

    private suspend fun refreshCustomerAfterAction(loyaltyId: String, successMessageRes: Int) {
        val previousState = _uiState.value
        runCatching { findCustomerByLoyaltyIdUseCase(loyaltyId, _uiState.value.selectedStoreId) }
            .onSuccess { customer ->
                scannedCustomerId.value = customer?.id
                val preserveVisitCount =
                    customer != null &&
                        previousState.visitCountedForCurrentScan &&
                        previousState.scannedLoyaltyId == loyaltyId &&
                        previousState.customer?.id == customer.id
                _uiState.value = previousState.copy(
                    contentMode = if (customer == null) ScanContentMode.ACTIVE_SCAN else ScanContentMode.CUSTOMER,
                    scannedLoyaltyId = loyaltyId,
                    isSearching = false,
                    isSubmitting = false,
                    customer = customer,
                    customerRewardHighlights = if (customer == null) emptyList() else previousState.customerRewardHighlights,
                    visitCountedForCurrentScan = preserveVisitCount,
                    successDialogMessageRes = if (customer != null) successMessageRes else null,
                    errorRes = if (customer == null) R.string.merchant_scan_error_not_found else null,
                    messageRes = null,
                    fieldErrors = emptyMap(),
                )
            }
            .onFailure {
                _uiState.value = previousState.copy(
                    contentMode = ScanContentMode.CUSTOMER,
                    isSearching = false,
                    isSubmitting = false,
                    successDialogMessageRes = null,
                    errorRes = R.string.merchant_scan_error_lookup_failed,
                    messageRes = null,
                    fieldErrors = emptyMap(),
                )
                scannedCustomerId.value = previousState.customer?.id
            }
    }

    private fun publishError(errorRes: Int, fieldErrors: Map<String, Int> = emptyMap()) {
        _uiState.value = _uiState.value.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            isSearching = false,
            isSubmitting = false,
            successDialogMessageRes = null,
            errorRes = errorRes,
            messageRes = null,
            fieldErrors = fieldErrors,
        )
    }

    private fun markVisitCountedForCurrentScan() {
        _uiState.value = _uiState.value.copy(visitCountedForCurrentScan = true)
    }
}

private fun RewardProgramScanAction.countsVisitFor(activePrograms: List<com.vector.verevcodex.domain.model.loyalty.RewardProgram>): Boolean =
    this == RewardProgramScanAction.EARN_POINTS ||
        this == RewardProgramScanAction.CHECK_IN ||
        (
            this != RewardProgramScanAction.CHECK_IN &&
                activePrograms.any { program ->
                    program.active &&
                        program.configuration.visitCheckInEnabled &&
                        RewardProgramScanAction.CHECK_IN in program.configuration.scanActions
                }
            )

private fun List<CustomerBonusAction>.filterRewardHighlights(): List<CustomerBonusAction> =
    filter { action ->
        action.type in setOf(
            CustomerBonusActionType.CHECK_IN_REWARD,
            CustomerBonusActionType.REFERRAL_REFERRER_REWARD,
            CustomerBonusActionType.REFERRAL_REFEREE_REWARD,
            CustomerBonusActionType.PURCHASE_FREQUENCY_REWARD,
            CustomerBonusActionType.TIER_LEVEL_REWARD,
            CustomerBonusActionType.TIER_BENEFIT_RECORDED,
            CustomerBonusActionType.DISCOUNT_APPLIED,
        )
    }.take(3)
