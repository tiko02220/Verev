package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.data.remote.api.VerevAuthApi
import com.vector.verevcodex.data.remote.api.auth.RefreshRequestDto
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

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        if (response.priorResponse != null) return null

        val newToken = runBlocking {
            val refreshToken = tokenStore.getRefreshToken() ?: return@runBlocking null
            val apiResponse = refreshOnlyApi.refresh(RefreshRequestDto(refreshToken = refreshToken))
            if (!apiResponse.isSuccessful) return@runBlocking null
            val envelope = apiResponse.body()
            val data = envelope?.data ?: return@runBlocking null
            if (envelope.error != null) return@runBlocking null
            val accessToken = data.accessToken ?: return@runBlocking null
            val nextRefreshToken = data.refreshToken ?: refreshToken
            tokenStore.setTokens(accessToken, nextRefreshToken)
            accessToken
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }
}
