package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.business.Store

data class StoreManagementUiState(
    val stores: List<Store> = emptyList(),
    val selectedStoreId: String? = null,
    val isEditorVisible: Boolean = false,
    val isSaving: Boolean = false,
    val editor: StoreEditorState = StoreEditorState(),
    val editorFieldErrors: Map<String, Int> = emptyMap(),
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)

data class StoreEditorState(
    val editingStoreId: String? = null,
    val name: String = "",
    val address: String = "",
    val contactInfo: String = "",
    val category: String = "",
    val workingHours: String = "",
    val primaryColor: String = "#FFBA00",
    val secondaryColor: String = "#6B9773",
)
