package com.vector.verevcodex.data.remote.store

import com.vector.verevcodex.common.phone.normalizePhoneNumber
import com.vector.verevcodex.data.remote.api.store.CreateStoreRequestDto
import com.vector.verevcodex.data.remote.api.store.StoreViewDto
import com.vector.verevcodex.data.remote.api.store.UpdateStoreRequestDto
import com.vector.verevcodex.data.remote.api.store.VerevStoresApi
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.core.remoteResult
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.business.StoreDraft
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRemoteDataSource @Inject constructor(
    private val api: VerevStoresApi,
) {

    suspend fun list(ownerId: String): Result<List<Store>> = remoteResult {
        val response = api.list()
        val list = response.unwrap { list: List<StoreViewDto> ->
            list.map { it.toDomain(ownerId) }
        }
        list
    }

    suspend fun get(storeId: String, ownerId: String): Result<Store> = remoteResult {
        val response = api.get(storeId)
        response.unwrap { dto -> dto.toDomain(ownerId) }
    }

    suspend fun create(draft: StoreDraft, ownerId: String): Result<Store> = remoteResult {
        val normalizedContactInfo = normalizePhoneNumber(draft.contactInfo)
        val request = CreateStoreRequestDto(
            name = draft.name.trim(),
            address = draft.address.trim(),
            contactInfo = normalizedContactInfo,
            category = draft.category.trim(),
            workingHours = draft.workingHours.trim(),
            logoUrl = "",
            primaryColor = draft.primaryColor.ifEmpty { "#111827" },
            secondaryColor = draft.secondaryColor.ifEmpty { "#F59E0B" },
        )
        val response = api.create(
            request = request,
            idempotencyKey = storeIdempotencyKey(
                action = RemoteIdempotencyAction.CREATE,
                draft.name,
                draft.address,
                normalizedContactInfo,
                draft.category,
                draft.workingHours,
                draft.primaryColor,
                draft.secondaryColor,
            ),
        )
        response.unwrap { dto -> dto.toDomain(ownerId) }
    }

    suspend fun update(store: Store): Result<Store> = remoteResult {
        val normalizedContactInfo = normalizePhoneNumber(store.contactInfo)
        val request = UpdateStoreRequestDto(
            name = store.name,
            address = store.address,
            contactInfo = normalizedContactInfo,
            category = store.category,
            workingHours = store.workingHours,
            logoUrl = store.logoUrl,
            primaryColor = store.primaryColor,
            secondaryColor = store.secondaryColor,
        )
        val response = api.update(
            storeId = store.id,
            request = request,
            idempotencyKey = storeIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                store.id,
                store.name,
                store.address,
                normalizedContactInfo,
                store.category,
                store.workingHours,
                store.logoUrl,
                store.primaryColor,
                store.secondaryColor,
            ),
        )
        response.unwrap { dto -> dto.toDomain(store.ownerId) }
    }

    suspend fun setActive(storeId: String, active: Boolean, ownerId: String): Result<Store> = remoteResult {
        val response = if (active) {
            api.activate(
                storeId = storeId,
                idempotencyKey = storeIdempotencyKey(
                    action = RemoteIdempotencyAction.ACTIVATE,
                    storeId,
                ),
            )
        } else {
            api.deactivate(
                storeId = storeId,
                idempotencyKey = storeIdempotencyKey(
                    action = RemoteIdempotencyAction.DEACTIVATE,
                    storeId,
                ),
            )
        }
        response.unwrap { dto -> dto.toDomain(ownerId) }
    }

    private fun storeIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.STORE, action, *parts)
}

private fun StoreViewDto.toDomain(ownerId: String) = Store(
    id = id.orEmpty(),
    ownerId = ownerId,
    name = name.orEmpty(),
    address = address.orEmpty(),
    contactInfo = contactInfo.orEmpty(),
    category = category.orEmpty(),
    workingHours = workingHours.orEmpty(),
    logoUrl = logoUrl.orEmpty(),
    primaryColor = primaryColor.orEmpty().ifEmpty { "#0C3B2E" },
    secondaryColor = secondaryColor.orEmpty().ifEmpty { "#FFBA00" },
    active = active ?: false,
)
