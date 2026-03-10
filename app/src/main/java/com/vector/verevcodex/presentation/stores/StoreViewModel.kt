package com.vector.verevcodex.presentation.stores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.domain.model.Store
import com.vector.verevcodex.domain.usecase.ObserveStoresUseCase
import com.vector.verevcodex.domain.usecase.SelectStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class StoreViewModel @Inject constructor(
    observeStores: ObserveStoresUseCase,
    private val selectStoreUseCase: SelectStoreUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Store>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Store>>> = _uiState.asStateFlow()

    init {
        observeStores()
            .onEach { stores -> _uiState.value = if (stores.isEmpty()) UiState.Empty else UiState.Success(stores) }
            .catch { _uiState.value = UiState.Error(it.message ?: "Failed to load stores") }
            .launchIn(viewModelScope)
    }

    fun selectStore(storeId: String, onSelected: () -> Unit) {
        viewModelScope.launch {
            selectStoreUseCase(storeId)
            onSelected()
        }
    }
}
