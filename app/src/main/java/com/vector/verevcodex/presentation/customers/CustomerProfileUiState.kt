package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.customer.CustomerActivity
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerMergePreview
import com.vector.verevcodex.domain.model.customer.CustomerSplitPreview
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.transactions.Transaction
import androidx.annotation.StringRes
import java.time.LocalDateTime

data class CustomerProfileUiState(
    val customer: Customer? = null,
    val relation: CustomerBusinessRelation? = null,
    val activeStoreId: String? = null,
    val activeStoreName: String? = null,
    val activeStoreAddress: String? = null,
    val favoriteStoreName: String? = null,
    val canManageDuplicates: Boolean = false,
    val organizationId: String? = null,
    val availableDuplicateCustomers: List<Customer> = emptyList(),
    val mergePreview: CustomerMergePreview? = null,
    val splitPreview: CustomerSplitPreview? = null,
    @StringRes val duplicateResolutionSuccessMessageRes: Int? = null,
    val duplicateResolutionErrorMessage: String? = null,
    val duplicateResolutionNavigationCustomerId: String? = null,
    val credentials: List<CustomerCredential> = emptyList(),
    val ledgerEntries: List<PointsLedger> = emptyList(),
    val bonusActions: List<CustomerBonusAction> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val activities: List<CustomerActivity> = emptyList(),
    val storeRewards: List<Reward> = emptyList(),
    val storePrograms: List<RewardProgram> = emptyList(),
    val storeCampaigns: List<Campaign> = emptyList(),
    val suggestedTags: List<String> = emptyList(),
    val scopedVisits: Int = 0,
    val scopedSpent: Double = 0.0,
    val scopedLastVisit: LocalDateTime? = null,
    val tierProgress: Float = 0f,
    val nextTierThreshold: Int? = null,
    val isSaving: Boolean = false,
    val isDuplicateResolutionLoading: Boolean = false,
    val feedbackMessageRes: Int? = null,
    val isMissingCustomer: Boolean = false,
)
