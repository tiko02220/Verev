package com.vector.verevcodex.presentation.auth.forgot

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.auth.common.AuthCenteredSection
import com.vector.verevcodex.presentation.auth.common.AuthGradientScreenScaffold
import com.vector.verevcodex.presentation.auth.common.ForgotPasswordStep
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(
    mode: RecoveryMode,
    onBackToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.step) {
        if (state.step == ForgotPasswordStep.SUCCESS) {
            delay(1800)
            onBackToLogin()
        }
    }

    AuthGradientScreenScaffold {
        Spacer(Modifier.height(8.dp))
        AuthCenteredSection {
            ForgotHeader(
                onBackToLogin = {
                    if (state.step == ForgotPasswordStep.EMAIL || state.step == ForgotPasswordStep.SUCCESS) {
                        onBackToLogin()
                    } else {
                        viewModel.back()
                    }
                },
            )
        }
        Spacer(Modifier.height(36.dp))
        AuthCenteredSection {
            when (state.step) {
                ForgotPasswordStep.EMAIL -> ForgotRequestCard(
                    mode = mode,
                    state = state,
                    onEmailChanged = viewModel::updateEmail,
                    onSend = { viewModel.sendCode(mode) },
                    focusManager = focusManager,
                )
                ForgotPasswordStep.OTP -> ForgotOtpCard(
                    state = state,
                    onOtpChanged = viewModel::updateOtp,
                    onVerify = { viewModel.verifyCode(mode) },
                    onResend = { viewModel.resendCode(mode) },
                )
                ForgotPasswordStep.NEW_PASSWORD -> ForgotResetCard(
                    mode = mode,
                    state = state,
                    onPasswordChanged = { value ->
                        viewModel.updateNewPassword(
                            if (mode == RecoveryMode.PIN) value.filter(Char::isDigit).take(4) else value
                        )
                    },
                    onConfirmPasswordChanged = { value ->
                        viewModel.updateConfirmPassword(
                            if (mode == RecoveryMode.PIN) value.filter(Char::isDigit).take(4) else value
                        )
                    },
                    onSubmit = { viewModel.submitReset(mode) },
                )
                ForgotPasswordStep.SUCCESS -> ForgotSuccessCard(email = state.email, mode = mode)
            }
        }
        Spacer(Modifier.height(28.dp))
        androidx.compose.material3.Text(
            text = stringResource(R.string.auth_support_footer),
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        state.dialogErrorMessage?.let { message ->
            MerchantErrorDialog(
                message = message,
                onDismiss = viewModel::dismissDialogError,
            )
        }
    }
}
