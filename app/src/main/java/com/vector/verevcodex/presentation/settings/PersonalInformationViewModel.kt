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

    init {
        viewModelScope.launch {
            observeSessionUseCase().collect { session ->
                val user = session?.user ?: return@collect
                _uiState.update { state ->
                    if (state.isSaving) state else state.copy(
                        fullName = user.fullName,
                        email = user.email,
                        phoneNumber = user.phoneNumber,
                    )
                }
            }
        }
    }

    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value, errorRes = null, messageRes = null) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, errorRes = null, messageRes = null) }
    fun updatePhoneNumber(value: String) = _uiState.update { it.copy(phoneNumber = value, errorRes = null, messageRes = null) }
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
                )
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, messageRes = R.string.merchant_settings_message_profile_updated) }
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
