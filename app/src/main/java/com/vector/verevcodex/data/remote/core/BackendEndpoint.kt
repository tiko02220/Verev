package com.vector.verevcodex.data.remote.core

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class BackendEndpoint(
    val httpBaseUrl: String,
    val webSocketUrl: String,
    val socketHost: String,
    val isConfigured: Boolean,
) {
    companion object {
        private const val DEFAULT_SOCKET_PATH = "ws"
        private const val UNCONFIGURED_HOST = "backend.invalid"

        fun from(rawValue: String): BackendEndpoint {
            val normalizedInput = rawValue.trim()
            val configured = normalizedInput.isNotBlank()
            val candidate = when {
                configured && normalizedInput.startsWith("http://") -> normalizedInput
                configured && normalizedInput.startsWith("https://") -> normalizedInput
                configured -> "http://$normalizedInput"
                else -> "http://$UNCONFIGURED_HOST/"
            }
            val httpUrl = "http://37.252.74.243:4444"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.encodedPath("/")
                ?.build()
                ?: error("Invalid backend base URL: $rawValue")

            val socketScheme = if (httpUrl.isHttps) "wss" else "ws"
            val socketAuthority = buildString {
                append(httpUrl.host)
                if (httpUrl.port != HttpUrlDefaults.DEFAULT_PORTS[httpUrl.scheme]) {
                    append(':').append(httpUrl.port)
                }
            }
            val socketUrl = "$socketScheme://$socketAuthority/$DEFAULT_SOCKET_PATH"

            return BackendEndpoint(
                httpBaseUrl = httpUrl.toString(),
                webSocketUrl = socketUrl.toString(),
                socketHost = httpUrl.host,
                isConfigured = configured,
        )
    }
}

private object HttpUrlDefaults {
    val DEFAULT_PORTS = mapOf(
        "http" to 80,
        "https" to 443,
    )
}
}

internal fun resolveBackendAbsoluteUrl(
    backendEndpoint: BackendEndpoint,
    urlOrPath: String,
): String {
    val normalized = urlOrPath.trim()
    if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
        return normalized
    }
    val baseUrl = backendEndpoint.httpBaseUrl.toHttpUrlOrNull()
        ?: error("Invalid backend base URL: ${backendEndpoint.httpBaseUrl}")
    return baseUrl
        .newBuilder()
        .encodedPath(
            "/" + normalized.trimStart('/'),
        )
        .build()
        .toString()
}
