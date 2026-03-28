package com.vector.verevcodex.domain.model.auth

import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.common.defaultPermissions

data class AuthUser(
    val id: String,
    val relatedEntityId: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val profilePhotoUri: String,
    val role: StaffRole,
    val status: String,
    val active: Boolean,
    val requiresPasswordSetup: Boolean,
    val permissions: StaffPermissions = role.defaultPermissions(),
)
