package com.vector.verevcodex.presentation.promotions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomersUseCase
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.promotions.PromotionBoostLevel
import com.vector.verevcodex.domain.model.promotions.PromotionVisibility
import com.vector.verevcodex.domain.usecase.promotions.CreatePromotionUseCase
import com.vector.verevcodex.domain.usecase.promotions.DeletePromotionUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.promotions.SetPromotionEnabledUseCase
import com.vector.verevcodex.domain.usecase.promotions.UpdatePromotionUseCase
import com.vector.verevcodex.domain.usecase.settings.ObservePaymentMethodsUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PromotionsViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeCampaignsUseCase: ObserveCampaignsUseCase,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    observeCustomersUseCase: ObserveCustomersUseCase,
    observePaymentMethodsUseCase: ObservePaymentMethodsUseCase,
    private val createPromotionUseCase: CreatePromotionUseCase,
    private val updatePromotionUseCase: UpdatePromotionUseCase,
    private val setPromotionEnabledUseCase: SetPromotionEnabledUseCase,
    private val deletePromotionUseCase: DeletePromotionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PromotionsUiState())
    val uiState: StateFlow<PromotionsUiState> = _uiState

    init {
        observeSelectedStoreUseCase()
            .flatMapLatest { store ->
                combine(
                    observeCampaignsUseCase(store?.id),
                    observeTransactionsUseCase(store?.id),
                    observeCustomersUseCase(store?.id),
                    observePaymentMethodsUseCase(store?.ownerId.orEmpty()),
                ) { promotions, transactions, customers, paymentMethods ->
                    PromotionsStoreSnapshot(
                        storeId = store?.id,
                        ownerId = store?.ownerId,
                        storeName = store?.name.orEmpty(),
                        promotions = promotions,
                        transactions = transactions,
                        customers = customers,
                        paymentMethods = paymentMethods,
                    )
                }
            }
            .onEach { snapshot ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    selectedStoreId = snapshot.storeId,
                    selectedOwnerId = snapshot.ownerId,
                    selectedStoreName = snapshot.storeName,
                    promotions = snapshot.promotions,
                    transactions = snapshot.transactions,
                    customers = snapshot.customers,
                    paymentMethods = snapshot.paymentMethods,
                    busyPromotionId = current.busyPromotionId?.takeIf { busyId -> snapshot.promotions.any { it.id == busyId } },
                )
            }
            .launchIn(viewModelScope)
    }

    fun selectFilter(filter: PromotionFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun openPromotionDetail(promotionId: String) {
        _uiState.value = _uiState.value.copy(selectedPromotionId = promotionId)
    }

    fun closePromotionDetail() {
        _uiState.value = _uiState.value.copy(selectedPromotionId = null)
    }

    fun openNetworkPromotionPayment(promotionId: String) {
        val promotion = _uiState.value.promotions.firstOrNull { it.id == promotionId } ?: return
        if (!promotion.paymentFlowEnabled) return
        _uiState.value = _uiState.value.copy(paymentPromotionId = promotionId, errorRes = null, messageRes = null)
    }

    fun dismissNetworkPromotionPayment() {
        _uiState.value = _uiState.value.copy(paymentPromotionId = null)
    }

    fun openCreatePromotion(type: PromotionType = PromotionType.PERCENT_DISCOUNT) {
        _uiState.value = _uiState.value.copy(
            editorState = defaultPromotionEditorState().copy(promotionType = type),
            editorFieldErrors = emptyMap(),
            deleteCandidate = null,
            errorRes = null,
            messageRes = null,
        )
    }

    fun openEditPromotion(promotionId: String) {
        val promotion = _uiState.value.promotions.firstOrNull { it.id == promotionId } ?: return
        _uiState.value = _uiState.value.copy(
            editorState = promotion.toPromotionEditorState(),
            editorFieldErrors = emptyMap(),
            deleteCandidate = null,
            errorRes = null,
            messageRes = null,
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(editorState = null, editorFieldErrors = emptyMap())
    }

    fun updateName(value: String) = updateEditor { copy(name = value) }
    fun updateDescription(value: String) = updateEditor { copy(description = value) }
    fun updateImageUri(value: String) = updateEditor { copy(imageUri = value) }
    fun updateStartDate(value: String) = updateEditor { copy(startDate = value) }
    fun updateEndDate(value: String) = updateEditor { copy(endDate = value) }
    fun updatePromotionType(value: PromotionType) = updateEditor { copy(promotionType = value) }
    fun updatePromotionValue(value: String) = updateEditor { copy(promotionValue = value.filter { it.isDigit() || it == '.' }) }
    fun updateMinimumPurchaseAmount(value: String) = updateEditor { copy(minimumPurchaseAmount = value.filter { it.isDigit() || it == '.' }) }
    fun updateUsageLimit(value: String) = updateEditor { copy(usageLimit = value.filter(Char::isDigit)) }
    fun updatePromoCode(value: String) = updateEditor { copy(promoCode = value.uppercase()) }
    fun updateVisibility(value: PromotionVisibility) = updateEditor {
        copy(
            visibility = value,
            paymentFlowEnabled = value == PromotionVisibility.NETWORK_WIDE,
        )
    }
    fun updateBoostLevel(value: PromotionBoostLevel) = updateEditor { copy(boostLevel = value) }
    fun updatePaymentFlowEnabled(value: Boolean) = updateEditor { copy(paymentFlowEnabled = value) }
    fun updateActive(value: Boolean) = updateEditor { copy(active = value) }
    fun updateTargetSegment(index: Int) = updateEditor {
        val segment = com.vector.verevcodex.domain.model.common.CampaignSegment.entries[index]
        copy(targetSegment = segment, targetDescription = segment.name)
    }
    fun updateTargetDescription(value: String) = updateEditor { copy(targetDescription = value) }

    fun savePromotion() {
        val state = _uiState.value
        val editor = state.editorState ?: return
        val storeId = state.selectedStoreId ?: run {
            _uiState.value = state.copy(errorRes = R.string.merchant_promotion_error_store_required)
            return
        }
        val fieldErrors = validatePromotionEditor(editor)
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(editorFieldErrors = fieldErrors, errorRes = null)
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, editorFieldErrors = emptyMap(), errorRes = null, messageRes = null)
            runCatching {
                val draft = editor.toDraft(storeId)
                if (editor.promotionId == null) createPromotionUseCase(draft) else updatePromotionUseCase(editor.promotionId, draft)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    editorState = null,
                    editorFieldErrors = emptyMap(),
                    messageRes = if (editor.promotionId == null) R.string.merchant_promotion_created_message else R.string.merchant_promotion_updated_message,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSubmitting = false, errorRes = R.string.merchant_promotion_error_save_failed)
            }
        }
    }

    fun togglePromotionEnabled(promotionId: String, enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busyPromotionId = promotionId, errorRes = null, messageRes = null)
            runCatching { setPromotionEnabledUseCase(promotionId, enabled) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        busyPromotionId = null,
                        messageRes = if (enabled) R.string.merchant_promotion_enabled_message else R.string.merchant_promotion_disabled_message,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(busyPromotionId = null, errorRes = R.string.merchant_promotion_error_save_failed)
                }
        }
    }

    fun requestDelete(promotionId: String) {
        _uiState.value = _uiState.value.copy(deleteCandidate = _uiState.value.promotions.firstOrNull { it.id == promotionId })
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(deleteCandidate = null)
    }

    fun publishNetworkPaymentConfirmed() {
        _uiState.value = _uiState.value.copy(
            paymentPromotionId = null,
            messageRes = R.string.merchant_network_promotion_payment_success,
            errorRes = null,
        )
    }

    fun confirmDeletePromotion() {
        val promotion = _uiState.value.deleteCandidate ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorRes = null, messageRes = null)
            runCatching { deletePromotionUseCase(promotion.id) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        deleteCandidate = null,
                        selectedPromotionId = null,
                        messageRes = R.string.merchant_promotion_deleted_message,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, deleteCandidate = null, errorRes = R.string.merchant_promotion_error_delete_failed)
                }
        }
    }

    fun dismissFeedback() {
        _uiState.value = _uiState.value.copy(messageRes = null, errorRes = null)
    }

    private fun updateEditor(transform: PromotionEditorState.() -> PromotionEditorState) {
        val current = _uiState.value.editorState ?: return
        _uiState.value = _uiState.value.copy(editorState = current.transform(), editorFieldErrors = emptyMap(), errorRes = null)
    }
}

private data class PromotionsStoreSnapshot(
    val storeId: String?,
    val ownerId: String?,
    val storeName: String,
    val promotions: List<com.vector.verevcodex.domain.model.promotions.Campaign>,
    val transactions: List<com.vector.verevcodex.domain.model.transactions.Transaction>,
    val customers: List<com.vector.verevcodex.domain.model.customer.Customer>,
    val paymentMethods: List<SavedPaymentMethod>,
)
