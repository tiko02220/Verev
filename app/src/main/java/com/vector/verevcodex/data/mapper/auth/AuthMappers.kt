package com.vector.verevcodex.data.mapper.auth

import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import com.vector.verevcodex.domain.model.StaffRole
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.AuthUser

fun AuthAccountEntity.toDomain() = AuthUser(
    id = id,
    relatedEntityId = relatedEntityId,
    fullName = fullName,
    email = email,
    phoneNumber = phoneNumber,
    role = StaffRole.valueOf(role),
    active = active,
)

fun AuthAccountEntity.toSession() = AuthSession(user = toDomain(), isAuthenticated = true)
