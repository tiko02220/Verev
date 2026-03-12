package com.vector.verevcodex.data.db.entity.customer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    @ColumnInfo(name = "nfcId") val loyaltyId: String,
    val enrolledDate: String,
    val totalVisits: Int,
    val totalSpent: Double,
    val currentPoints: Int,
    val loyaltyTier: String,
    val lastVisit: String?,
    val favoriteStoreId: String?,
)

@Entity(
    tableName = "customer_credentials",
    indices = [Index(value = ["customerId", "method"], unique = true)],
)
data class CustomerCredentialEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val loyaltyId: String,
    val method: String,
    val status: String,
    val referenceValue: String?,
    val updatedAt: String,
)
