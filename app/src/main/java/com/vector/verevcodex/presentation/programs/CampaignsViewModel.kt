package com.vector.verevcodex.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.usecase.loyalty.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.analytics.ObserveDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    observeDashboardUseCase: ObserveDashboardUseCase,
    observeCampaignsUseCase: ObserveCampaignsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CampaignsUiState())
    val uiState: StateFlow<CampaignsUiState> = _uiState.asStateFlow()

    init {
        combine(observeDashboardUseCase(), observeCampaignsUseCase()) { dashboard, campaigns ->
            CampaignsUiState(
                selectedStoreName = dashboard.selectedStore.name,
                customerCount = dashboard.analytics.totalCustomers,
                revenue = dashboard.recentTransactions.sumOf { it.amount },
                rewardRate = dashboard.analytics.rewardRedemptionRate,
                campaigns = campaigns,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }
}

data class CampaignsUiState(
    val selectedStoreName: String = "",
    val customerCount: Int = 0,
    val revenue: Double = 0.0,
    val rewardRate: Double = 0.0,
    val campaigns: List<Campaign> = emptyList(),
)
