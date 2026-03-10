package com.vector.verevcodex.presentation.settings

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

data class BillingEntryUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val status: String,
)

data class BrandingPaletteUi(
    val id: String,
    val name: String,
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
