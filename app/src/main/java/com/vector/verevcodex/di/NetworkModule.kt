package com.vector.verevcodex.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vector.verevcodex.BuildConfig
import com.vector.verevcodex.data.remote.api.VerevAuthApi
import com.vector.verevcodex.data.remote.api.analytics.VerevAnalyticsApi
import com.vector.verevcodex.data.remote.api.billing.VerevBillingApi
import com.vector.verevcodex.data.remote.api.customer.VerevCustomersApi
import com.vector.verevcodex.data.remote.api.engagement.VerevCheckInsApi
import com.vector.verevcodex.data.remote.api.reports.VerevReportsApi
import com.vector.verevcodex.data.remote.api.store.VerevStoresApi
import com.vector.verevcodex.data.remote.api.loyalty.VerevCampaignsApi
import com.vector.verevcodex.data.remote.api.loyalty.VerevProgramsApi
import com.vector.verevcodex.data.remote.api.loyalty.VerevRewardsApi
import com.vector.verevcodex.data.remote.api.staff.VerevStaffApi
import com.vector.verevcodex.data.remote.api.transactions.VerevTransactionsApi
import com.vector.verevcodex.data.remote.auth.AuthInterceptor
import com.vector.verevcodex.data.remote.auth.IdempotencyKeyInterceptor
import com.vector.verevcodex.data.remote.auth.TokenRefreshAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideBackendBaseUrl(): String = BuildConfig.VEREV_BACKEND_BASE_URL

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().serializeNulls().create()

    private fun baseUrl(): String =
        BuildConfig.VEREV_BACKEND_BASE_URL
            .takeIf { it.isNotBlank() }
            ?.trimEnd('/')
            ?.plus("/")
            ?: "http://localhost/"

    /** OkHttp without auth – used only for token refresh to avoid sending expired Bearer. */
    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshRetrofit(gson: Gson, @Named("refresh") okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshVerevAuthApi(@Named("refresh") retrofit: Retrofit): VerevAuthApi =
        retrofit.create(VerevAuthApi::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        idempotencyKeyInterceptor: IdempotencyKeyInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(idempotencyKeyInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenRefreshAuthenticator)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideVerevAuthApi(retrofit: Retrofit): VerevAuthApi = retrofit.create(VerevAuthApi::class.java)

    @Provides
    @Singleton
    fun provideVerevStoresApi(retrofit: Retrofit): VerevStoresApi = retrofit.create(VerevStoresApi::class.java)

    @Provides
    @Singleton
    fun provideVerevCustomersApi(retrofit: Retrofit): VerevCustomersApi = retrofit.create(VerevCustomersApi::class.java)

    @Provides
    @Singleton
    fun provideVerevCheckInsApi(retrofit: Retrofit): VerevCheckInsApi = retrofit.create(VerevCheckInsApi::class.java)

    @Provides
    @Singleton
    fun provideVerevReportsApi(retrofit: Retrofit): VerevReportsApi = retrofit.create(VerevReportsApi::class.java)

    @Provides
    @Singleton
    fun provideVerevTransactionsApi(retrofit: Retrofit): VerevTransactionsApi = retrofit.create(VerevTransactionsApi::class.java)

    @Provides
    @Singleton
    fun provideVerevProgramsApi(retrofit: Retrofit): VerevProgramsApi = retrofit.create(VerevProgramsApi::class.java)

    @Provides
    @Singleton
    fun provideVerevRewardsApi(retrofit: Retrofit): VerevRewardsApi = retrofit.create(VerevRewardsApi::class.java)

    @Provides
    @Singleton
    fun provideVerevCampaignsApi(retrofit: Retrofit): VerevCampaignsApi = retrofit.create(VerevCampaignsApi::class.java)

    @Provides
    @Singleton
    fun provideVerevStaffApi(retrofit: Retrofit): VerevStaffApi = retrofit.create(VerevStaffApi::class.java)

    @Provides
    @Singleton
    fun provideVerevAnalyticsApi(retrofit: Retrofit): VerevAnalyticsApi = retrofit.create(VerevAnalyticsApi::class.java)

    @Provides
    @Singleton
    fun provideVerevBillingApi(retrofit: Retrofit): VerevBillingApi = retrofit.create(VerevBillingApi::class.java)
}
