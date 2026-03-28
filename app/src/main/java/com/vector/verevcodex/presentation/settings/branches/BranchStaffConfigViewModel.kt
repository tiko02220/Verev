package com.vector.verevcodex.presentation.settings.branches

import com.vector.verevcodex.presentation.settings.*

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.common.phone.isValidPhoneNumber
import com.vector.verevcodex.common.phone.normalizePhoneNumber
import com.vector.verevcodex.common.validation.isValidStaffPassword
import com.vector.verevcodex.data.remote.core.RemoteException
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.common.summary
import com.vector.verevcodex.domain.usecase.staff.AddStaffMembersUseCase
import com.vector.verevcodex.domain.usecase.staff.ObserveStaffUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveStoresUseCase
import com.vector.verevcodex.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class BranchStaffConfigViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeStoresUseCase: ObserveStoresUseCase,
    observeStaffUseCase: ObserveStaffUseCase,
    private val addStaffMembersUseCase: AddStaffMembersUseCase,
) : ViewModel() {
    private val storeId: String? = savedStateHandle[Screen.BranchStaffConfig.ARG_STORE_ID]
    private val _uiState = MutableStateFlow(BranchStaffConfigUiState())
    val uiState: StateFlow<BranchStaffConfigUiState> = _uiState.asStateFlow()

    init {
        val branchStoreId = storeId
        if (branchStoreId.isNullOrBlank()) {
            _uiState.value = BranchStaffConfigUiState(errorRes = R.string.merchant_branch_staff_missing_store)
        } else {
            combine(
                observeStoresUseCase(),
                observeStaffUseCase(branchStoreId),
            ) { stores, members ->
                val store = stores.firstOrNull { it.id == branchStoreId }
                BranchStaffConfigUiState(
                    store = store,
                    storeName = store?.name.orEmpty(),
                    members = members,
                    errorRes = _uiState.value.errorRes,
                    errorMessage = _uiState.value.errorMessage,
                    messageRes = _uiState.value.messageRes,
                    isSaving = _uiState.value.isSaving,
                )
            }.onEach { _uiState.value = it }.launchIn(viewModelScope)
        }
    }

    fun addMember(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        role: StaffRole,
        permissions: StaffPermissions,
    ) {
        val branchStoreId = storeId
        if (branchStoreId.isNullOrBlank()) {
            publishError(R.string.merchant_branch_staff_missing_store)
            return
        }
        when {
            fullName.isBlank() -> publishError(R.string.merchant_staff_error_name)
            email.isBlank() -> publishError(R.string.merchant_staff_error_email)
            !isValidPhoneNumber(phoneNumber) -> publishError(R.string.merchant_staff_error_phone)
            !isValidStaffPassword(password) -> publishError(R.string.merchant_staff_error_password)
            else -> viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null, messageRes = null)
                addStaffMembersUseCase(
                    branchStoreId,
                    listOf(
                        StaffOnboardingMember(
                            fullName = fullName.trim(),
                            email = email.trim().lowercase(),
                            phoneNumber = normalizePhoneNumber(phoneNumber),
                            password = password,
                            role = role,
                            permissionsSummary = permissions.summary(),
                            permissions = permissions,
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
                            errorRes = if (it.toStaffErrorMessage() == null) R.string.merchant_staff_error_generic else null,
                            errorMessage = it.toStaffErrorMessage(),
                        )
                    },
                )
            }
        }
    }

    fun dismissFeedback() {
        _uiState.value = _uiState.value.copy(errorRes = null, errorMessage = null, messageRes = null)
    }

    private fun publishError(@StringRes errorRes: Int) {
        _uiState.value = _uiState.value.copy(errorRes = errorRes, errorMessage = null)
    }
}

private fun Throwable.toStaffErrorMessage(): String? {
    val message = when (this) {
        is RemoteException -> message
        else -> message
    }?.trim().orEmpty()
    return message.takeIf { it.isNotBlank() && !it.startsWith("HTTP ", ignoreCase = true) }
}
