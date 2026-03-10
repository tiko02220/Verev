package com.vector.verevcodex.presentation.auth.signup

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.StaffRole
import com.vector.verevcodex.presentation.auth.common.AuthErrorMessage
import com.vector.verevcodex.presentation.auth.common.AuthHeroIcon
import com.vector.verevcodex.presentation.auth.common.AuthInfoPanel
import com.vector.verevcodex.presentation.auth.common.AuthPinBoxes
import com.vector.verevcodex.presentation.auth.common.AuthPrimaryButton
import com.vector.verevcodex.presentation.auth.common.AuthProgressPill
import com.vector.verevcodex.presentation.auth.common.SignupStep

@Composable
internal fun SignupHeader(step: SignupStep) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(colorResource(R.color.brand_gold), colorResource(R.color.brand_tan))))
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Text(stringResource(R.string.auth_join_title), color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.auth_join_subtitle), color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            AuthProgressPill(active = step == SignupStep.BUSINESS)
            Spacer(Modifier.width(8.dp))
            AuthProgressPill(active = step == SignupStep.ACCOUNT)
        }
    }
}

@Composable
internal fun SignupFormCard(
    state: SignupUiState,
    viewModel: SignupViewModel,
    industries: Array<String>,
    industryExpanded: Boolean,
    onIndustryExpandedChange: (Boolean) -> Unit,
    focusManager: FocusManager,
    passwordVisible: Boolean,
    onPasswordVisibleChange: () -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibleChange: () -> Unit,
) {
    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (state.step == SignupStep.BUSINESS) stringResource(R.string.auth_business_information) else stringResource(R.string.auth_account_information),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = if (state.step == SignupStep.BUSINESS) stringResource(R.string.auth_business_information_subtitle) else stringResource(R.string.auth_account_information_subtitle),
                color = colorResource(R.color.text_secondary),
            )
            if (state.step == SignupStep.BUSINESS) {
                SignupField(
                    label = stringResource(R.string.auth_business_name),
                    value = state.businessName,
                    onValueChange = viewModel::updateBusinessName,
                    icon = Icons.Default.Business,
                    errorKey = state.errors["businessName"],
                    hintRes = R.string.auth_hint_business_name,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    SignupDropdownField(
                        label = stringResource(R.string.auth_industry),
                        value = state.industry.ifBlank { stringResource(R.string.auth_select_your_industry) },
                        icon = Icons.Default.Business,
                        isPlaceholder = state.industry.isBlank(),
                        isError = state.errors["industry"] != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIndustryExpandedChange(!industryExpanded) },
                    )
                    DropdownMenu(
                        expanded = industryExpanded,
                        onDismissRequest = { onIndustryExpandedChange(false) },
                        modifier = Modifier.background(colorResource(R.color.surface_white)),
                    ) {
                        industries.forEach { industry ->
                            DropdownMenuItem(
                                text = { Text(industry) },
                                onClick = {
                                    viewModel.updateIndustry(industry)
                                    onIndustryExpandedChange(false)
                                },
                            )
                        }
                    }
                }
                AuthErrorMessage(state.errors["industry"])
                SignupField(
                    label = stringResource(R.string.auth_address),
                    value = state.address,
                    onValueChange = viewModel::updateAddress,
                    icon = Icons.Default.LocationOn,
                    errorKey = state.errors["address"],
                    hintRes = R.string.auth_hint_address,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                )
                SignupField(
                    label = stringResource(R.string.auth_city),
                    value = state.city,
                    onValueChange = viewModel::updateCity,
                    icon = Icons.Default.LocationOn,
                    errorKey = state.errors["city"],
                    hintRes = R.string.auth_hint_city,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                )
                SignupField(
                    label = stringResource(R.string.auth_phone),
                    value = state.phone,
                    onValueChange = viewModel::updatePhone,
                    icon = Icons.Default.Phone,
                    errorKey = state.errors["phone"],
                    hintRes = R.string.auth_hint_phone,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        viewModel.continueToAccount()
                    },
                )
                AuthPrimaryButton(
                    text = stringResource(R.string.auth_continue),
                    loading = state.isLoading,
                    onClick = viewModel::continueToAccount,
                )
            } else {
                SignupField(
                    label = stringResource(R.string.auth_full_name),
                    value = state.fullName,
                    onValueChange = viewModel::updateFullName,
                    icon = Icons.Default.Person,
                    errorKey = state.errors["fullName"],
                    hintRes = R.string.auth_hint_full_name,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                )
                SignupField(
                    label = stringResource(R.string.auth_email_label),
                    value = state.email,
                    onValueChange = viewModel::updateEmail,
                    icon = Icons.Default.Email,
                    errorKey = state.errors["email"],
                    hintRes = R.string.auth_hint_email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                )
                SignupField(
                    label = stringResource(R.string.auth_password_label),
                    value = state.password,
                    onValueChange = viewModel::updatePassword,
                    icon = Icons.Default.Lock,
                    errorKey = state.errors["password"],
                    hintRes = R.string.auth_hint_password,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = onPasswordVisibleChange,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                )
                SignupField(
                    label = stringResource(R.string.auth_confirm_password),
                    value = state.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    icon = Icons.Default.Lock,
                    errorKey = state.errors["confirmPassword"],
                    hintRes = R.string.auth_hint_password,
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    onPasswordVisibilityChange = onConfirmPasswordVisibleChange,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        viewModel.createAccount()
                    },
                )
                AuthErrorMessage(state.submissionError)
                AuthPrimaryButton(
                    text = stringResource(R.string.auth_create_account),
                    loading = state.isLoading,
                    onClick = viewModel::createAccount,
                )
            }
        }
    }
}

@Composable
private fun SignupField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    errorKey: String?,
    @StringRes hintRes: Int,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.replace("\n", "")) },
        label = { Text(label) },
        placeholder = { Text(stringResource(hintRes)) },
        leadingIcon = { Icon(icon, null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        isError = errorKey != null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
        trailingIcon = if (isPassword && onPasswordVisibilityChange != null) {
            {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                    )
                }
            }
        } else {
            null
        },
    )
    AuthErrorMessage(errorKey)
}

@Composable
private fun SignupDropdownField(
    label: String,
    value: String,
    icon: ImageVector,
    isPlaceholder: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colorResource(R.color.text_secondary),
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colorResource(R.color.app_background))
                .border(
                    width = 1.dp,
                    color = if (isError) colorResource(R.color.error_red) else colorResource(R.color.text_hint).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPlaceholder) colorResource(R.color.text_hint) else colorResource(R.color.text_primary),
            )
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                color = if (isPlaceholder) colorResource(R.color.text_hint) else colorResource(R.color.text_primary),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = colorResource(R.color.text_secondary))
        }
    }
}

@Composable
internal fun PinSetupCard(state: SignupUiState, viewModel: SignupViewModel) {
    LaunchedEffect(state.pinConfirmed) {
        if (state.pinConfirmed) {
            kotlinx.coroutines.delay(500)
            viewModel.savePinAndContinue()
        }
    }

    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuthHeroIcon(
                backgroundBrush = Brush.linearGradient(listOf(colorResource(R.color.brand_green), colorResource(R.color.brand_forest))),
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(36.dp)) },
            )
            Text(
                text = if (state.pinSetupStep == PinSetupStep.CREATE) stringResource(R.string.auth_pin_setup_title) else stringResource(R.string.auth_pin_confirm_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (state.pinSetupStep == PinSetupStep.CREATE) stringResource(R.string.auth_pin_setup_subtitle) else stringResource(R.string.auth_pin_confirm_subtitle),
                color = colorResource(R.color.text_secondary),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            AuthPinBoxes(
                value = if (state.pinSetupStep == PinSetupStep.CREATE) state.pin else state.confirmPin,
                isError = state.pinError != null,
                onValueChange = if (state.pinSetupStep == PinSetupStep.CREATE) viewModel::updatePin else viewModel::updateConfirmPin,
            )
            if (state.pinConfirmed) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.app_background)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = colorResource(R.color.brand_green))
                        Spacer(Modifier.width(10.dp))
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
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                AuthProgressPill(active = state.pinSetupStep == PinSetupStep.CREATE)
                Spacer(Modifier.width(8.dp))
                AuthProgressPill(active = state.pinSetupStep == PinSetupStep.CONFIRM)
            }
            AuthInfoPanel(text = stringResource(R.string.auth_pin_info_note))
            Text(
                text = "\uD83D\uDD12 ${stringResource(R.string.auth_pin_secure_note)}",
                color = colorResource(R.color.text_secondary),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun BiometricSetupCard(state: SignupUiState, viewModel: SignupViewModel) {
    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuthHeroIcon(
                icon = { Icon(Icons.Default.Fingerprint, null, tint = Color.White, modifier = Modifier.size(40.dp)) },
            )
            Text(stringResource(R.string.auth_biometric_setup_title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.auth_biometric_setup_subtitle),
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
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.surface_white), contentColor = colorResource(R.color.text_secondary)),
            ) {
                Text(stringResource(R.string.auth_skip_for_now), style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                BiometricBenefitRow(
                    title = stringResource(R.string.auth_biometric_benefit_fast_title),
                    subtitle = stringResource(R.string.auth_biometric_benefit_fast_subtitle),
                )
                BiometricBenefitRow(
                    title = stringResource(R.string.auth_biometric_benefit_security_title),
                    subtitle = stringResource(R.string.auth_biometric_benefit_security_subtitle),
                )
                BiometricBenefitRow(
                    title = stringResource(R.string.auth_biometric_benefit_access_title),
                    subtitle = stringResource(R.string.auth_biometric_benefit_access_subtitle),
                )
            }
            AuthErrorMessage(state.submissionError)
            Text(
                text = "\uD83D\uDD12 ${stringResource(R.string.auth_biometric_secure_note)}",
                color = colorResource(R.color.text_secondary),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BiometricBenefitRow(title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.app_background)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Check, null, tint = colorResource(R.color.brand_green), modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = colorResource(R.color.text_secondary))
        }
    }
}

@Composable
internal fun StaffPromptCard(state: SignupUiState, onSkip: () -> Unit, onAddStaff: () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuthHeroIcon(icon = { Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(32.dp)) })
            Text(stringResource(R.string.auth_staff_prompt_title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(stringResource(R.string.auth_staff_prompt_subtitle), color = colorResource(R.color.text_secondary), textAlign = TextAlign.Center)
            AuthPrimaryButton(text = stringResource(R.string.auth_add_staff_members), loading = state.isLoading, onClick = onAddStaff)
            Button(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.surface_white), contentColor = colorResource(R.color.text_primary)),
            ) {
                Text(stringResource(R.string.auth_skip_for_now))
            }
        }
    }
}

@Composable
internal fun StaffSetupCard(state: SignupUiState, viewModel: SignupViewModel) {
    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AuthHeroIcon(icon = { Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(32.dp)) })
            Text(stringResource(R.string.auth_staff_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.auth_staff_subtitle), color = colorResource(R.color.text_secondary))
            SignupField(stringResource(R.string.auth_full_name), state.staffName, viewModel::updateStaffName, Icons.Default.Person, null, R.string.auth_hint_full_name)
            SignupField(stringResource(R.string.auth_email_label), state.staffEmail, viewModel::updateStaffEmail, Icons.Default.Email, null, R.string.auth_hint_email, keyboardType = KeyboardType.Email)
            SignupField(stringResource(R.string.auth_staff_temporary_password), state.staffPassword, viewModel::updateStaffPassword, Icons.Default.Lock, null, R.string.auth_hint_password, isPassword = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(
                    StaffRole.STORE_MANAGER to R.string.auth_staff_role_manager,
                    StaffRole.STAFF to R.string.auth_staff_role_staff,
                    StaffRole.CASHIER to R.string.auth_staff_role_cashier,
                ).forEach { (role, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (state.staffRole == role) colorResource(R.color.brand_gold).copy(alpha = 0.18f) else colorResource(R.color.app_background))
                            .clickable { viewModel.updateStaffRole(role) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(label), color = colorResource(R.color.text_primary))
                    }
                }
            }
            AuthErrorMessage(state.staffError)
            if (state.staffMembers.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.app_background)),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.auth_staff_added_count, state.staffMembers.size),
                            color = colorResource(R.color.brand_green),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        state.staffMembers.takeLast(3).forEach { member ->
                            Text(
                                stringResource(roleSummaryRes(member.role), member.fullName),
                                color = colorResource(R.color.text_secondary),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = viewModel::addStaffMember,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.app_background), contentColor = colorResource(R.color.text_primary)),
                ) { Text(stringResource(R.string.auth_staff_add_another)) }
                Button(
                    onClick = viewModel::completeStaffSetup,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(colorResource(R.color.brand_green), colorResource(R.color.brand_forest))))
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(R.string.auth_staff_complete_setup), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ExistingEmailDialog(
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onRecoverPassword: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AuthHeroIcon(icon = { Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.size(32.dp)) })
                Text(stringResource(R.string.auth_email_exists_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.auth_email_exists_subtitle), color = colorResource(R.color.text_secondary))
                AuthPrimaryButton(text = stringResource(R.string.auth_go_to_login), loading = false, onClick = onLogin)
                Button(
                    onClick = onRecoverPassword,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.app_background), contentColor = colorResource(R.color.text_primary)),
                ) {
                    Text(stringResource(R.string.auth_recover_password))
                }
            }
        }
    }
}

@StringRes
private fun roleSummaryRes(role: StaffRole): Int = when (role) {
    StaffRole.STORE_MANAGER -> R.string.auth_staff_summary_manager
    StaffRole.STAFF -> R.string.auth_staff_summary_staff
    StaffRole.CASHIER -> R.string.auth_staff_summary_cashier
    StaffRole.OWNER -> R.string.auth_staff_summary_owner
}
