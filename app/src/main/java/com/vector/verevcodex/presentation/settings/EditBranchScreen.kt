package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R

@Composable
fun EditBranchScreen(
    storeId: String,
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: StoreManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val branchExists = state.stores.any { it.id == storeId }

    LaunchedEffect(storeId, state.stores) {
        viewModel.prepareEdit(storeId)
    }

    LaunchedEffect(state.messageRes) {
        if (state.messageRes == R.string.merchant_store_editor_updated) {
            onBack()
        }
    }

    if (!branchExists && state.stores.isNotEmpty()) {
        Surface {
            SettingsDetailSection(title = stringResource(R.string.merchant_edit_branch)) {
                Text(text = stringResource(R.string.merchant_store_editor_error_not_found))
            }
        }
        return
    }

    BranchEditorScreenContent(
        contentPadding = contentPadding,
        onBack = onBack,
        titleRes = R.string.merchant_edit_branch,
        subtitleRes = R.string.merchant_edit_branch_subtitle,
        submitLabelRes = R.string.merchant_save_changes,
        state = state,
        onUpdateEditor = { editor -> viewModel.updateEditor { editor } },
        onSubmit = viewModel::submitEditor,
    )
}
