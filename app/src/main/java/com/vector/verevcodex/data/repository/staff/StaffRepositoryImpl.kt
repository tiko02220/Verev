package com.vector.verevcodex.data.repository.staff

import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.entity.business.StaffMemberEntity
import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import com.vector.verevcodex.data.mapper.toDomain
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.StaffAnalytics
import com.vector.verevcodex.domain.model.analytics.startDateFrom
import com.vector.verevcodex.domain.repository.staff.StaffRepository
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.model.business.StaffMemberDraft
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class StaffRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : StaffRepository {
    override fun observeStaff(storeId: String?) =
        database.staffDao().observeStaff(storeId).map { list -> list.map { it.toDomain() } }

    override fun observeStaffAnalytics(storeId: String?, range: AnalyticsTimeRange): Flow<List<StaffAnalytics>> = combine(
        observeStaff(storeId),
        database.transactionDao().observeTransactions(storeId),
    ) { staff, transactionEntities ->
        val today = LocalDate.now()
        val rangeStart = range.startDateFrom(today)
        val transactions = transactionEntities.map { it.toDomain(emptyList()) }
        val rangeTransactions = transactions.filter { transaction ->
            val date = transaction.timestamp.toLocalDate()
            date >= rangeStart && date <= today
        }
        staff.map { member ->
            val handled = rangeTransactions.filter { it.staffId == member.id }
            val revenue = handled.sumOf { it.amount }
            StaffAnalytics(
                id = member.id,
                staffId = member.id,
                staffName = listOf(member.firstName, member.lastName).joinToString(" ").trim(),
                storeId = member.storeId,
                transactionsProcessed = handled.size,
                revenueHandled = revenue,
                customersServed = handled.map { it.customerId }.distinct().size,
                rewardsRedeemed = handled.count { it.pointsRedeemed > 0 },
                averageTransactionValue = if (handled.isEmpty()) 0.0 else revenue / handled.size,
            )
        }
    }

    override suspend fun addStaffMembers(storeId: String, members: List<StaffOnboardingMember>): Result<Unit> {
        members.forEach { member ->
            val staffId = UUID.randomUUID().toString()
            val authId = UUID.randomUUID().toString()
            val nameParts = member.fullName.trim().split(" ", limit = 2)
            val firstName = nameParts.firstOrNull().orEmpty()
            val lastName = nameParts.getOrElse(1) { "" }

            database.staffDao().insertAll(
                listOf(
                    StaffMemberEntity(
                        id = staffId,
                        storeId = storeId,
                        firstName = firstName,
                        lastName = lastName,
                        email = member.email.trim().lowercase(),
                        phoneNumber = member.phoneNumber.trim(),
                        role = member.role.name,
                        active = true,
                        permissionsSummary = member.permissionsSummary,
                        canViewAnalytics = member.permissions.viewAnalytics,
                        canManagePrograms = member.permissions.managePrograms,
                        canProcessTransactions = member.permissions.processTransactions,
                        canManageCustomers = member.permissions.manageCustomers,
                        canManageStaff = member.permissions.manageStaff,
                        canViewSettings = member.permissions.viewSettings,
                    )
                )
            )
            database.authDao().insert(
                AuthAccountEntity(
                    id = authId,
                    relatedEntityId = staffId,
                    fullName = member.fullName.trim(),
                    email = member.email.trim().lowercase(),
                    phoneNumber = member.phoneNumber.trim(),
                    profilePhotoUri = "",
                    password = member.password,
                    role = member.role.name,
                    active = true,
                    canViewAnalytics = member.permissions.viewAnalytics,
                    canManagePrograms = member.permissions.managePrograms,
                    canProcessTransactions = member.permissions.processTransactions,
                    canManageCustomers = member.permissions.manageCustomers,
                    canManageStaff = member.permissions.manageStaff,
                    canViewSettings = member.permissions.viewSettings,
                )
            )
        }
        return Result.success(Unit)
    }

    override suspend fun updateStaffMember(staffId: String, draft: StaffMemberDraft): Result<Unit> = runCatching {
        val existing = database.staffDao().getById(staffId) ?: error("staff_not_found")
        val nameParts = draft.fullName.trim().split(" ", limit = 2)
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.getOrElse(1) { "" }
        val normalizedEmail = draft.email.trim().lowercase()
        database.staffDao().update(
            existing.copy(
                firstName = firstName,
                lastName = lastName,
                email = normalizedEmail,
                phoneNumber = draft.phoneNumber.trim(),
                role = draft.role.name,
                permissionsSummary = draft.permissionsSummary,
                canViewAnalytics = draft.permissions.viewAnalytics,
                canManagePrograms = draft.permissions.managePrograms,
                canProcessTransactions = draft.permissions.processTransactions,
                canManageCustomers = draft.permissions.manageCustomers,
                canManageStaff = draft.permissions.manageStaff,
                canViewSettings = draft.permissions.viewSettings,
            )
        )
        database.authDao().findByRelatedEntityId(staffId)?.let { authAccount ->
            database.authDao().update(
                authAccount.copy(
                    fullName = draft.fullName.trim(),
                    email = normalizedEmail,
                    phoneNumber = draft.phoneNumber.trim(),
                    role = draft.role.name,
                    canViewAnalytics = draft.permissions.viewAnalytics,
                    canManagePrograms = draft.permissions.managePrograms,
                    canProcessTransactions = draft.permissions.processTransactions,
                    canManageCustomers = draft.permissions.manageCustomers,
                    canManageStaff = draft.permissions.manageStaff,
                    canViewSettings = draft.permissions.viewSettings,
                )
            )
        }
    }

    override suspend fun removeStaffMember(staffId: String): Result<Unit> = runCatching {
        database.staffDao().deleteById(staffId)
        database.authDao().deleteByRelatedEntityId(staffId)
    }
}
