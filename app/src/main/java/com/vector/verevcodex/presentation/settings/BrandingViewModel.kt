package com.vector.verevcodex.presentation.settings

import androidx.lifecycle.ViewModel
import com.vector.verevcodex.presentation.theme.VerevColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class BrandingViewModel @Inject constructor() : ViewModel() {
    private val palettes = listOf(
        BrandingPaletteUi(
            id = "golden_hour",
            name = "Golden Hour",
            primary = VerevColors.Gold,
            secondary = VerevColors.Moss,
            accent = VerevColors.Tan,
        ),
        BrandingPaletteUi(
            id = "forest_mark",
            name = "Forest Mark",
            primary = VerevColors.Forest,
            secondary = VerevColors.Moss,
            accent = VerevColors.Gold,
        ),
        BrandingPaletteUi(
            id = "espresso",
            name = "Espresso",
            primary = VerevColors.Tan,
            secondary = VerevColors.Forest,
            accent = VerevColors.Gold,
        ),
    )

    private val _uiState = MutableStateFlow(
        BrandingUiState(
            palettes = palettes,
            selectedPaletteId = palettes.first().id,
        ),
    )
    val uiState: StateFlow<BrandingUiState> = _uiState.asStateFlow()

    fun selectPalette(paletteId: String) {
        _uiState.update { it.copy(selectedPaletteId = paletteId) }
    }

    fun setTheme(mode: ThemeModeUi) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun reset() {
        _uiState.update { it.copy(selectedPaletteId = palettes.first().id, themeMode = ThemeModeUi.LIGHT) }
    }
}

data class BrandingUiState(
    val palettes: List<BrandingPaletteUi> = emptyList(),
    val selectedPaletteId: String = "",
    val themeMode: ThemeModeUi = ThemeModeUi.LIGHT,
)
