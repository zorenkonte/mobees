package com.mobees.app.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object TmdbClient {
    const val BASE_URL = "https://api.themoviedb.org/3/"

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    /**
     * Builds the TMDB API. Both v3 API keys (query parameter) and v4 read access tokens
     * (bearer header, they start with "eyJ") are supported.
     */
    fun create(apiKey: String, enableLogging: Boolean = false): TmdbApi {
        val auth = Interceptor { chain ->
            val request = chain.request()
            val authorised = if (apiKey.startsWith("eyJ")) {
                request.newBuilder().header("Authorization", "Bearer $apiKey").build()
            } else {
                val url = request.url.newBuilder().addQueryParameter("api_key", apiKey).build()
                request.newBuilder().url(url).build()
            }
            chain.proceed(authorised)
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(auth)
            .apply {
                if (enableLogging) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
            .build()
            .create(TmdbApi::class.java)
    }
}
