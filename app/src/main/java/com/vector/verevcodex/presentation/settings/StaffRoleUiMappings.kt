package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.StaffRole

@StringRes
internal fun StaffRole.permissionsSummaryRes(): Int = when (this) {
    StaffRole.OWNER -> R.string.merchant_staff_role_summary_owner
    StaffRole.STORE_MANAGER -> R.string.merchant_staff_role_summary_manager
    StaffRole.CASHIER -> R.string.merchant_staff_role_summary_cashier
    StaffRole.STAFF -> R.string.merchant_staff_role_summary_staff
}
