package com.vector.verevcodex.domain.model

import java.time.LocalDateTime

data class CustomerBusinessRelation(
    override val id: String,
    val customerId: String,
    val storeId: String,
    val joinedAt: LocalDateTime,
    val notes: String,
) : Identifiable
