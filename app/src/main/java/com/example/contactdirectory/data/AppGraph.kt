package com.example.contactdirectory.data

import android.content.Context

/**
 * Tiny manual dependency graph (no DI framework). Holds the single
 * [SessionStore] and lazily (re)builds the [Backend] whenever the server URL
 * changes. Initialised once from MainActivity.
 */
object AppGraph {
    private lateinit var storeRef: SessionStore

    @Volatile
    private var backendRef: Backend? = null

    fun init(context: Context) {
        if (!this::storeRef.isInitialized) {
            storeRef = SessionStore(context.applicationContext)
        }
    }

    val store: SessionStore get() = storeRef

    /** The backend for the current server URL, rebuilt if the URL changed. */
    fun backend(): Backend {
        val url = storeRef.serverUrl
        val existing = backendRef
        if (existing == null || existing.baseUrl != url) {
            return Backend(url, storeRef).also { backendRef = it }
        }
        return existing
    }
}
