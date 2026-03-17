package com.vector.verevcodex.data.remote.core

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import com.vector.verevcodex.data.remote.auth.ApiException
import com.vector.verevcodex.domain.model.common.StaffRole
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

internal inline fun <T, R> Response<ApiEnvelope<T>>.unwrap(block: (T) -> R): R {
    if (!isSuccessful) {
        val msg = body()?.error?.message ?: "HTTP ${code()}"
        throw ApiException(code(), msg)
    }
    val envelope = body() ?: throw ApiException(code(), "Empty response")
    if (envelope.error != null) throw ApiException(code(), envelope.error.message ?: "Unknown API error")
    val data = envelope.data ?: throw ApiException(code(), "No data")
    return block(data)
}

internal inline fun <T, R> Response<ApiEnvelope<T>>.unwrapNullable(block: (T) -> R?): R? {
    if (!isSuccessful) {
        val msg = body()?.error?.message ?: "HTTP ${code()}"
        throw ApiException(code(), msg)
    }
    val envelope = body() ?: return null
    if (envelope.error != null) throw ApiException(code(), envelope.error.message ?: "Unknown API error")
    val data = envelope.data ?: return null
    return block(data)
}

internal fun <T> requireRemoteValue(value: T?, message: String): T =
    value ?: throw ApiException(500, message)

internal fun Int?.orZero(): Int = this ?: 0
internal fun Long?.orZero(): Long = this ?: 0L
internal fun Double?.orZero(): Double = this ?: 0.0
internal fun Float?.orZero(): Float = this ?: 0f
internal fun Boolean?.orFalse(): Boolean = this ?: false

internal fun parseRemoteLocalDate(value: String?, fallback: LocalDate = LocalDate.now()): LocalDate =
    runCatching { LocalDate.parse(value.orEmpty().take(10)) }.getOrElse { fallback }

internal fun parseRemoteInstant(value: String?, fallback: Instant = Instant.now()): Instant =
    runCatching { Instant.parse(value.orEmpty()) }.getOrElse { fallback }

internal fun parseRemoteStaffRole(value: String?, fallback: StaffRole = StaffRole.STAFF): StaffRole =
    runCatching {
        StaffRole.valueOf(
            value
                .orEmpty()
                .trim()
                .uppercase()
                .replace("-", "_")
                .replace(" ", "_"),
        )
    }.getOrElse { fallback }

internal enum class RemoteIdempotencyDomain {
    HTTP,
    STAFF,
    CUSTOMER,
    STORE,
    TRANSACTION,
    BILLING,
    PROGRAM,
    REWARD,
    CAMPAIGN,
    CHECK_IN,
    REPORT,
}

internal enum class RemoteIdempotencyAction {
    CREATE,
    BULK_CREATE,
    QUICK_REGISTER,
    UPDATE,
    UPSERT,
    PATCH,
    DELETE,
    ENABLE,
    DISABLE,
    ACTIVATE,
    DEACTIVATE,
    EXPORT,
    SET_DEFAULT,
    ADJUST_POINTS,
    ADJUST_VISITS,
}

internal fun buildRemoteIdempotencyKey(
    domain: RemoteIdempotencyDomain,
    action: RemoteIdempotencyAction,
    vararg parts: String?,
): String = buildRemoteIdempotencyKey("$domain:$action", *parts)

internal fun buildHttpRemoteIdempotencyKey(
    method: String,
    path: String,
    body: String,
): String = buildRemoteIdempotencyKey(
    domain = RemoteIdempotencyDomain.HTTP,
    action = when (method.uppercase()) {
        "POST" -> RemoteIdempotencyAction.CREATE
        "PUT" -> RemoteIdempotencyAction.UPDATE
        "PATCH" -> RemoteIdempotencyAction.PATCH
        "DELETE" -> RemoteIdempotencyAction.DELETE
        else -> RemoteIdempotencyAction.UPDATE
    },
    path,
    body,
)

private fun buildRemoteIdempotencyKey(scope: String, vararg parts: String?): String {
    val payload = buildString {
        append(scope)
        parts.forEach { part ->
            append('|')
            append(part.orEmpty().trim())
        }
    }
    return "$scope:${UUID.nameUUIDFromBytes(payload.toByteArray())}"
}
