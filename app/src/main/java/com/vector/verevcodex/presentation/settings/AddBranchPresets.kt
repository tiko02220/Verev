package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import com.vector.verevcodex.R

internal data class BranchHoursPreset(
    @StringRes val labelRes: Int,
    @StringRes val valueRes: Int,
)

internal val branchHoursPresets = listOf(
    BranchHoursPreset(
        labelRes = R.string.merchant_add_branch_preset_weekdays,
        valueRes = R.string.merchant_add_branch_preset_weekdays_value,
    ),
    BranchHoursPreset(
        labelRes = R.string.merchant_add_branch_preset_weekend,
        valueRes = R.string.merchant_add_branch_preset_weekend_value,
    ),
    BranchHoursPreset(
        labelRes = R.string.merchant_add_branch_preset_full_week,
        valueRes = R.string.merchant_add_branch_preset_full_week_value,
    ),
)
