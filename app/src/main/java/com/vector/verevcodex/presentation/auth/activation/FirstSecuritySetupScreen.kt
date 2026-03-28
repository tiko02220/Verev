package com.vector.verevcodex.presentation.auth.activation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.auth.common.AuthCenteredSection
import com.vector.verevcodex.presentation.auth.common.AuthErrorMessage
import com.vector.verevcodex.presentation.auth.common.AuthGradientScreenScaffold
import com.vector.verevcodex.presentation.auth.common.AuthPinBoxes
import com.vector.verevcodex.presentation.auth.common.AuthPrimaryButton
import com.vector.verevcodex.presentation.auth.common.showBiometricPrompt

@Composable
fun FirstSecuritySetupScreen(
    onCompleted: () -> Unit,
    viewModel: FirstSecuritySetupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? FragmentActivity

    LaunchedEffect(state.requestBiometricPrompt) {
        if (state.requestBiometricPrompt && activity != null) {
            showBiometricPrompt(
                activity = activity,
                titleRes = R.string.auth_biometric_title,
                subtitleRes = R.string.auth_biometric_subtitle,
                negativeRes = R.string.auth_biometric_negative,
                onResult = viewModel::biometricPromptHandled,
            )
        }
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onCompleted()
    }

    AuthGradientScreenScaffold(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Spacer(Modifier.height(32.dp))
        AuthCenteredSection {
            when (state.stage) {
                FirstSecuritySetupStage.PIN -> FirstPinSetupCard(state = state, viewModel = viewModel)
                FirstSecuritySetupStage.BIOMETRIC -> FirstBiometricSetupCard(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun FirstPinSetupCard(
    state: FirstSecuritySetupUiState,
    viewModel: FirstSecuritySetupViewModel,
) {
    val confirming = state.pin.length == 4
    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_white)),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = colorResource(R.color.brand_green),
                modifier = Modifier.size(42.dp),
            )
            Text(
                text = if (confirming) stringResource(R.string.auth_pin_confirm_title) else stringResource(R.string.auth_pin_setup_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (confirming) {
                    stringResource(R.string.auth_pin_confirm_subtitle)
                } else {
                    stringResource(R.string.auth_pin_setup_subtitle)
                },
                color = colorResource(R.color.text_secondary),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            AuthPinBoxes(
                value = if (confirming) state.confirmPin else state.pin,
                isError = state.pinError != null,
                focusResetKey = confirming,
                onValueChange = if (confirming) viewModel::updateConfirmPin else viewModel::updatePin,
            )
            if (state.pinConfirmed) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.app_background)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = colorResource(R.color.brand_green))
                        androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
                        Text(
                            text = stringResource(R.string.auth_pin_created_success),
                            color = colorResource(R.color.brand_green),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            } else {
                AuthErrorMessage(state.pinError, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun FirstBiometricSetupCard(
    state: FirstSecuritySetupUiState,
    viewModel: FirstSecuritySetupViewModel,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_white)),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = colorResource(R.color.brand_green),
                modifier = Modifier.size(46.dp),
            )
            Text(
                text = stringResource(R.string.auth_biometric_setup_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.auth_biometric_setup_subtitle),
                color = colorResource(R.color.text_secondary),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            AuthPrimaryButton(
                text = stringResource(R.string.auth_biometric_enable),
                loading = state.isLoading,
                onClick = viewModel::requestBiometricPrompt,
            )
            Button(
                onClick = { viewModel.completeBiometricSetup(false) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.surface_white),
                    contentColor = colorResource(R.color.text_secondary),
                ),
            ) {
                Text(
                    text = stringResource(R.string.auth_skip_for_now),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Unspecified,
                    fontWeight = FontWeight.Medium,
                )
            }
            AuthErrorMessage(state.submissionError, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}
