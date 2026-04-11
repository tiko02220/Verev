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
import com.vector.verevcodex.domain.usecase.loyalty.ObserveRewardsUseCase
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
import com.vector.verevcodex.domain.usecase.transactions.RecordTransactionUseCase
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.transactions.TransactionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.util.UUID
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
    private val observeRewardsUseCase: ObserveRewardsUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
    private val observeScanPreferencesUseCase: ObserveScanPreferencesUseCase,
    private val observeActiveScanActionsUseCase: ObserveActiveScanActionsUseCase,
    private val saveScanPreferenceUseCase: SaveScanPreferenceUseCase,
    private val clearScanPreferenceUseCase: ClearScanPreferenceUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    internal val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
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
                _uiState.value = current.withResolvedPrograms(
                    activePrograms = current.activePrograms,
                    remoteActions = remoteActions,
                )
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .flatMapLatest { store -> observeProgramsUseCase(store?.id) }
            .onEach { programs ->
                val current = _uiState.value
                _uiState.value = current.withResolvedPrograms(
                    activePrograms = programs.filter { it.active },
                    remoteActions = current.remoteActiveScanActions,
                )
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .flatMapLatest { store -> observeRewardsUseCase(store?.id) }
            .onEach { rewards ->
                _uiState.value = _uiState.value.copy(storeRewards = rewards)
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
            activePrograms = current.livePrograms,
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
                activePrograms = current.livePrograms,
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
            activePrograms = current.livePrograms,
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
        return resolveScanAvailableActions(
            livePrograms = activePrograms,
            remoteActions = remoteActions,
        )
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
            activePrograms = state.livePrograms
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
                activePrograms = state.livePrograms,
                visitAlreadyCounted = state.visitCountedForCurrentScan
            ).onSuccess {
                if (selectedAction.countsVisitFor(state.livePrograms)) {
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

    internal fun submitCheckout(
        amountInput: String,
        useBenefits: Boolean,
        spendMode: ScanSpendMode,
        pointsInput: String,
        selectedCouponId: String?,
    ) {
        val state = _uiState.value
        val storeId = state.selectedStoreId
        val customer = state.customer
        if (storeId.isNullOrBlank() || customer == null) {
            _uiState.value = state.copy(
                contentMode = ScanContentMode.CUSTOMER,
                errorRes = R.string.merchant_scan_error_missing_customer,
                messageRes = null,
                fieldErrors = emptyMap(),
            )
            return
        }

        val availableCoupons = state.storeRewards.availableScanCheckoutCoupons(
            customer = customer,
            activePrograms = state.livePrograms,
        )
        val preview = computeScanCheckoutPreview(
            customer = customer,
            activePrograms = state.livePrograms,
            availableCoupons = availableCoupons,
            amountInput = amountInput,
            useBenefits = useBenefits,
            spendMode = spendMode,
            pointsInput = pointsInput,
            selectedCouponId = selectedCouponId,
        )

        val rawPoints = pointsInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val pointsFieldError = when {
            useBenefits && spendMode == ScanSpendMode.POINTS && rawPoints > customer.currentPoints ->
                R.string.merchant_scan_error_points_exceeds_balance
            useBenefits && spendMode == ScanSpendMode.POINTS && rawPoints > preview.maxRedeemablePoints ->
                R.string.merchant_scan_error_points_exceeds_amount
            useBenefits &&
                spendMode == ScanSpendMode.POINTS &&
                rawPoints in 1 until preview.minimumRedeemPoints ->
                R.string.merchant_scan_error_points_minimum_required
            else -> null
        }

        when {
            !preview.checkoutAvailable -> {
                _uiState.value = state.copy(
                    contentMode = ScanContentMode.CUSTOMER,
                    isSubmitting = false,
                    errorRes = R.string.merchant_scan_error_no_program_actions,
                    messageRes = null,
                    fieldErrors = emptyMap(),
                )
                return
            }
            preview.purchaseAmount <= 0.0 -> {
                _uiState.value = state.copy(
                    contentMode = ScanContentMode.CUSTOMER,
                    isSubmitting = false,
                    errorRes = R.string.merchant_scan_error_amount,
                    messageRes = null,
                    fieldErrors = mapOf(SCAN_FIELD_AMOUNT to R.string.merchant_scan_error_amount),
                )
                return
            }
            pointsFieldError != null -> {
                _uiState.value = state.copy(
                    contentMode = ScanContentMode.CUSTOMER,
                    isSubmitting = false,
                    errorRes = pointsFieldError,
                    messageRes = null,
                    fieldErrors = mapOf(SCAN_FIELD_POINTS to pointsFieldError),
                )
                return
            }
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
                _uiState.value = _uiState.value.copy(
                    contentMode = ScanContentMode.CUSTOMER,
                    isSubmitting = false,
                    errorRes = R.string.merchant_scan_error_missing_staff_session,
                    messageRes = null,
                )
                return@launch
            }

            runCatching {
                val transactionId = UUID.randomUUID().toString()
                recordTransactionUseCase(
                    transaction = Transaction(
                        id = transactionId,
                        customerId = customer.id,
                        storeId = storeId,
                        staffId = staffId,
                        amount = preview.finalAmount,
                        pointsEarned = preview.totalEarnedPoints,
                        pointsRedeemed = preview.totalSpentPoints(),
                        timestamp = LocalDateTime.now(),
                        metadata = buildCheckoutMetadata(preview),
                        items = listOf(
                            TransactionItem(
                                id = UUID.randomUUID().toString(),
                                transactionId = transactionId,
                                name = "Scanned checkout",
                                quantity = 1,
                                unitPrice = preview.finalAmount,
                            ),
                        ),
                    ),
                    incrementVisit = !state.visitCountedForCurrentScan,
                    rewardId = preview.selectedCoupon?.id,
                )
            }.onSuccess {
                if (!state.visitCountedForCurrentScan) {
                    markVisitCountedForCurrentScan()
                }
                refreshCustomerAfterAction(
                    loyaltyId = customer.loyaltyId,
                    successMessageRes = R.string.merchant_scan_message_checkout_applied,
                )
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
                        if (_uiState.value.availableActions.isEmpty()) {
                            R.string.merchant_scan_message_member_found_no_actions
                        } else {
                            R.string.merchant_scan_message_member_found
                        }
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

private fun ScanUiState.withResolvedPrograms(
    activePrograms: List<com.vector.verevcodex.domain.model.loyalty.RewardProgram>,
    remoteActions: List<RewardProgramScanAction>,
): ScanUiState {
    val availability = resolveScanProgramAvailability(
        programs = activePrograms,
        remoteActions = remoteActions,
    )
    val resolvedActions = resolveScanAvailableActions(
        livePrograms = availability.livePrograms,
        remoteActions = remoteActions,
    )
    return copy(
        activePrograms = activePrograms,
        livePrograms = availability.livePrograms,
        remoteActiveScanActions = remoteActions,
        primaryInactiveReason = availability.primaryInactiveReason,
        availableActions = resolvedActions,
        selectedAction = selectedAction?.takeIf { it in resolvedActions } ?: resolvedActions.firstOrNull(),
    )
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

private fun buildCheckoutMetadata(preview: ScanCheckoutPreview): String =
    buildString {
        append("Scan checkout")
        if (preview.tierDiscountAmount > 0.0) {
            append(" • tier -")
            append(preview.tierDiscountPercent)
            append('%')
        }
        if (preview.selectedCoupon != null) {
            append(" • coupon ")
            append(preview.selectedCoupon.name)
        }
        if (preview.appliedRedeemPoints > 0) {
            append(" • redeemed ")
            append(preview.appliedRedeemPoints)
            append(" pts")
        }
    }
