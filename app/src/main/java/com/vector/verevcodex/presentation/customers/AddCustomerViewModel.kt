package com.vector.verevcodex.presentation.customers

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.CustomerDraft
import com.vector.verevcodex.domain.usecase.CreateCustomerUseCase
import com.vector.verevcodex.domain.usecase.ObserveSelectedStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class AddCustomerViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val createCustomerUseCase: CreateCustomerUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddCustomerUiState())
    val uiState: StateFlow<AddCustomerUiState> = _uiState.asStateFlow()

    init {
        observeSelectedStoreUseCase()
            .onEach { store ->
                _uiState.value = _uiState.value.copy(
                    selectedStoreId = store?.id,
                    selectedStoreName = store?.name.orEmpty(),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onFirstNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(firstName = sanitize(value), errorRes = null)
    }

    fun onLastNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(lastName = sanitize(value), errorRes = null)
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = sanitize(value), errorRes = null)
    }

    fun onPhoneChanged(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = sanitize(value), errorRes = null)
    }

    fun createCustomer() {
        val state = _uiState.value
        val storeId = state.selectedStoreId
        when {
            storeId.isNullOrBlank() -> publishError(R.string.merchant_add_customer_error_store)
            state.firstName.isBlank() -> publishError(R.string.merchant_add_customer_error_first_name)
            state.lastName.isBlank() -> publishError(R.string.merchant_add_customer_error_last_name)
            state.email.isBlank() -> publishError(R.string.merchant_add_customer_error_email)
            state.phoneNumber.isBlank() -> publishError(R.string.merchant_add_customer_error_phone)
            else -> {
                viewModelScope.launch {
                    _uiState.value = state.copy(isSaving = true, errorRes = null)
                    runCatching {
                        createCustomerUseCase(
                            CustomerDraft(
                                firstName = state.firstName,
                                lastName = state.lastName,
                                phoneNumber = state.phoneNumber,
                                email = state.email,
                            ),
                            storeId,
                        )
                    }.onSuccess { customer ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            createdCustomerId = customer.id,
                            generatedEnrollmentCode = customer.nfcId,
                            activationLink = "https://verev.app/enroll/${customer.id}",
                            successName = listOf(customer.firstName, customer.lastName).joinToString(" ").trim(),
                        )
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorRes = R.string.merchant_add_customer_error_generic,
                        )
                    }
                }
            }
        }
    }

    fun resetForm() {
        _uiState.value = AddCustomerUiState(
            selectedStoreId = _uiState.value.selectedStoreId,
            selectedStoreName = _uiState.value.selectedStoreName,
        )
    }

    private fun publishError(@StringRes errorRes: Int) {
        _uiState.value = _uiState.value.copy(errorRes = errorRes)
    }

    private fun sanitize(value: String): String = value.replace("\n", "")
}

data class AddCustomerUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val createdCustomerId: String? = null,
    val generatedEnrollmentCode: String = "",
    val activationLink: String = "",
    val successName: String = "",
)
