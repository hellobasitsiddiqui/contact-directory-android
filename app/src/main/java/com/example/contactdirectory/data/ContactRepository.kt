package com.example.contactdirectory.data

import retrofit2.HttpException

/**
 * Data access for auth + contacts. Every authenticated call is wrapped so that
 * a `401` triggers one silent refresh-token rotation and a single retry — the
 * mobile equivalent of the web app's silent refresh. A `401` that survives the
 * retry means the session is truly dead (caller should return to login).
 */
object ContactRepository {

    private val store get() = AppGraph.store

    // ---- auth ----------------------------------------------------------------

    suspend fun login(serverUrl: String, username: String, password: String): AuthResponse {
        store.serverUrl = serverUrl.trim()
        val res = AppGraph.backend().auth.login(LoginRequest(username, password))
        store.savePair(res)
        return res
    }

    suspend fun me(): MeResponse = authed { AppGraph.backend().auth.me() }

    suspend fun changePassword(current: String, new: String): AuthResponse = authed {
        val res = AppGraph.backend().auth.changePassword(ChangePasswordRequest(current, new))
        store.savePair(res) // server returns a fresh pair so this session continues
        res
    }

    /** Best-effort server-side logout, then clears local session regardless. */
    suspend fun logout() {
        try {
            store.refreshToken?.let { AppGraph.backend().auth.logout(LogoutRequest(it)) }
        } catch (_: Exception) {
            // ignore — local sign-out always wins
        } finally {
            store.clearSession()
        }
    }

    // ---- contacts ------------------------------------------------------------

    suspend fun listContacts(search: String?, page: Int, size: Int = PAGE_SIZE): Page<Contact> =
        authed {
            AppGraph.backend().contacts.list(
                search?.trim()?.ifBlank { null }, null, page, size, "lastName,asc"
            )
        }

    suspend fun getContact(id: Long): Contact = authed { AppGraph.backend().contacts.get(id) }

    suspend fun createContact(body: ContactRequest): Contact =
        authed { AppGraph.backend().contacts.create(body) }

    suspend fun updateContact(id: Long, body: ContactRequest): Contact =
        authed { AppGraph.backend().contacts.update(id, body) }

    suspend fun setFavorite(id: Long, favorite: Boolean): Contact =
        authed { AppGraph.backend().contacts.patch(id, ContactPatch(favorite = favorite)) }

    suspend fun deleteContact(id: Long) {
        authed { AppGraph.backend().contacts.delete(id) }
    }

    suspend fun listTrash(page: Int, size: Int = PAGE_SIZE): Page<Contact> =
        authed { AppGraph.backend().contacts.trash(page, size, "deletedAt,desc") }

    suspend fun restore(id: Long): Contact = authed { AppGraph.backend().contacts.restore(id) }

    suspend fun purge(id: Long) {
        authed { AppGraph.backend().contacts.purge(id) }
    }

    // ---- refresh-retry plumbing ---------------------------------------------

    private suspend fun tryRefresh(): Boolean {
        val rt = store.refreshToken ?: return false
        return try {
            store.savePair(AppGraph.backend().auth.refresh(RefreshRequest(rt)))
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun <T> authed(block: suspend () -> T): T = try {
        block()
    } catch (e: HttpException) {
        if (e.code() == 401 && tryRefresh()) block() else throw e
    }

    const val PAGE_SIZE = 20
}
