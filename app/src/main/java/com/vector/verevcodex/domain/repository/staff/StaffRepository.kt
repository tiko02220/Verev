package com.vector.verevcodex.domain.repository.staff

import com.vector.verevcodex.domain.model.analytics.StaffAnalytics
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import kotlinx.coroutines.flow.Flow

interface StaffRepository {
    fun observeStaff(storeId: String? = null): Flow<List<StaffMember>>
    fun observeStaffAnalytics(storeId: String? = null): Flow<List<StaffAnalytics>>
    suspend fun addStaffMembers(storeId: String, members: List<StaffOnboardingMember>): Result<Unit>
}
