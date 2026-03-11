package com.vector.verevcodex.domain.model

data class TransactionItem(
    override val id: String,
    val transactionId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
) : Identifiable
