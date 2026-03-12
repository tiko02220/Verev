package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes

data class PersonalInformationUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
