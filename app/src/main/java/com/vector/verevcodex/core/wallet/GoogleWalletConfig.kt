package com.vector.verevcodex.core.wallet

import com.vector.verevcodex.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleWalletConfig(
    val issuerEmail: String,
    val loyaltyClassId: String,
    val programName: String,
    val issuerName: String,
) {
    val isConfigured: Boolean = issuerEmail.isNotBlank() && loyaltyClassId.isNotBlank() && programName.isNotBlank() && issuerName.isNotBlank()
}

@Singleton
class GoogleWalletConfigProvider @Inject constructor() {
    fun get(): GoogleWalletConfig = GoogleWalletConfig(
        issuerEmail = BuildConfig.GOOGLE_WALLET_ISSUER_EMAIL,
        loyaltyClassId = BuildConfig.GOOGLE_WALLET_LOYALTY_CLASS_ID,
        programName = BuildConfig.GOOGLE_WALLET_PROGRAM_NAME,
        issuerName = BuildConfig.GOOGLE_WALLET_ISSUER_NAME,
    )
}
