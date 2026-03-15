package com.vector.verevcodex.domain.model.business

import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.StaffRole

data class StaffMemberDraft(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: StaffRole,
    val permissionsSummary: String,
    val permissions: StaffPermissions,
)
