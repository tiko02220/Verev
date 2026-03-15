package com.vector.verevcodex.presentation.promotions

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.transactions.Transaction

data class PromotionsUiState(
    val selectedStoreId: String? = null,
    val selectedOwnerId: String? = null,
    val selectedStoreName: String = "",
    val promotions: List<Campaign> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val paymentMethods: List<SavedPaymentMethod> = emptyList(),
    val selectedFilter: PromotionFilter = PromotionFilter.ALL,
    val selectedPromotionId: String? = null,
    val paymentPromotionId: String? = null,
    val editorState: PromotionEditorState? = null,
    val editorFieldErrors: Map<String, Int> = emptyMap(),
    val deleteCandidate: Campaign? = null,
    val isSubmitting: Boolean = false,
    val busyPromotionId: String? = null,
    @StringRes val messageRes: Int? = null,
    @StringRes val errorRes: Int? = null,
)
