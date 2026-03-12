package com.vector.verevcodex.presentation.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.usecase.customer.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerBonusActionsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerCredentialsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerPointsLedgerUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerUseCase
import com.vector.verevcodex.domain.usecase.customer.RecordCustomerBonusActionUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveRewardsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionsUseCase
import com.vector.verevcodex.domain.usecase.customer.UpdateCustomerContactUseCase
import com.vector.verevcodex.domain.usecase.customer.UpdateCustomerNotesAndTagsUseCase
import com.vector.verevcodex.presentation.navigation.Screen
import com.vector.verevcodex.presentation.merchant.common.displayName
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class CustomerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCustomerUseCase: ObserveCustomerUseCase,
    observeCustomerCredentialsUseCase: ObserveCustomerCredentialsUseCase,
    observeCustomerRelationsUseCase: ObserveCustomerRelationsUseCase,
    observeCustomerPointsLedgerUseCase: ObserveCustomerPointsLedgerUseCase,
    observeCustomerBonusActionsUseCase: ObserveCustomerBonusActionsUseCase,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeProgramsUseCase: ObserveProgramsUseCase,
    observeRewardsUseCase: ObserveRewardsUseCase,
    observeCampaignsUseCase: ObserveCampaignsUseCase,
    private val updateCustomerContactUseCase: UpdateCustomerContactUseCase,
    private val updateCustomerNotesAndTagsUseCase: UpdateCustomerNotesAndTagsUseCase,
    private val adjustCustomerPointsUseCase: AdjustCustomerPointsUseCase,
    private val recordCustomerBonusActionUseCase: RecordCustomerBonusActionUseCase,
) : ViewModel() {
    private val customerId: String? = savedStateHandle[Screen.CustomerProfile.ARG_CUSTOMER_ID]

    private val _uiState = MutableStateFlow(CustomerProfileUiState())
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()

    init {
        val currentCustomerId = customerId
        if (currentCustomerId.isNullOrBlank()) {
            _uiState.value = CustomerProfileUiState(isMissingCustomer = true)
        } else {
            combine(
                combine(
                    observeCustomerUseCase(currentCustomerId),
                    observeCustomerCredentialsUseCase(currentCustomerId),
                    observeCustomerRelationsUseCase(currentCustomerId),
                ) { customer, credentials, relations ->
                    Triple(customer, credentials, relations)
                },
                combine(
                    observeCustomerPointsLedgerUseCase(currentCustomerId),
                    observeCustomerBonusActionsUseCase(currentCustomerId),
                    observeTransactionsUseCase(),
                    observeSelectedStoreUseCase(),
                ) { ledgerEntries, bonusActions, transactions, selectedStore ->
                    CustomerProfileAuxBundle(
                        ledgerEntries = ledgerEntries,
                        bonusActions = bonusActions,
                        transactions = transactions,
                        selectedStore = selectedStore,
                    )
                },
                combine(
                    observeProgramsUseCase(),
                    observeRewardsUseCase(),
                    observeCampaignsUseCase(),
                ) { programs, rewards, campaigns ->
                    Triple(programs, rewards, campaigns)
                },
            ) { customerBundle, auxBundle, loyaltyBundle ->
                val customer = customerBundle.first
                val credentials = customerBundle.second
                val relations = customerBundle.third
                val ledgerEntries = auxBundle.ledgerEntries
                val bonusActions = auxBundle.bonusActions
                val transactions = auxBundle.transactions
                val selectedStore = auxBundle.selectedStore
                val programs = loyaltyBundle.first
                val rewards = loyaltyBundle.second
                val campaigns = loyaltyBundle.third
                val relevantTransactions = transactions
                    .filter { it.customerId == currentCustomerId }
                    .sortedByDescending { it.timestamp }
                val selectedRelation = relations.firstOrNull { it.storeId == selectedStore?.id }
                    ?: relations.firstOrNull { it.storeId == customer?.favoriteStoreId }
                    ?: relations.firstOrNull()
                val activeStoreId = selectedRelation?.storeId ?: selectedStore?.id ?: customer?.favoriteStoreId
                CustomerProfileUiState(
                    customer = customer,
                    relation = selectedRelation,
                    credentials = credentials,
                    ledgerEntries = ledgerEntries,
                    bonusActions = bonusActions,
                    transactions = relevantTransactions,
                    activities = CustomerActivityTimelineBuilder.build(
                        relation = selectedRelation,
                        transactions = relevantTransactions,
                        ledgerEntries = ledgerEntries,
                        bonusActions = bonusActions,
                    ),
                    storeRewards = rewards.filter { reward -> activeStoreId == null || reward.storeId == activeStoreId },
                    storePrograms = programs.filter { program -> activeStoreId == null || program.storeId == activeStoreId },
                    storeCampaigns = campaigns.filter { campaign -> activeStoreId == null || campaign.storeId == activeStoreId },
                    isSaving = _uiState.value.isSaving,
                    feedbackMessageRes = _uiState.value.feedbackMessageRes,
                )
            }.onEach { _uiState.value = it }.launchIn(viewModelScope)
        }
    }

    fun markDiscountApplied(campaignId: String) {
        val state = _uiState.value
        val customer = state.customer ?: return
        val relation = state.relation
        val campaign = state.storeCampaigns.firstOrNull { it.id == campaignId } ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                recordCustomerBonusActionUseCase(
                    customerId = customer.id,
                    storeId = relation?.storeId ?: campaign.storeId,
                    type = CustomerBonusActionType.DISCOUNT_APPLIED,
                    title = CustomerCrmConstants.discountActionTitle(campaign.name),
                    details = CustomerCrmConstants.discountActionDetails(campaign.description),
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_bonus_discount_applied_message,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_bonus_discount_apply_failed,
                )
            }
        }
    }

    fun recordTierBenefit() {
        val state = _uiState.value
        val customer = state.customer ?: return
        val relation = state.relation
        val tierName = customer.loyaltyTier.displayName()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                recordCustomerBonusActionUseCase(
                    customerId = customer.id,
                    storeId = relation?.storeId,
                    type = CustomerBonusActionType.TIER_BENEFIT_RECORDED,
                    title = CustomerCrmConstants.tierBenefitActionTitle(tierName),
                    details = CustomerCrmConstants.tierBenefitActionDetails(tierName),
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_bonus_tier_recorded_message,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_bonus_tier_record_failed,
                )
            }
        }
    }

    fun updateContact(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
    ) {
        val customer = _uiState.value.customer ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                updateCustomerContactUseCase(
                    customerId = customer.id,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    favoriteStoreId = customer.favoriteStoreId,
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_contact_updated,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_contact_update_failed,
                )
            }
        }
    }

    fun updateNotesAndTags(notes: String, tags: List<String>) {
        val customer = _uiState.value.customer ?: return
        val relation = _uiState.value.relation ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                updateCustomerNotesAndTagsUseCase(
                    customerId = customer.id,
                    storeId = relation.storeId,
                    notes = notes,
                    tags = tags,
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_crm_updated,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_crm_update_failed,
                )
            }
        }
    }

    fun adjustPoints(delta: Int, reason: String) {
        val customer = _uiState.value.customer ?: return
        if (delta == 0) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                adjustCustomerPointsUseCase(
                    customer.id,
                    delta,
                    reason.ifBlank { CustomerCrmConstants.manualAdjustmentReason },
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_points_updated,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_points_update_failed,
                )
            }
        }
    }

    fun redeemReward(rewardId: String) {
        val state = _uiState.value
        val customer = state.customer ?: return
        val reward = state.storeRewards.firstOrNull { it.id == rewardId } ?: return
        if (customer.currentPoints < reward.pointsRequired) {
            _uiState.value = state.copy(feedbackMessageRes = R.string.merchant_customer_bonus_reward_insufficient_points)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                adjustCustomerPointsUseCase(
                    customer.id,
                    -reward.pointsRequired,
                    CustomerCrmConstants.rewardRedemptionReason(reward.name),
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_bonus_reward_redeemed_message,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_bonus_reward_redeem_failed,
                )
            }
        }
    }

    fun redeemCoupon(programId: String) {
        val state = _uiState.value
        val customer = state.customer ?: return
        val program = state.storePrograms.firstOrNull { it.id == programId && it.configuration.couponEnabled } ?: return
        val couponRule = program.configuration.couponRule
        if (customer.currentPoints < couponRule.pointsCost) {
            _uiState.value = state.copy(feedbackMessageRes = R.string.merchant_customer_bonus_coupon_insufficient_points)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                adjustCustomerPointsUseCase(
                    customer.id,
                    -couponRule.pointsCost,
                    CustomerCrmConstants.couponRedemptionReason(couponRule.couponName),
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_bonus_coupon_redeemed_message,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_bonus_coupon_redeem_failed,
                )
            }
        }
    }

    fun consumeFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessageRes = null)
    }
}

private data class CustomerProfileAuxBundle(
    val ledgerEntries: List<com.vector.verevcodex.domain.model.loyalty.PointsLedger>,
    val bonusActions: List<com.vector.verevcodex.domain.model.customer.CustomerBonusAction>,
    val transactions: List<com.vector.verevcodex.domain.model.transactions.Transaction>,
    val selectedStore: com.vector.verevcodex.domain.model.business.Store?,
)
