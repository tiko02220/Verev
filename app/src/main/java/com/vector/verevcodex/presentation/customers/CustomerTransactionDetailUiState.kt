package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.transactions.TransactionApprovalRequest
import com.vector.verevcodex.domain.model.transactions.Transaction

data class CustomerTransactionDetailUiState(
    val transaction: Transaction? = null,
    val voidRequest: TransactionApprovalRequest? = null,
    val isMissingTransaction: Boolean = false,
    val canRequestVoid: Boolean = false,
    val isSubmittingVoid: Boolean = false,
    val showVoidDialog: Boolean = false,
    val voidReason: String = "",
    val voidReasonError: Int? = null,
    val successMessageRes: Int? = null,
    val errorMessageRes: Int? = null,
)
