package com.vector.verevcodex.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.common.input.sanitizeDecimalInput
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.transactions.TransactionItem
import com.vector.verevcodex.domain.usecase.loyalty.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomersUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionsUseCase
import com.vector.verevcodex.domain.usecase.transactions.RecordTransactionUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.presentation.customers.displayName
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
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
class TransactionViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeSessionUseCase: ObserveSessionUseCase,
    observeCustomersUseCase: ObserveCustomersUseCase,
    observeCampaignsUseCase: ObserveCampaignsUseCase,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TransactionEntryUiState(
            lineItems = listOf(newLineItemDraft()),
        )
    )
    val uiState: StateFlow<TransactionEntryUiState> = _uiState.asStateFlow()

    init {
        observeSessionUseCase()
            .onEach { session ->
                reduce {
                    copy(
                        cashierId = session?.user?.relatedEntityId ?: session?.user?.id.orEmpty(),
                        cashierName = session?.user?.fullName.orEmpty(),
                    )
                }
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .onEach { store ->
                reduce {
                    copy(
                        selectedStoreId = store?.id,
                        selectedStoreName = store?.name.orEmpty(),
                    )
                }
            }
            .launchIn(viewModelScope)

        observeSelectedStoreUseCase()
            .flatMapLatest { store ->
                combine(
                    observeCustomersUseCase(store?.id),
                    observeCampaignsUseCase(store?.id),
                    observeTransactionsUseCase(store?.id),
                ) { customers, promotions, transactions ->
                    Triple(customers, promotions, transactions)
                }
            }
            .onEach { (customers, promotions, transactions) ->
                val current = _uiState.value
                val selectedCustomer = current.selectedCustomer?.let { existing ->
                    customers.firstOrNull { it.id == existing.id }
                }
                val availablePromotions = promotions.filter { it.isPaymentPromotion() }
                _uiState.value = deriveState(
                    current.copy(
                        isLoading = false,
                        customers = customers,
                        availablePromotions = availablePromotions,
                        selectedPromotionId = current.selectedPromotionId?.takeIf { selectedId ->
                            availablePromotions.any { it.id == selectedId }
                        },
                        selectedCustomer = selectedCustomer,
                        recentTransactions = transactions
                            .take(8)
                            .map { transaction ->
                                RecentCheckoutRecord(
                                    id = transaction.id,
                                    customerName = customers.firstOrNull { it.id == transaction.customerId }?.displayName().orEmpty(),
                                    itemCount = transaction.items.sumOf { it.quantity },
                                    amount = transaction.amount,
                                    pointsEarned = transaction.pointsEarned,
                                    pointsRedeemed = transaction.pointsRedeemed,
                                    timestamp = transaction.timestamp,
                                    summary = transaction.metadata,
                                )
                            },
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    fun updateCustomerQuery(value: String) = reduce {
        copy(
            customerQuery = value,
            successRes = null,
            errorRes = null,
        )
    }

    fun selectCustomer(customerId: String) = reduce {
        copy(
            selectedCustomer = customers.firstOrNull { it.id == customerId },
            customerQuery = "",
            successRes = null,
            errorRes = null,
            fieldErrors = fieldErrors - TRANSACTION_FIELD_CUSTOMER,
        )
    }

    fun clearSelectedCustomer() = reduce {
        copy(
            selectedCustomer = null,
            customerQuery = "",
            successRes = null,
            errorRes = null,
        )
    }

    fun selectPromotion(promotionId: String?) = reduce {
        copy(
            selectedPromotionId = promotionId,
            successRes = null,
            errorRes = null,
        )
    }

    fun addLineItem() = reduce {
        copy(
            lineItems = lineItems + newLineItemDraft(),
            successRes = null,
            errorRes = null,
        )
    }

    fun removeLineItem(lineItemId: String) = reduce {
        copy(
            lineItems = lineItems.filterNot { it.id == lineItemId }.ifEmpty { listOf(newLineItemDraft()) },
            successRes = null,
            errorRes = null,
            fieldErrors = fieldErrors - transactionItemNameFieldKey(lineItemId) - transactionItemQuantityFieldKey(lineItemId) - transactionItemPriceFieldKey(lineItemId),
        )
    }

    fun updateLineItemName(lineItemId: String, value: String) = updateLineItem(lineItemId) { draft ->
        draft.copy(name = value)
    }

    fun updateLineItemQuantity(lineItemId: String, value: String) = updateLineItem(lineItemId) { draft ->
        draft.copy(quantity = value.filter(Char::isDigit).take(3))
    }

    fun updateLineItemUnitPrice(lineItemId: String, value: String) = updateLineItem(lineItemId) { draft ->
        draft.copy(unitPrice = sanitizeDecimalInput(value))
    }

    fun togglePointRedemption(enabled: Boolean) = reduce {
        copy(
            applyPointRedemption = enabled,
            redeemPointsInput = if (enabled) redeemPointsInput else "",
            fieldErrors = fieldErrors - TRANSACTION_FIELD_REDEEM,
            successRes = null,
            errorRes = null,
        )
    }

    fun updateRedeemPoints(value: String) = reduce {
        copy(
            redeemPointsInput = value.filter(Char::isDigit).take(6),
            fieldErrors = fieldErrors - TRANSACTION_FIELD_REDEEM,
            successRes = null,
            errorRes = null,
        )
    }

    fun updateNote(value: String) = reduce {
        copy(
            note = value,
            successRes = null,
            errorRes = null,
        )
    }

    fun clearFeedback() = reduce {
        copy(errorRes = null, successRes = null)
    }

    fun submitCheckout() {
        val state = _uiState.value
        val selectedStoreId = state.selectedStoreId
        val selectedCustomer = state.selectedCustomer
        val cashierId = state.cashierId
        val validationErrors = validateDraft(state)
        if (selectedStoreId.isNullOrBlank()) {
            reduce { copy(errorRes = R.string.merchant_transaction_error_store_missing) }
            return
        }
        if (selectedCustomer == null) {
            reduce {
                copy(
                    errorRes = R.string.merchant_transaction_error_customer_missing,
                    fieldErrors = fieldErrors + (TRANSACTION_FIELD_CUSTOMER to R.string.merchant_transaction_error_customer_missing),
                )
            }
            return
        }
        if (cashierId.isBlank()) {
            reduce { copy(errorRes = R.string.merchant_transaction_error_cashier_missing) }
            return
        }
        if (validationErrors.isNotEmpty()) {
            reduce {
                copy(
                    errorRes = R.string.merchant_transaction_error_validation,
                    successRes = null,
                    fieldErrors = validationErrors,
                )
            }
            return
        }

        val parsedItems = parseValidLineItems(state.lineItems)
        val selectedPromotion = state.availablePromotions.firstOrNull { it.id == state.selectedPromotionId }
        val totals = computeCheckoutTotals(
            items = parsedItems,
            availablePoints = selectedCustomer.currentPoints,
            redeemPointsInput = state.redeemPointsInput,
            applyPointRedemption = state.applyPointRedemption,
            selectedPromotion = selectedPromotion,
        )

        viewModelScope.launch {
            reduce { copy(isSubmitting = true, errorRes = null, successRes = null) }
            runCatching {
                val transactionId = UUID.randomUUID().toString()
                recordTransactionUseCase(
                    Transaction(
                        id = transactionId,
                        customerId = selectedCustomer.id,
                        storeId = selectedStoreId,
                        staffId = cashierId,
                        amount = totals.finalAmount,
                        pointsEarned = totals.pointsEarned,
                        pointsRedeemed = totals.redeemedPoints,
                        timestamp = LocalDateTime.now(),
                        metadata = buildTransactionSummary(state.note, parsedItems, selectedPromotion),
                        items = parsedItems.map { item ->
                            TransactionItem(
                                id = UUID.randomUUID().toString(),
                                transactionId = transactionId,
                                name = item.name,
                                quantity = item.quantity,
                                unitPrice = item.unitPrice,
                            )
                        },
                    )
                )
            }.onSuccess {
                reduce {
                    copy(
                        selectedCustomer = null,
                        customerQuery = "",
                        lineItems = listOf(newLineItemDraft()),
                        selectedPromotionId = null,
                        applyPointRedemption = false,
                        redeemPointsInput = "",
                        note = "",
                        isSubmitting = false,
                        errorRes = null,
                        successRes = R.string.merchant_transaction_success,
                        fieldErrors = emptyMap(),
                    )
                }
            }.onFailure {
                reduce {
                    copy(
                        isSubmitting = false,
                        errorRes = R.string.merchant_transaction_error_submit,
                        successRes = null,
                    )
                }
            }
        }
    }

    private fun updateLineItem(
        lineItemId: String,
        transform: (CheckoutLineItemDraft) -> CheckoutLineItemDraft,
    ) = reduce {
        copy(
            lineItems = lineItems.map { draft -> if (draft.id == lineItemId) transform(draft) else draft },
            fieldErrors = fieldErrors - transactionItemNameFieldKey(lineItemId) - transactionItemQuantityFieldKey(lineItemId) - transactionItemPriceFieldKey(lineItemId),
            successRes = null,
            errorRes = null,
        )
    }

    private fun validateDraft(state: TransactionEntryUiState): Map<String, Int> {
        val errors = linkedMapOf<String, Int>()
        if (state.selectedCustomer == null) {
            errors[TRANSACTION_FIELD_CUSTOMER] = R.string.merchant_transaction_error_customer_missing
        }
        if (state.lineItems.isEmpty()) {
            errors[TRANSACTION_FIELD_LINE_ITEMS] = R.string.merchant_transaction_error_line_item_missing
            return errors
        }
        state.lineItems.forEach { lineItem ->
            if (lineItem.name.isBlank()) {
                errors[transactionItemNameFieldKey(lineItem.id)] = R.string.merchant_transaction_error_item_name
            }
            val quantity = lineItem.quantity.toIntOrNull()
            if (quantity == null || quantity <= 0) {
                errors[transactionItemQuantityFieldKey(lineItem.id)] = R.string.merchant_transaction_error_item_quantity
            }
            val unitPrice = lineItem.unitPrice.toDoubleOrNull()
            if (unitPrice == null || unitPrice <= 0.0) {
                errors[transactionItemPriceFieldKey(lineItem.id)] = R.string.merchant_transaction_error_item_price
            }
        }
        if (state.applyPointRedemption) {
            val requestedPoints = state.redeemPointsInput.toIntOrNull()
            if (requestedPoints == null || requestedPoints < 0) {
                errors[TRANSACTION_FIELD_REDEEM] = R.string.merchant_transaction_error_redeem_invalid
            } else {
                val allowed = computeCheckoutTotals(
                    items = parseValidLineItems(state.lineItems),
                    availablePoints = state.selectedCustomer?.currentPoints ?: 0,
                    redeemPointsInput = state.redeemPointsInput,
                    applyPointRedemption = true,
                    selectedPromotion = state.availablePromotions.firstOrNull { it.id == state.selectedPromotionId },
                ).redeemablePoints
                if (requestedPoints > allowed) {
                    errors[TRANSACTION_FIELD_REDEEM] = R.string.merchant_transaction_error_redeem_exceeds
                }
            }
        }
        return errors
    }

    private fun parseValidLineItems(lineItems: List<CheckoutLineItemDraft>): List<ParsedCheckoutLineItem> =
        lineItems.mapNotNull { lineItem ->
            val quantity = lineItem.quantity.toIntOrNull()
            val unitPrice = lineItem.unitPrice.toDoubleOrNull()
            val name = lineItem.name.trim()
            if (name.isBlank() || quantity == null || quantity <= 0 || unitPrice == null || unitPrice <= 0.0) {
                null
            } else {
                ParsedCheckoutLineItem(
                    name = name,
                    quantity = quantity,
                    unitPrice = unitPrice,
                )
            }
        }

    private fun deriveState(state: TransactionEntryUiState): TransactionEntryUiState {
        val filteredCustomers = filterCustomers(state.customers, state.customerQuery)
        val selectedPromotion = state.availablePromotions.firstOrNull { it.id == state.selectedPromotionId }
        val totals = computeCheckoutTotals(
            items = parseValidLineItems(state.lineItems),
            availablePoints = state.selectedCustomer?.currentPoints ?: 0,
            redeemPointsInput = state.redeemPointsInput,
            applyPointRedemption = state.applyPointRedemption,
            selectedPromotion = selectedPromotion,
        )
        return state.copy(
            filteredCustomers = filteredCustomers,
            selectedPromotionId = selectedPromotion?.id,
            totals = totals,
        )
    }

    private fun reduce(transform: TransactionEntryUiState.() -> TransactionEntryUiState) {
        _uiState.value = deriveState(_uiState.value.transform())
    }

    private fun filterCustomers(customers: List<Customer>, query: String): List<Customer> {
        val trimmedQuery = query.trim()
        val filtered = if (trimmedQuery.isBlank()) {
            customers
        } else {
            customers.filter { customer ->
                customer.displayName().contains(trimmedQuery, ignoreCase = true) ||
                    customer.phoneNumber.contains(trimmedQuery, ignoreCase = true) ||
                    customer.email.contains(trimmedQuery, ignoreCase = true) ||
                    customer.loyaltyId.contains(trimmedQuery, ignoreCase = true)
            }
        }
        return filtered.take(8)
    }

private companion object {
    fun newLineItemDraft() = CheckoutLineItemDraft(id = UUID.randomUUID().toString())
}
}

private fun Campaign.isPaymentPromotion(today: LocalDate = LocalDate.now()): Boolean =
    paymentFlowEnabled && active && !startDate.isAfter(today) && !endDate.isBefore(today)
