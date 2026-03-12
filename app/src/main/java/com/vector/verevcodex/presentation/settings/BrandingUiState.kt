package com.vector.verevcodex.presentation.settings

data class BrandingUiState(
    val palettes: List<BrandingPaletteUi> = emptyList(),
    val selectedPaletteId: String = "",
    val themeMode: ThemeModeUi = ThemeModeUi.LIGHT,
)
