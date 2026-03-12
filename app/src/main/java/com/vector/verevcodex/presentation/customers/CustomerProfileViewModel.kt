package com.vector.verevcodex.presentation.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.usecase.customer.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerCredentialsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerPointsLedgerUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionsUseCase
import com.vector.verevcodex.domain.usecase.customer.UpdateCustomerContactUseCase
import com.vector.verevcodex.domain.usecase.customer.UpdateCustomerNotesAndTagsUseCase
import com.vector.verevcodex.presentation.navigation.Screen
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
class CustomerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCustomerUseCase: ObserveCustomerUseCase,
    observeCustomerCredentialsUseCase: ObserveCustomerCredentialsUseCase,
    observeCustomerRelationsUseCase: ObserveCustomerRelationsUseCase,
    observeCustomerPointsLedgerUseCase: ObserveCustomerPointsLedgerUseCase,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val updateCustomerContactUseCase: UpdateCustomerContactUseCase,
    private val updateCustomerNotesAndTagsUseCase: UpdateCustomerNotesAndTagsUseCase,
    private val adjustCustomerPointsUseCase: AdjustCustomerPointsUseCase,
) : ViewModel() {
    private val customerId: String? = savedStateHandle[Screen.CustomerProfile.ARG_CUSTOMER_ID]

    private val _uiState = MutableStateFlow(CustomerProfileUiState())
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()

    init {
        val currentCustomerId = customerId
        if (currentCustomerId.isNullOrBlank()) {
            _uiState.value = CustomerProfileUiState(isMissingCustomer = true)
        } else {
            combine(
                combine(
                    observeCustomerUseCase(currentCustomerId),
                    observeCustomerCredentialsUseCase(currentCustomerId),
                    observeCustomerRelationsUseCase(currentCustomerId),
                ) { customer, credentials, relations ->
                    Triple(customer, credentials, relations)
                },
                combine(
                    observeCustomerPointsLedgerUseCase(currentCustomerId),
                    observeTransactionsUseCase(),
                    observeSelectedStoreUseCase(),
                ) { ledgerEntries, transactions, selectedStore ->
                    Triple(ledgerEntries, transactions, selectedStore)
                },
            ) { customerBundle, auxBundle ->
                val customer = customerBundle.first
                val credentials = customerBundle.second
                val relations = customerBundle.third
                val ledgerEntries = auxBundle.first
                val transactions = auxBundle.second
                val selectedStore = auxBundle.third
                val relevantTransactions = transactions
                    .filter { it.customerId == currentCustomerId }
                    .sortedByDescending { it.timestamp }
                val selectedRelation = relations.firstOrNull { it.storeId == selectedStore?.id }
                    ?: relations.firstOrNull { it.storeId == customer?.favoriteStoreId }
                    ?: relations.firstOrNull()
                CustomerProfileUiState(
                    customer = customer,
                    relation = selectedRelation,
                    credentials = credentials,
                    ledgerEntries = ledgerEntries,
                    transactions = relevantTransactions,
                    activities = CustomerActivityTimelineBuilder.build(
                        relation = selectedRelation,
                        transactions = relevantTransactions,
                        ledgerEntries = ledgerEntries,
                    ),
                    isSaving = _uiState.value.isSaving,
                    feedbackMessageRes = _uiState.value.feedbackMessageRes,
                )
            }.onEach { _uiState.value = it }.launchIn(viewModelScope)
        }
    }

    fun updateContact(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
    ) {
        val customer = _uiState.value.customer ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                updateCustomerContactUseCase(
                    customerId = customer.id,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    favoriteStoreId = customer.favoriteStoreId,
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_contact_updated,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_contact_update_failed,
                )
            }
        }
    }

    fun updateNotesAndTags(notes: String, tags: List<String>) {
        val customer = _uiState.value.customer ?: return
        val relation = _uiState.value.relation ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                updateCustomerNotesAndTagsUseCase(
                    customerId = customer.id,
                    storeId = relation.storeId,
                    notes = notes,
                    tags = tags,
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_crm_updated,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_crm_update_failed,
                )
            }
        }
    }

    fun adjustPoints(delta: Int, reason: String) {
        val customer = _uiState.value.customer ?: return
        if (delta == 0) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, feedbackMessageRes = null)
            runCatching {
                adjustCustomerPointsUseCase(
                    customer.id,
                    delta,
                    reason.ifBlank { CustomerCrmConstants.manualAdjustmentReason },
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_points_updated,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    feedbackMessageRes = R.string.merchant_customer_points_update_failed,
                )
            }
        }
    }

    fun consumeFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessageRes = null)
    }
}
