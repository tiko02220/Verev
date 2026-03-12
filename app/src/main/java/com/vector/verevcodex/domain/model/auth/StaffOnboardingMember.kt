package com.vector.verevcodex.domain.model.auth

import com.vector.verevcodex.domain.model.common.StaffRole

data class StaffOnboardingMember(
    val fullName: String,
    val email: String,
    val password: String,
    val role: StaffRole,
    val permissionsSummary: String,
)
