package com.vector.verevcodex.presentation.staff

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.business.StaffMemberDraft
import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.common.summary
import com.vector.verevcodex.domain.repository.store.StoreRepository
import com.vector.verevcodex.domain.usecase.staff.AddStaffMembersUseCase
import com.vector.verevcodex.domain.usecase.staff.ObserveStaffUseCase
import com.vector.verevcodex.domain.usecase.staff.RemoveStaffMemberUseCase
import com.vector.verevcodex.domain.usecase.staff.UpdateStaffMemberUseCase
import com.vector.verevcodex.data.remote.auth.ApiException
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
    private val updateStaffMemberUseCase: UpdateStaffMemberUseCase,
    private val removeStaffMemberUseCase: RemoveStaffMemberUseCase,
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

    fun addMember(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        role: StaffRole,
        permissions: StaffPermissions,
    ) {
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
                        phoneNumber = phoneNumber.trim(),
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

    fun updateMember(
        staffId: String,
        fullName: String,
        email: String,
        phoneNumber: String,
        role: StaffRole,
        permissions: StaffPermissions,
    ) {
        if (fullName.isBlank()) {
            publishError(R.string.merchant_staff_error_name)
            return
        }
        if (email.isBlank()) {
            publishError(R.string.merchant_staff_error_email)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null)
            updateStaffMemberUseCase(
                staffId,
                StaffMemberDraft(
                    fullName = fullName.trim(),
                    email = email.trim().lowercase(),
                    phoneNumber = phoneNumber.trim(),
                    role = role,
                    permissionsSummary = permissions.summary(),
                    permissions = permissions,
                ),
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        messageRes = R.string.merchant_staff_message_updated,
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

    fun removeMember(staffId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null)
            removeStaffMemberUseCase(staffId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        messageRes = R.string.merchant_staff_message_deleted,
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

    fun dismissFeedback() {
        _uiState.value = _uiState.value.copy(errorRes = null, errorMessage = null, messageRes = null)
    }

    private fun publishError(@StringRes messageRes: Int) {
        _uiState.value = _uiState.value.copy(errorRes = messageRes, errorMessage = null)
    }
}

data class StaffUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val members: List<StaffMember> = emptyList(),
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorMessage: String? = null,
    @StringRes val messageRes: Int? = null,
)

private fun Throwable.toStaffErrorMessage(): String? {
    val message = when (this) {
        is ApiException -> message
        else -> message
    }?.trim().orEmpty()
    return message.takeIf { it.isNotBlank() && !it.startsWith("HTTP ", ignoreCase = true) }
}
