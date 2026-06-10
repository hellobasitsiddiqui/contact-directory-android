package com.example.contactdirectory.data

import android.content.Context

/**
 * Tiny SharedPreferences-backed store for the server URL (so it survives app
 * restarts) plus the access token and username from the last login. Plain prefs
 * are fine for this personal demo; a production app would use EncryptedSharedPreferences.
 */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("contact_directory", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    fun clearSession() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USERNAME).apply()
    }

    companion object {
        // Emulator -> host is 10.0.2.2; for a real phone replace with the laptop's
        // LAN IP (the app lets you edit this on the login screen).
        const val DEFAULT_SERVER_URL = "http://10.0.2.2:8080"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
    }
}
