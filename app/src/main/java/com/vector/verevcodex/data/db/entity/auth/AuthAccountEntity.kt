package com.vector.verevcodex.data.db.entity.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_accounts")
data class AuthAccountEntity(
    @PrimaryKey val id: String,
    val relatedEntityId: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val role: String,
    val active: Boolean,
)
