package com.vector.verevcodex.presentation.customers

import androidx.annotation.StringRes
import com.vector.verevcodex.R

internal enum class CustomerProfileTab(@StringRes val labelRes: Int) {
    OVERVIEW(R.string.merchant_customer_tab_overview),
    TRANSACTIONS(R.string.merchant_customer_tab_transactions),
    ACTIVITY(R.string.merchant_customer_tab_activity),
    ACCESS(R.string.merchant_customer_tab_access),
}
