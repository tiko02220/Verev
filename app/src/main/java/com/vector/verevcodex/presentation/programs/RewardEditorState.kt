package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardDraft
import java.time.LocalDate

internal const val REWARD_FIELD_NAME = "name"
internal const val REWARD_FIELD_POINTS = "points"
internal const val REWARD_FIELD_USAGE_LIMIT = "usageLimit"
internal const val REWARD_FIELD_EXPIRY = "expiry"

data class RewardEditorState(
    val rewardId: String? = null,
    val name: String = "",
    val description: String = "",
    val pointsRequired: String = "",
    val rewardType: RewardType = RewardType.FREE_PRODUCT,
    val expirationDate: String = "",
    val usageLimit: String = "",
    val activeStatus: Boolean = true,
)

internal fun Reward.toEditorState() = RewardEditorState(
    rewardId = id,
    name = name,
    description = description,
    pointsRequired = pointsRequired.toString(),
    rewardType = rewardType,
    expirationDate = expirationDate?.toString().orEmpty(),
    usageLimit = usageLimit.toString(),
    activeStatus = activeStatus,
)

internal fun RewardEditorState.toDraft(storeId: String): RewardDraft = RewardDraft(
    storeId = storeId,
    name = name.trim(),
    description = description.trim(),
    pointsRequired = pointsRequired.trim().toInt(),
    rewardType = rewardType,
    expirationDate = expirationDate.trim().takeIf { it.isNotEmpty() }?.let(LocalDate::parse),
    usageLimit = usageLimit.trim().toInt(),
    activeStatus = activeStatus,
)

internal fun RewardEditorState.validate(): Map<String, Int> {
    val errors = linkedMapOf<String, Int>()
    if (name.isBlank()) errors[REWARD_FIELD_NAME] = com.vector.verevcodex.R.string.merchant_reward_error_name_required
    if (pointsRequired.trim().toIntOrNull()?.takeIf { it > 0 } == null) {
        errors[REWARD_FIELD_POINTS] = com.vector.verevcodex.R.string.merchant_reward_error_points_required
    }
    if (usageLimit.trim().toIntOrNull()?.takeIf { it > 0 } == null) {
        errors[REWARD_FIELD_USAGE_LIMIT] = com.vector.verevcodex.R.string.merchant_reward_error_usage_limit_required
    }
    val expiry = expirationDate.trim()
    if (expiry.isNotEmpty()) {
        runCatching { LocalDate.parse(expiry) }.onFailure {
            errors[REWARD_FIELD_EXPIRY] = com.vector.verevcodex.R.string.merchant_reward_error_expiration_invalid
        }
    }
    return errors
}
