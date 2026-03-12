package com.vector.verevcodex.platform.nfc

import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec
import java.nio.charset.Charset
import org.json.JSONObject

object NfcCardPayloadParser {
    fun extractLoyaltyId(intent: Intent): String? {
        val ndefMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            ?.mapNotNull { it as? NdefMessage }
            .orEmpty()

        val fromPayload = ndefMessages
            .flatMap { it.records.asList() }
            .mapNotNull(::parseRecord)
            .map(LoyaltyIdCodec::normalize)
            .firstOrNull { it.isNotBlank() }

        if (!fromPayload.isNullOrBlank()) return fromPayload

        return intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
            ?.joinToString(separator = "") { byte -> "%02X".format(byte) }
            ?.let(LoyaltyIdCodec::normalize)
            ?.takeIf { it.isNotBlank() }
    }

    private fun parseRecord(record: NdefRecord): String? {
        return when {
            record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT) -> {
                parseStructuredPayload(parseTextRecord(record))
            }
            record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_URI) -> {
                record.toUri()?.let(::parseUri)
            }
            record.tnf == NdefRecord.TNF_MIME_MEDIA || record.tnf == NdefRecord.TNF_EXTERNAL_TYPE -> {
                parseStructuredPayload(record.payload.toString(Charset.forName("UTF-8")))
            }
            else -> null
        }
    }

    private fun parseTextRecord(record: NdefRecord): String? {
        val payload = record.payload
        if (payload.isEmpty()) return null
        val languageCodeLength = payload.first().toInt() and 0x3F
        return payload.decodeToString(
            startIndex = 1 + languageCodeLength,
            endIndex = payload.size,
        )
    }

    private fun parseStructuredPayload(value: String?): String? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isBlank()) return null
        if (!normalized.startsWith("{")) return normalized
        return runCatching {
            val payload = JSONObject(normalized)
            payload.optString("loyaltyId")
                .ifBlank { payload.optString("id") }
                .ifBlank { payload.optString("activationLink") }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun parseUri(uri: Uri): String {
        val direct = uri.getQueryParameter("id")
        if (!direct.isNullOrBlank()) return direct
        return uri.lastPathSegment.orEmpty()
    }
}
