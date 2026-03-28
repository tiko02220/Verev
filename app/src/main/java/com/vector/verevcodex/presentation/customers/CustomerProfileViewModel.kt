package com.vector.verevcodex.presentation.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.usecase.customer.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.customer.AdjustCustomerVisitsUseCase
import com.vector.verevcodex.domain.usecase.customer.MergeCustomersUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerBonusActionsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerCredentialsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerPointsLedgerUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsByStoreUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomersUseCase
import com.vector.verevcodex.domain.usecase.customer.PreviewCustomerMergeUseCase
import com.vector.verevcodex.domain.usecase.customer.PreviewCustomerSplitUseCase
import com.vector.verevcodex.domain.usecase.customer.RecordCustomerBonusActionUseCase
import com.vector.verevcodex.domain.usecase.customer.SplitCustomerUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveRewardsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveStoresUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionsUseCase
import com.vector.verevcodex.domain.usecase.transactions.RecordTransactionUseCase
import com.vector.verevcodex.domain.usecase.customer.UpdateCustomerContactUseCase
import com.vector.verevcodex.domain.usecase.customer.UpdateCustomerNotesAndTagsUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import com.vector.verevcodex.presentation.navigation.Screen
import com.vector.verevcodex.presentation.merchant.common.displayName
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CustomerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCustomersUseCase: ObserveCustomersUseCase,
    observeCustomerUseCase: ObserveCustomerUseCase,
    observeCustomerCredentialsUseCase: ObserveCustomerCredentialsUseCase,
    observeCustomerRelationsUseCase: ObserveCustomerRelationsUseCase,
    observeCustomerPointsLedgerUseCase: ObserveCustomerPointsLedgerUseCase,
    observeCustomerBonusActionsUseCase: ObserveCustomerBonusActionsUseCase,
    observeCustomerRelationsByStoreUseCase: ObserveCustomerRelationsByStoreUseCase,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    observeSessionUseCase: ObserveSessionUseCase,
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeStoresUseCase: ObserveStoresUseCase,
    observeProgramsUseCase: ObserveProgramsUseCase,
    observeRewardsUseCase: ObserveRewardsUseCase,
    observeCampaignsUseCase: ObserveCampaignsUseCase,
    private val updateCustomerContactUseCase: UpdateCustomerContactUseCase,
    private val updateCustomerNotesAndTagsUseCase: UpdateCustomerNotesAndTagsUseCase,
    private val adjustCustomerPointsUseCase: AdjustCustomerPointsUseCase,
    private val adjustCustomerVisitsUseCase: AdjustCustomerVisitsUseCase,
    private val recordCustomerBonusActionUseCase: RecordCustomerBonusActionUseCase,
    private val previewCustomerMergeUseCase: PreviewCustomerMergeUseCase,
    private val mergeCustomersUseCase: MergeCustomersUseCase,
    private val previewCustomerSplitUseCase: PreviewCustomerSplitUseCase,
    private val splitCustomerUseCase: SplitCustomerUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
) : ViewModel() {
    private val customerId: String? = savedStateHandle[Screen.CustomerProfile.ARG_CUSTOMER_ID]
    private val actingStaffId = MutableStateFlow("")

    private val _uiState = MutableStateFlow(CustomerProfileUiState())
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()

    init {
        val currentCustomerId = customerId
        if (currentCustomerId.isNullOrBlank()) {
            _uiState.value = CustomerProfileUiState(isMissingCustomer = true)
        } else {
            val storeRelationsFlow = observeSelectedStoreUseCase().flatMapLatest { selectedStore ->
                selectedStore?.id?.let(observeCustomerRelationsByStoreUseCase::invoke) ?: flowOf(emptyList())
            }
            val sessionFlow = observeSessionUseCase()
            sessionFlow
                .onEach { session ->
                    actingStaffId.value = session?.user?.relatedEntityId ?: session?.user?.id.orEmpty()
                }
                .launchIn(viewModelScope)

            combine(
                combine(
                    observeCustomerUseCase(currentCustomerId),
                    observeCustomerCredentialsUseCase(currentCustomerId),
                    observeCustomerRelationsUseCase(currentCustomerId),
                ) { customer, credentials, relations ->
                    Triple(customer, credentials, relations)
                },
                combine(
                    combine(
                        observeCustomerPointsLedgerUseCase(currentCustomerId),
                        observeCustomerBonusActionsUseCase(currentCustomerId),
                        observeTransactionsUseCase(),
                        observeSelectedStoreUseCase(),
                        observeStoresUseCase(),
                    ) { ledgerEntries, bonusActions, transactions, selectedStore, stores ->
                        CustomerProfileAuxBundle(
                            ledgerEntries = ledgerEntries,
                            bonusActions = bonusActions,
                            transactions = transactions,
                            selectedStore = selectedStore,
                            stores = stores,
                            storeRelations = emptyList(),
                        )
                    },
                    storeRelationsFlow,
                ) { auxBundle, storeRelations ->
                    CustomerProfileAuxBundle(
                        ledgerEntries = auxBundle.ledgerEntries,
                        bonusActions = auxBundle.bonusActions,
                        transactions = auxBundle.transactions,
                        selectedStore = auxBundle.selectedStore,
                        stores = auxBundle.stores,
                        storeRelations = storeRelations,
                    )
                },
                combine(
                    observeProgramsUseCase(),
                    observeRewardsUseCase(),
                    observeCampaignsUseCase(),
                ) { programs, rewards, campaigns ->
                    Triple(programs, rewards, campaigns)
                },
                sessionFlow,
                observeCustomersUseCase(),
            ) { customerBundle, auxBundle, loyaltyBundle, session, customers ->
                val customer = customerBundle.first
                val credentials = customerBundle.second
                val relations = customerBundle.third
                val ledgerEntries = auxBundle.ledgerEntries
                val bonusActions = auxBundle.bonusActions
                val transactions = auxBundle.transactions
                val selectedStore = auxBundle.selectedStore
                val stores = auxBundle.stores
                val storeRelations = auxBundle.storeRelations
                val programs = loyaltyBundle.first
                val rewards = loyaltyBundle.second
                val campaigns = loyaltyBundle.third
                val previousState = _uiState.value
                val selectedRelation = relations.firstOrNull { it.storeId == selectedStore?.id }
                    ?: relations.firstOrNull { it.storeId == customer?.favoriteStoreId }
                    ?: relations.firstOrNull()
                val activeStoreId = selectedRelation?.storeId ?: selectedStore?.id ?: customer?.favoriteStoreId
                val activeStore = stores.firstOrNull { it.id == activeStoreId }
                val activeStoreName = activeStore?.name
                val activeStoreAddress = activeStore?.address
                val favoriteStoreName = stores.firstOrNull { it.id == customer?.favoriteStoreId }?.name
                val relevantTransactions = transactions
                    .filter { it.customerId == currentCustomerId }
                    .filter { transaction -> activeStoreId == null || transaction.storeId == activeStoreId }
                    .sortedByDescending { it.timestamp }
                val storePrograms = programs.filter { program -> activeStoreId == null || program.storeId == activeStoreId }
                val tierProgram = storePrograms.firstOrNull { it.active && it.configuration.tierTrackingEnabled }
                val tierRule = tierProgram?.configuration?.tierRule
                val displayCustomer = customer?.resolveDisplayedTier(tierRule)
                val manualVisitDelta = bonusActions
                    .filter { action ->
                        action.storeId == null || activeStoreId == null || action.storeId == activeStoreId
                    }
                    .filter { action ->
                        action.type == CustomerBonusActionType.MANUAL_VISITS_ADDED ||
                            action.type == CustomerBonusActionType.MANUAL_VISITS_REMOVED
                    }
                    .sumOf { CustomerCrmConstants.parseManualVisitDelta(it.details) }
                val scopedVisits = (relevantTransactions.size + manualVisitDelta).coerceAtLeast(0)
                val scopedSpent = relevantTransactions.sumOf { it.amount }
                val scopedLastVisit = relevantTransactions.firstOrNull()?.timestamp
                val nextTierThreshold = tierRule?.nextThresholdFor(displayCustomer?.loyaltyTierLabel)
                val tierProgress = displayCustomer?.let {
                    calculateTierProgress(
                        currentPoints = it.currentPoints,
                        nextThreshold = nextTierThreshold,
                        currentTierFloor = tierRule?.currentTierFloorFor(it.loyaltyTierLabel) ?: 0,
                    )
                } ?: 0f
                CustomerProfileUiState(
                    customer = displayCustomer,
                    relation = selectedRelation,
                    activeStoreId = activeStoreId,
                    activeStoreName = activeStoreName,
                    activeStoreAddress = activeStoreAddress,
                    favoriteStoreName = favoriteStoreName,
                    canManageDuplicates = session?.user?.permissions?.manageCustomers == true,
                    organizationId = previousState.organizationId,
                    availableDuplicateCustomers = customers.filter { it.id != currentCustomerId },
                    mergePreview = previousState.mergePreview,
                    splitPreview = previousState.splitPreview,
                    duplicateResolutionSuccessMessageRes = previousState.duplicateResolutionSuccessMessageRes,
                    duplicateResolutionErrorMessage = previousState.duplicateResolutionErrorMessage,
                    duplicateResolutionNavigationCustomerId = previousState.duplicateResolutionNavigationCustomerId,
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
                    storeRewards = rewards.filter { reward ->
                        (activeStoreId == null || reward.storeId == activeStoreId) &&
                            reward.activeStatus &&
                            (reward.expirationDate == null || !reward.expirationDate.isBefore(LocalDate.now()))
                    },
                    storePrograms = storePrograms,
                    storeCampaigns = campaigns.filter { campaign -> activeStoreId == null || campaign.storeId == activeStoreId },
                    suggestedTags = storeRelations
                        .flatMap { it.tags }
                        .map(String::trim)
                        .filter(String::isNotBlank)
                        .distinct()
                        .sorted(),
                    scopedVisits = scopedVisits,
                    scopedSpent = scopedSpent,
                    scopedLastVisit = scopedLastVisit,
                    tierProgress = tierProgress,
                    nextTierThreshold = nextTierThreshold,
                    isSaving = previousState.isSaving,
                    isDuplicateResolutionLoading = previousState.isDuplicateResolutionLoading,
                    feedbackMessageRes = previousState.feedbackMessageRes,
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
        val tierName = customer.loyaltyTierLabel
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

    fun updateTags(tags: List<String>) {
        val relation = _uiState.value.relation ?: return
        updateNotesAndTags(relation.notes, tags)
    }

    fun recordManualTransaction(
        isRefund: Boolean,
        amount: Double,
        description: String,
        programId: String,
    ) {
        val state = _uiState.value
        val customer = state.customer ?: return
        val storeId = state.activeStoreId ?: return
        val staffId = actingStaffId.value.ifBlank { return }
        val program = state.storePrograms.firstOrNull { it.id == programId && it.active }
            ?: run {
                _uiState.value = state.copy(feedbackMessageRes = R.string.merchant_customer_program_required)
                return
            }
        val absoluteAmount = amount.coerceAtLeast(0.0)
        val pointsDelta = program.manualTransactionPoints(absoluteAmount)
        val finalAmount = if (isRefund) -absoluteAmount else absoluteAmount
        val finalPointsEarned = if (isRefund) -pointsDelta else pointsDelta
        val summary = description.trim().ifBlank {
            if (isRefund) CustomerCrmConstants.manualRefundSummary else CustomerCrmConstants.manualPurchaseSummary
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                recordTransactionUseCase(
                    transaction = com.vector.verevcodex.domain.model.transactions.Transaction(
                        id = UUID.randomUUID().toString(),
                        customerId = customer.id,
                        storeId = storeId,
                        staffId = staffId,
                        amount = finalAmount,
                        pointsEarned = finalPointsEarned,
                        pointsRedeemed = 0,
                        timestamp = LocalDateTime.now(),
                        metadata = "$summary • ${program.name}",
                    ),
                    incrementVisit = !isRefund,
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_transaction_created,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_transaction_create_failed,
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
                val result = adjustCustomerPointsUseCase(
                    customer.id,
                    delta,
                    reason.ifBlank { CustomerCrmConstants.manualAdjustmentReason },
                )
                result
            }.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = if (result.approvalRequired) {
                        R.string.merchant_customer_points_approval_requested
                    } else {
                        R.string.merchant_customer_points_updated
                    },
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_points_update_failed,
                )
            }
        }
    }

    fun adjustPoints(delta: Int, reason: String, programId: String) {
        val state = _uiState.value
        val program = state.storePrograms.firstOrNull { it.id == programId && it.active }
            ?: run {
                _uiState.value = state.copy(feedbackMessageRes = R.string.merchant_customer_program_required)
                return
            }
        when (program.type) {
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.DIGITAL_STAMP,
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.PURCHASE_FREQUENCY -> {
                adjustVisits(delta, reason)
            }
            else -> adjustPoints(delta, "$reason • ${program.name}")
        }
    }

    fun showProgramRequiredFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessageRes = R.string.merchant_customer_program_required)
    }

    private fun adjustVisits(delta: Int, reason: String) {
        val state = _uiState.value
        val customer = state.customer ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                adjustCustomerVisitsUseCase(
                    customer.id,
                    delta,
                    reason.ifBlank { CustomerCrmConstants.manualAdjustmentReason },
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_visits_updated,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_visits_update_failed,
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

    fun previewMerge(targetCustomerId: String) {
        val sourceCustomerId = _uiState.value.customer?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDuplicateResolutionLoading = true,
                duplicateResolutionErrorMessage = null,
                duplicateResolutionSuccessMessageRes = null,
                duplicateResolutionNavigationCustomerId = null,
            )
            runCatching {
                previewCustomerMergeUseCase(sourceCustomerId, targetCustomerId)
            }.onSuccess { preview ->
                _uiState.value = _uiState.value.copy(
                    isDuplicateResolutionLoading = false,
                    mergePreview = preview,
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isDuplicateResolutionLoading = false,
                    duplicateResolutionErrorMessage = throwable.message ?: "Unable to preview customer merge.",
                )
            }
        }
    }

    fun clearMergePreview() {
        _uiState.value = _uiState.value.copy(mergePreview = null)
    }

    fun mergeWithPreviewTarget() {
        val sourceCustomerId = _uiState.value.customer?.id ?: return
        val targetCustomerId = _uiState.value.mergePreview?.targetCustomer?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDuplicateResolutionLoading = true,
                duplicateResolutionErrorMessage = null,
            )
            runCatching {
                mergeCustomersUseCase(sourceCustomerId, targetCustomerId)
            }.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isDuplicateResolutionLoading = false,
                    mergePreview = null,
                    duplicateResolutionSuccessMessageRes = R.string.merchant_customer_merge_success_message,
                    duplicateResolutionNavigationCustomerId = result.targetCustomer.id,
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isDuplicateResolutionLoading = false,
                    duplicateResolutionErrorMessage = throwable.message ?: "Unable to merge customers.",
                )
            }
        }
    }

    fun previewSplit() {
        val sourceCustomerId = _uiState.value.customer?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDuplicateResolutionLoading = true,
                duplicateResolutionErrorMessage = null,
                duplicateResolutionSuccessMessageRes = null,
                duplicateResolutionNavigationCustomerId = null,
            )
            runCatching {
                previewCustomerSplitUseCase(sourceCustomerId)
            }.onSuccess { preview ->
                _uiState.value = _uiState.value.copy(
                    isDuplicateResolutionLoading = false,
                    organizationId = preview.organizationId,
                    splitPreview = preview,
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isDuplicateResolutionLoading = false,
                    duplicateResolutionErrorMessage = throwable.message ?: "Unable to preview customer split.",
                )
            }
        }
    }

    fun clearSplitPreview() {
        _uiState.value = _uiState.value.copy(splitPreview = null)
    }

    fun splitIdentity(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        notes: String,
    ) {
        val sourceCustomerId = _uiState.value.customer?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDuplicateResolutionLoading = true,
                duplicateResolutionErrorMessage = null,
            )
            runCatching {
                splitCustomerUseCase(
                    sourceCustomerId = sourceCustomerId,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    notes = notes.ifBlank { null },
                )
            }.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isDuplicateResolutionLoading = false,
                    splitPreview = null,
                    duplicateResolutionSuccessMessageRes = R.string.merchant_customer_split_success_message,
                    duplicateResolutionNavigationCustomerId = result.targetCustomer.id,
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isDuplicateResolutionLoading = false,
                    duplicateResolutionErrorMessage = throwable.message ?: "Unable to split customer identity.",
                )
            }
        }
    }

    fun clearDuplicateResolutionFeedback() {
        _uiState.value = _uiState.value.copy(
            duplicateResolutionSuccessMessageRes = null,
            duplicateResolutionErrorMessage = null,
        )
    }

    fun consumeDuplicateResolutionNavigation() {
        _uiState.value = _uiState.value.copy(duplicateResolutionNavigationCustomerId = null)
    }
}

private data class CustomerProfileAuxBundle(
    val ledgerEntries: List<com.vector.verevcodex.domain.model.loyalty.PointsLedger>,
    val bonusActions: List<com.vector.verevcodex.domain.model.customer.CustomerBonusAction>,
    val transactions: List<com.vector.verevcodex.domain.model.transactions.Transaction>,
    val selectedStore: com.vector.verevcodex.domain.model.business.Store?,
    val stores: List<com.vector.verevcodex.domain.model.business.Store>,
    val storeRelations: List<com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation>,
)

private fun com.vector.verevcodex.domain.model.loyalty.TierProgramRule.nextThresholdFor(
    tierLabel: String?,
): Int? = nextLevelAfter(tierLabel)?.threshold

private fun com.vector.verevcodex.domain.model.loyalty.TierProgramRule.currentTierFloorFor(
    tierLabel: String,
): Int = currentTierFloor(tierLabel)

private fun calculateTierProgress(
    currentPoints: Int,
    nextThreshold: Int?,
    currentTierFloor: Int,
): Float {
    if (nextThreshold == null) return 1f
    val range = (nextThreshold - currentTierFloor).coerceAtLeast(1)
    return ((currentPoints - currentTierFloor).toFloat() / range).coerceIn(0f, 1f)
}
