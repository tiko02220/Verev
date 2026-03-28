package com.vector.verevcodex.domain.model.realtime

sealed interface RealtimeConnectionState {
    data object Idle : RealtimeConnectionState
    data object Connecting : RealtimeConnectionState
    data object Connected : RealtimeConnectionState
    data class Disconnected(val retrying: Boolean) : RealtimeConnectionState
    data class Failed(val message: String) : RealtimeConnectionState
}
