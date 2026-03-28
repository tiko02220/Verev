package com.vector.verevcodex.presentation.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalRequest
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalStatus
import com.vector.verevcodex.domain.model.transactions.TransactionStatus
import com.vector.verevcodex.domain.model.transactions.TransactionType
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionVoidRequestUseCase
import com.vector.verevcodex.domain.usecase.transactions.RequestTransactionVoidUseCase
import com.vector.verevcodex.presentation.navigation.Screen
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
class CustomerTransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTransactionUseCase: ObserveTransactionUseCase,
    observeTransactionVoidRequestUseCase: ObserveTransactionVoidRequestUseCase,
    observeSessionUseCase: ObserveSessionUseCase,
    private val requestTransactionVoidUseCase: RequestTransactionVoidUseCase,
) : ViewModel() {
    private val transactionId: String? = savedStateHandle[Screen.CustomerTransactionDetail.ARG_TRANSACTION_ID]

    private val _uiState = MutableStateFlow(CustomerTransactionDetailUiState())
    val uiState: StateFlow<CustomerTransactionDetailUiState> = _uiState.asStateFlow()
    private val localVoidRequestOverride = MutableStateFlow<TransactionApprovalRequest?>(null)

    init {
        val currentTransactionId = transactionId
        if (currentTransactionId.isNullOrBlank()) {
            _uiState.value = CustomerTransactionDetailUiState(isMissingTransaction = true)
        } else {
            combine(
                observeTransactionUseCase(currentTransactionId),
                observeTransactionVoidRequestUseCase(currentTransactionId),
                observeSessionUseCase(),
                localVoidRequestOverride,
            ) { transaction, remoteVoidRequest, session, localVoidRequest ->
                val effectiveVoidRequest = remoteVoidRequest ?: localVoidRequest
                val canRequestVoid = canRequestVoid(
                    transaction = transaction,
                    approval = effectiveVoidRequest,
                    canProcessTransactions = session?.user?.permissions?.processTransactions == true,
                )
                _uiState.value.copy(
                    transaction = transaction,
                    voidRequest = effectiveVoidRequest,
                    isMissingTransaction = transaction == null,
                    canRequestVoid = canRequestVoid,
                )
            }
                .onEach { state ->
                    _uiState.value = state
                }
                .launchIn(viewModelScope)
        }
    }

    fun showVoidDialog() {
        if (!_uiState.value.canRequestVoid) return
        _uiState.value = _uiState.value.copy(
            showVoidDialog = true,
            voidReason = "",
            voidReasonError = null,
            errorMessageRes = null,
            successMessageRes = null,
        )
    }

    fun dismissVoidDialog() {
        _uiState.value = _uiState.value.copy(
            showVoidDialog = false,
            voidReason = "",
            voidReasonError = null,
        )
    }

    fun updateVoidReason(value: String) {
        _uiState.value = _uiState.value.copy(
            voidReason = value,
            voidReasonError = null,
        )
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(
            successMessageRes = null,
            errorMessageRes = null,
        )
    }

    fun submitVoidRequest() {
        val state = _uiState.value
        val transaction = state.transaction ?: return
        val reason = state.voidReason.trim()
        if (reason.isBlank()) {
            _uiState.value = state.copy(voidReasonError = R.string.merchant_transaction_void_reason_required)
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(
                isSubmittingVoid = true,
                voidReasonError = null,
                errorMessageRes = null,
                successMessageRes = null,
            )
            runCatching {
                requestTransactionVoidUseCase(transaction.id, reason)
            }.onSuccess { approval ->
                localVoidRequestOverride.value = approval
                _uiState.value = _uiState.value.copy(
                    showVoidDialog = false,
                    voidReason = "",
                    isSubmittingVoid = false,
                    successMessageRes = R.string.merchant_transaction_void_requested_success,
                    errorMessageRes = null,
                    voidRequest = approval,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSubmittingVoid = false,
                    errorMessageRes = R.string.merchant_transaction_void_requested_failed,
                    successMessageRes = null,
                )
            }
        }
    }

    private fun canRequestVoid(
        transaction: Transaction?,
        approval: TransactionApprovalRequest?,
        canProcessTransactions: Boolean,
    ): Boolean {
        if (!canProcessTransactions || transaction == null) return false
        if (transaction.type != TransactionType.PURCHASE || transaction.status != TransactionStatus.COMPLETED) return false
        return approval?.status != TransactionApprovalStatus.PENDING
    }
}
