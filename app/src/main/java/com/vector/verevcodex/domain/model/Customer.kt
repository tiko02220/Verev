package com.vector.verevcodex.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Customer(
    override val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val loyaltyId: String,
    val enrolledDate: LocalDate,
    val totalVisits: Int,
    val totalSpent: Double,
    val currentPoints: Int,
    val loyaltyTier: LoyaltyTier,
    val lastVisit: LocalDateTime?,
    val favoriteStoreId: String?,
) : Identifiable
