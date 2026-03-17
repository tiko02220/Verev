package com.vector.verevcodex.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.billing.PaymentMethodDraft
import com.vector.verevcodex.domain.usecase.settings.AddPaymentMethodUseCase
import com.vector.verevcodex.domain.usecase.settings.ObserveInvoicesUseCase
import com.vector.verevcodex.domain.usecase.settings.ObservePaymentMethodsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.settings.ObserveSubscriptionPlanUseCase
import com.vector.verevcodex.domain.usecase.settings.RemovePaymentMethodUseCase
import com.vector.verevcodex.domain.usecase.settings.SetDefaultPaymentMethodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeSubscriptionPlanUseCase: ObserveSubscriptionPlanUseCase,
    observePaymentMethodsUseCase: ObservePaymentMethodsUseCase,
    observeInvoicesUseCase: ObserveInvoicesUseCase,
    private val addPaymentMethodUseCase: AddPaymentMethodUseCase,
    private val setDefaultPaymentMethodUseCase: SetDefaultPaymentMethodUseCase,
    private val removePaymentMethodUseCase: RemovePaymentMethodUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    private var currentOwnerId: String? = null

    init {
        observeSelectedStoreUseCase()
            .flatMapLatest { store ->
                currentOwnerId = store?.ownerId
                if (store == null) {
                    emptyFlow<PaymentMethodsUiState>()
                } else {
                    combine(
                        observeSubscriptionPlanUseCase(store.ownerId),
                        observePaymentMethodsUseCase(store.ownerId),
                        observeInvoicesUseCase(store.ownerId),
                    ) { plan, methods, invoices ->
                        val paymentMethods = methods
                            .map { it.toUi() }
                            .let { mapped ->
                                if (mapped.size == 1 && mapped.none { it.isDefault }) {
                                    mapped.map { it.copy(isDefault = true) }
                                } else {
                                    mapped
                                }
                            }
                        PaymentMethodsUiState(
                            planKey = plan.nameLabel(),
                            planPrice = plan.priceLabel(),
                            renewalLabel = plan.renewalLabel(),
                            methods = paymentMethods,
                            invoices = invoices.map { it.toUi() },
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

    fun addCard(
        brand: String,
        last4: String,
        expiryMonth: Int,
        expiryYear: Int,
        isDefault: Boolean,
    ) {
        val ownerId = currentOwnerId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, messageRes = null, errorRes = null)
            runCatching {
                addPaymentMethodUseCase(
                    ownerId,
                    PaymentMethodDraft(
                        brand = brand,
                        last4 = last4,
                        expiryMonth = expiryMonth,
                        expiryYear = expiryYear,
                        isDefault = isDefault,
                    )
                ).getOrThrow()
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    messageRes = R.string.merchant_payment_methods_message_card_added,
                    errorRes = null,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    messageRes = null,
                    errorRes = R.string.merchant_payment_methods_message_invalid_card,
                )
            }
        }
    }

    fun makeDefault(methodId: String) {
        val ownerId = currentOwnerId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, messageRes = null, errorRes = null)
            runCatching {
                setDefaultPaymentMethodUseCase(ownerId, methodId).getOrThrow()
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    messageRes = R.string.merchant_payment_methods_message_default_updated,
                    errorRes = null,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    messageRes = null,
                    errorRes = R.string.merchant_payment_methods_error_action,
                )
            }
        }
    }

    fun removeMethod(methodId: String) {
        val ownerId = currentOwnerId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, messageRes = null, errorRes = null)
            runCatching {
                removePaymentMethodUseCase(ownerId, methodId).getOrThrow()
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    messageRes = R.string.merchant_payment_methods_message_removed,
                    errorRes = null,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    messageRes = null,
                    errorRes = R.string.merchant_payment_methods_error_action,
                )
            }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(messageRes = null, errorRes = null)
    }
}
