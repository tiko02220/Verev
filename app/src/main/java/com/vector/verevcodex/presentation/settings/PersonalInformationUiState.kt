package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes

data class PersonalInformationUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePhotoUri: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
