package com.vector.verevcodex.presentation.settings.branding

import com.vector.verevcodex.presentation.settings.*

import com.vector.verevcodex.R
import androidx.compose.ui.graphics.Color
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.model.settings.ThemeMode
import com.vector.verevcodex.presentation.theme.VerevColors

internal object BrandingCatalog {
    const val defaultPaletteId = "golden_hour"
    const val customPaletteId = "custom"

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
            nameRes = R.string.merchant_branding_palette_ocean_blue,
            primary = Color(0xFF3B82F6),
            secondary = Color(0xFF0EA5E9),
            accent = Color(0xFF06B6D4),
        ),
        BrandingPaletteUi(
            id = "espresso",
            nameRes = R.string.merchant_branding_palette_forest_green,
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            accent = Color(0xFF047857),
        ),
        BrandingPaletteUi(
            id = "royal_purple",
            nameRes = R.string.merchant_branding_palette_royal_purple,
            primary = Color(0xFF8B5CF6),
            secondary = Color(0xFFA78BFA),
            accent = Color(0xFF7C3AED),
        ),
        BrandingPaletteUi(
            id = "sunset_orange",
            nameRes = R.string.merchant_branding_palette_sunset_orange,
            primary = Color(0xFFF97316),
            secondary = Color(0xFFFB923C),
            accent = Color(0xFFEA580C),
        ),
        BrandingPaletteUi(
            id = "rose_garden",
            nameRes = R.string.merchant_branding_palette_rose_garden,
            primary = Color(0xFFEC4899),
            secondary = Color(0xFFF472B6),
            accent = Color(0xFFDB2777),
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
    logoUri = "",
)
