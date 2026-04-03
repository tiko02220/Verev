package com.vector.verevcodex.data.repository.staff

import com.vector.verevcodex.data.remote.analytics.AnalyticsRemoteDataSource
import com.vector.verevcodex.data.remote.staff.StaffRemoteDataSource
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.StaffAnalytics
import com.vector.verevcodex.domain.repository.realtime.RealtimeRepository
import com.vector.verevcodex.domain.repository.staff.StaffRepository
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.model.business.StaffMemberDraft
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class StaffRepositoryImpl @Inject constructor(
    private val staffRemote: StaffRemoteDataSource,
    private val analyticsRemote: AnalyticsRemoteDataSource,
    private val realtimeRepository: RealtimeRepository,
) : StaffRepository {
    private val staffRefreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val staffRefreshSignals = merge(
        staffRefreshRequests.onStart { emit(Unit) },
        realtimeRepository.observeRefreshSignals(),
    )

    override fun observeStaff(storeId: String?): Flow<List<com.vector.verevcodex.domain.model.business.StaffMember>> {
        return staffRefreshSignals
            .map {
                staffRemote.list(storeId).getOrElse { emptyList() }
            }
    }

    override fun observeStaffAnalytics(storeId: String?, range: AnalyticsTimeRange): Flow<List<StaffAnalytics>> {
        return staffRefreshSignals.map {
            analyticsRemote.staff(storeId, range).getOrElse { emptyList() }
        }
    }

    override suspend fun addStaffMembers(storeId: String, members: List<StaffOnboardingMember>): Result<Unit> {
        return staffRemote.bulkCreate(storeId, members)
            .onSuccess { staffRefreshRequests.tryEmit(Unit) }
    }

    override suspend fun updateStaffMember(staffId: String, draft: StaffMemberDraft): Result<Unit> {
        val currentStoreId = staffRemote.list(null).getOrElse { emptyList() }.firstOrNull { it.id == staffId }?.storeId
            ?: return Result.failure(IllegalStateException("Staff not found"))
        return staffRemote.update(staffId, currentStoreId, draft)
            .onSuccess { staffRefreshRequests.tryEmit(Unit) }
    }

    override suspend fun removeStaffMember(staffId: String): Result<Unit> {
        return staffRemote.delete(staffId)
            .onSuccess { staffRefreshRequests.tryEmit(Unit) }
    }
}
