package com.vector.verevcodex.presentation.merchant.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun MerchantPageHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.7f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
        }
    }
}

@Composable
fun MerchantBackHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.auth_back),
                tint = VerevColors.Forest,
            )
            Text(
                text = stringResource(R.string.auth_back),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
        MerchantPageHeader(
            title = title,
            subtitle = subtitle,
        )
    }
}

@Composable
fun MerchantSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = VerevColors.Forest,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
fun MerchantPrimaryCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun MerchantInteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun MerchantGradientMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(colors))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
                Text(text = title, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            }
            Text(text = value, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MerchantMiniMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    MerchantPrimaryCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
        }
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MerchantActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    MerchantInteractiveCard(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.6f))
    }
}

@Composable
fun MerchantStatusPill(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text = text, color = contentColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MerchantMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    MerchantInteractiveCard(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(VerevColors.Moss.copy(alpha = 0.1f), VerevColors.Forest.copy(alpha = 0.1f)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = VerevColors.Forest)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun MerchantSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.replace("\n", "")) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerevColors.Gold,
            unfocusedBorderColor = colorResource(R.color.text_hint).copy(alpha = 0.18f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedLabelColor = VerevColors.Forest,
            unfocusedLabelColor = VerevColors.Forest.copy(alpha = 0.56f),
            focusedLeadingIconColor = VerevColors.Gold,
            unfocusedLeadingIconColor = VerevColors.Forest.copy(alpha = 0.5f),
            cursorColor = VerevColors.Gold,
        ),
    )
}

@Composable
fun MerchantFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.replace("\n", "")) },
            modifier = modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null) },
            supportingText = if (errorText == null) {
                supportingText?.let { text ->
                    { Text(text = text, color = VerevColors.Forest.copy(alpha = 0.5f)) }
                }
            } else {
                null
            },
            singleLine = singleLine,
            readOnly = readOnly,
            enabled = enabled,
            isError = isError,
            keyboardOptions = keyboardOptions,
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
                cursorColor = VerevColors.Gold,
            ),
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.error_red),
            )
        }
    }
}

@Composable
fun MerchantFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                if (selected) Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Moss)) else Brush.horizontalGradient(listOf(Color.White, Color.White))
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = colorResource(R.color.text_hint).copy(alpha = 0.2f),
                shape = RoundedCornerShape(100.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else VerevColors.Forest,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun MerchantEmptyStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    MerchantPrimaryCard(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(VerevColors.Moss.copy(alpha = 0.12f), VerevColors.Forest.copy(alpha = 0.12f)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = VerevColors.Forest)
            }
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = VerevColors.Forest, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun MerchantTextAction(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Text(text = text, color = VerevColors.Moss, fontWeight = FontWeight.Medium)
    }
}
