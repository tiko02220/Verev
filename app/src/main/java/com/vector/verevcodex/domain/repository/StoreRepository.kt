package com.vector.verevcodex.domain.repository

import com.vector.verevcodex.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun observeStores(): Flow<List<Store>>
    fun observeSelectedStore(): Flow<Store?>
    suspend fun selectStore(storeId: String)
}
