package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.CustomerCredential
import com.vector.verevcodex.domain.model.Transaction

data class CustomerProfileUiState(
    val customer: Customer? = null,
    val credentials: List<CustomerCredential> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val isMissingCustomer: Boolean = false,
)
