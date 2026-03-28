package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

enum class ThemeModeUi {
    LIGHT,
    DARK,
    AUTO,
}

enum class PrivacyTabUi {
    PRIVACY,
    TERMS,
}

data class PaymentMethodUi(
    val id: String,
    val brand: String,
    val last4: String,
    val expiry: String,
    val isDefault: Boolean,
)

internal data class SettingsMenuGroup(
    val title: String,
    val subtitle: String? = null,
    val items: List<SettingsMenuItem>,
)

data class BillingEntryUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    @StringRes val statusRes: Int,
)

data class BrandingPaletteUi(
    val id: String,
    @StringRes val nameRes: Int,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
)

data class PrivacySectionUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val bullets: List<String>,
    val accentColors: List<Color>,
)
