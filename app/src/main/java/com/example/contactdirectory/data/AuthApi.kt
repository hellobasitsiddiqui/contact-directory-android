package com.example.contactdirectory.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/** Request/response shapes for the Contact Directory auth API. */
data class LoginRequest(val username: String, val password: String)

/**
 * Mirrors the server's AuthResponse. Only the fields this app needs are mapped;
 * Gson ignores the rest (refreshToken, expiresInMs, …).
 */
data class LoginResponse(
    val token: String?,
    val username: String?,
    val role: String?,
)

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}

/**
 * Builds an [AuthApi] for a user-entered base URL (e.g. http://192.168.1.50:8080).
 * The service is rebuilt per server URL since Retrofit's base URL is fixed at
 * construction — this lets the user point the app at any LAN address at runtime.
 */
object ApiFactory {
    fun authApi(baseUrl: String): AuthApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalized)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
