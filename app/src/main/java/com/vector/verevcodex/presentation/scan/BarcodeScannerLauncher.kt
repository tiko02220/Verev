package com.vector.verevcodex.presentation.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec

@Composable
fun rememberBarcodeScannerLauncher(
    onScanned: (String) -> Unit,
    onFailed: (BarcodeScanFailureReason) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_PDF417,
            )
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    return remember(scanner, onScanned, onFailed) {
        {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val value = barcode.rawValue.orEmpty()
                    val normalized = LoyaltyIdCodec.normalize(value)
                    if (normalized.isBlank()) {
                        onFailed(BarcodeScanFailureReason.UNSUPPORTED_CODE)
                    } else {
                        onScanned(value)
                    }
                }
                .addOnCanceledListener { onFailed(BarcodeScanFailureReason.CANCELLED) }
                .addOnFailureListener { onFailed(BarcodeScanFailureReason.GENERIC) }
        }
    }
}
