package com.vector.verevcodex.presentation.auth.forgot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusManager
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.auth.common.AuthBackRow
import com.vector.verevcodex.presentation.auth.common.AuthEmailChip
import com.vector.verevcodex.presentation.auth.common.AuthErrorMessage
import com.vector.verevcodex.presentation.auth.common.AuthFormField
import com.vector.verevcodex.presentation.auth.common.AuthHeroIcon
import com.vector.verevcodex.presentation.auth.common.AuthInfoPanel
import com.vector.verevcodex.presentation.auth.common.AuthOtpBoxes
import com.vector.verevcodex.presentation.auth.common.AuthPinBoxes
import com.vector.verevcodex.presentation.auth.common.AuthPrimaryButton

@Composable
internal fun ForgotHeader(onBackToLogin: () -> Unit) {
    AuthBackRow(
        text = stringResource(R.string.auth_back_to_login),
        onClick = onBackToLogin,
    )
}

@Composable
internal fun ForgotRequestCard(
    mode: RecoveryMode,
    state: ForgotPasswordUiState,
    onEmailChanged: (String) -> Unit,
    onSend: () -> Unit,
    focusManager: FocusManager,
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_white)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            AuthHeroIcon(icon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp)) })
            Text(
                text = stringResource(if (mode == RecoveryMode.PIN) R.string.auth_forgot_pin_title else R.string.auth_forgot_password_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(if (mode == RecoveryMode.PIN) R.string.auth_forgot_pin_subtitle else R.string.auth_forgot_password_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.text_secondary),
                textAlign = TextAlign.Center,
            )
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.auth_email_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.text_primary),
                )
                AuthFormField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    label = stringResource(R.string.auth_email_label),
                    placeholder = stringResource(R.string.auth_hint_email),
                    leadingIcon = Icons.Default.Email,
                    isError = state.error == "required_email" || state.error == "invalid_email",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onSend()
                    }),
                )
            }
            AuthErrorMessage(state.error, modifier = Modifier.fillMaxWidth())
            AuthPrimaryButton(
                text = stringResource(R.string.auth_send_reset_link),
                loading = state.isLoading,
                onClick = onSend,
            )
            AuthInfoPanel(text = stringResource(if (mode == RecoveryMode.PIN) R.string.auth_reset_pin_note else R.string.auth_reset_password_note))
        }
    }
}

@Composable
internal fun ForgotOtpCard(
    state: ForgotPasswordUiState,
    onOtpChanged: (Int, String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_white)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            AuthHeroIcon(icon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp)) })
            Text(
                text = stringResource(R.string.auth_verify_code),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.auth_verify_code_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.text_secondary),
                textAlign = TextAlign.Center,
            )
            AuthOtpBoxes(
                values = state.otp,
                isError = state.error == "code_incomplete" || state.error == "code_invalid",
                onValueChange = onOtpChanged,
            )
            ForgotResendCodeRow(
                countdownSeconds = state.resendCountdownSeconds,
                onResend = onResend,
            )
            AuthErrorMessage(state.error, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            AuthPrimaryButton(
                text = stringResource(R.string.auth_verify_code),
                loading = state.isLoading,
                onClick = onVerify,
            )
        }
    }
}

@Composable
internal fun ForgotResetCard(
    mode: RecoveryMode,
    state: ForgotPasswordUiState,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_white)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            AuthHeroIcon(icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp)) })
            Text(
                text = stringResource(if (mode == RecoveryMode.PIN) R.string.auth_new_pin_title else R.string.auth_new_password_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(if (mode == RecoveryMode.PIN) R.string.auth_new_pin_subtitle else R.string.auth_new_password_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.text_secondary),
                textAlign = TextAlign.Center,
            )
            if (mode == RecoveryMode.PIN) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.auth_new_pin_label), style = MaterialTheme.typography.titleMedium)
                        AuthPinBoxes(
                            value = state.newPassword,
                            isError = state.error == "pin_length" || state.error == "pin_mismatch",
                            onValueChange = onPasswordChanged,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.auth_confirm_pin_label), style = MaterialTheme.typography.titleMedium)
                        AuthPinBoxes(
                            value = state.confirmPassword,
                            isError = state.error == "pin_mismatch",
                            onValueChange = onConfirmPasswordChanged,
                        )
                    }
                }
            } else {
                NewPasswordFields(
                    password = state.newPassword,
                    confirmPassword = state.confirmPassword,
                    onPasswordChanged = onPasswordChanged,
                    onConfirmPasswordChanged = onConfirmPasswordChanged,
                )
            }
            AuthErrorMessage(
                errorKey = state.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = if (mode == RecoveryMode.PIN) TextAlign.Center else TextAlign.Start,
            )
            AuthPrimaryButton(
                text = stringResource(if (mode == RecoveryMode.PIN) R.string.auth_update_pin else R.string.auth_update_password),
                loading = state.isLoading,
                onClick = onSubmit,
            )
        }
    }
}

@Composable
internal fun ForgotSuccessCard(email: String, mode: RecoveryMode) {
    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_white)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            AuthHeroIcon(
                backgroundBrush = Brush.linearGradient(listOf(colorResource(R.color.brand_green), colorResource(R.color.brand_forest))),
                icon = { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp)) },
            )
            Text(
                text = stringResource(if (mode == RecoveryMode.PIN) R.string.auth_pin_reset_success_title else R.string.auth_password_reset_success_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(if (mode == RecoveryMode.PIN) R.string.auth_pin_reset_success_subtitle else R.string.auth_password_reset_success_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.text_secondary),
                textAlign = TextAlign.Center,
            )
            AuthEmailChip(email = email)
            CircularProgressIndicator(
                color = colorResource(R.color.brand_green),
                trackColor = colorResource(R.color.text_hint).copy(alpha = 0.16f),
            )
            Text(
                text = stringResource(R.string.auth_redirecting_to_login),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.text_secondary),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NewPasswordFields(
    password: String,
    confirmPassword: String,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
        AuthFormField(
            value = password,
            onValueChange = onPasswordChanged,
            label = stringResource(R.string.auth_new_password_label),
            placeholder = stringResource(R.string.auth_hint_password),
            leadingIcon = Icons.Default.Lock,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )
        AuthFormField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = stringResource(R.string.auth_confirm_password_label),
            placeholder = stringResource(R.string.auth_hint_password),
            leadingIcon = Icons.Default.Lock,
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        imageVector = if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )
    }
}

@Composable
private fun ForgotResendCodeRow(
    countdownSeconds: Int,
    onResend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.auth_resend_in_format, countdownSeconds),
            color = colorResource(R.color.text_secondary),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.auth_resend_code),
            color = if (countdownSeconds == 0) colorResource(R.color.brand_green) else colorResource(R.color.text_hint),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = countdownSeconds == 0, onClick = onResend)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
    }
}
