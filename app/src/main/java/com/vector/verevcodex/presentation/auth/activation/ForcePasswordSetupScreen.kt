package com.vector.verevcodex.presentation.auth.activation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun ForcePasswordSetupScreen(
    onCompleted: () -> Unit,
    viewModel: ForcePasswordSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onCompleted()
    }

    AuthGradientScreenScaffold(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Spacer(Modifier.height(32.dp))
        AuthCenteredSection {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.auth_force_password_setup_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorResource(R.color.surface_white),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.auth_force_password_setup_subtitle, state.email.ifBlank { "your staff account" }),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.surface_white).copy(alpha = 0.84f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        AuthCenteredSection {
            Card(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_white)),
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.auth_force_password_setup_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorResource(R.color.text_secondary),
                        )
                        AuthFormField(
                            value = state.newPassword,
                            onValueChange = viewModel::updateNewPassword,
                            label = stringResource(R.string.auth_new_password_label),
                            placeholder = stringResource(R.string.auth_hint_password),
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                    )
                                }
                            },
                            isError = state.errorKey == "password_short",
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next,
                            ),
                        )
                        AuthFormField(
                            value = state.confirmPassword,
                            onValueChange = viewModel::updateConfirmPassword,
                            label = stringResource(R.string.auth_confirm_password_label),
                            placeholder = stringResource(R.string.auth_confirm_password_label),
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                    )
                                }
                            },
                            isError = state.errorKey == "password_confirm",
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { viewModel.submit() }),
                        )
                        AuthErrorMessage(state.errorKey)
                        AuthPrimaryButton(
                            text = stringResource(R.string.auth_force_password_setup_action),
                            loading = state.isLoading,
                            onClick = viewModel::submit,
                        )
                    }
                }
            }
        }
    }
}
