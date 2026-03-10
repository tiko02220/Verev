package com.vector.verevcodex.presentation.auth.security

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.auth.common.AuthErrorMessage
import com.vector.verevcodex.presentation.auth.common.AuthPinBoxes

@Composable
internal fun SecurityBrandHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(listOf(colorResource(R.color.brand_gold), colorResource(R.color.brand_tan)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(46.dp))
        }
        Spacer(Modifier.size(18.dp))
        Text(
            text = stringResource(R.string.auth_security_brand_name),
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.auth_security_brand_portal),
            color = Color.White.copy(alpha = 0.82f),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
internal fun SecurityUnlockCard(
    state: AppSecurityUiState,
    pinValue: String,
    onPinChanged: (String) -> Unit,
    onUseBiometric: () -> Unit,
    onRecoverAccess: () -> Unit,
    onLogoutRequested: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.auth_welcome_back),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.auth_unlock_pin_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.text_secondary),
                textAlign = TextAlign.Center,
            )
            if (state.securityConfig?.biometricEnabled == true) {
                SecurityBiometricCheckingCard(
                    isActive = state.promptBiometric,
                    onClick = onUseBiometric,
                )
                Button(
                    onClick = onUseBiometric,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = colorResource(R.color.brand_green),
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.auth_unlock_use_biometric),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            AuthPinBoxes(
                value = pinValue,
                isError = state.pinError != null && state.pinError != "biometric_failed",
                onValueChange = onPinChanged,
            )
            if (state.pinError != null) {
                SecurityErrorStatusCard(errorKey = state.pinError)
            }
            Button(
                onClick = onRecoverAccess,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colorResource(R.color.brand_green),
                ),
            ) {
                Text(
                    text = stringResource(R.string.auth_unlock_forgot_pin),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(colorResource(R.color.text_hint).copy(alpha = 0.25f)))
                Text(
                    text = stringResource(R.string.auth_or),
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(colorResource(R.color.text_hint).copy(alpha = 0.25f)))
            }
            Button(
                onClick = onLogoutRequested,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFBECEC),
                    contentColor = Color.Red,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(R.string.auth_log_out), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SecurityBiometricCheckingCard(
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "biometric_loader")
    val animatedAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "biometric_alpha",
    )
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.app_background)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = colorResource(R.color.brand_green).copy(alpha = if (isActive) animatedAlpha else 1f),
            )
            Text(
                text = stringResource(R.string.auth_unlock_checking_biometric),
                color = colorResource(R.color.brand_green).copy(alpha = if (isActive) animatedAlpha else 1f),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun SecurityErrorStatusCard(errorKey: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (errorKey == "biometric_failed") colorResource(R.color.app_background) else Color(0xFFFBECEC),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (errorKey == "biometric_failed") Icons.Default.Fingerprint else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (errorKey == "biometric_failed") colorResource(R.color.brand_green) else colorResource(R.color.error_red),
            )
            AuthErrorMessage(
                errorKey = errorKey,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun SecurityLogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF7A00)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                }
                Text(
                    text = stringResource(R.string.auth_logout_confirmation_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.auth_logout_confirmation_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.text_secondary),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF7A00),
                        contentColor = Color.White,
                    ),
                ) {
                    Text(text = stringResource(R.string.auth_log_out), style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.surface_white),
                        contentColor = colorResource(R.color.brand_green),
                    ),
                ) {
                    Text(text = stringResource(R.string.auth_cancel), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
