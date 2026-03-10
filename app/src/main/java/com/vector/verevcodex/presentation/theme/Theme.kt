package com.vector.verevcodex.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.vector.verevcodex.R

object VerevColors {
    val Forest = Color(0xFF0C3B2E)
    val ForestDeep = Color(0xFF0A2F24)
    val Moss = Color(0xFF6B9773)
    val Gold = Color(0xFFFFBA00)
    val Tan = Color(0xFFBB8A52)
    val AppBackground = Color(0xFFF8F9FA)
    val MutedText = Color(0x99103B2E)
    val Inactive = Color(0xFF9CA3AF)
    val White = Color.White
}

@Composable
private fun verevLightColors() = lightColorScheme(
    primary = colorResource(R.color.brand_forest),
    onPrimary = colorResource(R.color.white),
    secondary = colorResource(R.color.brand_green),
    onSecondary = colorResource(R.color.white),
    tertiary = colorResource(R.color.brand_gold),
    background = colorResource(R.color.app_background),
    onBackground = colorResource(R.color.text_primary),
    surface = colorResource(R.color.surface_white),
    onSurface = colorResource(R.color.text_primary),
    error = colorResource(R.color.error_red),
)

@Composable
private fun verevDarkColors() = darkColorScheme(
    primary = colorResource(R.color.brand_gold),
    secondary = colorResource(R.color.brand_green),
    tertiary = colorResource(R.color.brand_tan),
    background = colorResource(R.color.brand_forest_deep),
    onBackground = colorResource(R.color.white),
    surface = colorResource(R.color.brand_forest),
    onSurface = colorResource(R.color.white),
    error = colorResource(R.color.error_red),
)

@Composable
fun VerevMerchantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) verevDarkColors() else verevLightColors(),
        typography = AppTypography,
        content = content,
    )
}
