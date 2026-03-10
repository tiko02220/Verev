package com.vector.verevcodex.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Customer(
    override val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val nfcId: String,
    val enrolledDate: LocalDate,
    val totalVisits: Int,
    val totalSpent: Double,
    val currentPoints: Int,
    val loyaltyTier: LoyaltyTier,
    val lastVisit: LocalDateTime?,
    val favoriteStoreId: String?,
) : Identifiable

data class CustomerDraft(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val nfcId: String? = null,
)

data class CustomerBusinessRelation(
    override val id: String,
    val customerId: String,
    val storeId: String,
    val joinedAt: LocalDateTime,
    val notes: String,
) : Identifiable
