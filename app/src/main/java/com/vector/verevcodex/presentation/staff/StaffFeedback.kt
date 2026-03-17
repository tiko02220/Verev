package com.vector.verevcodex.presentation.staff

import androidx.annotation.StringRes
import com.vector.verevcodex.R

internal data class StaffSuccessFeedback(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
)

internal fun resolveStaffSuccessFeedback(@StringRes messageRes: Int): StaffSuccessFeedback =
    when (messageRes) {
        R.string.merchant_staff_message_added -> StaffSuccessFeedback(
            titleRes = R.string.merchant_staff_message_added,
            messageRes = R.string.merchant_staff_success_added_supporting,
        )
        R.string.merchant_staff_message_updated -> StaffSuccessFeedback(
            titleRes = R.string.merchant_staff_message_updated,
            messageRes = R.string.merchant_staff_success_updated_supporting,
        )
        R.string.merchant_staff_message_deleted -> StaffSuccessFeedback(
            titleRes = R.string.merchant_staff_message_deleted,
            messageRes = R.string.merchant_staff_success_deleted_supporting,
        )
        else -> StaffSuccessFeedback(
            titleRes = R.string.merchant_success_dialog_title,
            messageRes = R.string.merchant_staff_success_generic_supporting,
        )
    }
