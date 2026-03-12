package com.vector.verevcodex.presentation.transactions

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.customer.Customer

data class TransactionEntryUiState(
    val isLoading: Boolean = true,
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val cashierId: String = "",
    val cashierName: String = "",
    val customers: List<Customer> = emptyList(),
    val filteredCustomers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val customerQuery: String = "",
    val lineItems: List<CheckoutLineItemDraft> = emptyList(),
    val availablePromotions: List<Campaign> = emptyList(),
    val selectedPromotionId: String? = null,
    val applyPointRedemption: Boolean = false,
    val redeemPointsInput: String = "",
    val note: String = "",
    val totals: CheckoutTotals = CheckoutTotals(),
    val recentTransactions: List<RecentCheckoutRecord> = emptyList(),
    val isSubmitting: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val successRes: Int? = null,
    val fieldErrors: Map<String, Int> = emptyMap(),
)
