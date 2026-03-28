package com.vector.verevcodex.domain.repository.realtime

import com.vector.verevcodex.domain.model.realtime.MerchantRealtimeEvent
import com.vector.verevcodex.domain.model.realtime.RealtimeConnectionState
import kotlinx.coroutines.flow.Flow

interface RealtimeRepository {
    fun observeEvents(): Flow<MerchantRealtimeEvent>
    fun observeRefreshSignals(): Flow<Unit>
    fun observeConnectionState(): Flow<RealtimeConnectionState>
}
