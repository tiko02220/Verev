package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.repository.StaffRepository

class ObserveStaffUseCase(private val repository: StaffRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeStaff(storeId)
}

class AddStaffMembersUseCase(private val repository: StaffRepository) {
    suspend operator fun invoke(storeId: String, members: List<StaffOnboardingMember>) = repository.addStaffMembers(storeId, members)
}

class ObserveStaffAnalyticsUseCase(private val repository: StaffRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeStaffAnalytics(storeId)
}
