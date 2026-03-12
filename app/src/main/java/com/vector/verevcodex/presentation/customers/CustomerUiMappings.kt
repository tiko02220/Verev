package com.vector.verevcodex.presentation.customers

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Stars
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.customer.CustomerActivity
import com.vector.verevcodex.domain.model.customer.CustomerActivityType
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors

internal data class CustomerTierColors(
    val background: Color,
    val content: Color,
)

internal fun LoyaltyTier.toUiColors(): CustomerTierColors = when (this) {
    LoyaltyTier.BRONZE -> CustomerTierColors(
        background = VerevColors.Tan.copy(alpha = 0.18f),
        content = VerevColors.Tan,
    )
    LoyaltyTier.SILVER -> CustomerTierColors(
        background = VerevColors.TierSilverContainer,
        content = VerevColors.TierSilverContent,
    )
    LoyaltyTier.GOLD -> CustomerTierColors(
        background = VerevColors.TierGoldContainer,
        content = VerevColors.Gold,
    )
    LoyaltyTier.VIP -> CustomerTierColors(
        background = VerevColors.TierVipContainer,
        content = VerevColors.TierVipContent,
    )
}

@StringRes
internal fun CustomerCredentialMethod.labelRes(): Int = when (this) {
    CustomerCredentialMethod.BARCODE_IMAGE -> R.string.merchant_customer_credential_barcode
    CustomerCredentialMethod.GOOGLE_WALLET -> R.string.merchant_customer_credential_wallet
    CustomerCredentialMethod.NFC_CARD -> R.string.merchant_customer_credential_nfc
}

@StringRes
internal fun CustomerCredentialStatus.labelRes(): Int = when (this) {
    CustomerCredentialStatus.AVAILABLE -> R.string.merchant_add_customer_method_available
    CustomerCredentialStatus.LINKED -> R.string.merchant_add_customer_method_linked
}

internal fun CustomerActivity.icon(): ImageVector = when (type) {
    CustomerActivityType.JOINED -> Icons.Default.Stars
    CustomerActivityType.TRANSACTION -> Icons.Default.History
    CustomerActivityType.POINTS_ADJUSTMENT -> if ((pointsDelta ?: 0) >= 0) Icons.Default.Stars else Icons.Default.RemoveCircle
    CustomerActivityType.DISCOUNT_APPLIED -> Icons.Default.LocalOffer
    CustomerActivityType.TIER_BENEFIT_RECORDED -> Icons.Default.Stars
    CustomerActivityType.NOTE_UPDATED -> Icons.Default.NoteAlt
    CustomerActivityType.TAGS_UPDATED -> Icons.Default.LocalOffer
}

internal fun CustomerActivity.subtitle(): String = buildString {
    amount?.let { append(formatWholeCurrency(it)) }
    pointsDelta?.let {
        if (isNotBlank()) append(" • ")
        append(if (it >= 0) "+" else "")
        append(it)
        append(" pts")
    }
    if (isNotBlank()) append(" • ")
    append(formatRelativeDateTime(timestamp))
}
