package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.Campaign
import com.vector.verevcodex.domain.model.Reward
import com.vector.verevcodex.domain.model.RewardProgram

data class LoyaltyUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val programs: List<RewardProgram> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val campaigns: List<Campaign> = emptyList(),
    val editorState: ProgramEditorState? = null,
    val deleteCandidate: RewardProgram? = null,
    val busyProgramId: String? = null,
    val isSubmitting: Boolean = false,
    val formErrorRes: Int? = null,
    val messageRes: Int? = null,
)
