package com.vector.verevcodex.domain.repository.store

import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.business.StoreDraft
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun observeStores(): Flow<List<Store>>
    fun observeSelectedStore(): Flow<Store?>
    suspend fun selectStore(storeId: String)
    suspend fun createStore(draft: StoreDraft): Result<Store>
    suspend fun updateStore(store: Store): Result<Store>
    suspend fun setStoreActive(storeId: String, active: Boolean): Result<Unit>
}
