package com.securescanner.app.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.securescanner.app.data.api.OsintIndustriesApi
import com.securescanner.app.data.api.SecureScannerApi
import com.securescanner.app.data.datastore.SettingsDataStore
import javax.inject.Named
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideDynamicBaseUrlInterceptor(settingsDataStore: SettingsDataStore): Interceptor {
        return Interceptor { chain ->
            val serverUrl = runBlocking { settingsDataStore.serverUrl.first() }
            if (serverUrl.isBlank()) {
                return@Interceptor chain.proceed(chain.request())
            }
            val baseUrl = serverUrl.let { if (!it.endsWith("/")) "$it/" else it }
            val newUrl = baseUrl.toHttpUrlOrNull()
            if (newUrl != null) {
                val originalUrl = chain.request().url
                val updatedUrl = originalUrl.newBuilder()
                    .scheme(newUrl.scheme)
                    .host(newUrl.host)
                    .port(newUrl.port)
                    .build()
                val newRequest = chain.request().newBuilder().url(updatedUrl).build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(chain.request())
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(dynamicBaseUrlInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost:5000/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideSecureScannerApi(retrofit: Retrofit): SecureScannerApi {
        return retrofit.create(SecureScannerApi::class.java)
    }

    @Provides
    @Singleton
    @Named("osintIndustries")
    fun provideOsintIndustriesRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.osint.industries/")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        }
                    )
                    .build()
            )
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideOsintIndustriesApi(@Named("osintIndustries") retrofit: Retrofit): OsintIndustriesApi {
        return retrofit.create(OsintIndustriesApi::class.java)
    }
}
