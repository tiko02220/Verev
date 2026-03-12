package com.vector.verevcodex.presentation.scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.vector.verevcodex.MainActivity
import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec
import com.vector.verevcodex.R
import com.vector.verevcodex.platform.android.findActivity
import com.vector.verevcodex.presentation.theme.VerevColors
import java.util.concurrent.Executors

@Composable
internal fun EmbeddedBarcodeScanner(
    modifier: Modifier = Modifier,
    retryToken: Int,
    onScanned: (String) -> Unit,
    onFailed: (BarcodeScanFailureReason) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hostActivity = context.findActivity() as? MainActivity
    val cameraPermission = Manifest.permission.CAMERA
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) onFailed(BarcodeScanFailureReason.PERMISSION_DENIED)
    }

    if (!hasCameraPermission) {
        BarcodePermissionPlaceholder(
            modifier = modifier,
            onRequestPermission = {
                hostActivity?.suppressRelockForTransientSystemUi()
                permissionLauncher.launch(cameraPermission)
            },
        )
        return
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93,
                    Barcode.FORMAT_CODABAR,
                    Barcode.FORMAT_ITF,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                )
                .build()
        )
    }

    val analyzer = remember(barcodeScanner, onScanned, onFailed) {
        LoyaltyBarcodeAnalyzer(
            barcodeScanner = barcodeScanner,
            onScanned = onScanned,
            onFailed = onFailed,
        )
    }

    LaunchedEffect(retryToken) {
        analyzer.reset()
    }

    DisposableEffect(lifecycleOwner, previewView, analyzer) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(1280, 720),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                                ),
                            )
                            .build()
                    )
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(analyzerExecutor, analyzer) }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis,
                )
            }.onFailure {
                onFailed(BarcodeScanFailureReason.CAMERA_UNAVAILABLE)
            }
        }
        cameraProviderFuture.addListener(listener, mainExecutor)
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
            barcodeScanner.close()
            analyzerExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black),
        factory = { previewView },
    )
}

@Composable
private fun BarcodePermissionPlaceholder(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFEEF2F1)),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = VerevColors.Forest,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Text(
                text = stringResource(R.string.merchant_scan_camera_permission_title),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
            )
            Text(
                text = stringResource(R.string.merchant_scan_camera_permission_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp),
            )
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string.merchant_scan_camera_permission_action))
            }
        }
    }
}

private class LoyaltyBarcodeAnalyzer(
    private val barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    private val onScanned: (String) -> Unit,
    private val onFailed: (BarcodeScanFailureReason) -> Unit,
) : ImageAnalysis.Analyzer {
    @Volatile
    private var handlingFrame = false

    @Volatile
    private var resultDelivered = false

    @Volatile
    private var unsupportedReported = false

    fun reset() {
        handlingFrame = false
        resultDelivered = false
        unsupportedReported = false
    }

    override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
        if (resultDelivered || handlingFrame) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        handlingFrame = true
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                val matchingBarcode = barcodes.firstOrNull { barcode ->
                    val rawValue = barcode.rawValue.orEmpty()
                    if (rawValue.isBlank()) return@firstOrNull false
                    val normalized = LoyaltyIdCodec.normalize(rawValue)
                    barcode.format == Barcode.FORMAT_CODE_128 ||
                        normalized.startsWith("VRV-") ||
                        rawValue.startsWith("https://onebonus.app/card/", ignoreCase = true) ||
                        rawValue.startsWith("verev://card/", ignoreCase = true)
                }
                val rawValue = matchingBarcode?.rawValue.orEmpty()
                if (rawValue.isNotBlank()) {
                    resultDelivered = true
                    onScanned(rawValue)
                } else if (barcodes.isNotEmpty() && !unsupportedReported) {
                    unsupportedReported = true
                    onFailed(BarcodeScanFailureReason.UNSUPPORTED_CODE)
                }
            }
            .addOnFailureListener { onFailed(BarcodeScanFailureReason.GENERIC) }
            .addOnCompleteListener {
                handlingFrame = false
                imageProxy.close()
            }
    }
}
