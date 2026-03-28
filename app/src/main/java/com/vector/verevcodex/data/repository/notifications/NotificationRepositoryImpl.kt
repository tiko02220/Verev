package com.vector.verevcodex.data.repository.notifications

import com.vector.verevcodex.data.remote.notifications.NotificationRemoteDataSource
import com.vector.verevcodex.domain.model.notifications.MerchantNotification
import com.vector.verevcodex.domain.repository.notifications.NotificationRepository
import com.vector.verevcodex.domain.repository.realtime.RealtimeRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationRemoteDataSource,
    private val realtimeRepository: RealtimeRepository,
) : NotificationRepository {
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationFlows = ConcurrentHashMap<Int, Flow<List<MerchantNotification>>>()

    override fun observeMerchantNotifications(limit: Int): Flow<List<MerchantNotification>> =
        notificationFlows.getOrPut(limit.coerceAtLeast(1)) {
            merge(
                refreshRequests.onStart { emit(Unit) },
                realtimeRepository.observeRefreshSignals().map { Unit },
            )
                .map { remoteDataSource.list(limit = limit).getOrElse { emptyList() } }
                .stateIn(
                    scope = repositoryScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = emptyList(),
                )
        }

    override fun observeUnreadMerchantNotificationCount(limit: Int): Flow<Int> =
        observeMerchantNotifications(limit).map { notifications -> notifications.count { !it.isRead } }

    override suspend fun markMerchantNotificationRead(notificationId: String): Result<Unit> =
        remoteDataSource.markRead(notificationId).onSuccess { refreshRequests.tryEmit(Unit) }

    override suspend fun markAllMerchantNotificationsRead(): Result<Unit> =
        remoteDataSource.markAllRead().onSuccess { refreshRequests.tryEmit(Unit) }
}
