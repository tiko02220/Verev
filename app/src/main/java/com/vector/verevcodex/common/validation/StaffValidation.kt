package com.vector.verevcodex.common.validation

const val MIN_STAFF_PASSWORD_LENGTH = 10

fun isValidStaffPassword(value: String): Boolean = value.length >= MIN_STAFF_PASSWORD_LENGTH
