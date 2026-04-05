package com.vector.verevcodex.presentation.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveStoresUseCase
import com.vector.verevcodex.domain.usecase.store.SelectStoreUseCase
import com.vector.verevcodex.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BranchProgramsConfigViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeStoresUseCase: ObserveStoresUseCase,
    observeProgramsUseCase: ObserveProgramsUseCase,
    private val selectStoreUseCase: SelectStoreUseCase,
) : ViewModel() {
    private val storeId: String? = savedStateHandle[Screen.BranchProgramsConfig.ARG_STORE_ID]
    private val _uiState = MutableStateFlow(BranchProgramsConfigUiState())
    val uiState: StateFlow<BranchProgramsConfigUiState> = _uiState.asStateFlow()

    init {
        val branchStoreId = storeId
        if (branchStoreId.isNullOrBlank()) {
            _uiState.value = BranchProgramsConfigUiState(errorRes = R.string.merchant_branch_programs_missing_store)
        } else {
            observeStoresUseCase()
                .flatMapLatest { stores ->
                    val store = stores.firstOrNull { it.id == branchStoreId }
                    observeProgramsUseCase(branchStoreId).onEach { programs ->
                        _uiState.value = BranchProgramsConfigUiState(
                            storeId = branchStoreId,
                            storeName = store?.name.orEmpty(),
                            cards = buildBranchProgramCards(programs),
                        )
                    }
                }
                .launchIn(viewModelScope)

            viewModelScope.launch {
                selectStoreUseCase(branchStoreId)
            }
        }
    }

    fun selectBranchForManagement() {
        val branchStoreId = storeId ?: return
        viewModelScope.launch { selectStoreUseCase(branchStoreId) }
    }
}

data class BranchProgramsConfigUiState(
    val storeId: String? = null,
    val storeName: String = "",
    val cards: List<BranchProgramCardUi> = emptyList(),
    val errorRes: Int? = null,
)

data class BranchProgramCardUi(
    val type: LoyaltyProgramType,
    val totalCount: Int,
    val activeCount: Int,
)

private fun buildBranchProgramCards(programs: List<RewardProgram>): List<BranchProgramCardUi> =
    listOf(
        LoyaltyProgramType.POINTS,
        LoyaltyProgramType.TIER,
        LoyaltyProgramType.COUPON,
        LoyaltyProgramType.DIGITAL_STAMP,
        LoyaltyProgramType.PURCHASE_FREQUENCY,
        LoyaltyProgramType.REFERRAL,
    ).map { type ->
        val typedPrograms = programs.filter { it.type == type }
        BranchProgramCardUi(
            type = type,
            totalCount = typedPrograms.size,
            activeCount = typedPrograms.count { it.active },
        )
    }
