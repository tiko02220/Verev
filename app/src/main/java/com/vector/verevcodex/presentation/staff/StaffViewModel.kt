package com.vector.verevcodex.presentation.staff

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.StaffMember
import com.vector.verevcodex.domain.model.StaffRole
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.repository.StoreRepository
import com.vector.verevcodex.domain.usecase.AddStaffMembersUseCase
import com.vector.verevcodex.domain.usecase.ObserveStaffUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StaffViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val addStaffMembersUseCase: AddStaffMembersUseCase,
    observeStaffUseCase: ObserveStaffUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    init {
        storeRepository.observeSelectedStore()
            .flatMapLatest { store ->
                observeStaffUseCase(store?.id).onEach { members ->
                    _uiState.value = _uiState.value.copy(
                        selectedStoreId = store?.id,
                        selectedStoreName = store?.name.orEmpty(),
                        members = members,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun addMember(fullName: String, email: String, password: String, role: StaffRole) {
        val selectedStoreId = _uiState.value.selectedStoreId
        if (selectedStoreId.isNullOrBlank()) {
            publishError(R.string.merchant_staff_error_missing_store)
            return
        }
        if (fullName.isBlank()) {
            publishError(R.string.merchant_staff_error_name)
            return
        }
        if (email.isBlank()) {
            publishError(R.string.merchant_staff_error_email)
            return
        }
        if (password.length < 8) {
            publishError(R.string.merchant_staff_error_password)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null)
            addStaffMembersUseCase(
                selectedStoreId,
                listOf(
                    StaffOnboardingMember(
                        fullName = fullName.trim(),
                        email = email.trim().lowercase(),
                        password = password,
                        role = role,
                        permissionsSummary = role.permissionsSummary(),
                    )
                ),
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        messageRes = R.string.merchant_staff_message_added,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorRes = R.string.merchant_staff_error_generic,
                    )
                },
            )
        }
    }

    fun dismissFeedback() {
        _uiState.value = _uiState.value.copy(errorRes = null, messageRes = null)
    }

    private fun publishError(@StringRes messageRes: Int) {
        _uiState.value = _uiState.value.copy(errorRes = messageRes)
    }
}

data class StaffUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val members: List<StaffMember> = emptyList(),
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)

private fun StaffRole.permissionsSummary(): String = when (this) {
    StaffRole.OWNER -> "Full access across stores, analytics, campaigns, and loyalty settings"
    StaffRole.STORE_MANAGER -> "Manage customers, rewards, staff coordination, and store analytics"
    StaffRole.CASHIER -> "Process scans, transactions, and reward redemptions"
    StaffRole.STAFF -> "Limited operational access for assisted checkout and customer handling"
}
