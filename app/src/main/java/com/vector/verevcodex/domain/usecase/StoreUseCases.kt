package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.repository.StoreRepository

class ObserveStoresUseCase(private val repository: StoreRepository) {
    operator fun invoke() = repository.observeStores()
}

class ObserveSelectedStoreUseCase(private val repository: StoreRepository) {
    operator fun invoke() = repository.observeSelectedStore()
}

class SelectStoreUseCase(private val repository: StoreRepository) {
    suspend operator fun invoke(storeId: String) = repository.selectStore(storeId)
}
