package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun AddCustomerFormSheet(
    state: AddCustomerUiState,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onCreateCustomer: () -> Unit,
) {
    val firstNameError = state.errorRes == R.string.merchant_add_customer_error_first_name
    val lastNameError = state.errorRes == R.string.merchant_add_customer_error_last_name
    val emailError = state.errorRes == R.string.merchant_add_customer_error_email
    val phoneError = state.errorRes == R.string.merchant_add_customer_error_phone

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(VerevColors.Moss.copy(alpha = 0.14f), VerevColors.Forest.copy(alpha = 0.1f))))
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.merchant_add_customer_form_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.merchant_add_customer_form_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.Forest.copy(alpha = 0.66f),
                        )
                    }
                }
                MerchantFormField(
                    value = state.firstName,
                    onValueChange = onFirstNameChanged,
                    label = stringResource(R.string.merchant_add_customer_first_name),
                    leadingIcon = Icons.Default.Person,
                    isError = firstNameError,
                    errorText = if (firstNameError) stringResource(R.string.merchant_add_customer_error_first_name) else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    ),
                )
                MerchantFormField(
                    value = state.lastName,
                    onValueChange = onLastNameChanged,
                    label = stringResource(R.string.merchant_add_customer_last_name),
                    leadingIcon = Icons.Default.Person,
                    isError = lastNameError,
                    errorText = if (lastNameError) stringResource(R.string.merchant_add_customer_error_last_name) else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    ),
                )
                MerchantFormField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    label = stringResource(R.string.merchant_add_customer_email),
                    leadingIcon = Icons.Default.Email,
                    isError = emailError,
                    errorText = if (emailError) stringResource(R.string.merchant_add_customer_error_email) else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    ),
                )
                MerchantFormField(
                    value = state.phoneNumber,
                    onValueChange = onPhoneChanged,
                    label = stringResource(R.string.merchant_add_customer_phone),
                    leadingIcon = Icons.Default.Phone,
                    isError = phoneError,
                    errorText = if (phoneError) stringResource(R.string.merchant_add_customer_error_phone) else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    ),
                )
                AddCustomerInfoCard(
                    icon = Icons.Default.CalendarMonth,
                    text = stringResource(R.string.merchant_add_customer_info),
                )
                state.errorRes?.takeIf {
                    it != R.string.merchant_add_customer_error_first_name &&
                        it != R.string.merchant_add_customer_error_last_name &&
                        it != R.string.merchant_add_customer_error_email &&
                        it != R.string.merchant_add_customer_error_phone
                }?.let { errorRes ->
                    AddCustomerErrorCard(text = stringResource(errorRes))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = onCreateCustomer,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerevColors.Gold,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                if (state.isSaving) R.string.merchant_add_customer_creating else R.string.merchant_add_customer_create,
                            ),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddCustomerInfoCard(
    icon: ImageVector,
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = VerevColors.Moss.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = VerevColors.Forest)
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest,
            )
        }
    }
}

@Composable
internal fun AddCustomerErrorCard(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFEE2E2),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7F1D1D),
        )
    }
}
