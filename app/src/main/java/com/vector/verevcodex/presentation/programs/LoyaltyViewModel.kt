package com.vector.verevcodex.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.LoyaltyProgramType
import com.vector.verevcodex.domain.model.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.usecase.CreateProgramUseCase
import com.vector.verevcodex.domain.usecase.DeleteProgramUseCase
import com.vector.verevcodex.domain.usecase.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.ObserveRewardsUseCase
import com.vector.verevcodex.domain.usecase.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.SetProgramEnabledUseCase
import com.vector.verevcodex.domain.usecase.UpdateProgramUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    private val createProgramUseCase: CreateProgramUseCase,
    private val updateProgramUseCase: UpdateProgramUseCase,
    private val setProgramEnabledUseCase: SetProgramEnabledUseCase,
    private val deleteProgramUseCase: DeleteProgramUseCase,
) : ViewModel() {
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(LoyaltyUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<LoyaltyUiState> = _uiState

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
            )
        }.launchIn(viewModelScope)
    }

    fun openCreateProgram() {
        _uiState.value = _uiState.value.copy(
            editorState = defaultProgramEditorState(),
            deleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun openEditProgram(programId: String) {
        val program = _uiState.value.programs.firstOrNull { it.id == programId } ?: return
        _uiState.value = _uiState.value.copy(
            editorState = program.toEditorState(),
            deleteCandidate = null,
            formErrorRes = null,
            messageRes = null,
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(editorState = null, formErrorRes = null)
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
    fun updateEditorRulesSummary(value: String) = updateEditor { copy(rulesSummary = value) }
    fun updateEditorActive(value: Boolean) = updateEditor { copy(active = value) }
    fun updateEarningEnabled(value: Boolean) = updateEditor { copy(earningEnabled = value) }
    fun updateRewardRedemptionEnabled(value: Boolean) = updateEditor { copy(rewardRedemptionEnabled = value) }
    fun updateVisitCheckInEnabled(value: Boolean) = updateEditor { copy(visitCheckInEnabled = value) }
    fun updateCashbackEnabled(value: Boolean) = updateEditor { copy(cashbackEnabled = value) }
    fun updateTierTrackingEnabled(value: Boolean) = updateEditor { copy(tierTrackingEnabled = value) }

    fun updateEditorType(type: LoyaltyProgramType) {
        val defaultConfig = RewardProgramConfigurationFactory.defaultFor(type = type, active = true)
        updateEditor {
            copy(
                type = type,
                earningEnabled = defaultConfig.earningEnabled,
                rewardRedemptionEnabled = defaultConfig.rewardRedemptionEnabled,
                visitCheckInEnabled = defaultConfig.visitCheckInEnabled,
                cashbackEnabled = defaultConfig.cashbackEnabled,
                tierTrackingEnabled = defaultConfig.tierTrackingEnabled,
            )
        }
    }

    fun saveProgram() {
        val state = _uiState.value
        val editor = state.editorState ?: return
        val storeId = state.selectedStoreId ?: run {
            _uiState.value = state.copy(formErrorRes = R.string.merchant_program_error_store_required)
            return
        }
        when {
            editor.name.isBlank() -> {
                _uiState.value = state.copy(formErrorRes = R.string.merchant_program_error_name_required)
                return
            }
            editor.description.isBlank() -> {
                _uiState.value = state.copy(formErrorRes = R.string.merchant_program_error_description_required)
                return
            }
            editor.rulesSummary.isBlank() -> {
                _uiState.value = state.copy(formErrorRes = R.string.merchant_program_error_rules_required)
                return
            }
            !editor.earningEnabled &&
                !editor.rewardRedemptionEnabled &&
                !editor.visitCheckInEnabled &&
                !editor.cashbackEnabled &&
                !editor.tierTrackingEnabled -> {
                _uiState.value = state.copy(formErrorRes = R.string.merchant_program_error_actions_required)
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, formErrorRes = null, messageRes = null)
            runCatching {
                val draft = editor.toDraft(storeId)
                if (editor.programId == null) {
                    createProgramUseCase(draft)
                } else {
                    updateProgramUseCase(editor.programId, draft)
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    editorState = null,
                    formErrorRes = null,
                    messageRes = if (editor.programId == null) {
                        R.string.merchant_program_created_message
                    } else {
                        R.string.merchant_program_updated_message
                    },
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    formErrorRes = R.string.merchant_program_error_save_failed,
                )
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
                        messageRes = if (enabled) R.string.merchant_program_enabled_message else R.string.merchant_program_disabled_message,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        busyProgramId = null,
                        formErrorRes = R.string.merchant_program_error_save_failed,
                    )
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

    private fun updateEditor(transform: ProgramEditorState.() -> ProgramEditorState) {
        val current = _uiState.value.editorState ?: return
        _uiState.value = _uiState.value.copy(editorState = current.transform(), formErrorRes = null)
    }
}
