package com.vector.verevcodex.common.identifiers

import java.security.SecureRandom

object LoyaltyIdCodec {
    private const val PREFIX = "VRV"
    private const val ACTIVATION_BASE_URL = "https://onebonus.app/card/"
    private const val WALLET_CHANNEL_PARAM = "?channel=wallet"
    private val secureRandom = SecureRandom()
    private val alphabet = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray()

    fun generate(): String = "$PREFIX-${encodeBase32(randomBytes(12))}"

    fun activationUrl(loyaltyId: String): String = "$ACTIVATION_BASE_URL${normalize(loyaltyId)}"

    fun walletInviteUrl(loyaltyId: String): String = "${activationUrl(loyaltyId)}$WALLET_CHANNEL_PARAM"

    fun normalize(rawValue: String): String {
        val trimmed = rawValue.trim()
        if (trimmed.isBlank()) return ""

        val fromUrl = when {
            trimmed.startsWith(ACTIVATION_BASE_URL, ignoreCase = true) ->
                trimmed.substringAfter(ACTIVATION_BASE_URL).substringBefore("?")
            trimmed.startsWith("verev://card/", ignoreCase = true) ->
                trimmed.substringAfter("verev://card/").substringBefore("?")
            else -> null
        }

        return (fromUrl ?: trimmed)
            .trim()
            .replace(" ", "")
            .uppercase()
    }

    private fun randomBytes(size: Int): ByteArray = ByteArray(size).also(secureRandom::nextBytes)

    private fun encodeBase32(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        val output = StringBuilder((bytes.size * 8 + 4) / 5)
        var buffer = 0
        var bitsLeft = 0
        for (byte in bytes) {
            buffer = (buffer shl 8) or (byte.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                val index = (buffer shr (bitsLeft - 5)) and 0x1F
                bitsLeft -= 5
                output.append(alphabet[index])
            }
        }
        if (bitsLeft > 0) {
            val index = (buffer shl (5 - bitsLeft)) and 0x1F
            output.append(alphabet[index])
        }
        return output.toString()
    }
}
