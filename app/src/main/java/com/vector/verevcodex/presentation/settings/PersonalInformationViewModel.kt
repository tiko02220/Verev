package com.vector.verevcodex.presentation.settings

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.auth.PersonalInformationUpdate
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateCurrentProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PersonalInformationViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    private val updateCurrentProfileUseCase: UpdateCurrentProfileUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PersonalInformationUiState())
    val uiState: StateFlow<PersonalInformationUiState> = _uiState.asStateFlow()
    private var persistedState = PersonalInformationUiState()

    init {
        viewModelScope.launch {
            observeSessionUseCase().collect { session ->
                val user = session?.user ?: return@collect
                persistedState = persistedState.copy(
                    fullName = user.fullName,
                    email = user.email,
                    phoneNumber = user.phoneNumber,
                    profilePhotoUri = user.profilePhotoUri,
                )
                _uiState.update { state ->
                    if (state.isSaving) {
                        state
                    } else if (state.isEditing) {
                        state
                    } else {
                        state.copy(
                            fullName = user.fullName,
                            email = user.email,
                            phoneNumber = user.phoneNumber,
                            profilePhotoUri = user.profilePhotoUri,
                        )
                    }
                }
            }
        }
    }

    fun startEditing() = _uiState.update { it.copy(isEditing = true, errorRes = null, messageRes = null) }

    fun cancelEditing() = _uiState.update {
        it.copy(
            fullName = persistedState.fullName,
            email = persistedState.email,
            phoneNumber = persistedState.phoneNumber,
            profilePhotoUri = persistedState.profilePhotoUri,
            isEditing = false,
            errorRes = null,
            messageRes = null,
        )
    }

    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value, errorRes = null, messageRes = null) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, errorRes = null, messageRes = null) }
    fun updatePhoneNumber(value: String) = _uiState.update { it.copy(phoneNumber = value, errorRes = null, messageRes = null) }
    fun updateProfilePhotoUri(value: String) = _uiState.update { it.copy(profilePhotoUri = value, errorRes = null, messageRes = null) }
    fun dismissMessage() = _uiState.update { it.copy(messageRes = null, errorRes = null) }

    fun save() {
        val state = _uiState.value
        val fullName = state.fullName.trim()
        val email = state.email.trim()
        val phoneNumber = state.phoneNumber.trim()

        val error = SettingsValidation.personalInformationError(
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches(),
        )
        if (error != null) {
            _uiState.update { it.copy(errorRes = error, messageRes = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorRes = null, messageRes = null) }
            updateCurrentProfileUseCase(
                PersonalInformationUpdate(
                    fullName = fullName,
                    email = email,
                    phoneNumber = phoneNumber,
                    profilePhotoUri = state.profilePhotoUri,
                )
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        messageRes = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorRes = throwable.toSettingsErrorRes(R.string.merchant_settings_error_profile_update_failed),
                    )
                }
            }
        }
    }
}
