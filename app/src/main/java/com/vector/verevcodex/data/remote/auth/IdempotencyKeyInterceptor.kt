package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.data.remote.core.buildHttpRemoteIdempotencyKey
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

@Singleton
class IdempotencyKeyInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method in methodsWithoutIdempotency || request.url.encodedPath.startsWith("/v1/auth")) {
            return chain.proceed(request)
        }
        if (request.header(IDEMPOTENCY_HEADER) != null) {
            return chain.proceed(request)
        }

        val idempotentRequest = request.newBuilder()
            .addHeader(
                IDEMPOTENCY_HEADER,
                buildHttpRemoteIdempotencyKey(
                    method = request.method,
                    path = request.url.encodedPath,
                    body = requestBodySnapshot(request),
                ),
            )
            .build()
        return chain.proceed(idempotentRequest)
    }

    private fun requestBodySnapshot(request: okhttp3.Request): String {
        val body = request.body ?: return ""
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
    }

    private companion object {
        const val IDEMPOTENCY_HEADER = "X-Idempotency-Key"
        val methodsWithoutIdempotency = setOf("GET", "HEAD", "OPTIONS")
    }
}
