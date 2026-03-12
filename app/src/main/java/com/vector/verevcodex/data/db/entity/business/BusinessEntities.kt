package com.vector.verevcodex.data.db.entity.business

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owners")
data class OwnerEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
)

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val address: String,
    val contactInfo: String,
    val category: String,
    val workingHours: String,
    val logoUrl: String,
    val primaryColor: String,
    val secondaryColor: String,
    val active: Boolean,
)

@Entity(tableName = "locations")
data class BusinessLocationEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

@Entity(tableName = "staff")
data class StaffMemberEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val active: Boolean,
    val permissionsSummary: String,
)
