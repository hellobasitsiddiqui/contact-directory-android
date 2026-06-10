package com.example.contactdirectory.data

/** Mirrors the server's AuthResponse (extra fields like expiresInMs are ignored). */
data class AuthResponse(
    val token: String? = null,
    val refreshToken: String? = null,
    val username: String? = null,
    val role: String? = null,
)

data class LoginRequest(val username: String, val password: String)
data class RefreshRequest(val refreshToken: String)
data class LogoutRequest(val refreshToken: String?)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

/** Current-user response from GET /api/v1/auth/me. */
data class MeResponse(
    val username: String? = null,
    val role: String? = null,
    val createdAt: String? = null,
)

/** A contact as returned by the API. */
data class Contact(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String? = null,
    val company: String? = null,
    val photoUrl: String? = null,
    val tags: List<String> = emptyList(),
    val favorite: Boolean = false,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null,
    val version: Long = 0,
) {
    val fullName: String get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
    val initials: String
        get() = (firstName.firstOrNull()?.toString().orEmpty() +
                lastName.firstOrNull()?.toString().orEmpty()).uppercase().ifBlank { "?" }
}

/** Create/replace body for POST and PUT /api/v1/contacts. */
data class ContactRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val company: String? = null,
    val tags: List<String>? = null,
    val favorite: Boolean = false,
    val notes: String? = null,
    val version: Long? = null,
)

/** Partial-update body for PATCH /api/v1/contacts/{id} (used for the favourite toggle). */
data class ContactPatch(
    val favorite: Boolean? = null,
)

/** Spring Data Page envelope — only the fields the app uses are mapped. */
data class Page<T>(
    val content: List<T> = emptyList(),
    val number: Int = 0,
    val size: Int = 20,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
)
