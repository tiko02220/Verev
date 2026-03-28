package com.vector.verevcodex.presentation.settings.branding

import com.vector.verevcodex.presentation.settings.*
import androidx.compose.ui.graphics.Color

data class BrandingUiState(
    val palettes: List<BrandingPaletteUi> = emptyList(),
    val selectedPaletteId: String = "",
    val themeMode: ThemeModeUi = ThemeModeUi.LIGHT,
    val primaryColor: Color = Color.Unspecified,
    val secondaryColor: Color = Color.Unspecified,
    val accentColor: Color = Color.Unspecified,
    val logoUri: String = "",
)
