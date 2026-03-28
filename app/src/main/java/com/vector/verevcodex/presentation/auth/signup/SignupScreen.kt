package com.vector.verevcodex.presentation.auth.signup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.auth.common.AuthBackRow
import com.vector.verevcodex.presentation.auth.common.AuthCenteredSection
import com.vector.verevcodex.presentation.auth.common.AuthGradientScreenScaffold

@Composable
fun SignupScreen(
    onBack: () -> Unit,
    onLoginRequested: () -> Unit,
    onForgotPasswordRequested: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val industries = stringArrayResource(R.array.auth_industries)
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var showIndustrySheet by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val submissionErrorMessage = state.submissionError?.let { errorKey: String ->
        com.vector.verevcodex.presentation.auth.common.authErrorRes(errorKey)?.let { messageRes: Int ->
            stringResource(messageRes)
        } ?: errorKey
    }

    LaunchedEffect(state.shouldNavigateToApp) {
        if (state.shouldNavigateToApp) {
            viewModel.navigationHandled()
        }
    }

    LaunchedEffect(state.requestBiometricPrompt) {
        if (state.requestBiometricPrompt && activity != null) {
            kotlinx.coroutines.delay(250)
            com.vector.verevcodex.presentation.auth.common.showBiometricPrompt(
                activity = activity,
                titleRes = R.string.auth_biometric_title,
                subtitleRes = R.string.auth_biometric_subtitle,
                negativeRes = R.string.auth_biometric_negative,
                onResult = viewModel::biometricPromptHandled,
            )
        }
    }

    Box {
        AuthGradientScreenScaffold {
            if (state.stage == SignupFlowStage.BUSINESS || state.stage == SignupFlowStage.ACCOUNT) {
                AuthCenteredSection {
                    SignupHeader(step = state.step)
                }
                Spacer(Modifier.height(16.dp))
                AuthCenteredSection {
                    AuthBackRow(
                        text = stringResource(R.string.auth_back),
                        onClick = {
                            if (state.stage == SignupFlowStage.BUSINESS) onBack() else viewModel.back()
                        },
                    )
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(24.dp))
            }

            AuthCenteredSection {
                when (state.stage) {
                    SignupFlowStage.BUSINESS,
                    SignupFlowStage.ACCOUNT,
                    -> SignupFormCard(
                        state = state,
                        viewModel = viewModel,
                        onOpenIndustrySheet = {
                            showIndustrySheet = true
                        },
                        focusManager = focusManager,
                        passwordVisible = passwordVisible,
                        onPasswordVisibleChange = { passwordVisible = !passwordVisible },
                        confirmPasswordVisible = confirmPasswordVisible,
                        onConfirmPasswordVisibleChange = { confirmPasswordVisible = !confirmPasswordVisible },
                    )

                    SignupFlowStage.PIN -> PinSetupCard(state = state, viewModel = viewModel)
                    SignupFlowStage.BIOMETRIC -> BiometricSetupCard(state = state, viewModel = viewModel)
                    SignupFlowStage.STAFF_PROMPT -> StaffPromptCard(
                        state = state,
                        onSkip = viewModel::skipStaffSetup,
                        onAddStaff = viewModel::startStaffSetup,
                    )
                    SignupFlowStage.STAFF_FORM -> StaffSetupCard(state = state, viewModel = viewModel)
                    SignupFlowStage.COMPLETE -> SignupCompletionCard(
                        onEnterApp = viewModel::enterApp,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        if (state.showExistingEmailDialog) {
            ExistingEmailDialog(
                onDismiss = viewModel::dismissExistingEmailDialog,
                onLogin = onLoginRequested,
                onRecoverPassword = onForgotPasswordRequested)
        }

        if (showIndustrySheet) {
            SignupIndustryDialog(
                onDismissRequest = { showIndustrySheet = false },
            ) { dismiss ->
                IndustrySelectionSheet(
                    industries = industries,
                    selectedIndustry = state.industry,
                    onIndustrySelected = { industry ->
                        viewModel.updateIndustry(industry)
                        dismiss()
                    },
                )
            }
        }

        com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay(
            isVisible = state.isLoading,
            title = stringResource(
                when (state.stage) {
                    SignupFlowStage.BUSINESS,
                    SignupFlowStage.ACCOUNT,
                    -> R.string.auth_loader_register_title
                    SignupFlowStage.PIN,
                    SignupFlowStage.BIOMETRIC,
                    -> R.string.auth_loader_security_title
                    SignupFlowStage.STAFF_FORM -> R.string.auth_loader_staff_title
                    SignupFlowStage.STAFF_PROMPT,
                    SignupFlowStage.COMPLETE,
                    -> R.string.auth_loader_finish_title
                }
            ),
            subtitle = stringResource(
                when (state.stage) {
                    SignupFlowStage.BUSINESS,
                    SignupFlowStage.ACCOUNT,
                    -> R.string.auth_loader_register_subtitle
                    SignupFlowStage.PIN,
                    SignupFlowStage.BIOMETRIC,
                    -> R.string.auth_loader_security_subtitle
                    SignupFlowStage.STAFF_FORM -> R.string.auth_loader_staff_subtitle
                    SignupFlowStage.STAFF_PROMPT,
                    SignupFlowStage.COMPLETE,
                    -> R.string.auth_loader_finish_subtitle
                }
            ),
        )

        submissionErrorMessage?.let { message ->
            com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog(
                message = message,
                onDismiss = viewModel::dismissSubmissionError,
            )
        }
    }
}
