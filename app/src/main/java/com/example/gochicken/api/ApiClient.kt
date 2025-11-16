package com.example.gochicken.api

import android.content.Context
import android.util.Log
import com.example.gochicken.utils.Prefs
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://172.16.174.167:8000/api/"

    private var retrofit: Retrofit? = null
    private var prefs: Prefs? = null

    fun initialize(context: Context) {
        prefs = Prefs(context) // Create Prefs instance from Context
        retrofit = createRetrofit()
    }

    private fun createRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = prefs?.getToken()
        val userRole = prefs?.getUserRole()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        // Add authorization header if token and role exist exists
        token?.let {
            if (it.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $it")

                // Also add user role header if available
                userRole?.let { role ->
                    requestBuilder.header("X-User-Role", role)
                }
            }
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    // Public instance for login and public routes (no auth required)
    val publicInstance: ApiService
        get() {
            if (retrofit == null) {
                // Create basic instance without auth for login
                val client = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(ApiService::class.java)
        }

    // Authenticated instance (requires initialization)
    val instance: ApiService
        get() {
            if (retrofit == null) {
                throw IllegalStateException("ApiClient must be initialized first. Call ApiClient.initialize(context) in your Application class or MainActivity.")
            }
            return retrofit!!.create(ApiService::class.java)
        }
}