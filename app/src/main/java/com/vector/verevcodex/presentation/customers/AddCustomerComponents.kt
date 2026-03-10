package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun AddCustomerHeroCard() {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_hero_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_add_customer_hero_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
        }
    }
}

@Composable
internal fun AddCustomerFormCard(
    state: AddCustomerUiState,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onCreateCustomer: () -> Unit,
) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
        MerchantFormField(
            value = state.firstName,
            onValueChange = onFirstNameChanged,
            label = stringResource(R.string.merchant_add_customer_first_name),
            leadingIcon = Icons.Default.Person,
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
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
            ),
        )
        AddCustomerInfoCard(
            icon = Icons.Default.DateRange,
            text = stringResource(R.string.merchant_add_customer_info),
        )
        state.errorRes?.let { errorRes ->
            AddCustomerErrorCard(text = stringResource(errorRes))
        }
        Button(
            onClick = onCreateCustomer,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
        ) {
            Text(
                text = stringResource(if (state.isSaving) R.string.merchant_add_customer_creating else R.string.merchant_add_customer_create),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
internal fun AddCustomerSuccessCard(
    state: AddCustomerUiState,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
    onOpenProfile: () -> Unit,
    onAddAnother: () -> Unit,
) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.merchant_add_customer_success_title),
                style = MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.merchant_add_customer_success_subtitle, state.successName),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.64f),
            )
        }
        AddCustomerActivationCard(
            enrollmentCode = state.generatedEnrollmentCode,
            activationLink = state.activationLink,
            onCopyLink = onCopyLink,
            onShareLink = onShareLink,
        )
        Button(
            onClick = onOpenProfile,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
        ) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.merchant_add_customer_open_profile),
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Medium,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onCopyLink,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerevColors.Forest),
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.merchant_add_customer_copy_link),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Medium,
                )
            }
            Button(
                onClick = onShareLink,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerevColors.Forest),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.merchant_add_customer_share_link),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Button(
            onClick = onAddAnother,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F6F4), contentColor = VerevColors.Forest),
        ) {
            Text(stringResource(R.string.merchant_add_customer_add_another), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AddCustomerActivationCard(
    enrollmentCode: String,
    activationLink: String,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
) {
    MerchantPrimaryCard(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(VerevColors.Moss.copy(alpha = 0.16f), VerevColors.Forest.copy(alpha = 0.16f)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = VerevColors.Forest)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_code_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.56f),
                )
                Text(
                    text = enrollmentCode,
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = activationLink,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AddCustomerMiniAction(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ContentCopy,
                label = stringResource(R.string.merchant_add_customer_copy_link),
                onClick = onCopyLink,
            )
            AddCustomerMiniAction(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Share,
                label = stringResource(R.string.merchant_add_customer_share_link),
                onClick = onShareLink,
            )
        }
    }
}

@Composable
private fun AddCustomerMiniAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VerevColors.AppBackground,
            contentColor = VerevColors.Forest,
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Text(text = label, modifier = Modifier.padding(start = 6.dp), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddCustomerInfoCard(icon: ImageVector, text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(VerevColors.Moss.copy(alpha = 0.1f), VerevColors.Forest.copy(alpha = 0.08f))))
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = VerevColors.Forest.copy(alpha = 0.72f))
            Text(text = text, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest)
        }
    }
}

@Composable
private fun AddCustomerErrorCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEE2E2))
            .padding(14.dp),
    ) {
        Text(text = text, color = Color(0xFFB91C1C), style = MaterialTheme.typography.bodyMedium)
    }
}
