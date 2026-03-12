package com.vector.verevcodex.presentation.settings

import androidx.compose.ui.graphics.Color
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.billing.BillingInvoice
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.model.billing.InvoiceStatus
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.model.billing.SubscriptionPlan
import com.vector.verevcodex.domain.model.settings.ThemeMode
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val LEGACY_PLAN_STARTER = "Starter"
private const val LEGACY_PLAN_BUSINESS_STANDARD = "Business Standard"
private const val LEGACY_PLAN_GROWTH_PLUS = "Growth Plus"

internal fun ThemeMode.toUi(): ThemeModeUi = when (this) {
    ThemeMode.LIGHT -> ThemeModeUi.LIGHT
    ThemeMode.DARK -> ThemeModeUi.DARK
    ThemeMode.AUTO -> ThemeModeUi.AUTO
}

internal fun ThemeModeUi.toDomain(): ThemeMode = when (this) {
    ThemeModeUi.LIGHT -> ThemeMode.LIGHT
    ThemeModeUi.DARK -> ThemeMode.DARK
    ThemeModeUi.AUTO -> ThemeMode.AUTO
}

internal fun SavedPaymentMethod.toUi() = PaymentMethodUi(
    id = id,
    brand = brand,
    last4 = last4,
    expiry = String.format(Locale.US, "%02d/%02d", expiryMonth, expiryYear % 100),
    isDefault = isDefault,
)

internal fun BillingInvoice.toUi() = BillingEntryUi(
    id = id,
    title = title,
    subtitle = periodLabel,
    amount = String.format(Locale.US, "$%.2f", amount),
    statusRes = when (status) {
        InvoiceStatus.PAID -> R.string.merchant_invoice_status_paid
        InvoiceStatus.DUE -> R.string.merchant_invoice_status_due
        InvoiceStatus.OVERDUE -> R.string.merchant_invoice_status_overdue
    },
)

internal fun BillingInvoice.amountLabel(): String = String.format(Locale.US, "$%.2f %s", amount, currencyCode)

internal fun BillingInvoice.issuedDateLabel(): String =
    issuedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

internal fun SubscriptionPlan?.priceLabel(): String =
    if (this == null) "-" else String.format(Locale.US, "$%.0f/mo", monthlyPrice)

internal fun SubscriptionPlan?.renewalLabel(): String =
    this?.renewalDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "-"

internal fun SubscriptionPlan?.nameLabel(): String = when (this?.name) {
    "starter", LEGACY_PLAN_STARTER -> "starter"
    "growth_plus", LEGACY_PLAN_GROWTH_PLUS -> "growth_plus"
    "business_standard", LEGACY_PLAN_BUSINESS_STANDARD -> "business_standard"
    else -> this?.name.orEmpty()
}

internal fun BrandingSettings?.themeModeUi(): ThemeModeUi = this?.themeMode?.toUi() ?: ThemeModeUi.LIGHT

internal fun Color.toHexString(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}
