package com.vector.verevcodex.presentation.customers

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.FileProvider
import com.vector.verevcodex.R
import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec
import java.io.File
import java.io.FileOutputStream

internal data class CustomerProvisioningSharePayload(
    val subject: String,
    val body: String,
    val copyValue: String,
    val attachmentUri: Uri? = null,
    val attachmentMimeType: String? = null,
)

internal fun buildProvisioningSharePayload(
    context: Context,
    customerName: String,
    loyaltyId: String,
    activationLink: String,
    option: CustomerCardProvisioningOption,
): CustomerProvisioningSharePayload {
    val safeName = customerName.ifBlank { context.getString(R.string.merchant_add_customer_title) }
    return when (option) {
        CustomerCardProvisioningOption.BARCODE_IMAGE -> {
            val barcodeBitmap = createBarcodeBitmap(loyaltyId, width = 960, height = 260)
            val barcodeUri = saveBarcodeBitmap(context, loyaltyId, barcodeBitmap)
            CustomerProvisioningSharePayload(
                subject = context.getString(R.string.merchant_add_customer_barcode_share_subject),
                body = context.getString(
                    R.string.merchant_add_customer_barcode_share_message,
                    safeName,
                    loyaltyId,
                    activationLink,
                ),
                copyValue = loyaltyId,
                attachmentUri = barcodeUri,
                attachmentMimeType = "image/png",
            )
        }
        CustomerCardProvisioningOption.GOOGLE_WALLET -> {
            val walletInvite = LoyaltyIdCodec.walletInviteUrl(loyaltyId)
            val qrBitmap = createQrBitmap(walletInvite, size = 1024)
            val qrUri = saveImageBitmap(context, "wallet_invite_$loyaltyId", qrBitmap)
            CustomerProvisioningSharePayload(
                subject = context.getString(R.string.merchant_add_customer_wallet_share_subject),
                body = context.getString(
                    R.string.merchant_add_customer_wallet_share_message,
                    safeName,
                    walletInvite,
                ),
                copyValue = walletInvite,
                attachmentUri = qrUri,
                attachmentMimeType = "image/png",
            )
        }
        CustomerCardProvisioningOption.NFC_CARD -> CustomerProvisioningSharePayload(
            subject = context.getString(R.string.merchant_add_customer_nfc_share_subject),
            body = context.getString(
                R.string.merchant_add_customer_nfc_share_message,
                safeName,
                activationLink,
            ),
            copyValue = activationLink,
        )
    }
}

internal fun shareProvisioningPayload(
    context: Context,
    payload: CustomerProvisioningSharePayload,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = payload.attachmentMimeType ?: "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, payload.subject)
        putExtra(Intent.EXTRA_TEXT, payload.body)
        payload.attachmentUri?.let { uri ->
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.merchant_add_customer_share_chooser)))
}

internal fun emailProvisioningPayload(
    context: Context,
    payload: CustomerProvisioningSharePayload,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = payload.attachmentMimeType ?: "message/rfc822"
        putExtra(Intent.EXTRA_SUBJECT, payload.subject)
        putExtra(Intent.EXTRA_TEXT, payload.body)
        payload.attachmentUri?.let { uri ->
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.merchant_add_customer_share_email)))
}

internal fun smsProvisioningPayload(
    context: Context,
    payload: CustomerProvisioningSharePayload,
) {
    if (payload.attachmentUri != null) {
        val smsPackage = Telephony.Sms.getDefaultSmsPackage(context)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = payload.attachmentMimeType ?: "image/png"
            putExtra(Intent.EXTRA_TEXT, payload.body)
            putExtra(Intent.EXTRA_STREAM, payload.attachmentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (!smsPackage.isNullOrBlank()) {
                setPackage(smsPackage)
            }
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.merchant_add_customer_share_sms)))
    } else {
        context.startActivity(
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", payload.body)
            }
        )
    }
}

private fun saveImageBitmap(
    context: Context,
    fileName: String,
    bitmap: Bitmap,
): Uri {
    val outputDir = File(context.cacheDir, "shared_barcodes").apply { mkdirs() }
    val outputFile = File(outputDir, "$fileName.png")
    FileOutputStream(outputFile).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outputFile,
    )
}

private fun saveBarcodeBitmap(
    context: Context,
    loyaltyId: String,
    bitmap: Bitmap,
): Uri = saveImageBitmap(context, "barcode_$loyaltyId", bitmap)
