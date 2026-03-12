package com.vector.verevcodex.platform.nfc

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.nio.charset.StandardCharsets
import org.json.JSONObject

private const val LOYALTY_MIME_TYPE = "application/vnd.com.vector.verevcodex.loyalty"

data class NfcCardWriteRequest(
    val loyaltyId: String,
    val activationLink: String,
    val customerName: String,
)

enum class NfcCardWriteError {
    TAG_NOT_SUPPORTED,
    TAG_READ_ONLY,
    CAPACITY_TOO_SMALL,
    FORMAT_FAILED,
    WRITE_FAILED,
}

class NfcCardWriteException(val error: NfcCardWriteError) : IllegalStateException(error.name)

object NfcCardWriter {
    fun write(tag: Tag, request: NfcCardWriteRequest) {
        val message = createMessage(request)
        val encodedSize = message.toByteArray().size
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                if (!ndef.isWritable) throw NfcCardWriteException(NfcCardWriteError.TAG_READ_ONLY)
                if (ndef.maxSize < encodedSize) throw NfcCardWriteException(NfcCardWriteError.CAPACITY_TOO_SMALL)
                ndef.writeNdefMessage(message)
            } catch (exception: FormatException) {
                throw NfcCardWriteException(NfcCardWriteError.FORMAT_FAILED)
            } catch (exception: NfcCardWriteException) {
                throw exception
            } catch (exception: Exception) {
                throw NfcCardWriteException(NfcCardWriteError.WRITE_FAILED)
            } finally {
                runCatching { ndef.close() }
            }
            return
        }

        val formatable = NdefFormatable.get(tag) ?: throw NfcCardWriteException(NfcCardWriteError.TAG_NOT_SUPPORTED)
        try {
            formatable.connect()
            formatable.format(message)
        } catch (exception: Exception) {
            throw NfcCardWriteException(NfcCardWriteError.FORMAT_FAILED)
        } finally {
            runCatching { formatable.close() }
        }
    }

    private fun createMessage(request: NfcCardWriteRequest): NdefMessage {
        val memberPayload = JSONObject()
            .put("loyaltyId", request.loyaltyId)
            .put("customerName", request.customerName)
            .put("activationLink", request.activationLink)
            .toString()
            .toByteArray(StandardCharsets.UTF_8)

        return NdefMessage(
            arrayOf(
                NdefRecord.createUri(request.activationLink),
                NdefRecord.createMime(LOYALTY_MIME_TYPE, memberPayload),
            )
        )
    }
}
