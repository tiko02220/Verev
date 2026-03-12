package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram

data class LoyaltyUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val programs: List<RewardProgram> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val campaigns: List<Campaign> = emptyList(),
    val activeSubEditor: ProgramSubEditor? = null,
    val editorState: ProgramEditorState? = null,
    val editorFieldErrors: Map<String, Int> = emptyMap(),
    val deleteCandidate: RewardProgram? = null,
    val rewardEditorState: RewardEditorState? = null,
    val rewardEditorFieldErrors: Map<String, Int> = emptyMap(),
    val rewardDeleteCandidate: Reward? = null,
    val busyProgramId: String? = null,
    val busyRewardId: String? = null,
    val isSubmitting: Boolean = false,
    val formErrorRes: Int? = null,
    val messageRes: Int? = null,
)
