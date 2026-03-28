package com.vector.verevcodex.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.auth.AuthUser
import com.vector.verevcodex.domain.repository.store.StoreRepository
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.notifications.ObserveUnreadMerchantNotificationCountUseCase
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
class ShellViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    observeSessionUseCase: ObserveSessionUseCase,
    observeUnreadMerchantNotificationCountUseCase: ObserveUnreadMerchantNotificationCountUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShellUiState())
    val uiState: StateFlow<ShellUiState> = _uiState.asStateFlow()

    init {
        combine(
            storeRepository.observeStores(),
            storeRepository.observeSelectedStore(),
            observeSessionUseCase(),
            observeUnreadMerchantNotificationCountUseCase(),
        ) { stores, selectedStore, session, unreadNotificationCount ->
            ShellUiState(
                stores = stores,
                selectedStore = selectedStore ?: stores.firstOrNull(),
                currentUser = session?.user,
                unreadNotificationCount = unreadNotificationCount,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    fun selectStore(storeId: String) {
        viewModelScope.launch { storeRepository.selectStore(storeId) }
    }
}

data class ShellUiState(
    val stores: List<Store> = emptyList(),
    val selectedStore: Store? = null,
    val currentUser: AuthUser? = null,
    val unreadNotificationCount: Int = 0,
)
