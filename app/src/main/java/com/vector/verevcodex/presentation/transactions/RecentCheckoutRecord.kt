package com.vector.verevcodex.presentation.transactions

import java.time.LocalDateTime

data class RecentCheckoutRecord(
    val id: String,
    val customerName: String,
    val itemCount: Int,
    val amount: Double,
    val pointsEarned: Int,
    val pointsRedeemed: Int,
    val timestamp: LocalDateTime,
    val summary: String,
)
