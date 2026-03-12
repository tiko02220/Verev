package com.vector.verevcodex.domain.usecase.loyalty

import com.vector.verevcodex.domain.model.loyalty.RewardDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft
import com.vector.verevcodex.domain.repository.loyalty.LoyaltyRepository

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

class CreateRewardUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(draft: RewardDraft) = repository.createReward(draft)
}

class UpdateRewardUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(rewardId: String, draft: RewardDraft) = repository.updateReward(rewardId, draft)
}

class SetRewardEnabledUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(rewardId: String, enabled: Boolean) = repository.setRewardEnabled(rewardId, enabled)
}

class DeleteRewardUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(rewardId: String) = repository.deleteReward(rewardId)
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
