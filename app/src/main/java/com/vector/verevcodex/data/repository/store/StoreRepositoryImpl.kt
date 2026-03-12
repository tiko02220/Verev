package com.vector.verevcodex.data.repository.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.DatabaseSeeder
import com.vector.verevcodex.data.preferences.merchantPreferenceStore
import com.vector.verevcodex.data.repository.settings.BusinessSettingsRepositoryImpl
import com.vector.verevcodex.data.mapper.toDomain
import com.vector.verevcodex.data.mapper.toEntity
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.business.StoreDraft
import com.vector.verevcodex.domain.repository.store.StoreRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.UUID

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext context: Context,
    private val authRepository: AuthRepository,
    private val businessSettingsRepository: BusinessSettingsRepositoryImpl,
    seeder: DatabaseSeeder,
) : StoreRepository {
    private val dataStore = context.merchantPreferenceStore

    init {
        runBlocking { seeder.seedIfNeeded() }
    }

    override fun observeStores(): Flow<List<Store>> = combine(
        database.storeDao().observeStores(),
        authRepository.observeSession(),
    ) { entities, session ->
        val filtered = when {
            session == null -> entities
            session.user.role == StaffRole.OWNER -> entities.filter { entity -> entity.ownerId == session.user.relatedEntityId }
            else -> {
                val staffStoreId = database.staffDao().getById(session.user.relatedEntityId)?.storeId
                entities.filter { entity -> entity.id == staffStoreId }
            }
        }
        filtered.map { it.toDomain() }
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
        val sessionId = authRepository.observeSession().first()?.user?.id ?: "anonymous"
        val selectedStoreKey = stringPreferencesKey("${sessionId}_selected_store_id")
        dataStore.edit { prefs -> prefs[selectedStoreKey] = storeId }
    }

    override suspend fun createStore(draft: StoreDraft): Result<Store> = runCatching {
        val session = authRepository.observeSession().first() ?: error("No active session")
        val ownerId = if (session.user.role == StaffRole.OWNER) {
            session.user.relatedEntityId
        } else {
            observeSelectedStore().first()?.ownerId ?: error("Only owners can create branches")
        }
        val store = Store(
            id = UUID.randomUUID().toString(),
            ownerId = ownerId,
            name = draft.name.trim(),
            address = draft.address.trim(),
            contactInfo = draft.contactInfo.trim(),
            category = draft.category.trim(),
            workingHours = draft.workingHours.trim(),
            logoUrl = "",
            primaryColor = draft.primaryColor,
            secondaryColor = draft.secondaryColor,
            active = true,
        )
        database.storeDao().insert(store.toEntity())
        businessSettingsRepository.createDefaultStoreSettings(
            storeId = store.id,
            ownerId = ownerId,
            primaryColor = store.primaryColor,
            secondaryColor = store.secondaryColor,
        )
        store
    }

    override suspend fun updateStore(store: Store): Result<Store> = runCatching {
        database.storeDao().insert(store.toEntity())
        store
    }

    override suspend fun setStoreActive(storeId: String, active: Boolean): Result<Unit> = runCatching {
        val current = database.storeDao().getStore(storeId) ?: error("Store not found")
        database.storeDao().insert(current.copy(active = active))
    }
}
