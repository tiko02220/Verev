package com.vector.verevcodex.presentation.auth.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun AuthGradientScreenScaffold(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.brand_forest_deep),
                        colorResource(R.color.brand_green),
                    )
                )
            )
    ) {
        AuthBackgroundDecor()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
fun AuthCenteredSection(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 620.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth),
            content = content,
        )
    }
}

@Composable
fun AuthProgressPill(active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) colorResource(R.color.brand_gold) else Color.White.copy(alpha = 0.28f))
            .size(width = 48.dp, height = 6.dp)
    )
}

@Composable
fun AuthErrorMessage(
    errorKey: String?,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    authErrorRes(errorKey)?.let {
        Text(
            text = stringResource(it),
            color = colorResource(R.color.error_red),
            style = MaterialTheme.typography.bodySmall,
            textAlign = textAlign,
            modifier = modifier,
        )
    }
}

@Composable
fun AuthPrimaryButton(
    text: String,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(colorResource(R.color.brand_gold), colorResource(R.color.brand_tan))))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (loading) stringResource(R.string.auth_loading) else text,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun AuthBackRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 10.dp),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            )
        }
    }
}

@Composable
fun AuthHeroIcon(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundBrush: Brush = Brush.linearGradient(listOf(colorResource(R.color.brand_gold), colorResource(R.color.brand_tan))),
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

@Composable
fun AuthInfoPanel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.app_background)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
            color = colorResource(R.color.text_secondary),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun AuthBackgroundDecor(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 80.dp, start = 10.dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
    }
}

@Composable
fun AuthFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.replace("\n", "")) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        readOnly = readOnly,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerevColors.Gold,
            unfocusedBorderColor = colorResource(R.color.text_hint).copy(alpha = 0.18f),
            errorBorderColor = colorResource(R.color.error_red),
            errorLabelColor = colorResource(R.color.error_red),
            errorLeadingIconColor = colorResource(R.color.error_red),
            errorCursorColor = colorResource(R.color.error_red),
            disabledBorderColor = colorResource(R.color.text_hint).copy(alpha = 0.14f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedLabelColor = VerevColors.Forest,
            unfocusedLabelColor = VerevColors.Forest.copy(alpha = 0.56f),
            focusedLeadingIconColor = VerevColors.Gold,
            unfocusedLeadingIconColor = VerevColors.Forest.copy(alpha = 0.5f),
            disabledLeadingIconColor = VerevColors.Forest.copy(alpha = 0.32f),
            focusedTrailingIconColor = VerevColors.Gold,
            unfocusedTrailingIconColor = VerevColors.Forest.copy(alpha = 0.5f),
            disabledTrailingIconColor = VerevColors.Forest.copy(alpha = 0.32f),
            cursorColor = VerevColors.Gold,
        ),
    )
}

@Composable
fun AuthSelectField(
    label: String,
    value: String,
    leadingIcon: ImageVector,
    isPlaceholder: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            color = VerevColors.Forest.copy(alpha = 0.56f),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = if (isError) colorResource(R.color.error_red) else colorResource(R.color.text_hint).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(18.dp),
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isPlaceholder) colorResource(R.color.text_hint) else VerevColors.Forest.copy(alpha = 0.5f),
            )
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                color = if (isPlaceholder) colorResource(R.color.text_hint) else colorResource(R.color.text_primary),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = VerevColors.Forest.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
fun AuthEmailChip(email: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.app_background)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Default.Email, contentDescription = null, tint = colorResource(R.color.brand_green))
            Text(
                text = email,
                color = colorResource(R.color.brand_green),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
