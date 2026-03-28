package com.vector.verevcodex.common.phone

const val DEFAULT_PHONE_COUNTRY_CODE = "+374"
private const val DEFAULT_PHONE_COUNTRY_DIGITS = "374"
private const val DEFAULT_PHONE_LOCAL_DIGIT_COUNT = 8

fun defaultPhoneNumberInput(): String = DEFAULT_PHONE_COUNTRY_CODE

fun sanitizePhoneNumberInput(value: String): String = buildString {
    append(DEFAULT_PHONE_COUNTRY_CODE)
    append(extractLocalDigits(value))
}

fun normalizePhoneNumber(value: String): String = sanitizePhoneNumberInput(value)

fun isValidPhoneNumber(value: String): Boolean = extractLocalDigits(value).length == DEFAULT_PHONE_LOCAL_DIGIT_COUNT

private fun extractLocalDigits(value: String): String {
    val digits = value.filter(Char::isDigit)
    val normalizedLocalDigits = when {
        digits.startsWith(DEFAULT_PHONE_COUNTRY_DIGITS) -> digits.removePrefix(DEFAULT_PHONE_COUNTRY_DIGITS)
        digits.startsWith("0") -> digits.removePrefix("0")
        else -> digits
    }
    return normalizedLocalDigits.take(DEFAULT_PHONE_LOCAL_DIGIT_COUNT)
}
