package com.vector.verevcodex.presentation.customers

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantPageHeader
import com.vector.verevcodex.presentation.settings.SettingsBackRow
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun AddCustomerScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: AddCustomerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val shareLink = state.activationLink
    val copyLink = {
        if (shareLink.isNotBlank()) {
            clipboardManager.setText(AnnotatedString(shareLink))
        }
    }
    val launchShareSheet = {
        if (shareLink.isNotBlank()) {
            context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.merchant_add_customer_share_subject))
                        putExtra(
                            Intent.EXTRA_TEXT,
                            context.getString(
                                R.string.merchant_add_customer_share_message,
                                state.successName.ifBlank { context.getString(R.string.merchant_add_customer_title) },
                                shareLink,
                            ),
                        )
                    },
                    context.getString(R.string.merchant_add_customer_share_chooser),
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerevColors.AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsBackRow(onBack = onBack)
        MerchantPageHeader(
            title = stringResource(R.string.merchant_add_customer_title),
            subtitle = stringResource(
                R.string.merchant_add_customer_subtitle,
                state.selectedStoreName.ifBlank { stringResource(R.string.merchant_select_store) },
            ),
        )
        AddCustomerHeroCard()
        if (state.createdCustomerId == null) {
            AddCustomerFormCard(
                state = state,
                onFirstNameChanged = viewModel::onFirstNameChanged,
                onLastNameChanged = viewModel::onLastNameChanged,
                onEmailChanged = viewModel::onEmailChanged,
                onPhoneChanged = viewModel::onPhoneChanged,
                onCreateCustomer = viewModel::createCustomer,
            )
        } else {
            AddCustomerSuccessCard(
                state = state,
                onCopyLink = copyLink,
                onShareLink = launchShareSheet,
                onOpenProfile = { state.createdCustomerId?.let(onOpenProfile) },
                onAddAnother = viewModel::resetForm,
            )
        }
    }
}
