package com.vector.verevcodex.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun BranchEditorScreenContent(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    @StringRes submitLabelRes: Int,
    state: StoreManagementUiState,
    onUpdateEditor: (StoreEditorState) -> Unit,
    onSubmit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = VerevColors.AppBackground,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
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
                    title = stringResource(titleRes),
                    subtitle = stringResource(subtitleRes),
                    icon = Icons.Default.Storefront,
                    colors = listOf(VerevColors.ForestDeep, VerevColors.Forest),
                )
            }
            state.errorRes?.let { errorRes ->
                item {
                    SettingsDetailSection(title = stringResource(R.string.merchant_form_issue_title)) {
                        Text(
                            text = stringResource(errorRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.ErrorText,
                        )
                    }
                }
            }
            item {
                SettingsDetailSection(title = stringResource(R.string.merchant_branch_basic_info_title)) {
                    MerchantFormField(
                        value = state.editor.name,
                        onValueChange = { onUpdateEditor(state.editor.copy(name = it)) },
                        label = stringResource(R.string.merchant_business_details_name_label),
                        leadingIcon = Icons.Default.Storefront,
                        isError = state.editorFieldErrors.containsKey(STORE_FIELD_NAME),
                        errorText = state.editorFieldErrors[STORE_FIELD_NAME]?.let { stringResource(id = it) },
                    )
                    MerchantFormField(
                        value = state.editor.address,
                        onValueChange = { onUpdateEditor(state.editor.copy(address = it)) },
                        label = stringResource(R.string.merchant_business_details_address_label),
                        leadingIcon = Icons.Default.LocationOn,
                        isError = state.editorFieldErrors.containsKey(STORE_FIELD_ADDRESS),
                        errorText = state.editorFieldErrors[STORE_FIELD_ADDRESS]?.let { stringResource(id = it) },
                    )
                    MerchantFormField(
                        value = state.editor.contactInfo,
                        onValueChange = { onUpdateEditor(state.editor.copy(contactInfo = it)) },
                        label = stringResource(R.string.merchant_business_details_contact_label),
                        leadingIcon = Icons.Default.Email,
                        isError = state.editorFieldErrors.containsKey(STORE_FIELD_CONTACT),
                        errorText = state.editorFieldErrors[STORE_FIELD_CONTACT]?.let { stringResource(id = it) },
                    )
                    MerchantFormField(
                        value = state.editor.category,
                        onValueChange = { onUpdateEditor(state.editor.copy(category = it)) },
                        label = stringResource(R.string.merchant_business_details_category_label),
                        leadingIcon = Icons.Default.Storefront,
                        isError = state.editorFieldErrors.containsKey(STORE_FIELD_CATEGORY),
                        errorText = state.editorFieldErrors[STORE_FIELD_CATEGORY]?.let { stringResource(id = it) },
                    )
                }
            }
            item {
                SettingsDetailSection(title = stringResource(R.string.merchant_branch_hours_title)) {
                    Text(
                        text = stringResource(R.string.merchant_add_branch_hours_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.58f),
                    )
                    MerchantFormField(
                        value = state.editor.workingHours,
                        onValueChange = { onUpdateEditor(state.editor.copy(workingHours = it)) },
                        label = stringResource(R.string.merchant_business_details_hours_label),
                        leadingIcon = Icons.Default.Description,
                        singleLine = false,
                        supportingText = stringResource(R.string.merchant_add_branch_hours_supporting),
                        isError = state.editorFieldErrors.containsKey(STORE_FIELD_HOURS),
                        errorText = state.editorFieldErrors[STORE_FIELD_HOURS]?.let { stringResource(id = it) },
                    )
                    branchHoursPresets.forEach { preset ->
                        val presetValue = stringResource(id = preset.valueRes)
                        Surface(
                            onClick = { onUpdateEditor(state.editor.copy(workingHours = presetValue)) },
                            color = Color.White,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                        ) {
                            Text(
                                text = stringResource(preset.labelRes),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = VerevColors.Forest,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerevColors.Forest,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = stringResource(if (state.isSaving) R.string.merchant_saving else submitLabelRes),
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
