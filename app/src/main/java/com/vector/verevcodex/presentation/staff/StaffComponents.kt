package com.vector.verevcodex.presentation.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.StaffRole
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.theme.VerevColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StaffAddMemberSheet(
    fullName: String,
    email: String,
    password: String,
    role: StaffRole,
    isSaving: Boolean,
    onFullNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRoleSelected: (StaffRole) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.merchant_staff_add_member),
                style = MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
            )
            Text(
                text = stringResource(R.string.merchant_staff_add_member_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.64f),
            )
            OutlinedTextField(
                value = fullName,
                onValueChange = { onFullNameChanged(it.replace("\n", "")) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.merchant_staff_form_name)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { onEmailChanged(it.replace("\n", "")) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.merchant_staff_form_email)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { onPasswordChanged(it.replace("\n", "")) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.merchant_staff_form_password)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.merchant_staff_form_role),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        StaffRole.STORE_MANAGER to R.string.merchant_role_manager,
                        StaffRole.CASHIER to R.string.merchant_role_cashier,
                        StaffRole.STAFF to R.string.merchant_role_staff,
                    ).forEach { (candidateRole, labelRes) ->
                        MerchantFilterChip(
                            text = stringResource(labelRes),
                            selected = candidateRole == role,
                            onClick = { onRoleSelected(candidateRole) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White, contentColor = VerevColors.Forest),
                ) {
                    Text(stringResource(R.string.auth_cancel))
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = androidx.compose.ui.graphics.Color.White),
                ) {
                    Text(stringResource(R.string.merchant_staff_save_member))
                }
            }
        }
    }
}
