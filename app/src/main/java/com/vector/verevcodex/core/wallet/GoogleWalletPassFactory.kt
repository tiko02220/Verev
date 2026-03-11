package com.vector.verevcodex.core.wallet

import com.vector.verevcodex.core.identifiers.LoyaltyIdCodec
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

enum class WalletBarcodeFormat {
    CODE_128,
}

data class GoogleWalletPassRequest(
    val objectId: String,
    val barcodeValue: String,
    val passJson: String,
)

@Singleton
class GoogleWalletPassFactory @Inject constructor(
    private val configProvider: GoogleWalletConfigProvider,
) {
    fun getConfig(): GoogleWalletConfig = configProvider.get()

    fun createLoyaltyPass(
        loyaltyId: String,
        customerName: String,
        currentPoints: Int,
        activationLink: String,
        barcodeFormat: WalletBarcodeFormat = WalletBarcodeFormat.CODE_128,
    ): GoogleWalletPassRequest? {
        val config = configProvider.get()
        if (!config.isConfigured) return null

        val normalizedId = LoyaltyIdCodec.normalize(loyaltyId)
        val objectId = "${config.loyaltyClassId.substringBeforeLast('.')}.${sanitizeIdentifier(normalizedId)}"
        val loyaltyObject = JSONObject()
            .put("id", objectId)
            .put("classId", config.loyaltyClassId)
            .put("state", "ACTIVE")
            .put("accountId", normalizedId)
            .put("accountName", customerName)
            .put(
                "barcode",
                JSONObject()
                    .put("type", barcodeFormat.name)
                    .put("value", normalizedId)
                    .put("alternateText", normalizedId),
            )
            .put(
                "linksModuleData",
                JSONObject().put(
                    "uris",
                    JSONArray().put(
                        JSONObject()
                            .put("uri", activationLink)
                            .put("description", config.programName)
                    )
                )
            )
            .put(
                "loyaltyPoints",
                JSONObject()
                    .put("label", config.programName)
                    .put(
                        "balance",
                        JSONObject().put("int", currentPoints),
                    )
            )

        val payload = JSONObject()
            .put("loyaltyObjects", JSONArray().put(loyaltyObject))

        val savePayload = JSONObject()
            .put("iss", config.issuerEmail)
            .put("aud", "google")
            .put("origins", JSONArray())
            .put("typ", "savetowallet")
            .put("payload", payload)

        return GoogleWalletPassRequest(
            objectId = objectId,
            barcodeValue = normalizedId,
            passJson = savePayload.toString(),
        )
    }

    private fun sanitizeIdentifier(value: String): String = value
        .lowercase(Locale.US)
        .replace("[^a-z0-9._-]".toRegex(), "-")
}
