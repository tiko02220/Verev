package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.presentation.theme.VerevColors

internal enum class CustomerFeatureHeaderStyle {
    PLAIN,
    GRADIENT,
}

@Composable
internal fun CustomerFeatureScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    headerStyle: CustomerFeatureHeaderStyle = if (onBack == null) CustomerFeatureHeaderStyle.PLAIN else CustomerFeatureHeaderStyle.GRADIENT,
    showTitle: Boolean = true,
    backLabel: String? = null,
    wrapBodyInSheet: Boolean = headerStyle == CustomerFeatureHeaderStyle.GRADIENT,
    headerContent: @Composable () -> Unit = {},
    body: @Composable () -> Unit,
) {
    val usesGradientHeader = headerStyle == CustomerFeatureHeaderStyle.GRADIENT
    Column(
        modifier = modifier
            .background(VerevColors.AppBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (usesGradientHeader) {
                        Brush.linearGradient(listOf(VerevColors.ForestDeep, VerevColors.Forest, Color(0xFF5B8B67)))
                    } else {
                        Brush.verticalGradient(listOf(VerevColors.AppBackground, VerevColors.AppBackground))
                    }
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = if (usesGradientHeader) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (onBack == null && showTitle) {
                CustomerFeatureTitle(
                    title = title,
                    subtitle = subtitle,
                    bright = usesGradientHeader,
                )
            } else if (onBack != null && showTitle) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CustomerBackButton(onBack = onBack)
                    CustomerFeatureTitle(
                        title = title,
                        subtitle = subtitle,
                        modifier = Modifier.weight(1f),
                        bright = usesGradientHeader,
                    )
                }
            } else if (onBack != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CustomerBackButton(onBack = onBack)
                    backLabel?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            headerContent()
        }
        if (wrapBodyInSheet) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = VerevColors.AppBackground,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                shadowElevation = 6.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                ) {
                    body()
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                body()
            }
        }
    }
}

@Composable
internal fun CustomerBodySection(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
private fun CustomerFeatureTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    bright: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = if (bright) Color.White else VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = if (bright) Color.White.copy(alpha = 0.78f) else VerevColors.Forest.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun CustomerBackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
    }
}
