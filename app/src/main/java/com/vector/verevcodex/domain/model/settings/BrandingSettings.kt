package com.vector.verevcodex.domain.model.settings

data class BrandingSettings(
    val storeId: String,
    val selectedPaletteId: String,
    val themeMode: ThemeMode,
    val primaryColor: String,
    val secondaryColor: String,
    val accentColor: String,
    val logoUri: String = "",
)
