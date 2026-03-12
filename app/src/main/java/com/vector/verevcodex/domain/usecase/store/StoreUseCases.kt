package com.vector.verevcodex.domain.usecase.store

import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.business.StoreDraft
import com.vector.verevcodex.domain.repository.store.StoreRepository

class ObserveStoresUseCase(private val repository: StoreRepository) {
    operator fun invoke() = repository.observeStores()
}

class ObserveSelectedStoreUseCase(private val repository: StoreRepository) {
    operator fun invoke() = repository.observeSelectedStore()
}

class SelectStoreUseCase(private val repository: StoreRepository) {
    suspend operator fun invoke(storeId: String) = repository.selectStore(storeId)
}

class CreateStoreUseCase(private val repository: StoreRepository) {
    suspend operator fun invoke(draft: StoreDraft): Result<Store> = repository.createStore(draft)
}

class UpdateStoreUseCase(private val repository: StoreRepository) {
    suspend operator fun invoke(store: Store): Result<Store> = repository.updateStore(store)
}

class SetStoreActiveUseCase(private val repository: StoreRepository) {
    suspend operator fun invoke(storeId: String, active: Boolean): Result<Unit> = repository.setStoreActive(storeId, active)
}
