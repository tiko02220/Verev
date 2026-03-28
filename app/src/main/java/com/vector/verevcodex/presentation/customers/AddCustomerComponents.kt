package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.People
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.customer.CustomerGender
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun AddCustomerFormSheet(
    state: AddCustomerUiState,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onGenderSelected: (CustomerGender?) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onCreateCustomer: () -> Unit,
    onCancel: () -> Unit,
) {
    val firstNameError = state.errorRes == R.string.merchant_add_customer_error_first_name
    val lastNameError = state.errorRes == R.string.merchant_add_customer_error_last_name
    val emailError = state.errorRes == R.string.merchant_add_customer_error_email
    val phoneError = state.errorRes == R.string.merchant_add_customer_error_phone

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = VerevColors.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_form_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_add_customer_form_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.66f),
                )
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.merchant_add_customer_gender),
                        style = MaterialTheme.typography.labelLarge,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CustomerGenderChip(
                            label = stringResource(R.string.merchant_add_customer_gender_female),
                            selected = state.gender == CustomerGender.FEMALE,
                            onClick = { onGenderSelected(CustomerGender.FEMALE) },
                        )
                        CustomerGenderChip(
                            label = stringResource(R.string.merchant_add_customer_gender_male),
                            selected = state.gender == CustomerGender.MALE,
                            onClick = { onGenderSelected(CustomerGender.MALE) },
                        )
                    }
                }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.auth_cancel),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Button(
                        onClick = onCreateCustomer,
                        modifier = Modifier.weight(1f),
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerevColors.Gold,
                            contentColor = VerevColors.White,
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                if (state.isSaving) R.string.merchant_add_customer_creating else R.string.merchant_add_customer_create,
                            ),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerGenderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    MerchantFilterChip(
        text = label,
        selected = selected,
        onClick = onClick,
    )
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
        color = VerevColors.ErrorContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.ErrorText,
        )
    }
}
