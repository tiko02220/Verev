package com.vector.verevcodex.domain.model

import java.time.LocalDateTime

data class Transaction(
    override val id: String,
    val customerId: String,
    val storeId: String,
    val staffId: String,
    val amount: Double,
    val pointsEarned: Int,
    val pointsRedeemed: Int,
    val timestamp: LocalDateTime,
    val metadata: String,
    val items: List<TransactionItem> = emptyList(),
) : Identifiable
