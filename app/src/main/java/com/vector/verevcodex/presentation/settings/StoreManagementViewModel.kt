package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.business.StoreDraft
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.usecase.store.CreateStoreUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveStoresUseCase
import com.vector.verevcodex.domain.usecase.store.SelectStoreUseCase
import com.vector.verevcodex.domain.usecase.store.SetStoreActiveUseCase
import com.vector.verevcodex.domain.usecase.store.UpdateStoreUseCase
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
class StoreManagementViewModel @Inject constructor(
    observeStoresUseCase: ObserveStoresUseCase,
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val selectStoreUseCase: SelectStoreUseCase,
    private val createStoreUseCase: CreateStoreUseCase,
    private val updateStoreUseCase: UpdateStoreUseCase,
    private val setStoreActiveUseCase: SetStoreActiveUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreManagementUiState())
    val uiState: StateFlow<StoreManagementUiState> = _uiState.asStateFlow()

    init {
        combine(
            observeStoresUseCase(),
            observeSelectedStoreUseCase(),
        ) { stores, selectedStore ->
            _uiState.value.copy(
                stores = stores,
                selectedStoreId = selectedStore?.id,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    fun selectStore(storeId: String) {
        viewModelScope.launch { selectStoreUseCase(storeId) }
    }

    fun startCreate() {
        _uiState.value = _uiState.value.copy(
            editor = StoreEditorState(),
            isEditorVisible = true,
            editorFieldErrors = emptyMap(),
            errorRes = null,
            messageRes = null,
        )
    }

    fun startEdit(store: Store) {
        _uiState.value = _uiState.value.copy(
            editor = StoreEditorState(
                editingStoreId = store.id,
                name = store.name,
                address = store.address,
                contactInfo = store.contactInfo,
                category = store.category,
                workingHours = store.workingHours,
                primaryColor = store.primaryColor,
                secondaryColor = store.secondaryColor,
            ),
            isEditorVisible = true,
            editorFieldErrors = emptyMap(),
            errorRes = null,
            messageRes = null,
        )
    }

    fun prepareEdit(storeId: String) {
        if (_uiState.value.editor.editingStoreId == storeId) return
        val store = _uiState.value.stores.firstOrNull { it.id == storeId } ?: return
        startEdit(store)
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(
            isEditorVisible = false,
            editor = StoreEditorState(),
            editorFieldErrors = emptyMap(),
        )
    }

    fun updateEditor(transform: (StoreEditorState) -> StoreEditorState) {
        _uiState.value = _uiState.value.copy(
            editor = transform(_uiState.value.editor),
            editorFieldErrors = emptyMap(),
            errorRes = null,
        )
    }

    fun submitEditor() {
        val editor = _uiState.value.editor
        val fieldErrors = validateEditor(editor)
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                editorFieldErrors = fieldErrors,
                errorRes = null,
                messageRes = null,
            )
            return
        }
        viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null, messageRes = null)
                val result = if (editor.editingStoreId == null) {
                    createStoreUseCase(
                        StoreDraft(
                            name = editor.name,
                            address = editor.address,
                            contactInfo = editor.contactInfo,
                            category = editor.category,
                            workingHours = editor.workingHours,
                            primaryColor = editor.primaryColor,
                            secondaryColor = editor.secondaryColor,
                        )
                    )
                } else {
                    val existing = _uiState.value.stores.firstOrNull { it.id == editor.editingStoreId }
                    if (existing == null) {
                        Result.failure(IllegalStateException("Store not found"))
                    } else {
                        updateStoreUseCase(
                            existing.copy(
                                name = editor.name.trim(),
                                address = editor.address.trim(),
                                contactInfo = editor.contactInfo.trim(),
                                category = editor.category.trim(),
                                workingHours = editor.workingHours.trim(),
                                primaryColor = editor.primaryColor,
                                secondaryColor = editor.secondaryColor,
                            )
                        )
                    }
                }
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isEditorVisible = false,
                            editor = StoreEditorState(),
                            editorFieldErrors = emptyMap(),
                            messageRes = if (editor.editingStoreId == null) {
                                R.string.merchant_store_editor_created
                            } else {
                                R.string.merchant_store_editor_updated
                            },
                        )
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorRes = R.string.merchant_store_editor_error_generic,
                        )
                    },
                )
        }
    }

    fun toggleStoreActive(store: Store) {
        viewModelScope.launch {
            setStoreActiveUseCase(store.id, !store.active).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        messageRes = if (store.active) {
                            R.string.merchant_store_disabled
                        } else {
                            R.string.merchant_store_enabled
                        },
                    )
                },
                onFailure = {
                    publishError(R.string.merchant_store_editor_error_generic)
                },
            )
        }
    }

    fun dismissFeedback() {
        _uiState.value = _uiState.value.copy(errorRes = null, messageRes = null)
    }

    private fun validateEditor(editor: StoreEditorState): Map<String, Int> {
        val errors = linkedMapOf<String, Int>()
        if (editor.name.isBlank()) errors[STORE_FIELD_NAME] = R.string.merchant_store_editor_error_name
        if (editor.address.isBlank()) errors[STORE_FIELD_ADDRESS] = R.string.merchant_store_editor_error_address
        if (editor.contactInfo.isBlank()) errors[STORE_FIELD_CONTACT] = R.string.merchant_store_editor_error_contact
        if (editor.category.isBlank()) errors[STORE_FIELD_CATEGORY] = R.string.merchant_store_editor_error_category
        if (editor.workingHours.isBlank()) errors[STORE_FIELD_HOURS] = R.string.merchant_store_editor_error_hours
        return errors
    }

    private fun publishError(@StringRes errorRes: Int) {
        _uiState.value = _uiState.value.copy(errorRes = errorRes)
    }
}
