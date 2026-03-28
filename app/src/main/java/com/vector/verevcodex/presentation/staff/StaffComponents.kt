package com.vector.verevcodex.presentation.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.common.phone.sanitizePhoneNumberInput
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.presentation.common.sheets.AppBottomSheetDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun StaffHeader(
    memberCount: Int,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(VerevColors.Forest, VerevColors.Moss)),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onBack),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
            Text(
                text = stringResource(R.string.auth_back),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
        Text(
            text = stringResource(R.string.merchant_staff_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.merchant_staff_subtitle, memberCount),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.72f),
        )
    }
}

@Composable
internal fun StaffPrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
internal fun StaffEmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.PeopleAlt,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Text(
                text = stringResource(R.string.merchant_staff_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.merchant_staff_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
internal fun StaffMemberCard(
    member: StaffMember,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest)), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "${member.firstName} ${member.lastName}".trim(),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = member.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.62f),
                    )
                    RolePill(role = member.role)
                }
            }

            PermissionGrid(member.permissions)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StaffActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Edit,
                    text = stringResource(R.string.merchant_edit),
                    background = VerevColors.Moss,
                    contentColor = Color.White,
                    onClick = onEdit,
                )
                StaffActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DeleteOutline,
                    text = stringResource(R.string.merchant_delete),
                    background = Color(0xFFDC2626),
                    contentColor = Color.White,
                    onClick = onDelete,
                )
            }
        }
    }
}

@Composable
internal fun StaffAddMemberSheet(
    fullName: String,
    email: String,
    phoneNumber: String,
    password: String,
    role: StaffRole,
    permissions: StaffPermissions,
    isSaving: Boolean,
    isEditing: Boolean,
    onFullNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRoleSelected: (StaffRole) -> Unit,
    onPermissionsChanged: (StaffPermissions) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AppBottomSheetDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .navigationBarsPadding(),
        allowSwipeToDismiss = false,
        contentPadding = PaddingValues(0.dp),
    ) { dismiss, _ ->
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(if (isEditing) R.string.merchant_staff_edit_member else R.string.merchant_staff_add_member),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(VerevColors.Forest.copy(alpha = 0.06f), CircleShape)
                        .clickable(enabled = !isSaving, onClick = dismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "×",
                        style = MaterialTheme.typography.headlineSmall,
                        color = VerevColors.Forest,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                FormSection {
                    MerchantFormField(
                        value = fullName,
                        onValueChange = onFullNameChanged,
                        label = stringResource(R.string.merchant_staff_form_name),
                        leadingIcon = Icons.Default.Person,
                    )
                }
                FormSection {
                    MerchantFormField(
                        value = email,
                        onValueChange = onEmailChanged,
                        label = stringResource(R.string.merchant_staff_form_email),
                        leadingIcon = Icons.Default.MailOutline,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                }
                FormSection {
                    MerchantFormField(
                        value = phoneNumber,
                        onValueChange = { onPhoneNumberChanged(sanitizePhoneNumberInput(it)) },
                        label = stringResource(R.string.merchant_staff_form_phone),
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                }
                if (!isEditing) {
                    FormSection {
                        MerchantFormField(
                            value = password,
                            onValueChange = onPasswordChanged,
                            label = stringResource(R.string.merchant_staff_form_password),
                            leadingIcon = Icons.Default.Edit,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(text = stringResource(R.string.merchant_staff_form_role))
                    StaffRole.entries.filter { it != StaffRole.OWNER }.forEach { roleOption ->
                        RoleSelectionCard(
                            role = roleOption,
                            selected = role == roleOption,
                            onClick = {
                                if (!isSaving) {
                                    onRoleSelected(roleOption)
                                }
                            },
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(text = stringResource(R.string.merchant_staff_permissions_title))
                    permissionOptions().forEach { option ->
                        PermissionToggleRow(
                            icon = option.icon,
                            label = stringResource(option.labelRes),
                            selected = option.selected(permissions),
                            onClick = {
                                if (!isSaving) {
                                    onPermissionsChanged(option.toggle(permissions))
                                }
                            },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, VerevColors.Forest.copy(alpha = 0.18f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isSaving, onClick = dismiss)
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.auth_cancel),
                                style = MaterialTheme.typography.titleMedium,
                                color = VerevColors.Forest,
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        shadowElevation = 8.dp,
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
                                .fillMaxWidth()
                                .clickable(enabled = !isSaving, onClick = onSave)
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (isSaving) {
                                    stringResource(R.string.auth_loading)
                                } else {
                                    stringResource(if (isEditing) R.string.merchant_staff_save_changes else R.string.merchant_staff_save_member)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun StaffDeleteDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.navigationBarsPadding(),
        contentPadding = PaddingValues(24.dp),
    ) { dismiss, dismissAfter ->
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text = stringResource(R.string.merchant_staff_delete_title),
                style = MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.merchant_staff_delete_subtitle, memberName),
                style = MaterialTheme.typography.bodyLarge,
                color = VerevColors.Forest.copy(alpha = 0.72f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StaffSheetActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.auth_cancel),
                    primary = false,
                    onClick = dismiss,
                )
                StaffSheetActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.merchant_delete),
                    primary = true,
                    onClick = { dismissAfter(onConfirm) },
                )
            }
        }
    }
}

@Composable
private fun PermissionGrid(permissions: StaffPermissions) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        permissionOptions().chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { option ->
                    PermissionCell(
                        modifier = Modifier.weight(1f),
                        icon = option.icon,
                        label = stringResource(option.labelRes),
                        enabled = option.selected(permissions),
                    )
                }
                if (rowOptions.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PermissionCell(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    enabled: Boolean,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) VerevColors.Moss.copy(alpha = 0.12f) else Color(0xFFF4F5F7),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) VerevColors.Moss.copy(alpha = 0.28f) else Color(0xFFE6E8EB),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.52f),
            )
        }
    }
}

@Composable
private fun PermissionToggleRow(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) VerevColors.Moss.copy(alpha = 0.1f) else Color(0xFFF8F9FA),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (selected) VerevColors.Moss else Color.Transparent,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selected) VerevColors.Moss else VerevColors.Forest.copy(alpha = 0.08f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else VerevColors.Forest.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.68f),
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        if (selected) VerevColors.Gold else Color.Transparent,
                        CircleShape,
                    )
                    .then(
                        if (!selected) {
                            Modifier.background(Color.Transparent, CircleShape)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(22.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(2.dp, VerevColors.Forest.copy(alpha = 0.18f)),
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    role: StaffRole,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) VerevColors.Moss.copy(alpha = 0.1f) else Color(0xFFF8F9FA),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (selected) VerevColors.Moss else VerevColors.Forest.copy(alpha = 0.08f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = role.displayName(),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(role.descriptionRes()),
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun RolePill(role: StaffRole) {
    val color = when (role) {
        StaffRole.OWNER -> VerevColors.Gold
        StaffRole.STORE_MANAGER -> VerevColors.Moss
        StaffRole.CASHIER -> VerevColors.Tan
        StaffRole.STAFF -> VerevColors.Tan
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color,
    ) {
        Text(
            text = role.displayName(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StaffActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    background: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun StaffSheetActionButton(
    modifier: Modifier = Modifier,
    text: String,
    primary: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = if (primary) null else androidx.compose.foundation.BorderStroke(1.5.dp, VerevColors.Forest.copy(alpha = 0.18f)),
        shadowElevation = if (primary) 8.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (primary) Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Tan)) else Brush.horizontalGradient(listOf(Color.White, Color.White)),
                )
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (primary) Color.White else VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun FormSection(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = VerevColors.Forest,
        fontWeight = FontWeight.Medium,
    )
}

private data class PermissionOption(
    val icon: ImageVector,
    val labelRes: Int,
    val selected: (StaffPermissions) -> Boolean,
    val toggle: (StaffPermissions) -> StaffPermissions,
)

private fun permissionOptions(): List<PermissionOption> = listOf(
    PermissionOption(
        icon = Icons.Default.Visibility,
        labelRes = R.string.merchant_staff_permission_analytics,
        selected = { it.viewAnalytics },
        toggle = { it.copy(viewAnalytics = !it.viewAnalytics) },
    ),
    PermissionOption(
        icon = Icons.Default.Settings,
        labelRes = R.string.merchant_staff_permission_programs,
        selected = { it.managePrograms },
        toggle = {
            val nextValue = !it.managePrograms
            it.copy(viewPrograms = nextValue, managePrograms = nextValue)
        },
    ),
    PermissionOption(
        icon = Icons.Default.Edit,
        labelRes = R.string.merchant_staff_permission_transactions,
        selected = { it.processTransactions },
        toggle = { it.copy(processTransactions = !it.processTransactions) },
    ),
    PermissionOption(
        icon = Icons.Default.PeopleAlt,
        labelRes = R.string.merchant_staff_permission_customers,
        selected = { it.manageCustomers },
        toggle = { it.copy(manageCustomers = !it.manageCustomers) },
    ),
    PermissionOption(
        icon = Icons.Default.Person,
        labelRes = R.string.merchant_staff_permission_staff,
        selected = { it.manageStaff },
        toggle = { it.copy(manageStaff = !it.manageStaff) },
    ),
)

private fun StaffRole.descriptionRes(): Int = when (this) {
    StaffRole.OWNER -> R.string.merchant_staff_role_owner_description
    StaffRole.STORE_MANAGER -> R.string.merchant_staff_role_manager_description
    StaffRole.CASHIER -> R.string.merchant_staff_role_cashier_description
    StaffRole.STAFF -> R.string.merchant_staff_role_staff_description
}
