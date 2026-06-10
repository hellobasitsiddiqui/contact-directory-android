package com.example.contactdirectory.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Adds the bearer access token to every request (read fresh from the store, so
 * a token refreshed mid-session is picked up on the next call).
 */
private class AuthInterceptor(private val store: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = store.token
        val request = if (token.isNullOrEmpty()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}

/**
 * The HTTP stack for one server base URL. Rebuilt by [AppGraph] when the user
 * changes the server URL. Exposes the typed APIs.
 */
class Backend(val baseUrl: String, store: SessionStore) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(store))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val auth: AuthApi = retrofit.create(AuthApi::class.java)
    val contacts: ContactApi = retrofit.create(ContactApi::class.java)
}
