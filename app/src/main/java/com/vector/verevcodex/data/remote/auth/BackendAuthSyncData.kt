package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.data.remote.api.auth.StoreViewDto

/**
 * Data from backend auth responses needed to sync owner + store into local DB
 * so dashboard and store list can emit (e.g. after login/signup/me).
 */
data class BackendAuthSyncData(
    val ownerId: String,
    val ownerFullName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val organizationDisplayName: String,
    val defaultCurrencyCode: String,
    val accessibleStoreIds: List<String>,
    val defaultStore: StoreViewDto?,
)
