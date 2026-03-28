package com.vector.verevcodex.presentation.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.vector.verevcodex.MainActivity
import com.vector.verevcodex.R
import com.vector.verevcodex.common.input.sanitizeDecimalInput
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.platform.android.findActivity
import com.vector.verevcodex.presentation.common.sheets.AppBottomSheetDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun ActiveScanSurface(
    contentPadding: PaddingValues,
    activeMethod: ScanMethod,
    scanSessionToken: Int,
    errorRes: Int?,
    messageRes: Int?,
    onSelectMethod: (ScanMethod) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onBarcodeFailed: (BarcodeScanFailureReason) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)),
    ) {
        ScanRouteHeader(
            title = stringResource(R.string.merchant_scan_title),
            onBack = onCancel,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (activeMethod == ScanMethod.BARCODE) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                ) {
                    EmbeddedBarcodeScanner(
                        modifier = Modifier.fillMaxSize(),
                        retryToken = scanSessionToken,
                        onScanned = onBarcodeScanned,
                        onFailed = onBarcodeFailed,
                    )
                }
            } else {
                ActiveScanAnimationCard(activeMethod = activeMethod)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.merchant_scan_scanning_title),
                style = MaterialTheme.typography.headlineMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = when (activeMethod) {
                    ScanMethod.NFC -> stringResource(R.string.merchant_scan_scanning_subtitle)
                    ScanMethod.BARCODE -> stringResource(R.string.merchant_scan_waiting_barcode_subtitle)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF4B5565),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            ScanProgressSegments()
            if (errorRes != null) {
                Spacer(Modifier.height(18.dp))
                ScanInlineErrorCard(
                    messageRes = errorRes,
                    onRetry = onRetry,
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_scan_method_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF667085),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ActiveScanMethodCard(
                        title = stringResource(R.string.merchant_scan_tab_nfc_reader),
                        icon = Icons.Default.Nfc,
                        selected = activeMethod == ScanMethod.NFC,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectMethod(ScanMethod.NFC) },
                    )
                    ActiveScanMethodCard(
                        title = stringResource(R.string.merchant_scan_tab_camera_scanner),
                        icon = Icons.Default.CameraAlt,
                        selected = activeMethod == ScanMethod.BARCODE,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectMethod(ScanMethod.BARCODE) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ScanRouteHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(VerevColors.ForestDeep, VerevColors.Forest)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.16f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.auth_back),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(R.string.auth_back),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun ScanCenteredStatusSurface(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4B5565),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ActiveScanAnimationCard(activeMethod: ScanMethod) {
    val transition = rememberInfiniteTransition(label = "active_scan")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "active_scan_pulse",
    )

    Box(
        modifier = Modifier
            .size(208.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Brush.verticalGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
            .scale(pulse),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(Color.White.copy(alpha = 0.18f)),
        )
        Icon(
            imageVector = if (activeMethod == ScanMethod.NFC) Icons.Default.Nfc else Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(58.dp),
        )
    }
}

@Composable
private fun ActiveScanMethodCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (selected) Color(0xFFECCB8F) else Color(0xFFF5F5F5)
    val iconTint = if (selected) Color.White else Color(0xFF97A0A9)
    val titleColor = if (selected) Color.White else VerevColors.Forest.copy(alpha = 0.72f)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = background,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = if (selected) 0.22f else 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ScanInlineErrorCard(
    @StringRes messageRes: Int,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFEEDEC),
        border = BorderStroke(1.dp, Color(0xFFF5B4AA)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB42318),
                textAlign = TextAlign.Center,
            )
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFE37B6D)),
            ) {
                Text(
                    text = stringResource(R.string.merchant_scan_retry_action),
                    color = Color(0xFFB42318),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ScanProgressSegments() {
    val transition = rememberInfiniteTransition(label = "scan_progress")
    val pulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scan_progress_pulse",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            val width = if (index == 2) 10.dp else 32.dp
            val height = if (index == 2) 8.dp else 10.dp
            val alpha = when (index) {
                0, 4 -> 0.42f
                1, 3 -> 0.75f
                else -> 1f
            }
            Box(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(999.dp))
                    .background(VerevColors.Gold.copy(alpha = alpha * pulse)),
            )
        }
    }
}


@Composable
internal fun ScanHeroCard(
    storeName: String,
    activeMethod: ScanMethod?,
    hasSavedMethod: Boolean,
    onStartScan: () -> Unit,
    onChooseDifferentMethod: () -> Unit,
) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (activeMethod == ScanMethod.BARCODE) Icons.Default.CameraAlt else Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_scan_intro_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_scan_intro_subtitle, storeName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.66f),
                )
            }
        }

        Text(
            text = when (activeMethod) {
                ScanMethod.NFC -> stringResource(R.string.merchant_scan_mode_nfc_subtitle)
                ScanMethod.BARCODE -> stringResource(R.string.merchant_scan_mode_barcode_subtitle)
                null -> stringResource(R.string.merchant_scan_prompt_subtitle)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.72f),
        )

        Button(
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerevColors.Forest,
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.merchant_scan_start_scan),
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Medium,
            )
        }

        if (hasSavedMethod) {
            OutlinedButton(
                onClick = onChooseDifferentMethod,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VerevColors.Forest),
            ) {
                Text(
                    text = stringResource(R.string.merchant_scan_change_saved_method),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
internal fun ScanCompactMethodCard(
    activeMethod: ScanMethod?,
    onStartScan: () -> Unit,
    onChooseDifferentMethod: () -> Unit,
) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            if (activeMethod == ScanMethod.BARCODE) {
                                listOf(VerevColors.Gold, VerevColors.Tan)
                            } else {
                                listOf(VerevColors.Moss, VerevColors.Forest)
                            },
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (activeMethod == ScanMethod.BARCODE) Icons.Default.CameraAlt else Icons.Default.Nfc,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = when (activeMethod) {
                        ScanMethod.BARCODE -> stringResource(R.string.merchant_scan_mode_barcode_title)
                        ScanMethod.NFC -> stringResource(R.string.merchant_scan_mode_nfc_title)
                        null -> stringResource(R.string.merchant_scan_title)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_scan_compact_method_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onChooseDifferentMethod,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.merchant_scan_change_saved_method))
            }
            Button(
                onClick = onStartScan,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Forest,
                    contentColor = Color.White,
                ),
            ) {
                Text(stringResource(R.string.merchant_scan_scan_another))
            }
        }
    }
}

@Composable
internal fun ScanMethodSheet(
    rememberChoice: Boolean,
    onRememberChoiceChanged: (Boolean) -> Unit,
    onSelectMethod: (ScanMethod) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val hostActivity = context.findActivity() as? MainActivity

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            onSelectMethod(ScanMethod.BARCODE)
        }
    }

    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
    ) { dismiss, dismissAfter ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
                        Text(
                            text = stringResource(R.string.merchant_scan_choose_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.merchant_scan_choose_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.Forest.copy(alpha = 0.66f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            ScanMethodChoiceCard(
                                title = stringResource(R.string.merchant_scan_sheet_nfc_title),
                                subtitle = stringResource(R.string.merchant_scan_sheet_nfc_subtitle),
                                icon = Icons.Default.Nfc,
                                colors = listOf(Color.White, Color(0xFFF7F7F7)),
                                modifier = Modifier.weight(1f),
                                onClick = { dismissAfter { onSelectMethod(ScanMethod.NFC) } },
                            )
                            ScanMethodChoiceCard(
                                title = stringResource(R.string.merchant_scan_sheet_barcode_title),
                                subtitle = stringResource(R.string.merchant_scan_sheet_barcode_subtitle),
                                icon = Icons.Default.CameraAlt,
                                colors = listOf(Color.White, Color(0xFFF7F7F7)),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val permissionState = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA,
                                )
                                if (permissionState == PackageManager.PERMISSION_GRANTED) {
                                        dismissAfter { onSelectMethod(ScanMethod.BARCODE) }
                                    } else {
                                        hostActivity?.suppressRelockForTransientSystemUi()
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = rememberChoice,
                                onCheckedChange = onRememberChoiceChanged,
                            )
                            Text(
                                text = stringResource(R.string.merchant_scan_remember_choice),
                                style = MaterialTheme.typography.bodyMedium,
                                color = VerevColors.Forest,
                            )
                        }
                        OutlinedButton(
                            onClick = dismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.auth_cancel),
                                style = MaterialTheme.typography.titleMedium,
                                color = VerevColors.Forest.copy(alpha = 0.84f),
                            )
                        }
        }
    }
}

@Composable
private fun ScanMethodChoiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    iconTint: Color = VerevColors.Forest,
    textColor: Color = VerevColors.Forest,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(VerevColors.AppBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(30.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun ScanStatusCard(
    activeMethod: ScanMethod?,
    scannedLoyaltyId: String?,
    isNfcListening: Boolean,
) {
    val transition = rememberInfiniteTransition(label = "nfc_listening")
    val outerScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nfc_outer_scale",
    )
    val innerScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nfc_inner_scale",
    )

    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
        if (isNfcListening) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .clip(RoundedCornerShape(44.dp))
                        .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
                        .padding(top = 30.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(94.dp)
                            .scale(outerScale),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(4.dp, Color.White.copy(alpha = 0.95f), CircleShape),
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.merchant_scan_scanning_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.merchant_scan_scanning_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest.copy(alpha = 0.76f),
                    textAlign = TextAlign.Center,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(5) { index ->
                        val segmentWidth = if (index == 2) 14.dp else 42.dp
                        val alpha = when (index) {
                            0, 4 -> 0.45f
                            1, 3 -> 0.78f
                            else -> 1f
                        }
                        Box(
                            modifier = Modifier
                                .width(segmentWidth)
                                .height(if (index == 2) 10.dp else 12.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(VerevColors.Gold.copy(alpha = alpha * innerScale)),
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(VerevColors.AppBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (activeMethod == ScanMethod.BARCODE) Icons.Default.CameraAlt else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = VerevColors.Forest,
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = when (activeMethod) {
                            ScanMethod.NFC -> stringResource(R.string.merchant_scan_waiting_nfc_title)
                            ScanMethod.BARCODE -> stringResource(R.string.merchant_scan_waiting_barcode_title)
                            null -> stringResource(R.string.merchant_scan_waiting_generic_title)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = scannedLoyaltyId?.let {
                            stringResource(R.string.merchant_scan_last_id, it)
                        } ?: when (activeMethod) {
                            ScanMethod.NFC -> stringResource(R.string.merchant_scan_waiting_nfc_subtitle)
                            ScanMethod.BARCODE -> stringResource(R.string.merchant_scan_waiting_barcode_subtitle)
                            null -> stringResource(R.string.merchant_scan_waiting_generic_subtitle)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.62f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun ScanCustomerCard(
    customer: Customer,
    showTier: Boolean,
    rewardHighlights: List<CustomerBonusAction>,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = listOf(customer.firstName, customer.lastName).filter { it.isNotBlank() }.joinToString(" "),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = customer.email.ifBlank { customer.phoneNumber },
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                )
            }
            if (showTier) {
                CustomerTierPill(
                    tier = customer.loyaltyTier,
                    label = customer.loyaltyTierLabel.ifBlank {
                        customer.loyaltyTier.name.lowercase().replaceFirstChar { it.uppercase() }
                    },
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = VerevColors.AppBackground,
        ) {
            Text(
                text = stringResource(R.string.merchant_scan_loyalty_id, customer.loyaltyId),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.74f),
                fontWeight = FontWeight.Medium,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ScanMetric(
                label = stringResource(R.string.merchant_metric_points),
                value = formatCompactCount(customer.currentPoints),
                modifier = Modifier.weight(1f),
            )
            ScanMetric(
                label = stringResource(R.string.merchant_metric_visits),
                value = formatCompactCount(customer.totalVisits),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ScanMetric(
                label = stringResource(R.string.merchant_metric_spent),
                value = formatWholeCurrency(customer.totalSpent),
                modifier = Modifier.weight(1f),
            )
            ScanMetric(
                label = stringResource(R.string.merchant_customer_last_visit_short),
                value = formatRelativeDateTime(customer.lastVisit),
                modifier = Modifier.weight(1f),
            )
        }
        if (rewardHighlights.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = VerevColors.Gold.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, VerevColors.Gold.copy(alpha = 0.18f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VerevColors.Gold.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = VerevColors.Gold,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.merchant_scan_rewards_ready_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = VerevColors.Forest,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.merchant_scan_rewards_ready_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = VerevColors.Forest.copy(alpha = 0.64f),
                            )
                        }
                    }
                    rewardHighlights.forEach { reward ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = reward.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VerevColors.Forest,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (reward.details.isNotBlank()) {
                                    Text(
                                        text = reward.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VerevColors.Forest.copy(alpha = 0.68f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
internal fun ScanActionComposerCard(
    activePrograms: List<RewardProgram>,
    availableActions: List<RewardProgramScanAction>,
    selectedAction: RewardProgramScanAction?,
    amount: String,
    points: String,
    customerPoints: Int,
    fieldErrors: Map<String, Int>,
    isSubmitting: Boolean,
    onAmountChanged: (String) -> Unit,
    onPointsChanged: (String) -> Unit,
    onActionSelected: (RewardProgramScanAction) -> Unit,
    onApply: () -> Unit,
    onClose: () -> Unit,
    onScanAnother: () -> Unit,
    onOpenPrograms: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
        if (availableActions.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = VerevColors.AppBackground,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(VerevColors.Gold.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = VerevColors.Gold,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.merchant_scan_no_actions_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = VerevColors.Forest,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.merchant_scan_no_actions_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = VerevColors.Forest.copy(alpha = 0.66f),
                            )
                        }
                    }
                    Button(
                        onClick = onOpenPrograms,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerevColors.Forest,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(stringResource(R.string.merchant_scan_open_programs))
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = VerevColors.Moss.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.merchant_scan_action_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.merchant_scan_action_panel_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.66f),
                    )
                }
            }
            ScanActionChips(
                actions = availableActions,
                selectedAction = selectedAction,
                onActionSelected = onActionSelected,
            )
            selectedAction?.let { action ->
                ScanSelectedActionContent(
                    activePrograms = activePrograms,
                    action = action,
                    amount = amount,
                    points = points,
                    customerPoints = customerPoints,
                    fieldErrors = fieldErrors,
                    onAmountChanged = onAmountChanged,
                    onPointsChanged = onPointsChanged,
                )
            }
        }
        ScanPrimaryActions(
            isSubmitting = isSubmitting,
            hasAction = selectedAction != null,
            onApply = onApply,
            onClose = onClose,
            onScanAnother = onScanAnother,
        )
        }
    }
}

@Composable
private fun ScanSelectedActionContent(
    activePrograms: List<RewardProgram>,
    action: RewardProgramScanAction,
    amount: String,
    points: String,
    customerPoints: Int,
    fieldErrors: Map<String, Int>,
    onAmountChanged: (String) -> Unit,
    onPointsChanged: (String) -> Unit,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = VerevColors.AppBackground) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = action.label(),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = action.supportingText(
                    activePrograms = activePrograms,
                    customerPoints = customerPoints,
                    amount = amount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
            when (action) {
                RewardProgramScanAction.EARN_POINTS -> {
                    ScanAmountField(
                        amount = amount,
                        onAmountChanged = onAmountChanged,
                        label = stringResource(R.string.merchant_scan_amount_label),
                        errorRes = fieldErrors[SCAN_FIELD_AMOUNT],
                    )
                }
                RewardProgramScanAction.REDEEM_REWARDS -> {
                    MerchantFormField(
                        value = points,
                        onValueChange = onPointsChanged,
                        label = stringResource(R.string.merchant_scan_points_label),
                        leadingIcon = Icons.Default.CreditCard,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        ),
                        isError = fieldErrors[SCAN_FIELD_POINTS] != null,
                        errorText = fieldErrors[SCAN_FIELD_POINTS]?.let { stringResource(it) },
                    )
                }
                RewardProgramScanAction.CHECK_IN -> Unit
                RewardProgramScanAction.APPLY_CASHBACK -> {
                    ScanAmountField(
                        amount = amount,
                        onAmountChanged = onAmountChanged,
                        label = stringResource(R.string.merchant_scan_cashback_amount_label),
                        errorRes = fieldErrors[SCAN_FIELD_AMOUNT],
                    )
                }
                RewardProgramScanAction.TRACK_TIER_PROGRESS -> Unit
            }
        }
    }
}

@Composable
private fun ScanAmountField(
    amount: String,
    onAmountChanged: (String) -> Unit,
    label: String,
    errorRes: Int?,
) {
    MerchantFormField(
        value = amount,
        onValueChange = { onAmountChanged(sanitizeDecimalInput(it)) },
        label = label,
        leadingIcon = Icons.Default.CreditCard,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
        ),
        isError = errorRes != null,
        errorText = errorRes?.let { stringResource(it) },
    )
}

@Composable
internal fun ScanActionChips(
    actions: List<RewardProgramScanAction>,
    selectedAction: RewardProgramScanAction?,
    onActionSelected: (RewardProgramScanAction) -> Unit,
) {
    if (actions.isEmpty()) {
        Text(
            text = stringResource(R.string.merchant_scan_no_actions_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.64f),
        )
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(actions) { action ->
            MerchantFilterChip(
                text = action.label(),
                selected = selectedAction == action,
                onClick = { onActionSelected(action) },
            )
        }
    }
}

@Composable
internal fun ScanPrimaryActions(
    isSubmitting: Boolean,
    hasAction: Boolean,
    onApply: () -> Unit,
    onClose: () -> Unit,
    onScanAnother: () -> Unit,
) {
    Button(
        onClick = onApply,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isSubmitting && hasAction,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
    ) {
        Text(stringResource(R.string.merchant_scan_submit_action))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onScanAnother,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
        ) {
            Text(stringResource(R.string.merchant_scan_scan_another))
        }
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(stringResource(R.string.merchant_close))
        }
    }
}

@Composable
private fun RewardProgramScanAction.label(): String = when (this) {
    RewardProgramScanAction.EARN_POINTS -> stringResource(R.string.merchant_scan_action_add_points)
    RewardProgramScanAction.REDEEM_REWARDS -> stringResource(R.string.merchant_scan_action_redeem_points)
    RewardProgramScanAction.CHECK_IN -> stringResource(R.string.merchant_scan_action_check_in)
    RewardProgramScanAction.APPLY_CASHBACK -> stringResource(R.string.merchant_scan_action_apply_cashback)
    RewardProgramScanAction.TRACK_TIER_PROGRESS -> stringResource(R.string.merchant_scan_action_track_tier)
}

@Composable
private fun RewardProgramScanAction.supportingText(
    activePrograms: List<RewardProgram>,
    customerPoints: Int,
    amount: String,
): String = when (this) {
    RewardProgramScanAction.EARN_POINTS ->
        stringResource(R.string.merchant_scan_points_preview, activePrograms.calculateEarnedPoints(amount.toDoubleOrNull() ?: 0.0))
    RewardProgramScanAction.REDEEM_REWARDS ->
        stringResource(R.string.merchant_scan_redeem_supporting, customerPoints)
    RewardProgramScanAction.CHECK_IN ->
        stringResource(R.string.merchant_scan_check_in_configured_supporting, activePrograms.checkInRewardSummary())
    RewardProgramScanAction.APPLY_CASHBACK ->
        stringResource(R.string.merchant_scan_cashback_preview, activePrograms.calculateCashbackCredit(amount.toDoubleOrNull() ?: 0.0))
    RewardProgramScanAction.TRACK_TIER_PROGRESS ->
        stringResource(R.string.merchant_scan_tier_progress_supporting)
}

@Composable
internal fun ScanInlineBanner(title: String, subtitle: String, positive: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(22.dp),
        color = if (positive) Color(0xFFEFF7F1) else Color(0xFFFEEDEC),
        border = BorderStroke(
            1.dp,
            if (positive) Color(0xFFC7E6D0) else Color(0xFFF5B4AA),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (positive) VerevColors.Moss.copy(alpha = 0.14f) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (positive) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = if (positive) VerevColors.Moss else Color(0xFFB91C1C),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (positive) VerevColors.Forest else Color(0xFF7F1D1D),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (positive) {
                        VerevColors.Forest.copy(alpha = 0.64f)
                    } else {
                        Color(0xFF7F1D1D).copy(alpha = 0.82f)
                    },
                )
            }
        }
        }
    }
}

@Composable
private fun ScanMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VerevColors.AppBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.5f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CustomerTierPill(
    tier: LoyaltyTier,
    label: String,
) {
    val (backgroundColor, contentColor) = when (tier) {
        LoyaltyTier.BRONZE -> VerevColors.Tan.copy(alpha = 0.2f) to VerevColors.Tan
        LoyaltyTier.SILVER -> Color(0xFFE5E7EB) to Color(0xFF6B7280)
        LoyaltyTier.GOLD -> VerevColors.Gold.copy(alpha = 0.18f) to VerevColors.Gold
        LoyaltyTier.VIP -> VerevColors.Forest.copy(alpha = 0.14f) to VerevColors.Forest
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
