package com.vector.verevcodex.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.loyalty.ProgramBenefitResetType
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.usesProgramBenefit
import com.vector.verevcodex.domain.model.loyalty.usesRewardItem
import com.vector.verevcodex.domain.usecase.loyalty.CreateRewardUseCase
import com.vector.verevcodex.domain.usecase.loyalty.AdjustRewardInventoryUseCase
import com.vector.verevcodex.domain.usecase.loyalty.DeleteRewardUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveActiveScanActionsUseCase
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
import com.vector.verevcodex.domain.usecase.store.ObserveStoresUseCase
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
    observeStoresUseCase: ObserveStoresUseCase,
    observeProgramsUseCase: ObserveProgramsUseCase,
    observeRewardsUseCase: ObserveRewardsUseCase,
    observeCampaignsUseCase: ObserveCampaignsUseCase,
    observeActiveScanActionsUseCase: ObserveActiveScanActionsUseCase,
    private val createRewardUseCase: CreateRewardUseCase,
    private val updateRewardUseCase: UpdateRewardUseCase,
    private val setRewardEnabledUseCase: SetRewardEnabledUseCase,
    private val adjustRewardInventoryUseCase: AdjustRewardInventoryUseCase,
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
        val storesFlow = observeStoresUseCase()
        val programsFlow = selectedStoreFlow.flatMapLatest { store -> observeProgramsUseCase(store?.id) }
        val allProgramsFlow = observeProgramsUseCase(null)
        val rewardsFlow = selectedStoreFlow.flatMapLatest { store -> observeRewardsUseCase(store?.id) }
        val campaignsFlow = selectedStoreFlow.flatMapLatest { store -> observeCampaignsUseCase(store?.id) }
        val activeScanActionsFlow = selectedStoreFlow.flatMapLatest { store -> observeActiveScanActionsUseCase(store?.id) }

        val storeContextFlow = combine(selectedStoreFlow, storesFlow) { store, stores ->
            store to stores
        }
        val loyaltyDataFlow = combine(programsFlow, allProgramsFlow, rewardsFlow, campaignsFlow, activeScanActionsFlow) { programs, allPrograms, rewards, campaigns, activeScanActions ->
            LoyaltyStoreSnapshot(
                programs = programs,
                allPrograms = allPrograms,
                rewards = rewards,
                campaigns = campaigns,
                activeScanActions = activeScanActions,
            )
        }

        combine(storeContextFlow, loyaltyDataFlow) { storeContext, data ->
            storeContext to data
        }.onEach { (storeContext, data) ->
            val (store, stores) = storeContext
            val programs = data.programs
            val rewards = data.rewards
            val campaigns = data.campaigns
            _uiState.value = _uiState.value.copy(
                selectedStoreId = store?.id,
                selectedStoreName = store?.name.orEmpty(),
                stores = stores,
                isLoading = false,
                programs = programs,
                allPrograms = data.allPrograms,
                rewards = rewards,
                campaigns = campaigns,
                activeScanActions = data.activeScanActions,
                busyProgramId = _uiState.value.busyProgramId?.takeIf { busyId -> programs.any { it.id == busyId } },
                busyRewardId = _uiState.value.busyRewardId?.takeIf { busyId -> rewards.any { it.id == busyId } },
                enableCandidate = _uiState.value.enableCandidate?.let { candidate ->
                    programs.firstOrNull { it.id == candidate.id }
                },
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
        val reward = _uiState.value.rewards.firstOrNull { it.id == rewardId } ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                rewardEditorState = null,
                rewardEditorFieldErrors = emptyMap(),
                rewardDeleteCandidate = null,
                formErrorRes = null,
                messageRes = null,
            )
            runCatching { deleteRewardUseCase(reward.id) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        rewardDeleteCandidate = null,
                        messageRes = null,
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

    fun dismissRewardDeleteDialog() {
        _uiState.value = _uiState.value.copy(rewardDeleteCandidate = null)
    }

    fun updateRewardName(value: String) = updateRewardEditor { copy(name = value) }
    fun updateRewardDescription(value: String) = updateRewardEditor { copy(description = value) }
    fun updateRewardImageUri(value: String) = updateRewardEditor { copy(imageUri = value) }
    fun updateRewardExpirationEnabled(value: Boolean) = updateRewardEditor {
        copy(
            expirationEnabled = value,
            expirationDate = if (value) expirationDate else "",
        )
    }
    fun updateRewardExpirationDate(value: String) = updateRewardEditor { copy(expirationDate = value) }
    fun updateRewardAvailableQuantity(value: String) = updateRewardEditor { copy(availableQuantity = value) }

    fun openCreateProgram(type: LoyaltyProgramType = LoyaltyProgramType.POINTS) {
        val state = _uiState.value
        val defaultStoreIds = state.stores.map { it.id }.ifEmpty { listOfNotNull(state.selectedStoreId) }
        _uiState.value = _uiState.value.copy(
            activeSubEditor = null,
            activeBenefitEditor = null,
            editorState = defaultProgramEditorState(type).copy(
                applyToAllBranches = defaultStoreIds.size > 1,
                targetStoreIds = defaultStoreIds,
                lockedStoreIds = emptyList(),
            ),
            editorFieldErrors = emptyMap(),
            deleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun openEditProgram(programId: String) {
        val program = _uiState.value.programs.firstOrNull { it.id == programId } ?: return
        val matchedStoreIds = resolveMatchingProgramStoreIds(program, _uiState.value.allPrograms)
        _uiState.value = _uiState.value.copy(
            activeSubEditor = null,
            activeBenefitEditor = null,
            editorState = program.toEditorState().copy(
                targetStoreIds = matchedStoreIds,
                lockedStoreIds = matchedStoreIds,
            ),
            editorFieldErrors = emptyMap(),
            deleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun openProgramSubEditor(programId: String, subEditor: ProgramSubEditor) {
        val program = _uiState.value.programs.firstOrNull { it.id == programId } ?: return
        val matchedStoreIds = resolveMatchingProgramStoreIds(program, _uiState.value.allPrograms)
        _uiState.value = _uiState.value.copy(
            activeSubEditor = subEditor,
            activeBenefitEditor = null,
            editorState = program.toEditorState().copy(
                targetStoreIds = matchedStoreIds,
                lockedStoreIds = matchedStoreIds,
            ),
            editorFieldErrors = emptyMap(),
            deleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun openActiveProgramSubEditor(subEditor: ProgramSubEditor) {
        val editor = _uiState.value.editorState ?: return
        _uiState.value = _uiState.value.copy(
            activeSubEditor = subEditor,
            activeBenefitEditor = null,
            editorState = editor,
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun closeProgramSubEditor() {
        _uiState.value = _uiState.value.copy(
            activeSubEditor = null,
            activeBenefitEditor = null,
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }

    fun applyProgramSubEditorChanges() {
        val state = _uiState.value
        val editor = state.editorState ?: return
        val activeSubEditor = state.activeSubEditor ?: return
        val fieldErrors = editor.validate()
        val scopedErrors = fieldErrors.filterKeys { key ->
            key.belongsToSubEditor(activeSubEditor, state.activeBenefitEditor)
        }
        if (scopedErrors.isNotEmpty()) {
            _uiState.value = state.copy(
                editorFieldErrors = scopedErrors,
                formErrorRes = R.string.merchant_form_issue_title,
            )
            return
        }
        _uiState.value = state.copy(
            activeSubEditor = null,
            activeBenefitEditor = null,
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(
            activeSubEditor = null,
            activeBenefitEditor = null,
            editorState = null,
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }

    fun applyEditorValidationErrors(errors: Map<String, Int>): Boolean {
        _uiState.value = _uiState.value.copy(
            editorFieldErrors = errors,
            formErrorRes = if (errors.isEmpty()) null else R.string.merchant_form_issue_title,
        )
        return errors.isEmpty()
    }

    fun requestDelete(programId: String) {
        val program = _uiState.value.programs.firstOrNull { it.id == programId } ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                deleteCandidate = null,
                messageRes = null,
                formErrorRes = null,
            )
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

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(deleteCandidate = null)
    }

    fun requestProgramToggle(programId: String, enabled: Boolean) {
        if (!enabled) {
            toggleProgramEnabled(programId = programId, enabled = false)
            return
        }
        val candidate = _uiState.value.programs.firstOrNull { it.id == programId } ?: return
        _uiState.value = _uiState.value.copy(enableCandidate = candidate, formErrorRes = null, messageRes = null)
    }

    fun dismissProgramEnableDialog() {
        _uiState.value = _uiState.value.copy(enableCandidate = null)
    }

    fun confirmProgramEnable() {
        val candidate = _uiState.value.enableCandidate ?: return
        _uiState.value = _uiState.value.copy(enableCandidate = null)
        toggleProgramEnabled(programId = candidate.id, enabled = true)
    }

    fun updateEditorName(value: String) = updateEditor { copy(name = value) }
    fun updateEditorDescription(value: String) = updateEditor { copy(description = value) }
    fun updateEditorApplyToAllBranches(value: Boolean) = updateEditor {
        copy(
            applyToAllBranches = value,
            targetStoreIds = if (value) {
                _uiState.value.stores.map { it.id }
            } else {
                targetStoreIds.ifEmpty { listOfNotNull(_uiState.value.selectedStoreId) }
            },
        )
    }
    fun toggleEditorStoreTarget(storeId: String) = updateEditor {
        val updated = if (targetStoreIds.contains(storeId)) {
            targetStoreIds - storeId
        } else {
            targetStoreIds + storeId
        }
        copy(
            applyToAllBranches = false,
            targetStoreIds = updated.distinct(),
        )
    }
    fun updateEditorActive(value: Boolean) = updateEditor { copy(active = value) }
    fun updateEditorTargetGender(value: String) = updateEditor { copy(targetGender = value.ifBlank { "ALL" }) }
    fun updateEditorAgeTargetingEnabled(value: Boolean) = updateEditor {
        copy(
            ageTargetingEnabled = value,
            targetAgeMin = if (value) targetAgeMin else "",
            targetAgeMax = if (value) targetAgeMax else "",
        )
    }
    fun updateEditorTargetAgeMin(value: String) = updateEditor { copy(targetAgeMin = value) }
    fun updateEditorTargetAgeMax(value: String) = updateEditor { copy(targetAgeMax = value) }
    fun updateEditorOneTimePerCustomer(value: Boolean) = updateEditor { copy(oneTimePerCustomer = value) }
    fun updateEditorAutoScheduleEnabled(value: Boolean) = updateEditor {
        copy(
            autoScheduleEnabled = value,
            scheduleStartDate = if (value) scheduleStartDate.ifBlank { java.time.LocalDate.now().toString() } else scheduleStartDate,
            scheduleEndDate = if (value) scheduleEndDate.ifBlank { java.time.LocalDate.now().plusDays(29).toString() } else scheduleEndDate,
        )
    }
    fun updateEditorScheduleStartDate(value: String) = updateEditor { copy(scheduleStartDate = value) }
    fun updateEditorScheduleEndDate(value: String) = updateEditor { copy(scheduleEndDate = value) }
    fun updateEditorAnnualRepeatEnabled(value: Boolean) = updateEditor { copy(annualRepeatEnabled = value) }
    fun updateEditorBenefitResetType(value: ProgramBenefitResetType) = updateEditor {
        copy(
            benefitResetType = value,
            benefitResetCustomDays = if (value == ProgramBenefitResetType.CUSTOM) benefitResetCustomDays else "",
        )
    }
    fun updateEditorBenefitResetCustomDays(value: String) = updateEditor { copy(benefitResetCustomDays = value) }
    fun updateEditorType(type: LoyaltyProgramType) {
        val defaults = defaultProgramEditorState(type)
        updateEditor {
            copy(
                type = type,
                benefitResetType = defaults.benefitResetType,
                benefitResetCustomDays = defaults.benefitResetCustomDays,
                pointsSpendStepAmount = defaults.pointsSpendStepAmount,
                pointsAwardedPerStep = defaults.pointsAwardedPerStep,
                pointsWelcomeBonus = defaults.pointsWelcomeBonus,
                pointsMinimumRedeem = defaults.pointsMinimumRedeem,
                cashbackPercent = defaults.cashbackPercent,
                cashbackMinimumSpendAmount = defaults.cashbackMinimumSpendAmount,
                tierLevels = defaults.tierLevels,
                couponName = defaults.couponName,
                couponPointsCost = defaults.couponPointsCost,
                couponDiscountAmount = defaults.couponDiscountAmount,
                couponMinimumSpendAmount = defaults.couponMinimumSpendAmount,
                checkInVisitsRequired = defaults.checkInVisitsRequired,
                checkInReward = defaults.checkInReward,
                purchaseFrequencyCount = defaults.purchaseFrequencyCount,
                purchaseFrequencyWindowDays = defaults.purchaseFrequencyWindowDays,
                purchaseFrequencyReward = defaults.purchaseFrequencyReward,
                referralReferrerReward = defaults.referralReferrerReward,
                referralRefereeReward = defaults.referralRefereeReward,
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
    fun updateTierLevelName(levelId: String, value: String) = updateEditor {
        copy(tierLevels = tierLevels.map { level -> if (level.id == levelId) level.copy(name = value) else level })
    }
    fun updateTierLevelThreshold(levelId: String, value: String) = updateEditor {
        copy(tierLevels = tierLevels.map { level -> if (level.id == levelId) level.copy(threshold = value) else level })
    }
    fun updateTierLevelBonusPercent(levelId: String, value: String) = updateEditor {
        copy(tierLevels = tierLevels.map { level -> if (level.id == levelId) level.copy(bonusPercent = value) else level })
    }
    fun updateTierLevelRewardType(levelId: String, value: ProgramRewardOutcomeType) = updateEditor {
        copy(
            tierLevels = tierLevels.map { level ->
                if (level.id == levelId) {
                    level.copy(
                        rewardOutcome = level.rewardOutcome.copy(
                            type = value,
                            pointsAmount = if (value == ProgramRewardOutcomeType.POINTS) level.rewardOutcome.pointsAmount else "",
                            rewardId = if (value.usesRewardItem()) level.rewardOutcome.rewardId else null,
                            programId = if (value.usesProgramBenefit()) level.rewardOutcome.programId else null,
                        ),
                    )
                } else {
                    level
                }
            },
        )
    }
    fun updateTierLevelRewardLabel(levelId: String, value: String) = updateEditor {
        copy(tierLevels = tierLevels.map { level -> if (level.id == levelId) level.copy(rewardOutcome = level.rewardOutcome.copy(label = value)) else level })
    }
    fun updateTierLevelRewardPoints(levelId: String, value: String) = updateEditor {
        copy(tierLevels = tierLevels.map { level -> if (level.id == levelId) level.copy(rewardOutcome = level.rewardOutcome.copy(pointsAmount = value)) else level })
    }
    fun updateTierLevelRewardRewardId(levelId: String, value: String?) = updateEditor {
        copy(tierLevels = tierLevels.map { level -> if (level.id == levelId) level.copy(rewardOutcome = level.rewardOutcome.copy(rewardId = value)) else level })
    }
    fun updateTierLevelRewardProgramId(levelId: String, value: String?) = updateEditor {
        copy(tierLevels = tierLevels.map { level -> if (level.id == levelId) level.copy(rewardOutcome = level.rewardOutcome.copy(programId = value)) else level })
    }
    fun clearTierLevelBenefit(levelId: String) = updateEditor {
        copy(
            tierLevels = tierLevels.map { level ->
                if (level.id == levelId) {
                    level.copy(rewardOutcome = ProgramRewardOutcomeEditorState())
                } else {
                    level
                }
            },
        )
    }
    fun addTierLevel() = updateEditor {
        val nextIndex = tierLevels.size
        val lastThreshold = tierLevels.lastOrNull()?.threshold?.toIntOrNull() ?: 0
        copy(
            tierLevels = tierLevels + TierLevelEditorState(
                id = "tier_${System.currentTimeMillis()}",
                name = DefaultTierNames.getOrNull(nextIndex) ?: "Custom Tier ${nextIndex + 1}",
                threshold = (lastThreshold + 250).toString(),
                bonusPercent = "0",
                rewardOutcome = ProgramRewardOutcomeEditorState(),
            ),
        )
    }
    fun removeTierLevel(levelId: String) = updateEditor {
        copy(tierLevels = tierLevels.filterNot { it.id == levelId }.ifEmpty { tierLevels })
    }
    fun updateCouponName(value: String) = updateEditor { copy(couponName = value) }
    fun updateCouponPointsCost(value: String) = updateEditor { copy(couponPointsCost = value) }
    fun updateCouponDiscountAmount(value: String) = updateEditor { copy(couponDiscountAmount = value) }
    fun updateCouponMinimumSpendAmount(value: String) = updateEditor { copy(couponMinimumSpendAmount = value) }
    fun updateCheckInVisitsRequired(value: String) = updateEditor { copy(checkInVisitsRequired = value) }
    fun updatePurchaseFrequencyCount(value: String) = updateEditor { copy(purchaseFrequencyCount = value) }
    fun updatePurchaseFrequencyWindowDays(value: String) = updateEditor { copy(purchaseFrequencyWindowDays = value) }
    fun updatePurchaseFrequencyRewardChoice(choice: ProgramBenefitChoice) {
        when (choice) {
            ProgramBenefitChoice.POINTS -> updateRewardOutcome(ProgramRewardSlot.PURCHASE_FREQUENCY) {
                copy(type = ProgramRewardOutcomeType.POINTS, rewardId = null, programId = null)
            }
            ProgramBenefitChoice.REWARD_CATALOG -> updateRewardOutcome(ProgramRewardSlot.PURCHASE_FREQUENCY) {
                copy(type = ProgramRewardOutcomeType.FREE_PRODUCT, pointsAmount = "", programId = null)
            }
        }
    }
    fun updatePurchaseFrequencyRewardPoints(value: String) =
        updateRewardOutcome(ProgramRewardSlot.PURCHASE_FREQUENCY) { copy(pointsAmount = value) }
    fun updatePurchaseFrequencyRewardId(value: String?) =
        updateRewardOutcome(ProgramRewardSlot.PURCHASE_FREQUENCY) { copy(rewardId = value) }
    fun updateReferralCodePrefix(value: String) = updateEditor { copy(referralCodePrefix = value) }
    fun updateRewardOutcomeType(slot: ProgramRewardSlot, value: ProgramRewardOutcomeType) =
        updateRewardOutcome(slot) {
            copy(
                type = value,
                pointsAmount = if (value == ProgramRewardOutcomeType.POINTS) pointsAmount else "",
                rewardId = if (value.usesRewardItem()) rewardId else null,
                programId = if (value.usesProgramBenefit()) programId else null,
            )
        }
    fun updateRewardOutcomeLabel(slot: ProgramRewardSlot, value: String) = updateRewardOutcome(slot) { copy(label = value) }
    fun updateRewardOutcomePoints(slot: ProgramRewardSlot, value: String) = updateRewardOutcome(slot) { copy(pointsAmount = value) }
    fun updateRewardOutcomeRewardId(slot: ProgramRewardSlot, value: String?) = updateRewardOutcome(slot) { copy(rewardId = value) }
    fun updateRewardOutcomeProgramId(slot: ProgramRewardSlot, value: String?) = updateRewardOutcome(slot) { copy(programId = value) }

    fun openTierBenefitEditor(levelId: String) {
        val editor = _uiState.value.editorState ?: return
        if (editor.tierLevels.none { it.id == levelId }) return
        _uiState.value = _uiState.value.copy(
            activeSubEditor = ProgramSubEditor.BENEFIT_EDIT,
            activeBenefitEditor = ProgramBenefitEditorTarget.tier(levelId),
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }

    fun openBenefitEditor(slot: ProgramRewardSlot) {
        _uiState.value = _uiState.value.copy(
            activeSubEditor = ProgramSubEditor.BENEFIT_EDIT,
            activeBenefitEditor = ProgramBenefitEditorTarget.slot(slot),
            editorFieldErrors = emptyMap(),
            formErrorRes = null,
        )
    }

    fun updateActiveBenefitChoice(choice: ProgramBenefitChoice) {
        when (choice) {
            ProgramBenefitChoice.POINTS -> updateActiveBenefit {
                copy(type = ProgramRewardOutcomeType.POINTS, rewardId = null, programId = null)
            }
            ProgramBenefitChoice.REWARD_CATALOG -> updateActiveBenefit {
                copy(type = ProgramRewardOutcomeType.FREE_PRODUCT, pointsAmount = "", programId = null)
            }
        }
    }

    fun updateActiveBenefitPoints(value: String) = updateActiveBenefit { copy(pointsAmount = value) }

    fun updateActiveBenefitRewardId(value: String?) = updateActiveBenefit { copy(rewardId = value) }

    fun clearActiveBenefit() = updateActiveBenefit { ProgramRewardOutcomeEditorState() }

    fun saveProgram() {
        val state = _uiState.value
        val editor = state.editorState ?: return
        val activeStoreId = state.selectedStoreId ?: run {
            _uiState.value = state.copy(formErrorRes = R.string.merchant_program_error_store_required)
            return
        }
        val targetStoreIds = resolveTargetStoreIds(editor, state.stores, activeStoreId)
        if (targetStoreIds.isEmpty()) {
            _uiState.value = state.copy(formErrorRes = R.string.merchant_program_error_store_required)
            return
        }
        val fieldErrors = editor.validate()
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(
                editorFieldErrors = fieldErrors,
                formErrorRes = R.string.merchant_form_issue_title,
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, editorFieldErrors = emptyMap(), formErrorRes = null, messageRes = null)
            runCatching {
                if (editor.programId == null) {
                    targetStoreIds.forEach { storeId ->
                        val draft = editor.toDraft(
                            storeId = storeId,
                            availablePrograms = state.allPrograms,
                            availableRewards = state.rewards,
                        )
                        createProgramUseCase(draft)
                    }
                } else {
                    val currentStoreId = editor.lockedStoreIds.firstOrNull() ?: activeStoreId
                    val updateDraft = editor.toDraft(
                        storeId = currentStoreId,
                        availablePrograms = state.allPrograms,
                        availableRewards = state.rewards,
                    )
                    updateProgramUseCase(editor.programId, updateDraft)
                    val additionalStoreIds = targetStoreIds
                        .filterNot { editor.lockedStoreIds.contains(it) }
                        .distinct()
                    additionalStoreIds.forEach { storeId ->
                        val draft = editor.toDraft(
                            storeId = storeId,
                            availablePrograms = state.allPrograms,
                            availableRewards = state.rewards,
                        )
                        createProgramUseCase(draft)
                    }
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    activeSubEditor = null,
                    activeBenefitEditor = null,
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
        _uiState.value = _uiState.value.copy(messageRes = null, formErrorRes = null, enableCandidate = null)
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
            _uiState.value = state.copy(
                rewardEditorFieldErrors = fieldErrors,
                formErrorRes = R.string.merchant_form_issue_title,
            )
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
                    messageRes = null,
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
                        messageRes = null,
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

    fun adjustRewardInventory(rewardId: String, delta: Int) {
        if (delta == 0) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                busyRewardId = rewardId,
                messageRes = null,
                formErrorRes = null,
            )
            runCatching { adjustRewardInventoryUseCase(rewardId, delta) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        busyRewardId = null,
                        messageRes = null,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        busyRewardId = null,
                        formErrorRes = R.string.merchant_reward_error_inventory_update_failed,
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
                        messageRes = null,
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

    private fun updateRewardOutcome(
        slot: ProgramRewardSlot,
        transform: ProgramRewardOutcomeEditorState.() -> ProgramRewardOutcomeEditorState,
    ) {
        updateEditor {
            when (slot) {
                ProgramRewardSlot.CHECK_IN -> copy(checkInReward = checkInReward.transform())
                ProgramRewardSlot.PURCHASE_FREQUENCY -> copy(purchaseFrequencyReward = purchaseFrequencyReward.transform())
                ProgramRewardSlot.REFERRAL_REFERRER -> copy(referralReferrerReward = referralReferrerReward.transform())
                ProgramRewardSlot.REFERRAL_REFEREE -> copy(referralRefereeReward = referralRefereeReward.transform())
            }
        }
    }

    private fun updateActiveBenefit(
        transform: ProgramRewardOutcomeEditorState.() -> ProgramRewardOutcomeEditorState,
    ) {
        val target = _uiState.value.activeBenefitEditor ?: return
        if (target.tierLevelId != null) {
            updateEditor {
                copy(
                    tierLevels = tierLevels.map { level ->
                        if (level.id == target.tierLevelId) {
                            level.copy(rewardOutcome = level.rewardOutcome.transform())
                        } else {
                            level
                        }
                    },
                )
            }
            return
        }
        val slot = target.slot ?: return
        updateRewardOutcome(slot, transform)
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

private fun String.belongsToSubEditor(
    subEditor: ProgramSubEditor,
    activeBenefitEditor: ProgramBenefitEditorTarget?,
): Boolean = when (subEditor) {
    ProgramSubEditor.BASICS_EDIT -> this in setOf(
        PROGRAM_FIELD_NAME,
        PROGRAM_FIELD_DESCRIPTION,
    )
    ProgramSubEditor.AUDIENCE_EDIT -> this in setOf(
        PROGRAM_FIELD_TARGET_AGE_MIN,
        PROGRAM_FIELD_TARGET_AGE_MAX,
    )
    ProgramSubEditor.AVAILABILITY_EDIT -> this in setOf(
        PROGRAM_FIELD_SCHEDULE_START,
        PROGRAM_FIELD_SCHEDULE_END,
    )
    ProgramSubEditor.TIER_EDIT -> this == PROGRAM_FIELD_TIER_SILVER || startsWith("tier_level_")
    ProgramSubEditor.EARN_RULES_EDIT -> this in setOf(
        PROGRAM_FIELD_POINTS_STEP,
        PROGRAM_FIELD_POINTS_AWARDED,
        PROGRAM_FIELD_POINTS_REDEEM,
    )
    ProgramSubEditor.REWARD_EDIT -> this in setOf(
        PROGRAM_FIELD_COUPON_NAME,
        PROGRAM_FIELD_COUPON_POINTS,
        PROGRAM_FIELD_COUPON_DISCOUNT,
    )
    ProgramSubEditor.CASHBACK_EDIT -> this == PROGRAM_FIELD_CASHBACK_PERCENT
    ProgramSubEditor.CHECKIN_EDIT -> this in setOf(
        PROGRAM_FIELD_CHECKIN_VISITS,
        PROGRAM_FIELD_CHECKIN_REWARD,
    )
    ProgramSubEditor.FREQUENCY_EDIT -> this in setOf(
        PROGRAM_FIELD_FREQUENCY_COUNT,
        PROGRAM_FIELD_FREQUENCY_WINDOW,
        PROGRAM_FIELD_FREQUENCY_REWARD,
    )
    ProgramSubEditor.REFERRAL_EDIT -> this in setOf(
        PROGRAM_FIELD_REFERRAL_REFERRER,
        PROGRAM_FIELD_REFERRAL_REFEREE,
        PROGRAM_FIELD_REFERRAL_PREFIX,
    )
    ProgramSubEditor.BENEFIT_EDIT -> when {
        activeBenefitEditor?.tierLevelId != null -> startsWith("tier_level_")
        activeBenefitEditor?.slot == ProgramRewardSlot.CHECK_IN -> this == PROGRAM_FIELD_CHECKIN_REWARD
        activeBenefitEditor?.slot == ProgramRewardSlot.PURCHASE_FREQUENCY -> this == PROGRAM_FIELD_FREQUENCY_REWARD
        activeBenefitEditor?.slot == ProgramRewardSlot.REFERRAL_REFERRER -> this == PROGRAM_FIELD_REFERRAL_REFERRER
        activeBenefitEditor?.slot == ProgramRewardSlot.REFERRAL_REFEREE -> this == PROGRAM_FIELD_REFERRAL_REFEREE
        else -> false
    }
}

private fun resolveTargetStoreIds(
    editor: ProgramEditorState,
    stores: List<com.vector.verevcodex.domain.model.business.Store>,
    fallbackStoreId: String,
): List<String> {
    val allStoreIds = stores.map { it.id }
    return when {
        allStoreIds.isEmpty() -> listOf(fallbackStoreId)
        stores.size == 1 -> listOf(allStoreIds.first())
        editor.applyToAllBranches -> allStoreIds
        editor.targetStoreIds.isNotEmpty() -> editor.targetStoreIds.distinct()
        else -> listOf(fallbackStoreId)
    }
}

private fun resolveMatchingProgramStoreIds(
    program: com.vector.verevcodex.domain.model.loyalty.RewardProgram,
    allPrograms: List<com.vector.verevcodex.domain.model.loyalty.RewardProgram>,
): List<String> {
    val matching = allPrograms.filter {
        it.type == program.type &&
            it.name == program.name &&
            it.description == program.description
    }.map { it.storeId }.distinct()
    return if (matching.isNotEmpty()) matching else listOf(program.storeId)
}

private data class LoyaltyStoreSnapshot(
    val programs: List<com.vector.verevcodex.domain.model.loyalty.RewardProgram>,
    val allPrograms: List<com.vector.verevcodex.domain.model.loyalty.RewardProgram>,
    val rewards: List<com.vector.verevcodex.domain.model.loyalty.Reward>,
    val campaigns: List<com.vector.verevcodex.domain.model.promotions.Campaign>,
    val activeScanActions: List<com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction>,
)
