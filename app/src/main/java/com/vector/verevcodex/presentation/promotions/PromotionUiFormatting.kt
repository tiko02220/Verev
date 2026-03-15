package com.vector.verevcodex.presentation.promotions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionType

@Composable
internal fun Campaign.promotionValueText(): String = when (promotionType) {
    PromotionType.POINTS_MULTIPLIER -> stringResource(
        R.string.merchant_promotion_value_points_multiplier,
        promotionValue,
    )
    PromotionType.PERCENT_DISCOUNT -> stringResource(
        R.string.merchant_promotion_value_percent_discount,
        promotionValue.toInt(),
    )
    PromotionType.FIXED_DISCOUNT -> stringResource(
        R.string.merchant_promotion_value_fixed_discount,
        promotionValue.toInt(),
    )
    PromotionType.BONUS_POINTS -> stringResource(
        R.string.merchant_promotion_value_bonus_points,
        promotionValue.toInt(),
    )
    PromotionType.BUY_ONE_GET_ONE -> stringResource(R.string.merchant_promotion_value_buy_one_get_one)
    PromotionType.FREE_ITEM -> stringResource(R.string.merchant_promotion_value_free_item)
}
