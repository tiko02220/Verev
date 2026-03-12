package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.transactions.Transaction

data class CustomerTransactionDetailUiState(
    val transaction: Transaction? = null,
    val isMissingTransaction: Boolean = false,
)
