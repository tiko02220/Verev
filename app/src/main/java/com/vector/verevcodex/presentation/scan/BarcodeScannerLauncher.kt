package com.vector.verevcodex.presentation.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun rememberBarcodeScannerLauncher(
    onScanned: (String) -> Unit,
    onFailed: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_CODE_128)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    return remember(scanner, onScanned, onFailed) {
        {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val value = barcode.rawValue.orEmpty()
                    if (value.isBlank()) {
                        onFailed()
                    } else {
                        onScanned(value)
                    }
                }
                .addOnCanceledListener { }
                .addOnFailureListener { onFailed() }
        }
    }
}
