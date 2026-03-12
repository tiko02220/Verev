package com.vector.verevcodex.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.usecase.settings.ObserveBrandingSettingsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.settings.SaveBrandingSettingsUseCase
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
class BrandingViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeBrandingSettingsUseCase: ObserveBrandingSettingsUseCase,
    private val saveBrandingSettingsUseCase: SaveBrandingSettingsUseCase,
) : ViewModel() {
    private val palettes = BrandingCatalog.palettes()
    private var currentStoreId: String? = null

    private val _uiState = MutableStateFlow(
        BrandingUiState(
            palettes = palettes,
            selectedPaletteId = palettes.first().id,
        ),
    )
    val uiState: StateFlow<BrandingUiState> = _uiState.asStateFlow()

    init {
        observeSelectedStoreUseCase()
            .flatMapLatest { store ->
                currentStoreId = store?.id
                if (store == null) {
                    emptyFlow<BrandingSettings>()
                } else {
                    observeBrandingSettingsUseCase(store.id).combine(kotlinx.coroutines.flow.flowOf(store)) { settings, currentStore ->
                        settings ?: defaultBrandingSettings(currentStore.id, currentStore.primaryColor, currentStore.secondaryColor)
                    }
                }
            }
            .onEach { settings ->
                _uiState.value = _uiState.value.copy(
                    palettes = palettes,
                    selectedPaletteId = settings.selectedPaletteId,
                    themeMode = settings.themeModeUi(),
                )
            }
            .launchIn(viewModelScope)
    }

    fun selectPalette(paletteId: String) {
        val storeId = currentStoreId ?: return
        val palette = palettes.firstOrNull { it.id == paletteId } ?: return
        persist(
            BrandingSettings(
                storeId = storeId,
                selectedPaletteId = palette.id,
                themeMode = _uiState.value.themeMode.toDomain(),
                primaryColor = palette.primary.toHexString(),
                secondaryColor = palette.secondary.toHexString(),
                accentColor = palette.accent.toHexString(),
            )
        )
    }

    fun setTheme(mode: ThemeModeUi) {
        val storeId = currentStoreId ?: return
        val palette = palettes.firstOrNull { it.id == _uiState.value.selectedPaletteId } ?: palettes.first()
        persist(
            BrandingSettings(
                storeId = storeId,
                selectedPaletteId = palette.id,
                themeMode = mode.toDomain(),
                primaryColor = palette.primary.toHexString(),
                secondaryColor = palette.secondary.toHexString(),
                accentColor = palette.accent.toHexString(),
            )
        )
    }

    fun reset() {
        selectPalette(palettes.first().id)
        setTheme(ThemeModeUi.LIGHT)
    }

    private fun persist(settings: BrandingSettings) {
        viewModelScope.launch {
            saveBrandingSettingsUseCase(settings)
        }
    }
}
