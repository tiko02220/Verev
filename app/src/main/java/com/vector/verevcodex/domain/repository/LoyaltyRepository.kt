package com.vector.verevcodex.domain.repository

import com.vector.verevcodex.domain.model.Campaign
import com.vector.verevcodex.domain.model.Reward
import com.vector.verevcodex.domain.model.RewardProgram
import com.vector.verevcodex.domain.model.RewardProgramDraft
import com.vector.verevcodex.domain.model.RewardProgramScanAction
import kotlinx.coroutines.flow.Flow

interface LoyaltyRepository {
    fun observePrograms(storeId: String? = null): Flow<List<RewardProgram>>
    fun observeRewards(storeId: String? = null): Flow<List<Reward>>
    fun observeCampaigns(storeId: String? = null): Flow<List<Campaign>>
    fun observeActiveScanActions(storeId: String? = null): Flow<List<RewardProgramScanAction>>
    suspend fun createProgram(draft: RewardProgramDraft): RewardProgram
    suspend fun updateProgram(programId: String, draft: RewardProgramDraft): RewardProgram
    suspend fun setProgramEnabled(programId: String, enabled: Boolean)
    suspend fun deleteProgram(programId: String)
}
