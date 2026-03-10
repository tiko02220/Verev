package com.vector.verevcodex.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val nfcId: String,
    val enrolledDate: String,
    val totalVisits: Int,
    val totalSpent: Double,
    val currentPoints: Int,
    val loyaltyTier: String,
    val lastVisit: String?,
    val favoriteStoreId: String?,
)

@Entity(tableName = "customer_business_relations")
data class CustomerBusinessRelationEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val storeId: String,
    val joinedAt: String,
    val notes: String,
)
