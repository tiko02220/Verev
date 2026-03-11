package com.vector.verevcodex.presentation.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.LoyaltyTier
import com.vector.verevcodex.domain.model.RewardProgramScanAction
import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun ActiveScanSurface(
    contentPadding: PaddingValues,
    activeMethod: ScanMethod,
    errorRes: Int?,
    messageRes: Int?,
    onSelectMethod: (ScanMethod) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onBarcodeFailed: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(contentPadding.calculateTopPadding() + 112.dp)
                .background(Brush.verticalGradient(listOf(VerevColors.ForestDeep, VerevColors.Forest))),
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 10.dp,
                )
                .zIndex(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                onClick = onCancel,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.16f),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.auth_back),
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Text(
                text = stringResource(R.string.auth_back),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding() + 112.dp))
            if (activeMethod == ScanMethod.BARCODE) {
                EmbeddedBarcodeScanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(368.dp),
                    onScanned = onBarcodeScanned,
                    onFailed = onBarcodeFailed,
                )
            } else {
                ActiveScanAnimationCard(activeMethod = activeMethod)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.merchant_scan_scanning_title),
                style = MaterialTheme.typography.headlineLarge,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = when (activeMethod) {
                    ScanMethod.NFC -> stringResource(R.string.merchant_scan_scanning_subtitle)
                    ScanMethod.BARCODE -> stringResource(R.string.merchant_scan_waiting_barcode_subtitle)
                },
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF4E596A),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            ScanProgressSegments()
            AnimatedVisibility(visible = errorRes != null || messageRes != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = if (errorRes != null) Color(0xFFFEEDEC) else Color(0xFFEAF6EE),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(errorRes ?: messageRes ?: R.string.merchant_scan_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (errorRes != null) Color(0xFFB42318) else VerevColors.Forest,
                            textAlign = TextAlign.Center,
                        )
                        if (errorRes != null) {
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Color(0xFFDD7E72)),
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
            }
            Spacer(Modifier.weight(1f))
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 18.dp,
                        bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.merchant_scan_method_title,
                    ),
                    style = MaterialTheme.typography.titleMedium,
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
            .size(292.dp)
            .clip(RoundedCornerShape(54.dp))
            .background(Brush.verticalGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
            .scale(pulse),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 46.dp, topEnd = 46.dp, bottomStart = 54.dp, bottomEnd = 54.dp))
                .background(Color.White.copy(alpha = 0.18f)),
        )
        Icon(
            imageVector = if (activeMethod == ScanMethod.NFC) Icons.Default.Nfc else Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(90.dp),
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
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = if (selected) 0.22f else 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                textAlign = TextAlign.Center,
            )
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
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            val width = if (index == 2) 14.dp else 46.dp
            val height = if (index == 2) 10.dp else 12.dp
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
    var visible by remember { mutableStateOf(false) }
    val scrimInteraction = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.18f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "scan_method_scrim",
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    fun dismissWithAnimation(action: () -> Unit) {
        scope.launch {
            visible = false
            delay(220)
            action()
        }
    }

    Dialog(
        onDismissRequest = { dismissWithAnimation(onDismiss) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha)),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = scrimInteraction,
                        indication = null,
                        onClick = { dismissWithAnimation(onDismiss) },
                    ),
            )
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 260),
                ) + fadeIn(animationSpec = tween(durationMillis = 180)),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 220),
                ) + fadeOut(animationSpec = tween(durationMillis = 160)),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 14.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
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
                                colors = listOf(Color(0xFFECCB8F), Color(0xFFE3C48E)),
                                modifier = Modifier.weight(1f),
                                onClick = { dismissWithAnimation { onSelectMethod(ScanMethod.NFC) } },
                            )
                            ScanMethodChoiceCard(
                                title = stringResource(R.string.merchant_scan_sheet_barcode_title),
                                subtitle = stringResource(R.string.merchant_scan_sheet_barcode_subtitle),
                                icon = Icons.Default.CameraAlt,
                                colors = listOf(Color(0xFFF3F3F3), Color(0xFFECECEC)),
                                iconTint = Color(0xFF97A0A9),
                                textColor = VerevColors.Forest.copy(alpha = 0.78f),
                                modifier = Modifier.weight(1f),
                                onClick = { dismissWithAnimation { onSelectMethod(ScanMethod.BARCODE) } },
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
                            onClick = { dismissWithAnimation(onDismiss) },
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
    iconTint: Color = Color.White,
    textColor: Color = Color.White,
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
                    .background(Color.White.copy(alpha = 0.24f)),
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
internal fun ScanCustomerCard(customer: Customer) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
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
            CustomerTierPill(customer.loyaltyTier)
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
    }
}

@Composable
internal fun ScanActionComposerCard(
    availableActions: List<RewardProgramScanAction>,
    selectedAction: RewardProgramScanAction?,
    amount: String,
    points: String,
    customerPoints: Int,
    isSubmitting: Boolean,
    onAmountChanged: (String) -> Unit,
    onPointsChanged: (String) -> Unit,
    onActionSelected: (RewardProgramScanAction) -> Unit,
    onApply: () -> Unit,
    onOpenProfile: () -> Unit,
    onScanAnother: () -> Unit,
) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
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
                action = action,
                amount = amount,
                points = points,
                customerPoints = customerPoints,
                onAmountChanged = onAmountChanged,
                onPointsChanged = onPointsChanged,
            )
        }
        ScanPrimaryActions(
            isSubmitting = isSubmitting,
            hasAction = selectedAction != null,
            onApply = onApply,
            onOpenProfile = onOpenProfile,
            onScanAnother = onScanAnother,
        )
    }
}

@Composable
private fun ScanSelectedActionContent(
    action: RewardProgramScanAction,
    amount: String,
    points: String,
    customerPoints: Int,
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
                text = action.supportingText(customerPoints = customerPoints, amount = amount),
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
            when (action) {
                RewardProgramScanAction.EARN_POINTS -> {
                    ScanAmountField(amount = amount, onAmountChanged = onAmountChanged, label = stringResource(R.string.merchant_scan_amount_label))
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
                    )
                }
                RewardProgramScanAction.CHECK_IN -> Unit
                RewardProgramScanAction.APPLY_CASHBACK -> {
                    ScanAmountField(amount = amount, onAmountChanged = onAmountChanged, label = stringResource(R.string.merchant_scan_cashback_amount_label))
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
) {
    MerchantFormField(
        value = amount,
        onValueChange = onAmountChanged,
        label = label,
        leadingIcon = Icons.Default.CreditCard,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
        ),
    )
}

@Composable
internal fun ScanQuickRegisterCard(
    loyaltyId: String,
    firstName: String,
    phone: String,
    isSubmitting: Boolean,
    onFirstNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
        Text(
            text = stringResource(R.string.merchant_scan_quick_register_title),
            style = MaterialTheme.typography.titleLarge,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.merchant_scan_quick_register_subtitle, loyaltyId),
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.64f),
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = VerevColors.Gold.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.merchant_scan_loyalty_id, loyaltyId),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                color = VerevColors.Forest,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        MerchantFormField(
            value = firstName,
            onValueChange = onFirstNameChanged,
            label = stringResource(R.string.merchant_scan_quick_register_name),
            leadingIcon = Icons.Default.Person,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next,
            ),
        )
        MerchantFormField(
            value = phone,
            onValueChange = onPhoneChanged,
            label = stringResource(R.string.merchant_scan_quick_register_phone),
            leadingIcon = Icons.Default.Phone,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
            ),
        )
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
        ) {
            Text(stringResource(R.string.merchant_scan_quick_register_submit))
        }
    }
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
    onOpenProfile: () -> Unit,
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
            onClick = onOpenProfile,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(stringResource(R.string.merchant_customer_view_profile))
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
private fun RewardProgramScanAction.supportingText(customerPoints: Int, amount: String): String = when (this) {
    RewardProgramScanAction.EARN_POINTS -> stringResource(R.string.merchant_scan_points_preview, calculatePreviewPointsInternal(amount))
    RewardProgramScanAction.REDEEM_REWARDS -> stringResource(R.string.merchant_scan_redeem_supporting, customerPoints)
    RewardProgramScanAction.CHECK_IN -> stringResource(R.string.merchant_scan_check_in_supporting)
    RewardProgramScanAction.APPLY_CASHBACK -> stringResource(R.string.merchant_scan_cashback_preview, calculateCashbackPreviewInternal(amount))
    RewardProgramScanAction.TRACK_TIER_PROGRESS -> stringResource(R.string.merchant_scan_tier_progress_supporting)
}

private fun calculatePreviewPointsInternal(amountInput: String): Int {
    val amount = amountInput.toDoubleOrNull() ?: return 0
    return (amount / 100.0).toInt().coerceAtLeast(1)
}

private fun calculateCashbackPreviewInternal(amountInput: String): Int {
    val amount = amountInput.toDoubleOrNull() ?: return 0
    return (amount * 0.05).toInt().coerceAtLeast(1)
}

@Composable
internal fun ScanFeedbackCard(title: String, subtitle: String, positive: Boolean) {
    MerchantPrimaryCard {
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
                    imageVector = Icons.Default.CheckCircle,
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
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
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
private fun CustomerTierPill(tier: LoyaltyTier) {
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
            text = tier.name,
            color = contentColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
