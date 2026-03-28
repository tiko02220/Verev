@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.ui.text.ExperimentalTextApi::class,
)

package com.vector.verevcodex.presentation.promotions

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionBoostLevel
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.promotions.PromotionVisibility
import com.vector.verevcodex.presentation.common.sheets.AppBottomSheetDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class PromotionEditorStep {
    DETAILS,
    PRICING,
}

private enum class PromotionPaymentChoice {
    EXISTING,
    NEW_CARD,
}

private val PromotionSheetShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
private val PromotionDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val PromotionAudienceSegments = listOf(
    CampaignSegment.ALL_CUSTOMERS,
    CampaignSegment.NEW_CUSTOMERS,
    CampaignSegment.LOYAL_CUSTOMERS,
    CampaignSegment.SPECIFIC_TIER,
)

@Composable
internal fun PromotionEditorDialog(
    editorState: PromotionEditorState,
    fieldErrors: Map<String, String>,
    isSubmitting: Boolean,
    paymentMethods: List<SavedPaymentMethod>,
    selectedStoreId: String,
    selectedStoreName: String,
    existingPromotions: List<Campaign>,
    existingPrograms: List<com.vector.verevcodex.domain.model.loyalty.RewardProgram>,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onTypeChange: (PromotionType) -> Unit,
    onValueChange: (String) -> Unit,
    onMinimumPurchaseAmountChange: (String) -> Unit,
    onUsageLimitChange: (String) -> Unit,
    onVisibilityChange: (PromotionVisibility) -> Unit,
    onBoostLevelChange: (PromotionBoostLevel) -> Unit,
    onTargetSegmentChange: (Int) -> Unit,
    onSave: () -> Unit,
) {
    val context = LocalContext.current
    var step by rememberSaveable(editorState.promotionId) { mutableStateOf(PromotionEditorStep.DETAILS) }
    var showStepOneValidation by rememberSaveable(editorState.promotionId) { mutableStateOf(false) }
    var showStepTwoValidation by rememberSaveable(editorState.promotionId) { mutableStateOf(false) }
    var showPaymentDialog by rememberSaveable(editorState.promotionId, step) { mutableStateOf(false) }

    val generatedErrors = validatePromotionEditor(editorState)
    val localErrors = if (showStepOneValidation || showStepTwoValidation) {
        generatedErrors.mapValues { stringResource(it.value) }
    } else {
        emptyMap()
    }
    val effectiveErrors = fieldErrors + localErrors
    val previewPromotion = remember(editorState, selectedStoreId) { editorState.toPreviewCampaign(selectedStoreId) }
    val previewBreakdown = remember(editorState.startDate, editorState.endDate, editorState.visibility, editorState.boostLevel) {
        previewPromotion.toNetworkPromotionBreakdown()
    }
    val operationalSnapshot = remember(editorState, selectedStoreId, existingPromotions, existingPrograms) {
        editorState.toOperationalSnapshot(
            storeId = selectedStoreId,
            existingPromotions = existingPromotions,
            existingPrograms = existingPrograms,
        )
    }

    if (showPaymentDialog) {
        NetworkPromotionPaymentDialog(
            promotion = previewPromotion,
            paymentMethods = paymentMethods,
            onDismiss = { showPaymentDialog = false },
            onConfirm = {
                showPaymentDialog = false
                onSave()
            },
        )
    }

    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.95f),
        allowSwipeToDismiss = false,
        contentPadding = PaddingValues(0.dp),
    ) { dismiss, _ ->
        PromotionSheetContainer {
            PromotionSheetHeader(
                title = if (editorState.promotionId == null) {
                    stringResource(R.string.merchant_promotion_create_new_title)
                } else {
                    stringResource(R.string.merchant_promotion_edit_new_title)
                },
                subtitle = if (step == PromotionEditorStep.DETAILS) {
                    stringResource(R.string.merchant_promotion_step_details)
                } else {
                    stringResource(R.string.merchant_promotion_step_pricing)
                },
                showBack = step == PromotionEditorStep.PRICING,
                onBack = { step = PromotionEditorStep.DETAILS },
                onClose = dismiss,
            )
            PromotionStepIndicator(step = step)

            when (step) {
                PromotionEditorStep.DETAILS -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            PromotionUploadSection(
                                imageUri = editorState.imageUri,
                                onUpload = onPickImage,
                            )
                        }
                        item {
                            PromotionCustomerPreviewCard(
                                editorState = editorState,
                                selectedStoreName = selectedStoreName,
                            )
                        }
                        item {
                            PromotionSheetInput(
                                value = editorState.name,
                                onValueChange = onNameChange,
                                label = stringResource(R.string.merchant_promotion_title_label),
                                error = effectiveErrors[PROMOTION_FIELD_NAME],
                            )
                        }
                        item {
                            PromotionSheetInput(
                                value = editorState.description,
                                onValueChange = onDescriptionChange,
                                label = stringResource(R.string.merchant_description_required),
                                error = effectiveErrors[PROMOTION_FIELD_DESCRIPTION],
                                minLines = 4,
                            )
                        }
                        item {
                            PromotionSectionLabel(stringResource(R.string.merchant_promotion_discount_type_label))
                            Spacer(modifier = Modifier.height(12.dp))
                            PromotionTwoColumnGrid(
                                items = PromotionType.entries.filter {
                                    it in setOf(
                                        PromotionType.PERCENT_DISCOUNT,
                                        PromotionType.FIXED_DISCOUNT,
                                        PromotionType.BUY_ONE_GET_ONE,
                                        PromotionType.FREE_ITEM,
                                    )
                                },
                            ) { type, itemModifier ->
                                PromotionSelectableCard(
                                    modifier = itemModifier,
                                    title = stringResource(type.createLabelRes()),
                                    subtitle = null,
                                    icon = type.createIcon(),
                                    selected = editorState.promotionType == type,
                                    onClick = { onTypeChange(type) },
                                )
                            }
                        }
                        if (editorState.promotionType.requiresNumericValue()) {
                            item {
                                PromotionSheetInput(
                                    value = editorState.promotionValue,
                                    onValueChange = onValueChange,
                                    label = stringResource(editorState.promotionType.valueLabelRes()),
                                    error = effectiveErrors[PROMOTION_FIELD_VALUE],
                                    leadingIcon = editorState.promotionType.valueIcon(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                )
                            }
                        }
                        item {
                            PromotionSectionLabel(stringResource(R.string.merchant_promotion_target_audience_label))
                            Spacer(modifier = Modifier.height(12.dp))
                            PromotionTwoColumnGrid(items = PromotionAudienceSegments) { segment, itemModifier ->
                                PromotionSelectableCard(
                                    modifier = itemModifier,
                                    title = stringResource(segment.audienceLabelRes()),
                                    subtitle = null,
                                    icon = segment.audienceIcon(),
                                    selected = editorState.targetSegment == segment,
                                    onClick = { onTargetSegmentChange(segment.ordinal) },
                                )
                            }
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                PromotionSectionLabel(stringResource(R.string.merchant_promotion_optional_settings_title))
                                PromotionSheetInput(
                                    value = editorState.minimumPurchaseAmount,
                                    onValueChange = onMinimumPurchaseAmountChange,
                                    label = stringResource(R.string.merchant_promotion_minimum_purchase_label),
                                    leadingIcon = Icons.Default.MonetizationOn,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                )
                                PromotionSheetInput(
                                    value = editorState.usageLimit,
                                    onValueChange = onUsageLimitChange,
                                    label = stringResource(R.string.merchant_promotion_usage_limit_label),
                                    leadingIcon = Icons.Outlined.TrendingUp,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                            }
                        }
                    }
                    PromotionPrimaryActionButton(
                        text = stringResource(R.string.merchant_promotion_continue_to_pricing),
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        enabled = !isSubmitting,
                        onClick = {
                            showStepOneValidation = true
                            val blockingKeys = setOf(PROMOTION_FIELD_NAME, PROMOTION_FIELD_DESCRIPTION, PROMOTION_FIELD_VALUE)
                            if (generatedErrors.keys.none { it in blockingKeys }) {
                                step = PromotionEditorStep.PRICING
                            }
                        },
                    )
                }

                PromotionEditorStep.PRICING -> {
                    Column(modifier = Modifier.weight(1f)) {
                        if (editorState.visibility == PromotionVisibility.NETWORK_WIDE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                            ) {
                                PromotionCostSummaryCard(previewBreakdown = previewBreakdown)
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = if (editorState.visibility == PromotionVisibility.NETWORK_WIDE) 4.dp else 14.dp,
                                bottom = 14.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item {
                                PromotionBranchImpactCard(
                                    storeName = selectedStoreName,
                                    editorState = editorState,
                                )
                            }
                            item {
                                PromotionGuardrailCard(
                                    snapshot = operationalSnapshot,
                                )
                            }
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    PromotionSectionLabel(stringResource(R.string.merchant_promotion_campaign_duration_label))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                        PromotionDateField(
                                            modifier = Modifier.weight(1f),
                                            label = stringResource(R.string.merchant_campaign_start_date),
                                            value = editorState.startDate.toDisplayDate(),
                                            error = effectiveErrors[PROMOTION_FIELD_START],
                                            onClick = {
                                                context.openPromotionDatePicker(editorState.startDate) { onStartDateChange(it.toString()) }
                                            },
                                        )
                                        PromotionDateField(
                                            modifier = Modifier.weight(1f),
                                            label = stringResource(R.string.merchant_campaign_end_date),
                                            value = editorState.endDate.toDisplayDate(),
                                            error = effectiveErrors[PROMOTION_FIELD_END],
                                            onClick = {
                                                context.openPromotionDatePicker(editorState.endDate) { onEndDateChange(it.toString()) }
                                            },
                                        )
                                    }
                                }
                            }
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    PromotionSectionLabel(stringResource(R.string.merchant_promotion_visibility_label))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                        PromotionVisibilityCard(
                                            modifier = Modifier.weight(1f),
                                            title = stringResource(R.string.merchant_promotion_visibility_business_only),
                                            subtitle = stringResource(R.string.merchant_promotion_visibility_business_subtitle),
                                            priceLabel = stringResource(R.string.merchant_promotion_free_label),
                                            icon = Icons.Outlined.Storefront,
                                            selected = editorState.visibility == PromotionVisibility.BUSINESS_ONLY,
                                            onClick = { onVisibilityChange(PromotionVisibility.BUSINESS_ONLY) },
                                        )
                                        PromotionVisibilityCard(
                                            modifier = Modifier.weight(1f),
                                            title = stringResource(R.string.merchant_promotion_visibility_network),
                                            subtitle = stringResource(R.string.merchant_promotion_visibility_network_subtitle),
                                            priceLabel = stringResource(R.string.merchant_promotion_network_price_from, previewBreakdown.totalLabel),
                                            icon = Icons.Default.Public,
                                            selected = editorState.visibility == PromotionVisibility.NETWORK_WIDE,
                                            onClick = { onVisibilityChange(PromotionVisibility.NETWORK_WIDE) },
                                        )
                                    }
                                }
                            }
                            if (editorState.visibility == PromotionVisibility.NETWORK_WIDE) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        PromotionSectionLabel(stringResource(R.string.merchant_promotion_boost_level_label))
                                        PromotionBoostLevel.entries.forEach { boost ->
                                            PromotionBoostCard(
                                                boostLevel = boost,
                                                selected = editorState.boostLevel == boost,
                                                onClick = { onBoostLevelChange(boost) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PromotionPrimaryActionButton(
                        text = if (editorState.visibility == PromotionVisibility.NETWORK_WIDE) {
                            stringResource(R.string.merchant_promotion_continue_to_payment, previewBreakdown.totalLabel)
                        } else {
                            stringResource(R.string.merchant_promotion_create_button)
                        },
                        icon = if (editorState.visibility == PromotionVisibility.NETWORK_WIDE) {
                            Icons.AutoMirrored.Filled.ArrowForward
                        } else {
                            null
                        },
                        enabled = !isSubmitting,
                        onClick = {
                            showStepTwoValidation = true
                            val blockingKeys = setOf(PROMOTION_FIELD_START, PROMOTION_FIELD_END)
                            if (generatedErrors.keys.any { it in blockingKeys }) return@PromotionPrimaryActionButton
                            if (editorState.visibility == PromotionVisibility.NETWORK_WIDE) {
                                showPaymentDialog = true
                            } else {
                                onSave()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun NetworkPromotionPaymentDialog(
    promotion: Campaign,
    paymentMethods: List<SavedPaymentMethod>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val breakdown = remember(promotion) { promotion.toNetworkPromotionBreakdown() }
    var paymentChoice by remember { mutableStateOf(if (paymentMethods.isEmpty()) PromotionPaymentChoice.NEW_CARD else PromotionPaymentChoice.EXISTING) }
    var selectedMethodId by remember(paymentMethods) { mutableStateOf(paymentMethods.firstOrNull { it.isDefault }?.id ?: paymentMethods.firstOrNull()?.id.orEmpty()) }
    var cardholderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var validationRes by remember { mutableStateOf<Int?>(null) }

    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.95f),
        allowSwipeToDismiss = false,
        contentPadding = PaddingValues(0.dp),
    ) { dismiss, _ ->
        PromotionSheetContainer {
            PaymentHeroHeader(onDismiss = dismiss)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    PaymentPromotionSummaryCard(
                        promotion = promotion,
                        breakdown = breakdown,
                    )
                }
                item {
                    PaymentBreakdownCard(
                        breakdown = breakdown,
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PromotionSectionLabel(stringResource(R.string.merchant_network_promotion_payment_method_title))
                        paymentMethods.forEach { method ->
                            PaymentMethodSelectionCard(
                                method = method,
                                selected = paymentChoice == PromotionPaymentChoice.EXISTING && selectedMethodId == method.id,
                                onClick = {
                                    validationRes = null
                                    paymentChoice = PromotionPaymentChoice.EXISTING
                                    selectedMethodId = method.id
                                },
                            )
                        }
                        AddNewCardSelectionCard(
                            selected = paymentChoice == PromotionPaymentChoice.NEW_CARD,
                            onClick = {
                                validationRes = null
                                paymentChoice = PromotionPaymentChoice.NEW_CARD
                            },
                        )
                        if (paymentChoice == PromotionPaymentChoice.NEW_CARD) {
                            PaymentCardForm(
                                cardholderName = cardholderName,
                                cardNumber = cardNumber,
                                expiryDate = expiryDate,
                                cvv = cvv,
                                onCardholderNameChange = {
                                    validationRes = null
                                    cardholderName = it
                                },
                                onCardNumberChange = {
                                    validationRes = null
                                    cardNumber = it.filter(Char::isDigit).take(16)
                                },
                                onExpiryDateChange = {
                                    validationRes = null
                                    expiryDate = it.take(5)
                                },
                                onCvvChange = {
                                    validationRes = null
                                    cvv = it.filter(Char::isDigit).take(4)
                                },
                            )
                        }
                    }
                }
                item {
                    PaymentExpectedResultsCard(
                        breakdown = breakdown,
                    )
                }
                item {
                    PaymentImportantNotice()
                }
                item {
                    validationRes?.let {
                        Text(
                            text = stringResource(it),
                            color = VerevColors.ErrorText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = dismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerevColors.Forest),
                            contentPadding = PaddingValues(vertical = 14.dp),
                        ) {
                            Text(stringResource(R.string.auth_cancel), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = {
                                validationRes = when {
                                    paymentChoice == PromotionPaymentChoice.EXISTING && selectedMethodId.isBlank() ->
                                        R.string.merchant_network_promotion_payment_select_method_error
                                    paymentChoice == PromotionPaymentChoice.NEW_CARD && cardholderName.isBlank() ->
                                        R.string.merchant_network_promotion_payment_error_cardholder
                                    paymentChoice == PromotionPaymentChoice.NEW_CARD && cardNumber.length < 16 ->
                                        R.string.merchant_network_promotion_payment_error_card_number
                                    paymentChoice == PromotionPaymentChoice.NEW_CARD && expiryDate.length < 5 ->
                                        R.string.merchant_network_promotion_payment_error_expiry
                                    paymentChoice == PromotionPaymentChoice.NEW_CARD && cvv.length < 3 ->
                                        R.string.merchant_network_promotion_payment_error_cvv
                                    else -> null
                                }
                                if (validationRes == null) {
                                    onConfirm()
                                }
                            },
                            modifier = Modifier.weight(1.15f),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                            contentPadding = PaddingValues(vertical = 14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Moss)))
                                    .padding(horizontal = 14.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = stringResource(R.string.merchant_network_promotion_confirm_pay, breakdown.totalLabel),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PromotionDeleteDialog(
    promotionName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.merchant_promotion_delete_title), color = VerevColors.Forest) },
        text = { Text(stringResource(R.string.merchant_promotion_delete_message, promotionName), color = VerevColors.Forest.copy(alpha = 0.68f)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSubmitting) {
                Text(stringResource(R.string.merchant_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.auth_cancel))
            }
        },
    )
}

@Composable
private fun PromotionSheetContainer(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        shape = PromotionSheetShape,
        color = Color.White,
        shadowElevation = 14.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
private fun PromotionSheetHeader(
    title: String,
    subtitle: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 18.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (showBack) {
            PromotionHeaderIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = VerevColors.Forest.copy(alpha = 0.58f),
            )
        }
        PromotionHeaderIconButton(icon = Icons.Default.Close, onClick = onClose)
    }
}

@Composable
private fun PaymentHeroHeader(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(VerevColors.ForestDeep, VerevColors.Moss)))
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_network_promotion_payment_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.merchant_network_promotion_payment_subtitle_new),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
            PromotionHeaderIconButton(
                icon = Icons.Default.Close,
                onClick = onDismiss,
                tint = Color.White,
                background = Color.White.copy(alpha = 0.14f),
            )
        }
    }
}

@Composable
private fun PromotionHeaderIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = VerevColors.Forest,
    background: Color = Color(0xFFF5F6F3),
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun PromotionCustomerPreviewCard(
    editorState: PromotionEditorState,
    selectedStoreName: String,
) {
    PromotionInsightCard(
        icon = Icons.Default.LocalOffer,
        accent = VerevColors.Forest,
        title = stringResource(R.string.merchant_promotion_preview_title),
        body = promotionCustomerPreview(editorState),
        supporting = stringResource(
            R.string.merchant_promotion_preview_supporting,
            editorState.targetSegment.displayLabel(),
            selectedStoreName.ifBlank { stringResource(R.string.merchant_select_store) },
        ),
    )
}

@Composable
private fun PromotionGuardrailCard(snapshot: PromotionOperationalSnapshot) {
    if (snapshot.overlapWarnings.isEmpty()) return
    PromotionInsightCard(
        icon = Icons.Default.WarningAmber,
        accent = VerevColors.Gold,
        title = stringResource(R.string.merchant_promotion_guardrails_title),
        body = snapshot.overlapWarnings.firstOrNull()?.let { warning ->
            when (warning) {
                is PromotionOverlapWarning.CampaignConflict ->
                    stringResource(R.string.merchant_promotion_overlap_campaign_warning, warning.campaignName)
                is PromotionOverlapWarning.LoyaltyStackingConflict ->
                    stringResource(R.string.merchant_promotion_overlap_program_warning, warning.programName)
            }
        }.orEmpty(),
        supporting = additionalGuardrailLines(snapshot.overlapWarnings.drop(1).take(2)),
        emphasized = true,
    )
}

@Composable
private fun PromotionBranchImpactCard(
    storeName: String,
    editorState: PromotionEditorState,
) {
    PromotionInsightCard(
        icon = Icons.Outlined.Storefront,
        accent = VerevColors.Moss,
        title = stringResource(R.string.merchant_promotion_branch_impact_title),
        body = stringResource(
            R.string.merchant_promotion_branch_impact_message,
            storeName.ifBlank { stringResource(R.string.merchant_select_store) },
            if (editorState.visibility == PromotionVisibility.NETWORK_WIDE) {
                stringResource(R.string.merchant_promotion_visibility_network)
            } else {
                stringResource(R.string.merchant_promotion_visibility_business_only)
            },
        ),
    )
}

@Composable
private fun promotionCustomerPreview(editorState: PromotionEditorState): String = when (editorState.promotionType) {
    PromotionType.POINTS_MULTIPLIER -> stringResource(
        R.string.merchant_promotion_preview_points_multiplier,
        editorState.promotionValue.toDoubleOrNull() ?: 0.0,
    )
    PromotionType.PERCENT_DISCOUNT -> stringResource(
        R.string.merchant_promotion_preview_percent_discount,
        editorState.promotionValue.toIntOrNull() ?: 0,
    )
    PromotionType.FIXED_DISCOUNT -> stringResource(
        R.string.merchant_promotion_preview_fixed_discount,
        editorState.promotionValue.toIntOrNull() ?: 0,
    )
    PromotionType.BONUS_POINTS -> stringResource(
        R.string.merchant_promotion_preview_bonus_points,
        editorState.promotionValue.toIntOrNull() ?: 0,
    )
    PromotionType.BUY_ONE_GET_ONE -> stringResource(R.string.merchant_promotion_preview_bogo)
    PromotionType.FREE_ITEM -> stringResource(R.string.merchant_promotion_preview_free_item)
}

@Composable
private fun CampaignSegment.displayLabel(): String = stringResource(displayLabelRes())

@Composable
private fun additionalGuardrailLines(warnings: List<PromotionOverlapWarning>): String? {
    if (warnings.isEmpty()) return null
    val lines = buildList {
        warnings.forEach { warning ->
            add(
                when (warning) {
                    is PromotionOverlapWarning.CampaignConflict ->
                        stringResource(R.string.merchant_promotion_overlap_campaign_warning, warning.campaignName)
                    is PromotionOverlapWarning.LoyaltyStackingConflict ->
                        stringResource(R.string.merchant_promotion_overlap_program_warning, warning.programName)
                },
            )
        }
    }
    return lines.joinToString(separator = "\n").ifBlank { null }
}

@Composable
private fun PromotionInsightCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    body: String,
    supporting: String? = null,
    emphasized: Boolean = false,
) {
    Surface(
        color = if (emphasized) Color(0xFFFFF6E8) else accent.copy(alpha = 0.08f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = if (emphasized) 0.18f else 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest,
                )
                supporting?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.66f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PromotionStepIndicator(step: PromotionEditorStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PromotionProgressBar(active = true, modifier = Modifier.weight(1f))
        PromotionProgressBar(active = step == PromotionEditorStep.PRICING, modifier = Modifier.weight(1f))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(VerevColors.Forest.copy(alpha = 0.1f)),
    )
}

@Composable
private fun PromotionProgressBar(active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) VerevColors.Gold else Color(0xFFE5E8E6)),
    )
}

@Composable
private fun PromotionUploadSection(
    imageUri: String,
    onUpload: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PromotionSectionLabel(stringResource(R.string.merchant_promotion_photo_label))
        PromotionUploadCard(imageUri = imageUri, onUpload = onUpload)
    }
}

@Composable
private fun PromotionUploadCard(
    imageUri: String,
    onUpload: () -> Unit,
) {
    val image = rememberUriBitmap(imageUri)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, VerevColors.Forest.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .clickable(onClick = onUpload),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.merchant_promotion_replace_photo),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Moss))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Text(
                    text = stringResource(R.string.merchant_promotion_upload_photo_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_promotion_upload_photo_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Composable
private fun PromotionSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = VerevColors.Forest,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun PromotionSheetInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    minLines: Int = 1,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (minLines == 1 && leadingIcon != null) {
            MerchantFormField(
                value = value,
                onValueChange = onValueChange,
                label = label,
                leadingIcon = leadingIcon,
                keyboardOptions = keyboardOptions.copy(capitalization = KeyboardCapitalization.Sentences),
                isError = error != null,
                errorText = error,
            )
        } else {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                singleLine = minLines == 1,
                minLines = minLines,
                keyboardOptions = keyboardOptions.copy(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(18.dp),
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerevColors.Gold,
                    unfocusedBorderColor = colorResource(R.color.text_hint).copy(alpha = 0.18f),
                    errorBorderColor = colorResource(R.color.error_red),
                    errorLabelColor = colorResource(R.color.error_red),
                    errorCursorColor = colorResource(R.color.error_red),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    focusedLabelColor = VerevColors.Forest,
                    unfocusedLabelColor = VerevColors.Forest.copy(alpha = 0.56f),
                    cursorColor = VerevColors.Gold,
                ),
            )
            error?.let {
                Text(text = it, color = VerevColors.ErrorText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun <T> PromotionTwoColumnGrid(
    items: List<T>,
    itemContent: @Composable (T, Modifier) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { item ->
                    itemContent(item, Modifier.weight(1f))
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PromotionSelectableCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .height(if (subtitle == null) 78.dp else 96.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = if (selected) 4.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 0.dp else 1.dp,
            color = VerevColors.Forest.copy(alpha = 0.12f),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (selected) {
                        Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Moss))
                    } else {
                        Brush.horizontalGradient(listOf(Color.White, Color.White))
                    }
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else VerevColors.Forest.copy(alpha = 0.55f),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White else VerevColors.Forest.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) Color.White.copy(alpha = 0.84f) else VerevColors.Forest.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun PromotionDateField(
    label: String,
    value: String,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = VerevColors.Forest.copy(alpha = 0.58f),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFF9FBF9),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (error == null) VerevColors.Forest.copy(alpha = 0.12f) else Color(0xFFDC2626),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = VerevColors.Forest.copy(alpha = 0.35f), modifier = Modifier.size(24.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        error?.let {
            Text(text = it, color = VerevColors.ErrorText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PromotionVisibilityCard(
    title: String,
    subtitle: String,
    priceLabel: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(168.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = if (selected) 4.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 3.dp else 1.dp,
            if (selected) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (selected) Color(0xFFFEFCF5) else Color.White)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) VerevColors.Gold else VerevColors.Forest.copy(alpha = 0.35f), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = VerevColors.Forest, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.56f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = priceLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) VerevColors.Gold else VerevColors.Moss,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PromotionBoostCard(
    boostLevel: PromotionBoostLevel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = when (boostLevel) {
        PromotionBoostLevel.STANDARD -> VerevColors.Moss
        PromotionBoostLevel.FEATURED -> VerevColors.Gold
        PromotionBoostLevel.PREMIUM -> VerevColors.Tan
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = if (selected) 4.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) accent else VerevColors.Forest.copy(alpha = 0.12f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selected) accent.copy(alpha = 0.06f) else Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(boostLevel.icon(), contentDescription = null, tint = accent, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(boostLevel.titleRes()),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VerevColors.Forest,
                    )
                    if (boostLevel.additionalCost > 0.0) {
                        Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                            Text(
                                text = stringResource(R.string.merchant_promotion_boost_extra_cost, formatWholeCurrency(boostLevel.additionalCost)),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = accent,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(boostLevel.subtitleRes()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                )
                Text(
                    text = stringResource(R.string.merchant_promotion_boost_reach_estimate, formatCompactCount(boostLevel.estimatedReach)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = accent,
                    fontWeight = FontWeight.Medium,
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (selected) accent else VerevColors.Forest.copy(alpha = 0.2f), CircleShape)
                    .background(if (selected) accent else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                    )
                }
            }
        }
    }
}

@Composable
private fun PromotionCostSummaryCard(previewBreakdown: NetworkPromotionBreakdown) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Moss)))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.merchant_promotion_total_campaign_cost),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.92f),
                    )
                    Text(
                        text = previewBreakdown.totalLabel,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(36.dp))
            }
            Text(
                text = stringResource(
                    R.string.merchant_promotion_campaign_cost_parts,
                    formatWholeCurrency(previewBreakdown.baseFee),
                    previewBreakdown.durationDays,
                    formatWholeCurrency(previewBreakdown.durationFee),
                    formatWholeCurrency(previewBreakdown.boostFee),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.94f),
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.25f)))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.92f))
                Text(
                    text = stringResource(R.string.merchant_network_promotion_payment_reach_value, previewBreakdown.estimatedReach),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.94f),
                )
            }
        }
    }
}

@Composable
private fun PaymentPromotionSummaryCard(
    promotion: Campaign,
    breakdown: NetworkPromotionBreakdown,
) {
    PromotionBodyCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(VerevColors.Tan.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    promotionBoostIcon(promotion.boostLevel ?: PromotionBoostLevel.STANDARD),
                    contentDescription = null,
                    tint = VerevColors.Tan,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = promotion.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = VerevColors.Forest)
                Text(text = promotion.dateRangeText(), style = MaterialTheme.typography.bodyLarge, color = VerevColors.Forest.copy(alpha = 0.56f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Surface(shape = RoundedCornerShape(16.dp), color = VerevColors.Tan.copy(alpha = 0.08f)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(promotionBoostIcon(promotion.boostLevel ?: PromotionBoostLevel.STANDARD), contentDescription = null, tint = VerevColors.Tan, modifier = Modifier.size(16.dp))
                            Text(text = stringResource((promotion.boostLevel ?: PromotionBoostLevel.STANDARD).titleRes()), color = VerevColors.Tan, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = stringResource(R.string.merchant_network_promotion_estimated_reach_title), style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.56f))
                        Text(text = stringResource(R.string.merchant_network_promotion_payment_reach_value, breakdown.estimatedReach), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = VerevColors.Gold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentBreakdownCard(
    breakdown: NetworkPromotionBreakdown,
) {
    PromotionBodyCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PaymentCardSectionTitle(
                icon = Icons.Default.Info,
                title = stringResource(R.string.merchant_network_promotion_payment_cost_title),
            )
            PaymentLineItem(label = stringResource(R.string.merchant_network_promotion_fee_base), value = formatWholeCurrency(breakdown.baseFee))
            PaymentLineItem(label = stringResource(R.string.merchant_network_promotion_fee_duration, breakdown.durationDays), value = formatWholeCurrency(breakdown.durationFee))
            PaymentLineItem(label = stringResource(R.string.merchant_network_promotion_fee_boost), value = formatWholeCurrency(breakdown.boostFee))
            PaymentLineItem(
                label = stringResource(R.string.merchant_network_promotion_payment_total),
                value = breakdown.totalLabel,
                emphasize = true,
            )
        }
    }
}

@Composable
private fun PaymentExpectedResultsCard(
    breakdown: NetworkPromotionBreakdown,
) {
    PromotionBodyCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PaymentCardSectionTitle(
                icon = Icons.Outlined.TrendingUp,
                title = stringResource(R.string.merchant_network_promotion_expected_title),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PaymentResultTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Group,
                    label = stringResource(R.string.merchant_network_promotion_reach_title),
                    value = formatCompactCount(breakdown.estimatedReach),
                    tint = VerevColors.Moss,
                )
                PaymentResultTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.TrendingUp,
                    label = stringResource(R.string.merchant_network_promotion_new_customers_title),
                    value = formatCompactCount(breakdown.estimatedNewCustomers),
                    tint = VerevColors.Gold,
                )
            }
        }
    }
}

@Composable
private fun PaymentImportantNotice() {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFFF8E8),
        border = androidx.compose.foundation.BorderStroke(1.dp, VerevColors.Gold.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = VerevColors.Gold, modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.merchant_important_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = VerevColors.Forest,
                )
                Text(
                    text = stringResource(R.string.merchant_network_promotion_important_copy),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodSelectionCard(
    method: SavedPaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.12f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PaymentBrandBadge(brand = method.brand)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "${method.brand} •••• ${method.last4}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = VerevColors.Forest)
                Text(
                    text = stringResource(R.string.merchant_network_promotion_card_expiry, method.expiryMonth, method.expiryYear % 100),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.56f),
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(VerevColors.Gold),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun AddNewCardSelectionCard(
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) Color(0xFFFEFCF5) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.12f),
        ),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(VerevColors.Moss, VerevColors.ForestDeep))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(R.string.merchant_network_promotion_add_new_card), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = VerevColors.Forest)
                Text(text = stringResource(R.string.merchant_network_promotion_add_new_card_subtitle), style = MaterialTheme.typography.bodyLarge, color = VerevColors.Forest.copy(alpha = 0.56f))
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(VerevColors.Gold),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PaymentCardForm(
    cardholderName: String,
    cardNumber: String,
    expiryDate: String,
    cvv: String,
    onCardholderNameChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PaymentTextField(
            label = stringResource(R.string.merchant_network_promotion_cardholder_name),
            value = cardholderName,
            onValueChange = onCardholderNameChange,
            leadingIcon = Icons.Default.CreditCard,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words),
        )
        PaymentTextField(
            label = stringResource(R.string.merchant_network_promotion_card_number),
            value = cardNumber,
            onValueChange = onCardNumberChange,
            leadingIcon = Icons.Default.CreditCard,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PaymentTextField(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.merchant_network_promotion_payment_expiry),
                value = expiryDate,
                onValueChange = onExpiryDateChange,
                leadingIcon = Icons.Default.CalendarMonth,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            PaymentTextField(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.merchant_network_promotion_payment_cvv),
                value = cvv,
                onValueChange = onCvvChange,
                leadingIcon = Icons.Default.CreditCard,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF7FAF7),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = VerevColors.Moss, modifier = Modifier.size(20.dp))
                Text(
                    text = stringResource(R.string.merchant_network_promotion_card_security_note),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                )
            }
        }
    }
}

@Composable
private fun PaymentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
) {
    MerchantFormField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = leadingIcon,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
    )
}

@Composable
private fun PromotionBodyCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.06f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun PaymentCardSectionTitle(
    icon: ImageVector,
    title: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = VerevColors.Moss, modifier = Modifier.size(22.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = VerevColors.Forest)
    }
}

@Composable
private fun PaymentLineItem(
    label: String,
    value: String,
    emphasize: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleLarge,
                fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Medium,
                color = VerevColors.Forest.copy(alpha = if (emphasize) 1f else 0.72f),
            )
            Text(
                text = value,
                style = if (emphasize) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleLarge,
                fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Medium,
                color = if (emphasize) VerevColors.Gold else VerevColors.Forest,
            )
        }
        if (!emphasize) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(VerevColors.Forest.copy(alpha = 0.08f)),
            )
        }
    }
}

@Composable
private fun PaymentResultTile(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFCFDFB),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = VerevColors.Forest.copy(alpha = 0.58f), textAlign = TextAlign.Center)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = VerevColors.Forest)
        }
    }
}

@Composable
private fun PaymentBrandBadge(brand: String) {
    val gradient = when (brand.lowercase()) {
        "mastercard" -> listOf(Color(0xFFFF3B30), Color(0xFFFFA000))
        else -> listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
    }
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun PromotionPrimaryActionButton(
    text: String,
    icon: ImageVector?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
        contentPadding = PaddingValues(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Moss)))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun rememberUriBitmap(imageUri: String): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, imageUri) {
        value = if (imageUri.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { input ->
                        BitmapFactory.decodeStream(input)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }.value
}

private fun PromotionType.createLabelRes(): Int = when (this) {
    PromotionType.PERCENT_DISCOUNT -> R.string.merchant_promotion_type_percentage_off
    PromotionType.FIXED_DISCOUNT -> R.string.merchant_promotion_type_fixed_amount
    PromotionType.BUY_ONE_GET_ONE -> R.string.merchant_promotion_type_buy_one_get_one_short
    PromotionType.FREE_ITEM -> R.string.merchant_promotion_type_free_item
    PromotionType.POINTS_MULTIPLIER -> R.string.merchant_promotion_type_points_multiplier
    PromotionType.BONUS_POINTS -> R.string.merchant_promotion_type_bonus_points
}

private fun PromotionType.createIcon(): ImageVector = when (this) {
    PromotionType.PERCENT_DISCOUNT -> Icons.Default.Percent
    PromotionType.FIXED_DISCOUNT -> Icons.Default.MonetizationOn
    PromotionType.BUY_ONE_GET_ONE -> Icons.Outlined.Redeem
    PromotionType.FREE_ITEM -> Icons.Default.LocalOffer
    PromotionType.POINTS_MULTIPLIER -> Icons.Default.Campaign
    PromotionType.BONUS_POINTS -> Icons.Default.StarOutline
}

private fun PromotionType.valueLabelRes(): Int = when (this) {
    PromotionType.PERCENT_DISCOUNT -> R.string.merchant_promotion_discount_percentage_label
    PromotionType.FIXED_DISCOUNT -> R.string.merchant_promotion_fixed_amount_label
    PromotionType.POINTS_MULTIPLIER -> R.string.merchant_promotion_value_title
    PromotionType.BONUS_POINTS -> R.string.merchant_promotion_value_title
    PromotionType.BUY_ONE_GET_ONE -> R.string.merchant_promotion_value_title
    PromotionType.FREE_ITEM -> R.string.merchant_promotion_value_title
}

private fun PromotionType.valueIcon(): ImageVector = when (this) {
    PromotionType.PERCENT_DISCOUNT -> Icons.Default.Percent
    PromotionType.FIXED_DISCOUNT -> Icons.Default.MonetizationOn
    PromotionType.POINTS_MULTIPLIER -> Icons.Default.Campaign
    PromotionType.BONUS_POINTS -> Icons.Default.StarOutline
    PromotionType.BUY_ONE_GET_ONE -> Icons.Outlined.Redeem
    PromotionType.FREE_ITEM -> Icons.Default.LocalOffer
}

private fun CampaignSegment.audienceLabelRes(): Int = displayLabelRes()

private fun CampaignSegment.audienceIcon(): ImageVector = when (this) {
    CampaignSegment.ALL_CUSTOMERS -> Icons.Outlined.Group
    CampaignSegment.NEW_CUSTOMERS -> Icons.Outlined.TrendingUp
    CampaignSegment.LOYAL_CUSTOMERS -> Icons.Outlined.Redeem
    CampaignSegment.SPECIFIC_TIER -> Icons.Default.LocalOffer
    CampaignSegment.FREQUENT_VISITORS -> Icons.Outlined.Group
    CampaignSegment.HIGH_SPENDERS -> Icons.Outlined.Redeem
    CampaignSegment.TIER_MEMBERS -> Icons.Default.LocalOffer
    CampaignSegment.INACTIVE_CUSTOMERS -> Icons.Outlined.TrendingUp
    CampaignSegment.HIGH_VALUE_CUSTOMERS -> Icons.Outlined.Redeem
}

private fun PromotionBoostLevel.titleRes(): Int = when (this) {
    PromotionBoostLevel.STANDARD -> R.string.merchant_promotion_boost_standard
    PromotionBoostLevel.FEATURED -> R.string.merchant_promotion_boost_featured
    PromotionBoostLevel.PREMIUM -> R.string.merchant_promotion_boost_premium
}

private fun PromotionBoostLevel.subtitleRes(): Int = when (this) {
    PromotionBoostLevel.STANDARD -> R.string.merchant_promotion_boost_standard_subtitle
    PromotionBoostLevel.FEATURED -> R.string.merchant_promotion_boost_featured_subtitle
    PromotionBoostLevel.PREMIUM -> R.string.merchant_promotion_boost_premium_subtitle
}

private fun PromotionBoostLevel.icon(): ImageVector = promotionBoostIcon(this)

private fun promotionBoostIcon(boostLevel: PromotionBoostLevel): ImageVector = when (boostLevel) {
    PromotionBoostLevel.STANDARD -> Icons.Outlined.TrendingUp
    PromotionBoostLevel.FEATURED -> Icons.Default.StarOutline
    PromotionBoostLevel.PREMIUM -> Icons.Default.WorkspacePremium
}

private fun String.toDisplayDate(): String = runCatching {
    LocalDate.parse(this).format(PromotionDateFormatter)
}.getOrDefault(this)

private fun android.content.Context.openPromotionDatePicker(
    currentValue: String,
    onDateSelected: (LocalDate) -> Unit,
) {
    val initialDate = runCatching { LocalDate.parse(currentValue) }.getOrDefault(LocalDate.now())
    DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth,
    ).show()
}
