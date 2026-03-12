package com.vector.verevcodex.data.repository.staff

import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.entity.business.StaffMemberEntity
import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import com.vector.verevcodex.data.mapper.toDomain
import com.vector.verevcodex.domain.model.analytics.StaffAnalytics
import com.vector.verevcodex.domain.repository.staff.StaffRepository
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
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

    override fun observeStaffAnalytics(storeId: String?): Flow<List<StaffAnalytics>> = combine(
        observeStaff(storeId),
        database.transactionDao().observeTransactions(storeId),
    ) { staff, transactions ->
        staff.map { member ->
            val handled = transactions.filter { it.staffId == member.id }
            val revenue = handled.sumOf { it.amount }
            StaffAnalytics(
                id = member.id,
                staffId = member.id,
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
                        phoneNumber = "",
                        role = member.role.name,
                        active = true,
                        permissionsSummary = member.permissionsSummary,
                    )
                )
            )
            database.authDao().insert(
                AuthAccountEntity(
                    id = authId,
                    relatedEntityId = staffId,
                    fullName = member.fullName.trim(),
                    email = member.email.trim().lowercase(),
                    phoneNumber = "",
                    password = member.password,
                    role = member.role.name,
                    active = true,
                )
            )
        }
        return Result.success(Unit)
    }
}
