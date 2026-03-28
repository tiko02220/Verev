package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import com.vector.verevcodex.data.remote.api.VerevAuthApi
import com.vector.verevcodex.data.remote.api.auth.RefreshRequestDto
import com.vector.verevcodex.data.remote.api.auth.RefreshResponseDto
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response as RetrofitResponse

class TokenRefreshAuthenticatorTest {

    private val tokenStore = mockk<TokenStore>()
    private val authApi = mockk<VerevAuthApi>()
    private val authenticator = TokenRefreshAuthenticator(
        tokenStore = tokenStore,
        refreshOnlyApi = authApi,
    )

    @Test
    fun `authenticate refreshes token and retries request`() {
        coEvery { tokenStore.getAccessToken() } returns "expired-token"
        coEvery { tokenStore.getRefreshToken() } returns "refresh-token"
        coJustRun { tokenStore.setTokens("fresh-token", "next-refresh-token") }
        coEvery {
            authApi.refresh(RefreshRequestDto(refreshToken = "refresh-token"))
        } returns RetrofitResponse.success(
            ApiEnvelope(
                data = RefreshResponseDto(
                    accessToken = "fresh-token",
                    refreshToken = "next-refresh-token",
                ),
            ),
        )

        val retried = authenticator.authenticate(route = null, response = unauthorizedResponse("expired-token"))

        assertEquals("Bearer fresh-token", retried?.header("Authorization"))
        coVerify(exactly = 1) { authApi.refresh(RefreshRequestDto(refreshToken = "refresh-token")) }
        coVerify(exactly = 1) { tokenStore.setTokens("fresh-token", "next-refresh-token") }
    }

    @Test
    fun `authenticate reuses newer stored token without refreshing again`() {
        coEvery { tokenStore.getAccessToken() } returns "fresh-token"

        val retried = authenticator.authenticate(route = null, response = unauthorizedResponse("expired-token"))

        assertEquals("Bearer fresh-token", retried?.header("Authorization"))
        coVerify(exactly = 0) { tokenStore.getRefreshToken() }
        coVerify(exactly = 0) { authApi.refresh(any()) }
        coVerify(exactly = 0) { tokenStore.setTokens(any(), any()) }
    }

    @Test
    fun `authenticate skips refresh endpoint`() {
        val request = Request.Builder()
            .url("https://example.com/v1/auth/refresh")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody())
            .build()

        val retried = authenticator.authenticate(route = null, response = response)

        assertNull(retried)
        coVerify(exactly = 0) { authApi.refresh(any()) }
    }

    private fun unauthorizedResponse(accessToken: String): Response {
        val request = Request.Builder()
            .url("https://example.com/v1/customers")
            .header("Authorization", "Bearer $accessToken")
            .build()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody())
            .build()
    }
}
