package com.vector.verevcodex.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.model.TransactionItem
import com.vector.verevcodex.domain.usecase.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.FindCustomerByNfcUseCase
import com.vector.verevcodex.domain.usecase.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.RecordTransactionUseCase
import com.vector.verevcodex.domain.usecase.QuickRegisterCustomerUseCase
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val findCustomerByNfcUseCase: FindCustomerByNfcUseCase,
    private val quickRegisterCustomerUseCase: QuickRegisterCustomerUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
    private val adjustCustomerPointsUseCase: AdjustCustomerPointsUseCase,
    private val observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    val pendingNfcId: StateFlow<String?> get() = _pendingNfcId.asStateFlow()
    private val _pendingNfcId = MutableStateFlow<String?>(null)

    init {
        observeSelectedStoreUseCase()
            .onEach { store ->
                _uiState.value = _uiState.value.copy(
                    selectedStoreId = store?.id,
                    selectedStoreName = store?.name.orEmpty(),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onExternalNfcScan(nfcId: String) {
        _pendingNfcId.value = nfcId
        performLookup(nfcId)
    }

    fun simulateKnownScan() = onExternalNfcScan("04AABBCC")

    fun simulateNewScan() = onExternalNfcScan("04NEW999")

    fun selectAction(action: ScanCustomerAction) {
        _uiState.value = _uiState.value.copy(selectedAction = action, errorRes = null, messageRes = null)
    }

    fun quickRegister(firstName: String, phone: String) {
        val scannedId = _uiState.value.scannedId ?: return
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
        if (storeId.isNullOrBlank() || customer == null) {
            publishError(R.string.merchant_scan_error_missing_customer)
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, errorRes = null, messageRes = null)
            val session = observeSessionUseCase().first()
            val staffId = session?.user?.relatedEntityId ?: session?.user?.id ?: "manual-staff"
            when (state.selectedAction) {
                ScanCustomerAction.ADD_POINTS -> {
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
                            metadata = "Dashboard scan purchase",
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
                    performLookup(customer.nfcId, R.string.merchant_scan_message_points_added)
                }
                ScanCustomerAction.REDEEM_POINTS -> {
                    val points = pointsInput.toIntOrNull()
                    if (points == null || points <= 0) {
                        _uiState.value = _uiState.value.copy(isSubmitting = false, errorRes = R.string.merchant_scan_error_points)
                        return@launch
                    }
                    adjustCustomerPointsUseCase(customer.id, -points, "Dashboard reward redemption")
                    performLookup(customer.nfcId, R.string.merchant_scan_message_points_redeemed)
                }
                ScanCustomerAction.CHECK_IN -> {
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
                            metadata = "Visit check-in",
                        )
                    )
                    performLookup(customer.nfcId, R.string.merchant_scan_message_check_in)
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorRes = null, messageRes = null)
    }

    private fun performLookup(nfcId: String, successMessageRes: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scannedId = nfcId, isSearching = true, customer = null, errorRes = null)
            val customer = findCustomerByNfcUseCase(nfcId)
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                isSubmitting = false,
                customer = customer,
                errorRes = if (customer == null) R.string.merchant_scan_error_not_found else null,
                messageRes = successMessageRes,
            )
        }
    }

    private fun publishError(errorRes: Int) {
        _uiState.value = _uiState.value.copy(errorRes = errorRes, messageRes = null)
    }
}
