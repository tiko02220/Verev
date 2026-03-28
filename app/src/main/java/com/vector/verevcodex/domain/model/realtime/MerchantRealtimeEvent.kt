package com.vector.verevcodex.domain.model.realtime

data class MerchantRealtimeEvent(
    val eventId: String,
    val eventType: String,
    val scope: String,
    val payload: Map<String, Any?>,
    val createdAt: String,
    val destination: String? = null,
)
