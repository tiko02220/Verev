package com.vector.verevcodex.data.remote.core

import com.google.gson.JsonParser
import com.vector.verevcodex.data.remote.api.ApiEnvelope
import com.vector.verevcodex.data.remote.api.ApiError
import com.vector.verevcodex.domain.model.common.StaffRole
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.CancellationException

class RemoteException(
    val kind: Kind,
    override val message: String,
    val backendCode: String? = null,
    val httpStatus: Int? = null,
    val requestId: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause) {
    enum class Kind {
        Api,
        Connectivity,
        Timeout,
        Data,
        Unexpected,
    }
}

internal suspend inline fun <T> remoteResult(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (throwable: Throwable) {
        if (throwable is CancellationException) {
            throw throwable
        }
        Result.failure(throwable.toRemoteException())
    }

internal fun <T, R> Response<ApiEnvelope<T>>.unwrap(block: (T) -> R): R {
    val parsedError = parseRemoteError()
    if (!isSuccessful) {
        throw parsedError.toRemoteApiException(code())
    }
    val envelope = body() ?: throw RemoteException(
        kind = RemoteException.Kind.Data,
        message = "Received empty server response.",
        httpStatus = code(),
        requestId = parsedError.requestId,
    )
    if (envelope.error != null) throw envelope.error.toRemoteApiException(code(), envelope.meta?.requestId)
    val data = envelope.data ?: throw RemoteException(
        kind = RemoteException.Kind.Data,
        message = "Received server response without data.",
        httpStatus = code(),
        requestId = envelope.meta?.requestId,
    )
    return block(data)
}

internal fun <T, R> Response<ApiEnvelope<T>>.unwrapNullable(block: (T) -> R?): R? {
    val parsedError = parseRemoteError()
    if (!isSuccessful) {
        throw parsedError.toRemoteApiException(code())
    }
    val envelope = body() ?: return null
    if (envelope.error != null) throw envelope.error.toRemoteApiException(code(), envelope.meta?.requestId)
    val data = envelope.data ?: return null
    return block(data)
}

internal fun <T> requireRemoteValue(value: T?, message: String): T =
    value ?: throw RemoteException(
        kind = RemoteException.Kind.Data,
        message = message,
        httpStatus = 500,
    )

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

internal fun Throwable.toRemoteException(): RemoteException = when (this) {
    is RemoteException -> this
    is SocketTimeoutException -> RemoteException(
        kind = RemoteException.Kind.Timeout,
        message = "The server took too long to respond. Please try again.",
        cause = this,
    )
    is ConnectException, is UnknownHostException -> RemoteException(
        kind = RemoteException.Kind.Connectivity,
        message = "Cannot reach the server. Check your connection and try again.",
        cause = this,
    )
    is IOException -> RemoteException(
        kind = RemoteException.Kind.Connectivity,
        message = "The network request failed. Check your connection and try again.",
        cause = this,
    )
    else -> RemoteException(
        kind = RemoteException.Kind.Unexpected,
        message = message?.takeIf { it.isNotBlank() } ?: "Unexpected error. Please try again.",
        cause = this,
    )
}

private data class ParsedRemoteError(
    val backendCode: String?,
    val message: String?,
    val requestId: String?,
)

private fun ParsedRemoteError.toRemoteApiException(httpStatus: Int): RemoteException =
    RemoteException(
        kind = RemoteException.Kind.Api,
        message = message?.takeIf { it.isNotBlank() } ?: "Request failed.",
        backendCode = backendCode,
        httpStatus = httpStatus,
        requestId = requestId,
    )

private fun ApiError.toRemoteApiException(httpStatus: Int?, requestId: String?): RemoteException =
    RemoteException(
        kind = RemoteException.Kind.Api,
        message = message?.takeIf { it.isNotBlank() } ?: "Request failed.",
        backendCode = code,
        httpStatus = httpStatus,
        requestId = requestId,
    )

private fun <T> Response<ApiEnvelope<T>>.parseRemoteError(): ParsedRemoteError {
    val envelope = body()
    if (envelope != null) {
        return ParsedRemoteError(
            backendCode = envelope.error?.code,
            message = envelope.error?.message,
            requestId = envelope.meta?.requestId,
        )
    }

    val raw = errorBody()?.string().orEmpty()
    if (raw.isBlank()) {
        return ParsedRemoteError(
            backendCode = null,
            message = message().takeIf { it.isNotBlank() } ?: "HTTP ${code()}",
            requestId = null,
        )
    }

    return runCatching {
        val json = JsonParser.parseString(raw).asJsonObject
        val error = json.getAsJsonObject("error")
        val meta = json.getAsJsonObject("meta")
        ParsedRemoteError(
            backendCode = error?.get("code")?.takeUnless { it.isJsonNull }?.asString,
            message = error?.get("message")?.takeUnless { it.isJsonNull }?.asString,
            requestId = meta?.get("requestId")?.takeUnless { it.isJsonNull }?.asString,
        )
    }.getOrElse {
        ParsedRemoteError(
            backendCode = null,
            message = message().takeIf { it.isNotBlank() } ?: "HTTP ${code()}",
            requestId = null,
        )
    }
}
