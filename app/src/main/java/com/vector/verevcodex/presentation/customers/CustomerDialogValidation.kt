package com.vector.verevcodex.presentation.customers

internal object CustomerDialogValidation {
    fun validateContact(
        firstName: String,
        email: String,
    ): ContactValidationResult {
        val normalizedName = firstName.trim()
        val normalizedEmail = email.trim()
        return ContactValidationResult(
            firstNameError = normalizedName.isBlank(),
            emailError = normalizedEmail.isNotEmpty() && !normalizedEmail.contains("@"),
        )
    }

    fun validatePointAdjustment(
        deltaText: String,
        reason: String,
    ): PointAdjustmentValidationResult {
        val parsedDelta = deltaText.toIntOrNull()
        return PointAdjustmentValidationResult(
            parsedDelta = parsedDelta,
            deltaError = parsedDelta == null || parsedDelta == 0,
            reasonError = reason.trim().isBlank(),
        )
    }
}

internal data class ContactValidationResult(
    val firstNameError: Boolean,
    val emailError: Boolean,
) {
    val hasErrors: Boolean = firstNameError || emailError
}

internal data class PointAdjustmentValidationResult(
    val parsedDelta: Int?,
    val deltaError: Boolean,
    val reasonError: Boolean,
) {
    val hasErrors: Boolean = deltaError || reasonError
}
