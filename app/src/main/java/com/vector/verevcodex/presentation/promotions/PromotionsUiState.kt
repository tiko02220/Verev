package com.vector.verevcodex.presentation.promotions

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.promotions.Campaign

data class PromotionsUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val promotions: List<Campaign> = emptyList(),
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
