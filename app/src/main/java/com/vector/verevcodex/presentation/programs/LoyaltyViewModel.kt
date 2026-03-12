package com.vector.verevcodex.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.usecase.loyalty.CreateRewardUseCase
import com.vector.verevcodex.domain.usecase.loyalty.DeleteRewardUseCase
import com.vector.verevcodex.domain.usecase.loyalty.CreateProgramUseCase
import com.vector.verevcodex.domain.usecase.loyalty.DeleteProgramUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveRewardsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.SetRewardEnabledUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.loyalty.SetProgramEnabledUseCase
import com.vector.verevcodex.domain.usecase.loyalty.UpdateRewardUseCase
import com.vector.verevcodex.domain.usecase.loyalty.UpdateProgramUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class LoyaltyViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeProgramsUseCase: ObserveProgramsUseCase,
    observeRewardsUseCase: ObserveRewardsUseCase,
    observeCampaignsUseCase: ObserveCampaignsUseCase,
    private val createRewardUseCase: CreateRewardUseCase,
    private val updateRewardUseCase: UpdateRewardUseCase,
    private val setRewardEnabledUseCase: SetRewardEnabledUseCase,
    private val deleteRewardUseCase: DeleteRewardUseCase,
    private val createProgramUseCase: CreateProgramUseCase,
    private val updateProgramUseCase: UpdateProgramUseCase,
    private val setProgramEnabledUseCase: SetProgramEnabledUseCase,
    private val deleteProgramUseCase: DeleteProgramUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoyaltyUiState())
    val uiState: StateFlow<LoyaltyUiState> = _uiState

    init {
        val selectedStoreFlow = observeSelectedStoreUseCase()
        val programsFlow = selectedStoreFlow.flatMapLatest { store -> observeProgramsUseCase(store?.id) }
        val rewardsFlow = selectedStoreFlow.flatMapLatest { store -> observeRewardsUseCase(store?.id) }
        val campaignsFlow = selectedStoreFlow.flatMapLatest { store -> observeCampaignsUseCase(store?.id) }

        combine(selectedStoreFlow, programsFlow, rewardsFlow, campaignsFlow) { store, programs, rewards, campaigns ->
            store to Triple(programs, rewards, campaigns)
        }.onEach { (store, data) ->
            val (programs, rewards, campaigns) = data
            _uiState.value = _uiState.value.copy(
                selectedStoreId = store?.id,
                selectedStoreName = store?.name.orEmpty(),
                programs = programs,
                rewards = rewards,
                campaigns = campaigns,
                busyProgramId = _uiState.value.busyProgramId?.takeIf { busyId -> programs.any { it.id == busyId } },
                busyRewardId = _uiState.value.busyRewardId?.takeIf { busyId -> rewards.any { it.id == busyId } },
            )
        }.launchIn(viewModelScope)
    }

    fun openCreateReward() {
        _uiState.value = _uiState.value.copy(
            rewardEditorState = RewardEditorState(),
            rewardEditorFieldErrors = emptyMap(),
            rewardDeleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun openEditReward(rewardId: String) {
        val reward = _uiState.value.rewards.firstOrNull { it.id == rewardId } ?: return
        _uiState.value = _uiState.value.copy(
            rewardEditorState = reward.toEditorState(),
            rewardEditorFieldErrors = emptyMap(),
            rewardDeleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun dismissRewardEditor() {
        _uiState.value = _uiState.value.copy(
            rewardEditorState = null,
            rewardEditorFieldErrors = emptyMap(),
        )
    }

    fun requestDeleteReward(rewardId: String) {
        _uiState.value = _uiState.value.copy(
            rewardDeleteCandidate = _uiState.value.rewards.firstOrNull { it.id == rewardId },
            rewardEditorState = null,
            rewardEditorFieldErrors = emptyMap(),
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun dismissRewardDeleteDialog() {
        _uiState.value = _uiState.value.copy(rewardDeleteCandidate = null)
    }

    fun updateRewardName(value: String) = updateRewardEditor { copy(name = value) }
    fun updateRewardDescription(value: String) = updateRewardEditor { copy(description = value) }
    fun updateRewardPointsRequired(value: String) = updateRewardEditor { copy(pointsRequired = value) }
    fun updateRewardType(value: RewardType) = updateRewardEditor { copy(rewardType = value) }
    fun updateRewardExpirationDate(value: String) = updateRewardEditor { copy(expirationDate = value) }
    fun updateRewardUsageLimit(value: String) = updateRewardEditor { copy(usageLimit = value) }
    fun updateRewardActiveStatus(value: Boolean) = updateRewardEditor { copy(activeStatus = value) }

    fun openCreateProgram(type: LoyaltyProgramType = LoyaltyProgramType.POINTS) {
        _uiState.value = _uiState.value.copy(
            activeSubEditor = null,
            editorState = defaultProgramEditorState(type),
            editorFieldErrors = emptyMap(),
            deleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun openEditProgram(programId: String) {
        val program = _uiState.value.programs.firstOrNull { it.id == programId } ?: return
        _uiState.value = _uiState.value.copy(
            activeSubEditor = null,
            editorState = program.toEditorState(),
            editorFieldErrors = emptyMap(),
            deleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun openProgramSubEditor(programId: String, subEditor: ProgramSubEditor) {
        val program = _uiState.value.programs.firstOrNull { it.id == programId } ?: return
        _uiState.value = _uiState.value.copy(
            activeSubEditor = subEditor,
            editorState = program.toEditorState(),
            editorFieldErrors = emptyMap(),
            deleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun closeProgramSubEditor() {
        _uiState.value = _uiState.value.copy(
            activeSubEditor = null,
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(
            activeSubEditor = null,
            editorState = null,
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }

    fun requestDelete(programId: String) {
        _uiState.value = _uiState.value.copy(
            deleteCandidate = _uiState.value.programs.firstOrNull { it.id == programId },
            messageRes = null,
            formErrorRes = null,
        )
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(deleteCandidate = null)
    }

    fun updateEditorName(value: String) = updateEditor { copy(name = value) }
    fun updateEditorDescription(value: String) = updateEditor { copy(description = value) }
    fun updateEditorActive(value: Boolean) = updateEditor { copy(active = value) }
    fun updateEditorType(type: LoyaltyProgramType) {
        val defaults = defaultProgramEditorState(type)
        updateEditor {
            copy(
                type = type,
                pointsSpendStepAmount = defaults.pointsSpendStepAmount,
                pointsAwardedPerStep = defaults.pointsAwardedPerStep,
                pointsWelcomeBonus = defaults.pointsWelcomeBonus,
                pointsMinimumRedeem = defaults.pointsMinimumRedeem,
                cashbackPercent = defaults.cashbackPercent,
                cashbackMinimumSpendAmount = defaults.cashbackMinimumSpendAmount,
                tierSilverThreshold = defaults.tierSilverThreshold,
                tierGoldThreshold = defaults.tierGoldThreshold,
                tierVipThreshold = defaults.tierVipThreshold,
                tierBonusPercent = defaults.tierBonusPercent,
                couponName = defaults.couponName,
                couponPointsCost = defaults.couponPointsCost,
                couponDiscountAmount = defaults.couponDiscountAmount,
                couponMinimumSpendAmount = defaults.couponMinimumSpendAmount,
                checkInVisitsRequired = defaults.checkInVisitsRequired,
                checkInRewardPoints = defaults.checkInRewardPoints,
                checkInRewardName = defaults.checkInRewardName,
                purchaseFrequencyCount = defaults.purchaseFrequencyCount,
                purchaseFrequencyWindowDays = defaults.purchaseFrequencyWindowDays,
                purchaseFrequencyRewardPoints = defaults.purchaseFrequencyRewardPoints,
                purchaseFrequencyRewardName = defaults.purchaseFrequencyRewardName,
                referralReferrerRewardPoints = defaults.referralReferrerRewardPoints,
                referralRefereeRewardPoints = defaults.referralRefereeRewardPoints,
                referralCodePrefix = defaults.referralCodePrefix,
            )
        }
    }

    fun updatePointsSpendStepAmount(value: String) = updateEditor { copy(pointsSpendStepAmount = value) }
    fun updatePointsAwardedPerStep(value: String) = updateEditor { copy(pointsAwardedPerStep = value) }
    fun updatePointsWelcomeBonus(value: String) = updateEditor { copy(pointsWelcomeBonus = value) }
    fun updatePointsMinimumRedeem(value: String) = updateEditor { copy(pointsMinimumRedeem = value) }
    fun updateCashbackPercent(value: String) = updateEditor { copy(cashbackPercent = value) }
    fun updateCashbackMinimumSpendAmount(value: String) = updateEditor { copy(cashbackMinimumSpendAmount = value) }
    fun updateTierSilverThreshold(value: String) = updateEditor { copy(tierSilverThreshold = value) }
    fun updateTierGoldThreshold(value: String) = updateEditor { copy(tierGoldThreshold = value) }
    fun updateTierVipThreshold(value: String) = updateEditor { copy(tierVipThreshold = value) }
    fun updateTierBonusPercent(value: String) = updateEditor { copy(tierBonusPercent = value) }
    fun updateCouponName(value: String) = updateEditor { copy(couponName = value) }
    fun updateCouponPointsCost(value: String) = updateEditor { copy(couponPointsCost = value) }
    fun updateCouponDiscountAmount(value: String) = updateEditor { copy(couponDiscountAmount = value) }
    fun updateCouponMinimumSpendAmount(value: String) = updateEditor { copy(couponMinimumSpendAmount = value) }
    fun updateCheckInVisitsRequired(value: String) = updateEditor { copy(checkInVisitsRequired = value) }
    fun updateCheckInRewardPoints(value: String) = updateEditor { copy(checkInRewardPoints = value) }
    fun updateCheckInRewardName(value: String) = updateEditor { copy(checkInRewardName = value) }
    fun updatePurchaseFrequencyCount(value: String) = updateEditor { copy(purchaseFrequencyCount = value) }
    fun updatePurchaseFrequencyWindowDays(value: String) = updateEditor { copy(purchaseFrequencyWindowDays = value) }
    fun updatePurchaseFrequencyRewardPoints(value: String) = updateEditor { copy(purchaseFrequencyRewardPoints = value) }
    fun updatePurchaseFrequencyRewardName(value: String) = updateEditor { copy(purchaseFrequencyRewardName = value) }
    fun updateReferralReferrerRewardPoints(value: String) = updateEditor { copy(referralReferrerRewardPoints = value) }
    fun updateReferralRefereeRewardPoints(value: String) = updateEditor { copy(referralRefereeRewardPoints = value) }
    fun updateReferralCodePrefix(value: String) = updateEditor { copy(referralCodePrefix = value) }

    fun saveProgram() {
        val state = _uiState.value
        val editor = state.editorState ?: return
        val storeId = state.selectedStoreId ?: run {
            _uiState.value = state.copy(formErrorRes = R.string.merchant_program_error_store_required)
            return
        }
        val fieldErrors = editor.validate()
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(editorFieldErrors = fieldErrors, formErrorRes = null)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, editorFieldErrors = emptyMap(), formErrorRes = null, messageRes = null)
            runCatching {
                val draft = editor.toDraft(storeId)
                if (editor.programId == null) createProgramUseCase(draft) else updateProgramUseCase(editor.programId, draft)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    activeSubEditor = null,
                    isSubmitting = false,
                    editorState = null,
                    editorFieldErrors = emptyMap(),
                    formErrorRes = null,
                    messageRes = if (editor.programId == null) R.string.merchant_program_created_message else R.string.merchant_program_updated_message,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSubmitting = false, formErrorRes = R.string.merchant_program_error_save_failed)
            }
        }
    }

    fun toggleProgramEnabled(programId: String, enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busyProgramId = programId, messageRes = null, formErrorRes = null)
            runCatching { setProgramEnabledUseCase(programId, enabled) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        busyProgramId = null,
                        messageRes = null,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(busyProgramId = null, formErrorRes = R.string.merchant_program_error_save_failed)
                }
        }
    }

    fun confirmDeleteProgram() {
        val program = _uiState.value.deleteCandidate ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, formErrorRes = null, messageRes = null)
            runCatching { deleteProgramUseCase(program.id) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        deleteCandidate = null,
                        messageRes = R.string.merchant_program_deleted_message,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        deleteCandidate = null,
                        formErrorRes = R.string.merchant_program_error_delete_failed,
                    )
                }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(messageRes = null, formErrorRes = null)
    }

    fun saveReward() {
        val state = _uiState.value
        val editor = state.rewardEditorState ?: return
        val storeId = state.selectedStoreId ?: run {
            _uiState.value = state.copy(formErrorRes = R.string.merchant_reward_error_store_required)
            return
        }
        val fieldErrors = editor.validate()
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(rewardEditorFieldErrors = fieldErrors, formErrorRes = null)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isSubmitting = true,
                rewardEditorFieldErrors = emptyMap(),
                formErrorRes = null,
                messageRes = null,
            )
            runCatching {
                val draft = editor.toDraft(storeId)
                if (editor.rewardId == null) createRewardUseCase(draft) else updateRewardUseCase(editor.rewardId, draft)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    rewardEditorState = null,
                    rewardEditorFieldErrors = emptyMap(),
                    formErrorRes = null,
                    messageRes = if (editor.rewardId == null) {
                        R.string.merchant_reward_created_message
                    } else {
                        R.string.merchant_reward_updated_message
                    },
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    formErrorRes = R.string.merchant_reward_error_save_failed,
                )
            }
        }
    }

    fun toggleRewardEnabled(rewardId: String, enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                busyRewardId = rewardId,
                messageRes = null,
                formErrorRes = null,
            )
            runCatching { setRewardEnabledUseCase(rewardId, enabled) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        busyRewardId = null,
                        messageRes = if (enabled) {
                            R.string.merchant_reward_enabled_message
                        } else {
                            R.string.merchant_reward_disabled_message
                        },
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        busyRewardId = null,
                        formErrorRes = R.string.merchant_reward_error_save_failed,
                    )
                }
        }
    }

    fun confirmDeleteReward() {
        val reward = _uiState.value.rewardDeleteCandidate ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                formErrorRes = null,
                messageRes = null,
            )
            runCatching { deleteRewardUseCase(reward.id) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        rewardDeleteCandidate = null,
                        messageRes = R.string.merchant_reward_deleted_message,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        rewardDeleteCandidate = null,
                        formErrorRes = R.string.merchant_reward_error_delete_failed,
                    )
                }
        }
    }

    private fun updateEditor(transform: ProgramEditorState.() -> ProgramEditorState) {
        val current = _uiState.value.editorState ?: return
        _uiState.value = _uiState.value.copy(
            editorState = current.transform(),
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }

    private fun updateRewardEditor(transform: RewardEditorState.() -> RewardEditorState) {
        val current = _uiState.value.rewardEditorState ?: return
        _uiState.value = _uiState.value.copy(
            rewardEditorState = current.transform(),
            rewardEditorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }
}
