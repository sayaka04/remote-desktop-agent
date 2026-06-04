package io.github.sayaka04.androidremoteclient.api

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var baseUrl: String = "http://localhost/api/"
    private var token: String? = null
    private var retrofit: Retrofit? = null

    // Required for PreferenceDatastoreUtil references
    fun setApiBaseUrl(newUrl: String) {
        val formattedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        if (this.baseUrl != formattedUrl) {
            this.baseUrl = formattedUrl
            this.retrofit = null // Reset to force rebuild
            Log.d("API_CLIENT", "URL Updated and Retrofit reset. New URL: $baseUrl")
        } else {
            Log.d("API_CLIENT", "URL is the same, no update needed: $baseUrl")
        }
    }

    fun setToken(newToken: String?) {
        if (this.token != newToken) {
            this.token = newToken
            this.retrofit = null // Reset to force rebuild
            Log.d("API_CLIENT", "Token Updated and Retrofit reset. Token is blank? ${newToken.isNullOrBlank()}")
        }
    }

    private fun getRetrofit(): Retrofit {
        return retrofit ?: synchronized(this) {
            Log.d("API_CLIENT", "Building new Retrofit instance with URL: $baseUrl")
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")

                token?.let {
                    if (it.isNotBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer $it")
                        Log.d("API_CLIENT", "Attached Bearer Token to Request")
                    }
                }
                chain.proceed(requestBuilder.build())
            }.build()

            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build().also { retrofit = it }
        }
    }
    
    val service: RemoteApiService get() = getRetrofit().create(RemoteApiService::class.java)
}