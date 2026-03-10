package com.vector.verevcodex.presentation.scan

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.Customer

enum class ScanCustomerAction {
    ADD_POINTS,
    REDEEM_POINTS,
    CHECK_IN,
}

data class ScanUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val scannedId: String? = null,
    val customer: Customer? = null,
    val selectedAction: ScanCustomerAction = ScanCustomerAction.ADD_POINTS,
    val isSearching: Boolean = false,
    val isSubmitting: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
