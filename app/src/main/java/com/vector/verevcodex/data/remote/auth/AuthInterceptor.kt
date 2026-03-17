package com.vector.verevcodex.data.remote.auth

import android.os.Build
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        runBlocking {
            tokenStore.getAccessToken()?.let { token ->
                request.addHeader("Authorization", "Bearer $token")
            }
        }
        request.addHeader("X-Device-Id", deviceId())
        request.addHeader("Accept", "application/json")
        request.addHeader("Content-Type", "application/json")
        return chain.proceed(request.build())
    }

    private fun deviceId(): String = "android-${Build.MANUFACTURER}-${Build.MODEL}-${Build.VERSION.SDK_INT}"
}
