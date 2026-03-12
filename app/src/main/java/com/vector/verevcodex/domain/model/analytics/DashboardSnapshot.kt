package com.vector.verevcodex.domain.model.analytics

import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.business.BusinessOwner
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.business.Store

data class DashboardSnapshot(
    val owner: BusinessOwner,
    val selectedStore: Store,
    val stores: List<Store>,
    val analytics: BusinessAnalytics,
    val activePrograms: List<RewardProgram>,
    val activeCampaigns: List<Campaign>,
    val topStaff: List<Pair<StaffMember, StaffAnalytics>>,
    val recentTransactions: List<Transaction>,
)
