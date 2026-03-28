package com.vector.verevcodex.presentation.settings.account

import com.vector.verevcodex.presentation.settings.*

import androidx.annotation.StringRes
import com.vector.verevcodex.common.phone.defaultPhoneNumberInput

data class PersonalInformationUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = defaultPhoneNumberInput(),
    val profilePhotoUri: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
