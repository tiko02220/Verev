package com.vector.verevcodex.presentation.settings

import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.model.settings.ThemeMode
import com.vector.verevcodex.presentation.theme.VerevColors

internal object BrandingCatalog {
    const val defaultPaletteId = "golden_hour"

    fun palettes(): List<BrandingPaletteUi> = listOf(
        BrandingPaletteUi(
            id = "golden_hour",
            nameRes = R.string.merchant_branding_palette_golden_hour,
            primary = VerevColors.Gold,
            secondary = VerevColors.Moss,
            accent = VerevColors.Tan,
        ),
        BrandingPaletteUi(
            id = "forest_mark",
            nameRes = R.string.merchant_branding_palette_forest_mark,
            primary = VerevColors.Forest,
            secondary = VerevColors.Moss,
            accent = VerevColors.Gold,
        ),
        BrandingPaletteUi(
            id = "espresso",
            nameRes = R.string.merchant_branding_palette_espresso,
            primary = VerevColors.Tan,
            secondary = VerevColors.Forest,
            accent = VerevColors.Gold,
        ),
    )
}

internal fun defaultBrandingSettings(
    storeId: String,
    primaryColor: String,
    secondaryColor: String,
) = BrandingSettings(
    storeId = storeId,
    selectedPaletteId = BrandingCatalog.defaultPaletteId,
    themeMode = ThemeMode.LIGHT,
    primaryColor = primaryColor,
    secondaryColor = secondaryColor,
    accentColor = secondaryColor,
)
