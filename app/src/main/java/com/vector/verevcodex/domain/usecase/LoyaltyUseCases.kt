package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.model.RewardProgramDraft
import com.vector.verevcodex.domain.repository.LoyaltyRepository

class ObserveProgramsUseCase(private val repository: LoyaltyRepository) {
    operator fun invoke(storeId: String? = null) = repository.observePrograms(storeId)
}

class ObserveRewardsUseCase(private val repository: LoyaltyRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeRewards(storeId)
}

class ObserveCampaignsUseCase(private val repository: LoyaltyRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeCampaigns(storeId)
}

class ObserveActiveScanActionsUseCase(private val repository: LoyaltyRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeActiveScanActions(storeId)
}

class CreateProgramUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(draft: RewardProgramDraft) = repository.createProgram(draft)
}

class UpdateProgramUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(programId: String, draft: RewardProgramDraft) = repository.updateProgram(programId, draft)
}

class SetProgramEnabledUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(programId: String, enabled: Boolean) = repository.setProgramEnabled(programId, enabled)
}

class DeleteProgramUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(programId: String) = repository.deleteProgram(programId)
}
