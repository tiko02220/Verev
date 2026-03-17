package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.data.remote.api.auth.UserViewDto
import com.vector.verevcodex.data.remote.core.parseRemoteStaffRole
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.AuthUser

fun UserViewDto.toAuthUser(relatedEntityId: String = id.orEmpty()): AuthUser = AuthUser(
    id = id.orEmpty(),
    relatedEntityId = relatedEntityId,
    fullName = fullName.orEmpty(),
    email = email.orEmpty(),
    phoneNumber = phoneNumber.orEmpty(),
    profilePhotoUri = profilePhotoUri.orEmpty(),
    role = parseRemoteStaffRole(role),
    active = status.orEmpty().equals("ACTIVE", ignoreCase = true),
)

fun UserViewDto.toAuthSession(): AuthSession = AuthSession(
    user = toAuthUser(),
    isAuthenticated = true,
)
