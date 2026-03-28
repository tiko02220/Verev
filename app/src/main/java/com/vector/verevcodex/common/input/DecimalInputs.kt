package com.vector.verevcodex.common.input

private const val DEFAULT_MAX_INTEGER_DIGITS = 6
private const val DEFAULT_MAX_FRACTION_DIGITS = 2

fun sanitizeDecimalInput(
    value: String,
    maxIntegerDigits: Int = DEFAULT_MAX_INTEGER_DIGITS,
    maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
): String {
    val filtered = buildString(value.length) {
        value.forEach { char ->
            if (char.isDigit() || char == '.') append(char)
        }
    }
    val firstDot = filtered.indexOf('.')
    if (firstDot == -1) {
        return filtered.take(maxIntegerDigits)
    }

    val integerPart = filtered.substring(0, firstDot).take(maxIntegerDigits)
    val fractionPart = filtered
        .substring(firstDot + 1)
        .replace(".", "")
        .take(maxFractionDigits)

    return buildString {
        append(integerPart)
        append('.')
        append(fractionPart)
    }
}
