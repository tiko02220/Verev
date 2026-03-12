package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.customer.CustomerActivity
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.transactions.Transaction

data class CustomerProfileUiState(
    val customer: Customer? = null,
    val relation: CustomerBusinessRelation? = null,
    val credentials: List<CustomerCredential> = emptyList(),
    val ledgerEntries: List<PointsLedger> = emptyList(),
    val bonusActions: List<CustomerBonusAction> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val activities: List<CustomerActivity> = emptyList(),
    val storeRewards: List<Reward> = emptyList(),
    val storePrograms: List<RewardProgram> = emptyList(),
    val storeCampaigns: List<Campaign> = emptyList(),
    val isSaving: Boolean = false,
    val feedbackMessageRes: Int? = null,
    val isMissingCustomer: Boolean = false,
)
