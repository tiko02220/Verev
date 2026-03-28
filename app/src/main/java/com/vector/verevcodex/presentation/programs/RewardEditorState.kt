package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardDraft
import java.time.LocalDate

internal const val REWARD_FIELD_NAME = "name"
internal const val REWARD_FIELD_EXPIRY = "expiry"
internal const val REWARD_FIELD_AVAILABLE_QUANTITY = "availableQuantity"

data class RewardEditorState(
    val rewardId: String? = null,
    val name: String = "",
    val description: String = "",
    val imageUri: String = "",
    val expirationEnabled: Boolean = false,
    val expirationDate: String = "",
    val availableQuantity: String = "0",
    val activeStatus: Boolean = true,
    val usageLimit: Int = 1,
    val legacyPointsRequired: Int = 1,
)

internal fun Reward.toEditorState() = RewardEditorState(
    rewardId = id,
    name = name,
    description = description,
    imageUri = imageUri.orEmpty(),
    expirationEnabled = expirationDate != null,
    expirationDate = expirationDate?.toString().orEmpty(),
    availableQuantity = availableQuantity?.toString().orEmpty().ifBlank { "0" },
    activeStatus = activeStatus,
    usageLimit = usageLimit.coerceAtLeast(1),
    legacyPointsRequired = pointsRequired,
)

internal fun RewardEditorState.toDraft(storeId: String): RewardDraft = RewardDraft(
    storeId = storeId,
    name = name.trim(),
    description = description.trim(),
    pointsRequired = legacyPointsRequired.coerceAtLeast(1),
    rewardType = RewardType.FREE_PRODUCT,
    imageUri = imageUri.trim().ifBlank { null },
    expirationDate = expirationDate.trim().takeIf { expirationEnabled && it.isNotEmpty() }?.let(LocalDate::parse),
    usageLimit = usageLimit.coerceAtLeast(1),
    inventoryTracked = true,
    availableQuantity = availableQuantity.trim().ifBlank { "0" }.toInt(),
    activeStatus = activeStatus,
)

internal fun RewardEditorState.validate(): Map<String, Int> {
    val errors = linkedMapOf<String, Int>()
    if (name.isBlank()) errors[REWARD_FIELD_NAME] = com.vector.verevcodex.R.string.merchant_reward_error_name_required
    if (availableQuantity.trim().toIntOrNull()?.takeIf { it >= 0 } == null) {
        errors[REWARD_FIELD_AVAILABLE_QUANTITY] = com.vector.verevcodex.R.string.merchant_reward_error_inventory_required
    }
    val expiry = expirationDate.trim()
    if (expirationEnabled && expiry.isNotEmpty()) {
        runCatching { LocalDate.parse(expiry) }.onFailure {
            errors[REWARD_FIELD_EXPIRY] = com.vector.verevcodex.R.string.merchant_reward_error_expiration_invalid
        }
    } else if (expirationEnabled && expiry.isEmpty()) {
        errors[REWARD_FIELD_EXPIRY] = com.vector.verevcodex.R.string.merchant_reward_error_expiration_invalid
    }
    return errors
}
