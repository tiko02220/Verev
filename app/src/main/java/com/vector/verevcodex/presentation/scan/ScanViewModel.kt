package com.vector.verevcodex.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.core.identifiers.LoyaltyIdCodec
import com.vector.verevcodex.domain.model.RewardProgramScanAction
import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.model.TransactionItem
import com.vector.verevcodex.domain.usecase.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.ClearScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.FindCustomerByLoyaltyIdUseCase
import com.vector.verevcodex.domain.usecase.ObserveActiveScanActionsUseCase
import com.vector.verevcodex.domain.usecase.ObserveScanPreferencesUseCase
import com.vector.verevcodex.domain.usecase.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.QuickRegisterCustomerUseCase
import com.vector.verevcodex.domain.usecase.RecordTransactionUseCase
import com.vector.verevcodex.domain.usecase.SaveScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt
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
    private val quickRegisterCustomerUseCase: QuickRegisterCustomerUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
    private val adjustCustomerPointsUseCase: AdjustCustomerPointsUseCase,
    private val observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
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

        observeScanPreferencesUseCase()
            .onEach { preferences ->
                _uiState.value = _uiState.value.copy(scanPreferences = preferences)
            }
            .launchIn(viewModelScope)
    }

    fun prepareScanEntry(initialMethod: ScanMethod? = null) {
        if (
            initialMethod != null &&
            _uiState.value.activeScanMethod == initialMethod &&
            _uiState.value.customer == null &&
            _uiState.value.scannedLoyaltyId == null &&
            _uiState.value.errorRes == null
        ) {
            return
        }
        val nextAction = _uiState.value.availableActions.firstOrNull()
        _uiState.value = _uiState.value.copy(
            customer = null,
            scannedLoyaltyId = null,
            selectedAction = nextAction,
            isSearching = false,
            isSubmitting = false,
            errorRes = null,
            messageRes = null,
        )
        enterScan(initialMethod)
    }

    fun enterScan(method: ScanMethod? = null, rememberChoice: Boolean = false) {
        viewModelScope.launch {
            val nextAction = _uiState.value.availableActions.firstOrNull()
            _uiState.value = _uiState.value.copy(
                customer = null,
                scannedLoyaltyId = null,
                selectedAction = nextAction,
                isSearching = false,
                isSubmitting = false,
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
            customer = null,
            scannedLoyaltyId = null,
            selectedAction = nextAction,
            isSearching = false,
            isSubmitting = false,
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
            _uiState.value = _uiState.value.copy(errorRes = null, messageRes = null)
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
            activeScanMethod = ScanMethod.NFC,
            errorRes = R.string.merchant_scan_error_nfc_failed,
            messageRes = null,
            isSearching = false,
        )
    }

    fun onBarcodeScanned(rawValue: String) {
        handleResolvedScan(ScanMethod.BARCODE, rawValue)
    }

    fun onBarcodeScanFailed() {
        _uiState.value = _uiState.value.copy(
            activeScanMethod = ScanMethod.BARCODE,
            errorRes = R.string.merchant_scan_error_barcode_failed,
            messageRes = null,
            isSearching = false,
        )
    }

    fun selectAction(action: RewardProgramScanAction) {
        _uiState.value = _uiState.value.copy(selectedAction = action, errorRes = null, messageRes = null)
    }

    fun quickRegister(firstName: String, phone: String) {
        val scannedId = _uiState.value.scannedLoyaltyId ?: return
        val storeId = _uiState.value.selectedStoreId ?: run {
            publishError(R.string.merchant_scan_error_no_store)
            return
        }
        if (firstName.isBlank()) {
            publishError(R.string.merchant_scan_error_first_name)
            return
        }
        if (phone.isBlank()) {
            publishError(R.string.merchant_scan_error_phone)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorRes = null, messageRes = null)
            runCatching {
                quickRegisterCustomerUseCase(firstName.trim(), phone.trim(), scannedId, storeId)
            }.onSuccess { customer ->
                _uiState.value = _uiState.value.copy(
                    customer = customer,
                    isSubmitting = false,
                    errorRes = null,
                    messageRes = R.string.merchant_scan_message_customer_registered,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSubmitting = false, errorRes = R.string.merchant_scan_error_register)
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
        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, errorRes = null, messageRes = null)
            val session = observeSessionUseCase().first()
            val staffId = session?.user?.relatedEntityId ?: session?.user?.id ?: "manual-staff"
            when (selectedAction) {
                RewardProgramScanAction.EARN_POINTS -> {
                    val amount = amountInput.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        _uiState.value = _uiState.value.copy(isSubmitting = false, errorRes = R.string.merchant_scan_error_amount)
                        return@launch
                    }
                    val earnedPoints = (amount / 100.0).roundToInt().coerceAtLeast(1)
                    val transactionId = UUID.randomUUID().toString()
                    recordTransactionUseCase(
                        Transaction(
                            id = transactionId,
                            customerId = customer.id,
                            storeId = storeId,
                            staffId = staffId,
                            amount = amount,
                            pointsEarned = earnedPoints,
                            pointsRedeemed = 0,
                            timestamp = LocalDateTime.now(),
                            metadata = "Configured loyalty points accrual",
                            items = listOf(
                                TransactionItem(
                                    id = UUID.randomUUID().toString(),
                                    transactionId = transactionId,
                                    name = "Loyalty purchase",
                                    quantity = 1,
                                    unitPrice = amount,
                                )
                            ),
                        )
                    )
                    performLookup(customer.loyaltyId, R.string.merchant_scan_message_points_added)
                }

                RewardProgramScanAction.REDEEM_REWARDS -> {
                    val points = pointsInput.toIntOrNull()
                    if (points == null || points <= 0) {
                        _uiState.value = _uiState.value.copy(isSubmitting = false, errorRes = R.string.merchant_scan_error_points)
                        return@launch
                    }
                    adjustCustomerPointsUseCase(customer.id, -points, "Configured reward redemption")
                    performLookup(customer.loyaltyId, R.string.merchant_scan_message_points_redeemed)
                }

                RewardProgramScanAction.CHECK_IN -> {
                    recordTransactionUseCase(
                        Transaction(
                            id = UUID.randomUUID().toString(),
                            customerId = customer.id,
                            storeId = storeId,
                            staffId = staffId,
                            amount = 0.0,
                            pointsEarned = 10,
                            pointsRedeemed = 0,
                            timestamp = LocalDateTime.now(),
                            metadata = "Configured visit check-in",
                        )
                    )
                    performLookup(customer.loyaltyId, R.string.merchant_scan_message_check_in)
                }

                RewardProgramScanAction.APPLY_CASHBACK -> {
                    val amount = amountInput.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        _uiState.value = _uiState.value.copy(isSubmitting = false, errorRes = R.string.merchant_scan_error_amount)
                        return@launch
                    }
                    val cashbackValue = (amount * 0.05).roundToInt().coerceAtLeast(1)
                    adjustCustomerPointsUseCase(customer.id, cashbackValue, "Configured cashback credit")
                    performLookup(customer.loyaltyId, R.string.merchant_scan_message_cashback_applied)
                }

                RewardProgramScanAction.TRACK_TIER_PROGRESS -> {
                    performLookup(customer.loyaltyId, R.string.merchant_scan_message_tier_progress_updated)
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorRes = null, messageRes = null)
    }

    private fun startScan(method: ScanMethod) {
        _uiState.value = _uiState.value.copy(
            activeScanMethod = method,
            customer = null,
            scannedLoyaltyId = null,
            isSearching = false,
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
        _uiState.value = _uiState.value.copy(activeScanMethod = method)
        performLookup(loyaltyId)
    }

    private fun performLookup(loyaltyId: String, successMessageRes: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                scannedLoyaltyId = loyaltyId,
                isSearching = true,
                customer = null,
                errorRes = null,
                messageRes = successMessageRes,
            )
            val customer = findCustomerByLoyaltyIdUseCase(loyaltyId)
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                isSubmitting = false,
                customer = customer,
                errorRes = if (customer == null) R.string.merchant_scan_error_not_found else null,
                messageRes = successMessageRes ?: if (customer != null) R.string.merchant_scan_message_member_found else null,
            )
        }
    }

    private fun publishError(errorRes: Int) {
        _uiState.value = _uiState.value.copy(errorRes = errorRes, messageRes = null)
    }
}
