package com.vector.verevcodex.domain.model.business

import com.vector.verevcodex.domain.model.common.Identifiable
import com.vector.verevcodex.domain.model.common.StaffRole

data class StaffMember(
    override val id: String,
    val storeId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val role: StaffRole,
    val active: Boolean,
    val permissionsSummary: String,
) : Identifiable
