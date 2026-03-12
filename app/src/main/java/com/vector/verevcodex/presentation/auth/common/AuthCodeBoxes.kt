package com.vector.verevcodex.presentation.auth.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.KeyboardOptions
import com.vector.verevcodex.R

@Composable
fun AuthOtpBoxes(
    values: List<String>,
    isError: Boolean,
    onValueChange: (Int, String) -> Unit,
) {
    val focusRequesters = remember { List(values.size) { FocusRequester() } }
    val focusedIndex = remember { mutableStateListOf(*Array(values.size) { false }) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val boxWidth = if (maxWidth < 360.dp) 38.dp else 42.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            values.forEachIndexed { index, digit ->
                BasicTextField(
                    value = digit,
                    onValueChange = { typed ->
                        val sanitized = typed.filter(Char::isDigit).take(1)
                        onValueChange(index, sanitized)
                        if (sanitized.isNotBlank()) {
                            if (index < focusRequesters.lastIndex) {
                                focusRequesters[index + 1].requestFocus()
                            } else {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                            }
                        } else if (index > 0) {
                            focusRequesters[index - 1].requestFocus()
                        }
                    },
                    modifier = Modifier
                        .requiredWidth(boxWidth)
                        .requiredHeight(56.dp)
                        .focusRequester(focusRequesters[index])
                        .onFocusChanged { focusedIndex[index] = it.isFocused }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            focusRequesters[index].requestFocus()
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next,
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = colorResource(R.color.text_primary),
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(colorResource(R.color.brand_green)),
                    decorationBox = { inner ->
                        AuthCodeCell(
                            isFocused = focusedIndex[index],
                            isFilled = digit.isNotEmpty(),
                            isError = isError,
                            cornerRadius = 18.dp,
                        ) { inner() }
                    },
                    interactionSource = remember { MutableInteractionSource() },
                )
                if (index < values.lastIndex) Spacer(Modifier.width(6.dp))
            }
        }
    }
}

@Composable
fun AuthPinBoxes(
    value: String,
    isError: Boolean,
    autoFocus: Boolean = true,
    focusResetKey: Any? = Unit,
    onValueChange: (String) -> Unit,
) {
    key(focusResetKey) {
        val focusRequesters = remember { List(4) { FocusRequester() } }
        val focusedIndex = remember { mutableStateListOf(*Array(4) { false }) }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(autoFocus) {
            if (autoFocus) {
                focusRequesters.first().requestFocus()
            } else {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val boxWidth = if (maxWidth < 360.dp) 54.dp else 58.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) { index ->
                    val digit = value.getOrNull(index)?.toString().orEmpty()
                    BasicTextField(
                        value = digit,
                        onValueChange = { typed ->
                            val filtered = typed.filter(Char::isDigit)
                            if (filtered.isBlank()) {
                                if (digit.isNotBlank()) {
                                    val chars = value.toMutableList()
                                    chars.removeAt(index)
                                    onValueChange(chars.joinToString(""))
                                } else if (index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            } else {
                                val currentDigits = value.padEnd(4, ' ').toMutableList()
                                currentDigits[index] = filtered.first()
                                val sanitized = currentDigits.joinToString("").trimEnd().take(4)
                                onValueChange(sanitized)
                                if (index < focusRequesters.lastIndex && sanitized.length > index) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                                if (sanitized.length == 4) {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                }
                            }
                        },
                        modifier = Modifier
                            .requiredWidth(boxWidth)
                            .requiredHeight(68.dp)
                            .focusRequester(focusRequesters[index])
                            .onFocusChanged { focusedIndex[index] = it.isFocused }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                focusRequesters[index].requestFocus()
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = if (index == 3) ImeAction.Done else ImeAction.Next,
                        ),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = colorResource(R.color.text_primary),
                            textAlign = TextAlign.Center,
                        ),
                        cursorBrush = SolidColor(colorResource(R.color.brand_green)),
                        decorationBox = { inner ->
                            AuthCodeCell(
                                isFocused = focusedIndex[index],
                                isFilled = digit.isNotEmpty(),
                                isError = isError,
                                cornerRadius = 20.dp,
                            ) { inner() }
                        },
                        interactionSource = remember { MutableInteractionSource() },
                    )
                    if (index < 3) Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AuthCodeCell(
    isFocused: Boolean,
    isFilled: Boolean,
    isError: Boolean,
    cornerRadius: Dp,
    content: @Composable () -> Unit,
) {
    val borderColor = when {
        isError -> colorResource(R.color.error_red)
        isFocused -> colorResource(R.color.brand_green)
        isFilled -> colorResource(R.color.brand_forest).copy(alpha = 0.42f)
        else -> colorResource(R.color.text_hint).copy(alpha = 0.26f)
    }
    val borderWidth = if (isFocused) 2.dp else 1.dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White.copy(alpha = 0.88f))
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
