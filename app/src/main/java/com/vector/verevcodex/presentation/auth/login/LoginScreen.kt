package com.vector.verevcodex.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.auth.common.AuthCenteredSection
import com.vector.verevcodex.presentation.auth.common.AuthErrorMessage
import com.vector.verevcodex.presentation.auth.common.AuthFormField
import com.vector.verevcodex.presentation.auth.common.AuthGradientScreenScaffold
import com.vector.verevcodex.presentation.auth.common.AuthPrimaryButton
import com.vector.verevcodex.presentation.auth.common.authErrorRes
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onSignup: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onLoggedIn()
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        AuthGradientScreenScaffold(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Spacer(Modifier.height(32.dp))
            AuthCenteredSection {
                LoginBrandHeader()
            }
            Spacer(Modifier.height(8.dp))
            AuthCenteredSection {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_white)),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.auth_welcome_back),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = stringResource(R.string.auth_sign_in_dashboard),
                            color = colorResource(R.color.text_secondary),
                        )
                        AuthFormField(
                            value = state.email,
                            onValueChange = viewModel::updateEmail,
                            label = stringResource(R.string.auth_email_label),
                            placeholder = stringResource(R.string.auth_hint_email),
                            leadingIcon = Icons.Default.Email,
                            isError = state.emailError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        )
                        AuthErrorMessage(state.emailError)
                        AuthFormField(
                            value = state.password,
                            onValueChange = viewModel::updatePassword,
                            label = stringResource(R.string.auth_password_label),
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
                            isError = state.passwordError != null,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.submit()
                            }),
                        )
                        AuthErrorMessage(state.passwordError)
                        Text(
                            text = stringResource(R.string.auth_forgot_password),
                            color = colorResource(R.color.brand_green),
                            modifier = Modifier
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onForgotPassword)
                                .padding(4.dp),
                        )
                        AuthPrimaryButton(
                            text = stringResource(R.string.auth_sign_in),
                            loading = state.isLoading,
                            onClick = viewModel::submit,
                        )
                        authErrorRes(state.authError)?.let { errorRes ->
                            Text(
                                text = stringResource(errorRes),
                                color = colorResource(R.color.error_red),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(colorResource(R.color.text_hint)))
                            Text(
                                text = stringResource(R.string.auth_or),
                                color = colorResource(R.color.text_secondary),
                                modifier = Modifier.padding(horizontal = 12.dp),
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(colorResource(R.color.text_hint)))
                        }
                        Text(
                            text = stringResource(R.string.auth_no_account),
                            color = colorResource(R.color.text_secondary),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colorResource(R.color.brand_green).copy(alpha = 0.06f))
                                .border(
                                    width = 2.dp,
                                    color = colorResource(R.color.brand_green).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable(onClick = onSignup)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = colorResource(R.color.brand_green),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.auth_register_business),
                                color = colorResource(R.color.brand_green),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        MerchantLoadingOverlay(
            isVisible = state.isLoading,
            title = stringResource(R.string.auth_loader_login_title),
            subtitle = stringResource(R.string.auth_loader_login_subtitle),
        )
    }
}

@Composable
private fun LoginBrandHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(colorResource(R.color.brand_gold), colorResource(R.color.brand_tan))))
                .padding(20.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.auth_brand_name),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.auth_business_portal),
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.auth_manage_loyalty),
            color = Color.White.copy(alpha = 0.62f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
