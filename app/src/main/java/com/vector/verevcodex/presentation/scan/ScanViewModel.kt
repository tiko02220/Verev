package com.vector.verevcodex.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.transactions.TransactionItem
import com.vector.verevcodex.domain.usecase.customer.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.scan.ClearScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.customer.FindCustomerByLoyaltyIdUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveActiveScanActionsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.scan.ObserveScanPreferencesUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.transactions.RecordTransactionUseCase
import com.vector.verevcodex.domain.usecase.scan.SaveScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
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
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val findCustomerByLoyaltyIdUseCase: FindCustomerByLoyaltyIdUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
    private val adjustCustomerPointsUseCase: AdjustCustomerPointsUseCase,
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

    init {
        observeSelectedStoreUseCase()
            .onEach { store ->
                _uiState.value = _uiState.value.copy(
                    selectedStoreId = store?.id,
                    selectedStoreName = store?.name.orEmpty(),
                )
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .flatMapLatest { store -> observeActiveScanActionsUseCase(store?.id) }
            .onEach { actions ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    availableActions = actions,
                    selectedAction = current.selectedAction?.takeIf { it in actions } ?: actions.firstOrNull(),
                )
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .flatMapLatest { store -> observeProgramsUseCase(store?.id) }
            .onEach { programs ->
                _uiState.value = _uiState.value.copy(activePrograms = programs.filter { it.active })
            }
            .launchIn(viewModelScope)

        observeScanPreferencesUseCase()
            .onEach { preferences ->
                _uiState.value = _uiState.value.copy(scanPreferences = preferences)
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
        val nextAction = current.availableActions.firstOrNull()
        _uiState.value = current.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            customer = null,
            scannedLoyaltyId = null,
            visitCountedForCurrentScan = false,
            selectedAction = nextAction,
            isSearching = false,
            isSubmitting = false,
            fieldErrors = emptyMap(),
            errorRes = null,
            messageRes = null,
        )
        enterScan(initialMethod)
    }

    fun enterScan(method: ScanMethod? = null, rememberChoice: Boolean = false) {
        viewModelScope.launch {
            val nextAction = _uiState.value.availableActions.firstOrNull()
            _uiState.value = _uiState.value.copy(
                contentMode = ScanContentMode.ACTIVE_SCAN,
                customer = null,
                scannedLoyaltyId = null,
                visitCountedForCurrentScan = false,
                selectedAction = nextAction,
                isSearching = false,
                isSubmitting = false,
                fieldErrors = emptyMap(),
                errorRes = null,
                messageRes = null,
            )
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
        val nextAction = _uiState.value.availableActions.firstOrNull()
        _uiState.value = _uiState.value.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            customer = null,
            scannedLoyaltyId = null,
            visitCountedForCurrentScan = false,
            selectedAction = nextAction,
            isSearching = false,
            isSubmitting = false,
            fieldErrors = emptyMap(),
            errorRes = null,
            messageRes = null,
        )
        if (nextAction == null) {
            _uiState.value = _uiState.value.copy(errorRes = R.string.merchant_scan_error_no_program_actions)
            return
        }
        val resolvedMethod = _uiState.value.activeScanMethod
            ?: _uiState.value.scanPreferences.preferredMethod
            ?: ScanMethod.NFC
        startScan(resolvedMethod)
    }

    fun clearSavedScanChoiceAndRequest() {
        viewModelScope.launch {
            clearScanPreferenceUseCase()
            _uiState.value = _uiState.value.copy(
                contentMode = ScanContentMode.ACTIVE_SCAN,
                scannedLoyaltyId = null,
                visitCountedForCurrentScan = false,
                errorRes = null,
                messageRes = null,
                fieldErrors = emptyMap(),
            )
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

    fun submitAction(amountInput: String, pointsInput: String) {
        val state = _uiState.value
        val storeId = state.selectedStoreId
        val customer = state.customer
        val selectedAction = state.selectedAction
        if (storeId.isNullOrBlank() || customer == null || selectedAction == null) {
            publishError(R.string.merchant_scan_error_missing_customer)
            return
        }
        val validationErrors = validateActionInput(
            state = state,
            customerPoints = customer.currentPoints,
            selectedAction = selectedAction,
            amountInput = amountInput,
            pointsInput = pointsInput,
        )
        if (validationErrors.isNotEmpty()) {
            _uiState.value = state.copy(
                contentMode = ScanContentMode.CUSTOMER,
                isSubmitting = false,
                errorRes = validationErrors.values.first(),
                messageRes = null,
                fieldErrors = validationErrors,
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
            runCatching {
                executeSelectedAction(
                    state = state,
                    storeId = storeId,
                    customerId = customer.id,
                    staffId = staffId,
                    selectedAction = selectedAction,
                    amountInput = amountInput,
                    pointsInput = pointsInput,
                )
            }.onSuccess { successMessageRes ->
                performLookup(customer.loyaltyId, successMessageRes)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    contentMode = ScanContentMode.CUSTOMER,
                    isSubmitting = false,
                    errorRes = R.string.merchant_scan_error_action_failed,
                    messageRes = null,
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorRes = null, messageRes = null)
    }

    private fun startScan(method: ScanMethod) {
        _uiState.value = _uiState.value.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            scanSessionToken = _uiState.value.scanSessionToken + 1,
            activeScanMethod = method,
            customer = null,
            scannedLoyaltyId = null,
            visitCountedForCurrentScan = false,
            isSearching = false,
            isSubmitting = false,
            fieldErrors = emptyMap(),
            errorRes = null,
            messageRes = when (method) {
                ScanMethod.NFC -> R.string.merchant_scan_message_nfc_ready
                ScanMethod.BARCODE -> R.string.merchant_scan_message_barcode_ready
            },
        )
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
            fieldErrors = emptyMap(),
            errorRes = null,
            messageRes = successMessageRes,
        )
        runCatching { findCustomerByLoyaltyIdUseCase(loyaltyId) }
            .onSuccess { customer ->
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
                    errorRes = R.string.merchant_scan_error_lookup_failed,
                    messageRes = null,
                )
            }
    }

    private suspend fun executeSelectedAction(
        state: ScanUiState,
        storeId: String,
        customerId: String,
        staffId: String,
        selectedAction: RewardProgramScanAction,
        amountInput: String,
        pointsInput: String,
    ): Int = when (selectedAction) {
        RewardProgramScanAction.EARN_POINTS -> {
            val amount = amountInput.toDoubleOrNull() ?: error(SCAN_FIELD_AMOUNT)
            val earnedPoints = state.activePrograms.calculateEarnedPoints(amount)
            val transactionId = UUID.randomUUID().toString()
            val program = state.activePrograms.resolveProgramFor(selectedAction)
            val programName = program?.name ?: ScanTransactionMetadata.defaultProgramName(selectedAction)
            recordTransactionUseCase(
                Transaction(
                    id = transactionId,
                    customerId = customerId,
                    storeId = storeId,
                    staffId = staffId,
                    amount = amount,
                    pointsEarned = earnedPoints,
                    pointsRedeemed = 0,
                    timestamp = LocalDateTime.now(),
                    metadata = ScanTransactionMetadata.purchase(program),
                    items = listOf(
                        TransactionItem(
                            id = UUID.randomUUID().toString(),
                            transactionId = transactionId,
                            name = programName,
                            quantity = 1,
                            unitPrice = amount,
                        )
                    ),
                )
                ,
                incrementVisit = !state.visitCountedForCurrentScan,
            )
            markVisitCountedForCurrentScan()
            R.string.merchant_scan_message_points_added
        }
        RewardProgramScanAction.REDEEM_REWARDS -> {
            val points = pointsInput.toIntOrNull() ?: error(SCAN_FIELD_POINTS)
            adjustCustomerPointsUseCase(customerId, -points, ScanTransactionMetadata.REWARD_REDEMPTION_REASON)
            R.string.merchant_scan_message_points_redeemed
        }
        RewardProgramScanAction.CHECK_IN -> {
            val rewardPoints = state.activePrograms.checkInRewardPoints()
            val transactionId = UUID.randomUUID().toString()
            val program = state.activePrograms.resolveProgramFor(selectedAction)
            recordTransactionUseCase(
                Transaction(
                    id = transactionId,
                    customerId = customerId,
                    storeId = storeId,
                    staffId = staffId,
                    amount = 0.0,
                    pointsEarned = rewardPoints,
                    pointsRedeemed = 0,
                    timestamp = LocalDateTime.now(),
                    metadata = ScanTransactionMetadata.checkIn(program),
                )
                ,
                incrementVisit = !state.visitCountedForCurrentScan,
            )
            markVisitCountedForCurrentScan()
            R.string.merchant_scan_message_check_in
        }
        RewardProgramScanAction.APPLY_CASHBACK -> {
            val amount = amountInput.toDoubleOrNull() ?: error(SCAN_FIELD_AMOUNT)
            val cashbackValue = state.activePrograms.calculateCashbackCredit(amount)
            adjustCustomerPointsUseCase(customerId, cashbackValue, ScanTransactionMetadata.CASHBACK_REASON)
            R.string.merchant_scan_message_cashback_applied
        }
        RewardProgramScanAction.TRACK_TIER_PROGRESS -> {
            R.string.merchant_scan_message_tier_progress_updated
        }
    }

    private fun validateActionInput(
        state: ScanUiState,
        customerPoints: Int,
        selectedAction: RewardProgramScanAction,
        amountInput: String,
        pointsInput: String,
    ): Map<String, Int> = buildMap {
        when (selectedAction) {
            RewardProgramScanAction.EARN_POINTS -> {
                val amount = amountInput.toDoubleOrNull()
                if (amount == null || amount <= 0.0) {
                    put(SCAN_FIELD_AMOUNT, R.string.merchant_scan_error_amount)
                }
            }
            RewardProgramScanAction.REDEEM_REWARDS -> {
                val points = pointsInput.toIntOrNull()
                val minimumRedeemPoints = state.activePrograms.minimumRedeemPoints()
                when {
                    points == null || points <= 0 -> put(SCAN_FIELD_POINTS, R.string.merchant_scan_error_points)
                    points > customerPoints -> put(SCAN_FIELD_POINTS, R.string.merchant_scan_error_points_exceeds_balance)
                    points < minimumRedeemPoints -> put(SCAN_FIELD_POINTS, R.string.merchant_scan_error_points_minimum_required)
                }
            }
            RewardProgramScanAction.CHECK_IN -> Unit
            RewardProgramScanAction.APPLY_CASHBACK -> {
                val amount = amountInput.toDoubleOrNull()
                val minimumSpendAmount = state.activePrograms.cashbackMinimumSpendAmount()
                when {
                    amount == null || amount <= 0.0 -> put(SCAN_FIELD_AMOUNT, R.string.merchant_scan_error_amount)
                    minimumSpendAmount > 0.0 && amount < minimumSpendAmount ->
                        put(SCAN_FIELD_AMOUNT, R.string.merchant_scan_error_cashback_minimum_spend)
                }
            }
            RewardProgramScanAction.TRACK_TIER_PROGRESS -> Unit
        }
    }

    private fun publishError(errorRes: Int, fieldErrors: Map<String, Int> = emptyMap()) {
        _uiState.value = _uiState.value.copy(
            contentMode = ScanContentMode.ACTIVE_SCAN,
            isSearching = false,
            isSubmitting = false,
            errorRes = errorRes,
            messageRes = null,
            fieldErrors = fieldErrors,
        )
    }

    private fun markVisitCountedForCurrentScan() {
        _uiState.value = _uiState.value.copy(visitCountedForCurrentScan = true)
    }
}
