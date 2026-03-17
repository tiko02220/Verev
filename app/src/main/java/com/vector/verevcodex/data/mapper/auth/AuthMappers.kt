package com.vector.verevcodex.data.mapper.auth

import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.AuthUser

fun AuthUser.toEntity(password: String = ""): AuthAccountEntity {
    val role = role
    val canView = role == StaffRole.OWNER || role == StaffRole.STORE_MANAGER
    return AuthAccountEntity(
        id = id,
        relatedEntityId = relatedEntityId,
        fullName = fullName,
        email = email,
        phoneNumber = phoneNumber,
        profilePhotoUri = profilePhotoUri,
        password = password,
        role = role.name,
        active = active,
        canViewAnalytics = canView,
        canManagePrograms = canView,
        canProcessTransactions = true,
        canManageCustomers = canView,
        canManageStaff = role == StaffRole.OWNER || role == StaffRole.STORE_MANAGER,
        canViewSettings = true,
    )
}

fun AuthAccountEntity.toDomain() = AuthUser(
    id = id,
    relatedEntityId = relatedEntityId,
    fullName = fullName,
    email = email,
    phoneNumber = phoneNumber,
    profilePhotoUri = profilePhotoUri,
    role = StaffRole.valueOf(role),
    active = active,
)

fun AuthAccountEntity.toSession() = AuthSession(user = toDomain(), isAuthenticated = true)
