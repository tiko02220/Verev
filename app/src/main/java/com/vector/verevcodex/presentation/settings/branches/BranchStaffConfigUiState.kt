package com.vector.verevcodex.presentation.settings.branches

import com.vector.verevcodex.presentation.settings.*

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.business.Store

data class BranchStaffConfigUiState(
    val store: Store? = null,
    val storeName: String = "",
    val members: List<StaffMember> = emptyList(),
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorMessage: String? = null,
    @StringRes val messageRes: Int? = null,
)
