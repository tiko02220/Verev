package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction

data class ProgramToggleCandidate(
    val program: RewardProgram,
    val enabled: Boolean,
    val autoScheduleWarning: Boolean,
)

data class LoyaltyUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val currencyCode: String = "AMD",
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = true,
    val programs: List<RewardProgram> = emptyList(),
    val allPrograms: List<RewardProgram> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val campaigns: List<Campaign> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val activeScanActions: List<RewardProgramScanAction> = emptyList(),
    val activeSubEditor: ProgramSubEditor? = null,
    val activeBenefitEditor: ProgramBenefitEditorTarget? = null,
    val editorState: ProgramEditorState? = null,
    val editorFieldErrors: Map<String, Int> = emptyMap(),
    val deleteCandidate: RewardProgram? = null,
    val programToggleCandidate: ProgramToggleCandidate? = null,
    val rewardEditorState: RewardEditorState? = null,
    val rewardEditorFieldErrors: Map<String, Int> = emptyMap(),
    val rewardDeleteCandidate: Reward? = null,
    val giveawayEditorState: GiveawayEditorState? = null,
    val giveawayEditorFieldErrors: Map<String, Int> = emptyMap(),
    val giveawayDeleteCandidate: Campaign? = null,
    val busyProgramId: String? = null,
    val busyRewardId: String? = null,
    val busyCampaignId: String? = null,
    val isSubmitting: Boolean = false,
    val formErrorRes: Int? = null,
    val messageRes: Int? = null,
)
