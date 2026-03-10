package com.vector.verevcodex.presentation.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.StaffRole
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.settings.SettingsBackRow
import com.vector.verevcodex.presentation.settings.SettingsHeroCard
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun StaffManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: StaffViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(StaffRole.STAFF) }

    LaunchedEffect(state.messageRes) {
        if (state.messageRes != null) {
            fullName = ""
            email = ""
            password = ""
            role = StaffRole.STAFF
            showAddSheet = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SettingsBackRow(onBack = onBack) }
        item {
            SettingsHeroCard(
                title = stringResource(R.string.merchant_staff_title),
                subtitle = stringResource(R.string.merchant_staff_subtitle, state.members.size),
                icon = Icons.Default.PeopleAlt,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
            )
        }
        state.messageRes?.let { messageRes ->
            item {
                MerchantPrimaryCard {
                    Text(
                        text = stringResource(messageRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                    )
                    Text(
                        text = stringResource(R.string.merchant_staff_feedback_subtitle, state.selectedStoreName.ifBlank { stringResource(R.string.merchant_select_store) }),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.6f),
                    )
                }
            }
        }
        state.errorRes?.let { errorRes ->
            item {
                MerchantPrimaryCard {
                    Text(
                        text = stringResource(errorRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFB91C1C),
                    )
                    Text(
                        text = stringResource(R.string.merchant_staff_error_supporting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.64f),
                    )
                }
            }
        }
        item {
            Button(
                onClick = {
                    viewModel.dismissFeedback()
                    showAddSheet = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
            ) {
                Text(stringResource(R.string.merchant_staff_add_member))
            }
        }
        if (state.members.isEmpty()) {
            item {
                MerchantEmptyStateCard(
                    title = stringResource(R.string.merchant_staff_empty_title),
                    subtitle = stringResource(R.string.merchant_staff_empty_subtitle),
                    icon = Icons.Default.Person,
                )
            }
        } else {
            items(state.members, key = { it.id }) { member ->
                MerchantPrimaryCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${member.firstName} ${member.lastName}".trim(),
                                style = MaterialTheme.typography.titleMedium,
                                color = VerevColors.Forest,
                            )
                            Text(
                                text = member.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = VerevColors.Forest.copy(alpha = 0.6f),
                            )
                        }
                        MerchantStatusPill(
                            text = member.role.displayName(),
                            backgroundColor = if (member.active) VerevColors.Moss.copy(alpha = 0.16f) else Color(0xFFF3F4F6),
                            contentColor = if (member.active) VerevColors.Moss else VerevColors.Inactive,
                        )
                    }
                    Text(
                        text = member.permissionsSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        StaffAddMemberSheet(
            fullName = fullName,
            email = email,
            password = password,
            role = role,
            isSaving = state.isSaving,
            onFullNameChanged = { fullName = it },
            onEmailChanged = { email = it },
            onPasswordChanged = { password = it },
            onRoleSelected = { role = it },
            onDismiss = { showAddSheet = false },
            onSave = {
                viewModel.addMember(fullName = fullName, email = email, password = password, role = role)
            },
        )
    }
}
