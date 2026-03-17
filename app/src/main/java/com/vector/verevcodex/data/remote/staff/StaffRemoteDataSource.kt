package com.vector.verevcodex.data.remote.staff

import com.vector.verevcodex.data.remote.api.staff.BulkCreateStaffRequestDto
import com.vector.verevcodex.data.remote.api.staff.StaffPermissionsDto
import com.vector.verevcodex.data.remote.api.staff.StaffOnboardingMemberRequestDto
import com.vector.verevcodex.data.remote.api.staff.StaffViewDto
import com.vector.verevcodex.data.remote.api.staff.UpdateStaffRequestDto
import com.vector.verevcodex.data.remote.api.staff.VerevStaffApi
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.core.parseRemoteStaffRole
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.business.StaffMemberDraft
import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.defaultPermissions
import com.vector.verevcodex.domain.model.common.summary
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRemoteDataSource @Inject constructor(
    private val api: VerevStaffApi,
) {

    suspend fun list(storeId: String?): Result<List<StaffMember>> = runCatching {
        val response = api.list()
        val list = response.unwrap { it.map { dto -> dto.toDomain() } }
        if (storeId != null) list.filter { it.storeId == storeId } else list
    }

    suspend fun bulkCreate(storeId: String, members: List<StaffOnboardingMember>): Result<Unit> = runCatching {
        val request = BulkCreateStaffRequestDto(
            primaryStoreId = storeId,
            storeIds = listOf(storeId),
            members = members.map { m ->
                StaffOnboardingMemberRequestDto(
                    fullName = m.fullName.trim(),
                    email = m.email.trim(),
                    phoneNumber = m.phoneNumber.trim(),
                    password = m.password,
                    role = m.role.name,
                    permissionsSummary = m.permissionsSummary,
                    permissions = m.permissions.toDto(),
                )
            },
        )
        val response = api.bulkCreate(
            request = request,
            idempotencyKey = staffIdempotencyKey(
                action = RemoteIdempotencyAction.BULK_CREATE,
                storeId,
                members.joinToString(separator = "|") { member ->
                    listOf(
                        member.fullName.trim(),
                        member.email.trim().lowercase(),
                        member.phoneNumber.trim(),
                        member.role.name,
                    ).joinToString(":")
                },
            ),
        )
        response.unwrap { Unit }
    }

    suspend fun update(staffId: String, primaryStoreId: String, draft: StaffMemberDraft): Result<Unit> = runCatching {
        val nameParts = draft.fullName.trim().split(" ", limit = 2)
        val request = UpdateStaffRequestDto(
            firstName = nameParts.firstOrNull().orEmpty(),
            lastName = nameParts.getOrElse(1) { "" },
            email = draft.email.trim(),
            phoneNumber = draft.phoneNumber.trim(),
            role = draft.role.name,
            permissions = draft.permissions.toDto(),
            primaryStoreId = primaryStoreId,
        )
        val response = api.update(
            staffId = staffId,
            request = request,
            idempotencyKey = staffIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                staffId,
                primaryStoreId,
                draft.fullName,
                draft.email,
                draft.phoneNumber,
                draft.role.name,
            ),
        )
        response.unwrap { Unit }
    }

    suspend fun deactivate(staffId: String): Result<Unit> = runCatching {
        val response = api.deactivate(
            staffId = staffId,
            idempotencyKey = staffIdempotencyKey(
                action = RemoteIdempotencyAction.DEACTIVATE,
                staffId,
            ),
        )
        response.unwrap { Unit }
    }

    private fun staffIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.STAFF, action, *parts)
}

private fun StaffViewDto.toDomain() = StaffMember(
    id = staffId.orEmpty(),
    storeId = primaryStoreId.orEmpty(),
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    email = email.orEmpty(),
    phoneNumber = phoneNumber.orEmpty(),
    role = parseRemoteStaffRole(role),
    active = active ?: false,
    permissionsSummary = permissionsSummary.orEmpty().ifBlank {
        permissions?.toDomain()?.summary().orEmpty()
    },
    permissions = permissions?.toDomain() ?: parseRemoteStaffRole(role).defaultPermissions(),
)

private fun StaffPermissions.toDto() = StaffPermissionsDto(
    viewAnalytics = viewAnalytics,
    managePrograms = managePrograms,
    processTransactions = processTransactions,
    manageCustomers = manageCustomers,
    manageStaff = manageStaff,
    viewSettings = viewSettings,
)

private fun StaffPermissionsDto.toDomain() = StaffPermissions(
    viewAnalytics = viewAnalytics ?: false,
    managePrograms = managePrograms ?: false,
    processTransactions = processTransactions ?: false,
    manageCustomers = manageCustomers ?: false,
    manageStaff = manageStaff ?: false,
    viewSettings = viewSettings ?: false,
)
