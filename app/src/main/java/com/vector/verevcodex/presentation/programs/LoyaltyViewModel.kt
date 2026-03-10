package com.vector.verevcodex.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.Campaign
import com.vector.verevcodex.domain.model.Reward
import com.vector.verevcodex.domain.model.RewardProgram
import com.vector.verevcodex.domain.usecase.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.ObserveRewardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class LoyaltyViewModel @Inject constructor(
    observeProgramsUseCase: ObserveProgramsUseCase,
    observeRewardsUseCase: ObserveRewardsUseCase,
    observeCampaignsUseCase: ObserveCampaignsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoyaltyUiState())
    val uiState: StateFlow<LoyaltyUiState> = _uiState.asStateFlow()

    init {
        combine(observeProgramsUseCase(), observeRewardsUseCase(), observeCampaignsUseCase()) { programs, rewards, campaigns ->
            LoyaltyUiState(programs, rewards, campaigns)
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }
}

data class LoyaltyUiState(
    val programs: List<RewardProgram> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val campaigns: List<Campaign> = emptyList(),
)
