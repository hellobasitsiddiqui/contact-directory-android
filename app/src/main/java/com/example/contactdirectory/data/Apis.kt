package com.example.contactdirectory.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: LogoutRequest): retrofit2.Response<Unit>

    @GET("api/v1/auth/me")
    suspend fun me(): MeResponse

    @POST("api/v1/auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): AuthResponse
}

interface ContactApi {
    @GET("api/v1/contacts")
    suspend fun list(
        @Query("search") search: String?,
        @Query("tag") tag: String?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): Page<Contact>

    @GET("api/v1/contacts/{id}")
    suspend fun get(@Path("id") id: Long): Contact

    @POST("api/v1/contacts")
    suspend fun create(@Body body: ContactRequest): Contact

    @PUT("api/v1/contacts/{id}")
    suspend fun update(@Path("id") id: Long, @Body body: ContactRequest): Contact

    @PATCH("api/v1/contacts/{id}")
    suspend fun patch(@Path("id") id: Long, @Body body: ContactPatch): Contact

    @DELETE("api/v1/contacts/{id}")
    suspend fun delete(@Path("id") id: Long): retrofit2.Response<Unit>

    @GET("api/v1/contacts/trash")
    suspend fun trash(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): Page<Contact>

    @POST("api/v1/contacts/{id}/restore")
    suspend fun restore(@Path("id") id: Long): Contact

    @DELETE("api/v1/contacts/{id}/permanent")
    suspend fun purge(@Path("id") id: Long): retrofit2.Response<Unit>
}
