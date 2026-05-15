package io.github.sayaka04.androidremoteclient.api

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Note: If testing on Android Emulator, use 10.0.2.2 instead of 127.0.0.1
    private var BASE_URL = "http://10.26.140.122/remote-desktop-agent/laravel-server/public/api/"
    private var TOKEN = "1|fnccfhDzYt3ivRavfkGj7W2RVjCsfnN8JcN56MaX39756c7a"

    fun setToken(newToken: String) {
        TOKEN = newToken
    }

    fun setApiBaseUrl(newUrl: String) {
        // Retrofit requires the base URL to end with a slash

        val formattedUrl = if (newUrl.endsWith("api/")) {
            newUrl
        } else {
            "${newUrl}api/"
        }
        Log.e("BASE_URL", "Updated BASE_URL = $BASE_URL")

        BASE_URL = formattedUrl
    }


    val service: RemoteApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $TOKEN")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RemoteApiService::class.java)
    }
}