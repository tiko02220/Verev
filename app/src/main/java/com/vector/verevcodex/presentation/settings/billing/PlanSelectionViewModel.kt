package com.vector.verevcodex.presentation.settings.billing

import com.vector.verevcodex.presentation.settings.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.usecase.settings.ObserveAvailableSubscriptionPlansUseCase
import com.vector.verevcodex.domain.usecase.settings.ObserveSubscriptionPlanUseCase
import com.vector.verevcodex.domain.usecase.settings.UpdateSubscriptionPlanUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanSelectionViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeSubscriptionPlanUseCase: ObserveSubscriptionPlanUseCase,
    observeAvailableSubscriptionPlansUseCase: ObserveAvailableSubscriptionPlansUseCase,
    private val updateSubscriptionPlanUseCase: UpdateSubscriptionPlanUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlanSelectionUiState())
    val uiState: StateFlow<PlanSelectionUiState> = _uiState.asStateFlow()

    private var currentOwnerId: String? = null

    init {
        observeSelectedStoreUseCase()
            .flatMapLatest { store ->
                currentOwnerId = store?.ownerId
                if (store == null) {
                    flowOf(PlanSelectionUiState(errorRes = R.string.merchant_payment_methods_error_store_missing))
                } else {
                    combine(
                        observeSubscriptionPlanUseCase(store.ownerId),
                        observeAvailableSubscriptionPlansUseCase(),
                    ) { currentPlan, availablePlans ->
                        PlanSelectionUiState(
                            currentPlanKey = currentPlan.nameLabel(),
                            currentPlanPrice = currentPlan.priceLabel(),
                            options = availablePlans.map { option ->
                                val spec = BillingPlanUiCatalog.specFor(option.id)
                                SubscriptionPlanOptionUi(
                                    id = option.id,
                                    nameRes = spec.nameRes,
                                    priceLabel = formatPlanPrice(option.monthlyPrice, option.currencyCode),
                                    summaryRes = spec.summaryRes,
                                    featureResIds = spec.featureResIds,
                                    isSelected = currentPlan?.name == option.id,
                                )
                            },
                            isSaving = _uiState.value.isSaving,
                            messageRes = _uiState.value.messageRes,
                            errorRes = _uiState.value.errorRes,
                        )
                    }
                }
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun selectPlan(planId: String) {
        val ownerId = currentOwnerId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null, messageRes = null)
            runCatching { updateSubscriptionPlanUseCase(ownerId, planId).getOrThrow() }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        messageRes = R.string.merchant_plan_selection_success,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorRes = R.string.merchant_plan_selection_error,
                    )
                }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(errorRes = null, messageRes = null)
    }
}

private fun formatPlanPrice(amount: Double, currencyCode: String): String =
    String.format("%.0f %s/mo", amount, currencyCode)
