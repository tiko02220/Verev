package com.vector.verevcodex.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.usecase.notifications.MarkAllMerchantNotificationsReadUseCase
import com.vector.verevcodex.domain.usecase.notifications.MarkMerchantNotificationReadUseCase
import com.vector.verevcodex.domain.usecase.notifications.ObserveMerchantNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationInboxViewModel @Inject constructor(
    observeMerchantNotificationsUseCase: ObserveMerchantNotificationsUseCase,
    private val markMerchantNotificationReadUseCase: MarkMerchantNotificationReadUseCase,
    private val markAllMerchantNotificationsReadUseCase: MarkAllMerchantNotificationsReadUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationInboxUiState())
    val uiState: StateFlow<NotificationInboxUiState> = _uiState.asStateFlow()

    init {
        observeMerchantNotificationsUseCase()
            .onEach { notifications ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    notifications = notifications,
                    unreadCount = notifications.count { !it.isRead },
                )
            }
            .launchIn(viewModelScope)
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            markMerchantNotificationReadUseCase(notificationId)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMarkingAllRead = true)
            markAllMerchantNotificationsReadUseCase()
            _uiState.value = _uiState.value.copy(isMarkingAllRead = false)
        }
    }
}
