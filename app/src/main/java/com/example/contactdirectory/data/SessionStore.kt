package com.example.contactdirectory.data

import android.content.Context

/**
 * SharedPreferences-backed session: the server URL (persists across restarts)
 * plus the access token, refresh token, username and role from the last login.
 * Plain prefs are fine for this personal demo.
 */
class SessionStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("contact_directory", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    /** Persists a token pair from login/register/refresh/change-password. */
    fun savePair(res: AuthResponse) {
        token = res.token
        if (!res.refreshToken.isNullOrEmpty()) refreshToken = res.refreshToken
        if (!res.username.isNullOrEmpty()) username = res.username
        if (!res.role.isNullOrEmpty()) role = res.role
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN).remove(KEY_REFRESH).remove(KEY_USERNAME).remove(KEY_ROLE)
            .apply()
    }

    val isLoggedIn: Boolean get() = !token.isNullOrEmpty()

    companion object {
        const val DEFAULT_SERVER_URL = "http://10.0.2.2:8080"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_TOKEN = "token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
    }
}
