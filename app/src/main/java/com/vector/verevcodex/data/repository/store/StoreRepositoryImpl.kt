package com.vector.verevcodex.data.repository.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.vector.verevcodex.common.errors.AppStateException
import com.vector.verevcodex.data.preferences.AccountPreferenceKeys
import com.vector.verevcodex.data.preferences.merchantPreferenceStore
import com.vector.verevcodex.data.remote.auth.AuthRemoteDataSource
import com.vector.verevcodex.data.remote.store.StoreRemoteDataSource
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.business.StoreDraft
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.repository.store.StoreRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@Singleton
class StoreRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val authRepository: AuthRepository,
    private val authRemote: AuthRemoteDataSource,
    private val storeRemote: StoreRemoteDataSource,
) : StoreRepository {

    private val dataStore = context.merchantPreferenceStore
    private val storeRefreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val storesFlow = combine(
        authRepository.observeSession(),
        storeRefreshTrigger.onStart { emit(Unit) },
    ) { session, _ -> session }
        .flatMapLatest { session ->
            if (session == null) return@flatMapLatest flowOf(emptyList<Store>())
            val ownerId = resolveOwnerId(session)
            kotlinx.coroutines.flow.flow {
                emit(storeRemote.list(ownerId).getOrElse { emptyList() })
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = emptyList(),
        )

    private val selectedStoreFlow = combine(
        storesFlow,
        authRepository.observeSession(),
        dataStore.data,
    ) { stores, session, preferences ->
        val selectedStoreKey = AccountPreferenceKeys.selectedStoreId(session?.user?.id)
        val selectedId = preferences[selectedStoreKey]
        stores.firstOrNull { it.id == selectedId } ?: stores.firstOrNull()
    }
        .distinctUntilChanged()
        .stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = null,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeStores(): Flow<List<Store>> = storesFlow

    override fun observeSelectedStore(): Flow<Store?> = selectedStoreFlow

    override suspend fun selectStore(storeId: String) {
        authRemote.updateSelectedStore(storeId).getOrThrow()
        val sessionId = authRepository.observeSession().first()?.user?.id
        val selectedStoreKey = AccountPreferenceKeys.selectedStoreId(sessionId)
        dataStore.edit { prefs -> prefs[selectedStoreKey] = storeId }
    }

    override suspend fun createStore(draft: StoreDraft): Result<Store> {
        val session = authRepository.observeSession().first()
            ?: return Result.failure(AppStateException(AppStateException.Reason.NoActiveSession))
        val ownerId = if (session.user.role == StaffRole.OWNER) {
            session.user.relatedEntityId
        } else {
            observeSelectedStore().first()?.ownerId ?: return Result.failure(IllegalStateException("Only owners can create branches"))
        }
        return storeRemote.create(draft, ownerId)
            .also { if (it.isSuccess) storeRefreshTrigger.emit(Unit) }
    }

    override suspend fun updateStore(store: Store): Result<Store> {
        return storeRemote.update(store)
            .also { if (it.isSuccess) storeRefreshTrigger.emit(Unit) }
    }

    override suspend fun setStoreActive(storeId: String, active: Boolean): Result<Unit> {
        val session = authRepository.observeSession().first()
            ?: return Result.failure(AppStateException(AppStateException.Reason.NoActiveSession))
        val ownerId = resolveOwnerId(session)
        val result = storeRemote.setActive(storeId, active, ownerId)
        if (result.isSuccess) storeRefreshTrigger.emit(Unit)
        return result.map { }
    }

    private suspend fun resolveOwnerId(session: com.vector.verevcodex.domain.model.auth.AuthSession): String =
        if (session.user.role == StaffRole.OWNER) session.user.relatedEntityId
        else ""
}
