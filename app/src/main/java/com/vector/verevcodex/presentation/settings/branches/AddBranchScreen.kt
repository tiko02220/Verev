package com.vector.verevcodex.presentation.settings.branches

import com.vector.verevcodex.presentation.settings.*

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R

@Composable
fun AddBranchScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: StoreManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (!state.isEditorVisible || state.editor.editingStoreId != null) {
            viewModel.startCreate()
        }
    }

    LaunchedEffect(state.messageRes) {
        if (state.messageRes == R.string.merchant_store_editor_created) {
            onBack()
        }
    }

    BranchEditorScreenContent(
        contentPadding = contentPadding,
        onBack = onBack,
        titleRes = R.string.merchant_add_branch,
        subtitleRes = R.string.merchant_add_branch_subtitle,
        submitLabelRes = R.string.merchant_add_branch,
        state = state,
        onUpdateEditor = { editor -> viewModel.updateEditor { editor } },
        onSubmit = viewModel::submitEditor,
    )
}
