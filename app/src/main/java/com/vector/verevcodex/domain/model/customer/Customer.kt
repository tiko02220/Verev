package com.vector.verevcodex.domain.model.customer

import com.vector.verevcodex.domain.model.common.Identifiable
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import java.time.LocalDate
import java.time.LocalDateTime

data class Customer(
    override val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val birthDate: LocalDate? = null,
    val loyaltyId: String,
    val enrolledDate: LocalDate,
    val totalVisits: Int,
    val totalSpent: Double,
    val currentPoints: Int,
    val loyaltyTier: LoyaltyTier,
    val loyaltyTierLabel: String,
    val lastVisit: LocalDateTime?,
    val favoriteStoreId: String?,
    val gender: CustomerGender? = null,
) : Identifiable
