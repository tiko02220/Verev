package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.data.remote.api.auth.UserViewDto
import com.vector.verevcodex.data.remote.core.parseRemoteStaffRole
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.AuthUser
import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.defaultPermissions

fun UserViewDto.toAuthUser(
    relatedEntityId: String = id.orEmpty(),
    defaultCurrencyCode: String = "AMD",
    permissions: StaffPermissions = parseRemoteStaffRole(role).defaultPermissions(),
): AuthUser {
    val parsedRole = parseRemoteStaffRole(role)
    return AuthUser(
        id = id.orEmpty(),
        relatedEntityId = relatedEntityId,
        fullName = fullName.orEmpty(),
        email = email.orEmpty(),
        phoneNumber = phoneNumber.orEmpty(),
        profilePhotoUri = profilePhotoUri.orEmpty(),
        defaultCurrencyCode = defaultCurrencyCode.ifBlank { "AMD" },
        role = parsedRole,
        status = status.orEmpty().uppercase(),
        active = status.orEmpty().equals("ACTIVE", ignoreCase = true),
        requiresPasswordSetup = status.orEmpty().equals("INVITED", ignoreCase = true),
        permissions = permissions,
    )
}

fun UserViewDto.toAuthSession(
    defaultCurrencyCode: String = "AMD",
    permissions: StaffPermissions = parseRemoteStaffRole(role).defaultPermissions(),
): AuthSession = AuthSession(
    user = toAuthUser(defaultCurrencyCode = defaultCurrencyCode, permissions = permissions),
    isAuthenticated = true,
)
