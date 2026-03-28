package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.data.remote.api.VerevAuthApi
import com.vector.verevcodex.data.remote.api.auth.RefreshRequestDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * On 401, refreshes the access token using the refresh-only API (no Bearer header)
 * and retries the request with the new token. Prevents logout on token expiry.
 */
@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    @Named("refresh") private val refreshOnlyApi: VerevAuthApi,
) : Authenticator {
    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        if (response.request.url.encodedPath == REFRESH_PATH) return null
        if (response.request.header(AUTHORIZATION_HEADER).isNullOrBlank()) return null
        if (responseCount(response) >= MAX_AUTH_RETRIES) return null

        val newToken = runBlocking {
            refreshMutex.withLock {
                val requestToken = response.request.header(AUTHORIZATION_HEADER)
                    ?.removePrefix(BEARER_PREFIX)
                    ?.trim()
                    .orEmpty()
                val latestAccessToken = tokenStore.getAccessToken()
                if (!latestAccessToken.isNullOrBlank() && latestAccessToken != requestToken) {
                    return@withLock latestAccessToken
                }

                val refreshToken = tokenStore.getRefreshToken() ?: return@withLock null
                val apiResponse = refreshOnlyApi.refresh(RefreshRequestDto(refreshToken = refreshToken))
                if (!apiResponse.isSuccessful) return@withLock null
                val envelope = apiResponse.body()
                val data = envelope?.data ?: return@withLock null
                if (envelope.error != null) return@withLock null
                val accessToken = data.accessToken ?: return@withLock null
                val nextRefreshToken = data.refreshToken ?: refreshToken
                tokenStore.setTokens(accessToken, nextRefreshToken)
                accessToken
            }
        } ?: return null

        return response.request.newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$newToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result += 1
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer "
        const val REFRESH_PATH = "/v1/auth/refresh"
        const val MAX_AUTH_RETRIES = 2
    }
}
