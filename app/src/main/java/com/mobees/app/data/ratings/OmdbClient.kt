package com.mobees.app.data.ratings

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object OmdbClient {
    const val BASE_URL = "https://www.omdbapi.com/"

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    fun create(
        apiKey: String,
        enableLogging: Boolean = false,
        interceptors: List<Interceptor> = emptyList(),
    ): OmdbApi {
        val auth = Interceptor { chain ->
            val request = chain.request()
            val url = request.url.newBuilder().addQueryParameter("apikey", apiKey).build()
            chain.proceed(request.newBuilder().url(url).build())
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(auth)
            .apply {
                interceptors.forEach { addInterceptor(it) }
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
            .create(OmdbApi::class.java)
    }
}
