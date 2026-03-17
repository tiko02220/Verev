package com.vector.verevcodex.data.repository.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.DatabaseSeeder
import com.vector.verevcodex.data.preferences.merchantPreferenceStore
import com.vector.verevcodex.data.remote.auth.AuthRemoteDataSource
import com.vector.verevcodex.data.remote.store.StoreRemoteDataSource
import com.vector.verevcodex.data.repository.settings.BusinessSettingsRepositoryImpl
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.business.StoreDraft
import com.vector.verevcodex.domain.repository.store.StoreRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext context: Context,
    private val authRepository: AuthRepository,
    private val authRemote: AuthRemoteDataSource,
    private val businessSettingsRepository: BusinessSettingsRepositoryImpl,
    private val storeRemote: StoreRemoteDataSource,
    seeder: DatabaseSeeder,
) : StoreRepository {

    private val dataStore = context.merchantPreferenceStore
    private val storeRefreshTrigger = MutableSharedFlow<Unit>(replay = 0)

    init {
        runBlocking { seeder.seedIfNeeded() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeStores(): Flow<List<Store>> {
        return combine(
            authRepository.observeSession(),
            merge(flowOf(Unit), storeRefreshTrigger),
        ) { session, _ -> session }
            .flatMapLatest { session ->
                if (session == null) return@flatMapLatest flowOf(emptyList<Store>())
                val ownerId = resolveOwnerId(session)
                kotlinx.coroutines.flow.flow {
                    val list = storeRemote.list(ownerId).getOrElse { emptyList() }
                    emit(list)
                }
            }
    }

    override fun observeSelectedStore(): Flow<Store?> = combine(
        observeStores(),
        authRepository.observeSession(),
        dataStore.data,
    ) { stores, session, preferences ->
        val selectedStoreKey = stringPreferencesKey("${session?.user?.id ?: "anonymous"}_selected_store_id")
        val selectedId = preferences[selectedStoreKey]
        stores.firstOrNull { it.id == selectedId } ?: stores.firstOrNull()
    }

    override suspend fun selectStore(storeId: String) {
        authRemote.updateSelectedStore(storeId).getOrThrow()
        val sessionId = authRepository.observeSession().first()?.user?.id ?: "anonymous"
        val selectedStoreKey = stringPreferencesKey("${sessionId}_selected_store_id")
        dataStore.edit { prefs -> prefs[selectedStoreKey] = storeId }
    }

    override suspend fun createStore(draft: StoreDraft): Result<Store> {
        val session = authRepository.observeSession().first() ?: return Result.failure(IllegalStateException("No active session"))
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
        val session = authRepository.observeSession().first() ?: return Result.failure(IllegalStateException("No active session"))
        val ownerId = resolveOwnerId(session)
        val result = storeRemote.setActive(storeId, active, ownerId)
        if (result.isSuccess) storeRefreshTrigger.emit(Unit)
        return result.map { }
    }

    private suspend fun resolveOwnerId(session: com.vector.verevcodex.domain.model.auth.AuthSession): String =
        if (session.user.role == StaffRole.OWNER) session.user.relatedEntityId
        else kotlin.runCatching { database.ownerDao().getOwner().id }.getOrElse { "" }
}
