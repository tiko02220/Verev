package com.vector.verevcodex.domain.model.transactions

import com.vector.verevcodex.domain.model.common.Identifiable

data class TransactionItem(
    override val id: String,
    val transactionId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
) : Identifiable
