package com.vector.verevcodex.domain.model.auth

import com.vector.verevcodex.domain.model.common.StaffRole

data class AuthUser(
    val id: String,
    val relatedEntityId: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: StaffRole,
    val active: Boolean,
)
