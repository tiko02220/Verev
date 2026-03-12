package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import com.vector.verevcodex.R

internal data class BillingPlanUiSpec(
    @StringRes val nameRes: Int,
    @StringRes val summaryRes: Int,
    val featureResIds: List<Int>,
)

internal object BillingPlanUiCatalog {
    fun specFor(planIdOrName: String?): BillingPlanUiSpec = when (planIdOrName) {
        "starter", "Starter" -> BillingPlanUiSpec(
            nameRes = R.string.merchant_billing_plan_starter_name,
            summaryRes = R.string.merchant_billing_plan_starter_summary,
            featureResIds = listOf(
                R.string.merchant_billing_plan_starter_feature_1,
                R.string.merchant_billing_plan_starter_feature_2,
                R.string.merchant_billing_plan_starter_feature_3,
            ),
        )
        "growth_plus", "Growth Plus" -> BillingPlanUiSpec(
            nameRes = R.string.merchant_billing_plan_growth_plus_name,
            summaryRes = R.string.merchant_billing_plan_growth_plus_summary,
            featureResIds = listOf(
                R.string.merchant_billing_plan_growth_plus_feature_1,
                R.string.merchant_billing_plan_growth_plus_feature_2,
                R.string.merchant_billing_plan_growth_plus_feature_3,
            ),
        )
        else -> BillingPlanUiSpec(
            nameRes = R.string.merchant_billing_plan_business_standard_name,
            summaryRes = R.string.merchant_billing_plan_business_standard_summary,
            featureResIds = listOf(
                R.string.merchant_billing_plan_business_standard_feature_1,
                R.string.merchant_billing_plan_business_standard_feature_2,
                R.string.merchant_billing_plan_business_standard_feature_3,
            ),
        )
    }
}
