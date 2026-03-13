package com.vector.verevcodex.domain.model.analytics

import com.vector.verevcodex.domain.model.common.Identifiable

data class StaffAnalytics(
    override val id: String,
    val staffId: String,
    val staffName: String,
    val storeId: String,
    val transactionsProcessed: Int,
    val revenueHandled: Double,
    val customersServed: Int,
    val rewardsRedeemed: Int,
    val averageTransactionValue: Double,
) : Identifiable
