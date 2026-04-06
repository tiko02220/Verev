package com.vector.verevcodex.presentation.auth.common

import androidx.annotation.StringRes
import com.vector.verevcodex.R

@StringRes
fun authErrorRes(errorKey: String?): Int? = when (errorKey) {
    "required_email" -> R.string.auth_error_required_email
    "invalid_email" -> R.string.auth_error_invalid_email
    "required_password" -> R.string.auth_error_required_password
    "password_short" -> R.string.auth_error_password_short
    "password_confirm" -> R.string.auth_error_password_confirm
    "activation_failed" -> R.string.auth_force_password_setup_failed
    "required_business_name" -> R.string.auth_error_required_business_name
    "required_industry" -> R.string.auth_error_required_industry
    "required_address" -> R.string.auth_error_required_address
    "required_city" -> R.string.auth_error_required_city
    "required_zip_code" -> R.string.auth_error_required_zip_code
    "required_phone" -> R.string.auth_error_required_phone
    "required_full_name" -> R.string.auth_error_required_full_name
    "invalid_credentials" -> R.string.auth_error_invalid_credentials
    "connection_failed" -> R.string.auth_error_connection_failed
    "signup_email_not_verified" -> R.string.auth_error_signup_email_not_verified
    "code_incomplete" -> R.string.auth_error_code_incomplete
    "code_invalid" -> R.string.auth_error_code_invalid
    "reset_failed" -> R.string.auth_error_reset_failed
    "auth_pin_invalid" -> R.string.auth_pin_invalid
    "biometric_failed" -> R.string.auth_biometric_failed
    "pin_length" -> R.string.auth_pin_error_length
    "pin_mismatch" -> R.string.auth_pin_error_mismatch
    "staff_incomplete" -> R.string.auth_staff_error_incomplete
    "staff_phone_invalid" -> R.string.merchant_staff_error_phone
    "staff_password_short" -> R.string.auth_staff_error_password_short
    "staff_failed" -> R.string.auth_staff_error_failed
    "staff_missing_store" -> R.string.merchant_staff_error_missing_store
    else -> null
}
