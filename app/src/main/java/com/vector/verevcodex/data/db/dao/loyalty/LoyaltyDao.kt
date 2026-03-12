package com.vector.verevcodex.data.db.dao.loyalty

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.loyalty.CampaignEntity
import com.vector.verevcodex.data.db.entity.loyalty.CampaignTargetEntity
import com.vector.verevcodex.data.db.entity.loyalty.RewardEntity
import com.vector.verevcodex.data.db.entity.loyalty.RewardProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM reward_programs WHERE (:storeId IS NULL OR storeId = :storeId) ORDER BY name")
    fun observePrograms(storeId: String?): Flow<List<RewardProgramEntity>>

    @Query("SELECT * FROM reward_programs WHERE id = :programId LIMIT 1")
    suspend fun getProgram(programId: String): RewardProgramEntity?

    @Query("SELECT * FROM rewards WHERE (:storeId IS NULL OR storeId = :storeId) ORDER BY pointsRequired")
    fun observeRewards(storeId: String?): Flow<List<RewardEntity>>

    @Query("SELECT * FROM campaigns WHERE (:storeId IS NULL OR storeId = :storeId) ORDER BY startDate DESC")
    fun observeCampaigns(storeId: String?): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaign_targets ORDER BY description")
    fun observeCampaignTargets(): Flow<List<CampaignTargetEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :campaignId LIMIT 1")
    suspend fun getCampaign(campaignId: String): CampaignEntity?

    @Query("SELECT * FROM campaign_targets WHERE campaignId = :campaignId LIMIT 1")
    suspend fun getCampaignTarget(campaignId: String): CampaignTargetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(items: List<RewardProgramEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(item: RewardProgramEntity)

    @Query("DELETE FROM reward_programs WHERE id = :programId")
    suspend fun deleteProgram(programId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(items: List<RewardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaigns(items: List<CampaignEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(item: CampaignEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaignTargets(items: List<CampaignTargetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaignTarget(item: CampaignTargetEntity)

    @Query("DELETE FROM campaign_targets WHERE campaignId = :campaignId")
    suspend fun deleteCampaignTarget(campaignId: String)

    @Query("DELETE FROM campaigns WHERE id = :campaignId")
    suspend fun deleteCampaign(campaignId: String)
}
