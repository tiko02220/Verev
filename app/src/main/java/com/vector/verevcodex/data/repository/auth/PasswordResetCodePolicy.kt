package com.vector.verevcodex.data.repository.auth

import kotlin.random.Random

internal object PasswordResetCodePolicy {
    private const val RESET_CODE_LENGTH = 6
    const val EXPIRATION_MILLIS: Long = 10 * 60 * 1000L

    fun generate(random: Random = Random.Default): String =
        buildString(RESET_CODE_LENGTH) {
            repeat(RESET_CODE_LENGTH) {
                append(random.nextInt(0, 10))
            }
        }

    fun isExpired(issuedAtMillis: Long, nowMillis: Long): Boolean =
        nowMillis - issuedAtMillis > EXPIRATION_MILLIS
}
