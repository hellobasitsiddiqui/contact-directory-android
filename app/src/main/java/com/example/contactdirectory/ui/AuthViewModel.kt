package com.example.contactdirectory.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactdirectory.data.ApiFactory
import com.example.contactdirectory.data.LoginRequest
import com.example.contactdirectory.data.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/** Screen the app is currently showing. */
sealed interface Screen {
    data object Login : Screen
    data class Welcome(val username: String) : Screen
}

/** Immutable UI state for the login screen. */
data class LoginUiState(
    val serverUrl: String,
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SessionStore(app)

    var uiState by mutableStateOf(
        LoginUiState(serverUrl = store.serverUrl)
    )
        private set

    // If a previous session is stored, start already-logged-in.
    var screen by mutableStateOf<Screen>(
        store.username?.let { Screen.Welcome(it) } ?: Screen.Login
    )
        private set

    fun onServerUrlChange(v: String) { uiState = uiState.copy(serverUrl = v, error = null) }
    fun onUsernameChange(v: String) { uiState = uiState.copy(username = v, error = null) }
    fun onPasswordChange(v: String) { uiState = uiState.copy(password = v, error = null) }

    fun login() {
        val url = uiState.serverUrl.trim()
        val user = uiState.username.trim()
        val pass = uiState.password
        if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            uiState = uiState.copy(error = "Enter server URL, username and password.")
            return
        }
        uiState = uiState.copy(loading = true, error = null)
        store.serverUrl = url // remember the server even before a successful login

        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    ApiFactory.authApi(url).login(LoginRequest(user, pass))
                }
                val name = res.username ?: user
                store.token = res.token
                store.username = name
                uiState = uiState.copy(loading = false, password = "")
                screen = Screen.Welcome(name)
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    401 -> "Invalid username or password."
                    423 -> "Account locked — too many attempts. Try later."
                    else -> "Login failed (HTTP ${e.code()})."
                }
                uiState = uiState.copy(loading = false, error = msg)
            } catch (e: IOException) {
                uiState = uiState.copy(
                    loading = false,
                    error = "Can't reach the server. Check the URL, same Wi-Fi, and that the app is running."
                )
            } catch (e: Exception) {
                uiState = uiState.copy(loading = false, error = "Unexpected error: ${e.message}")
            }
        }
    }

    fun logout() {
        store.clearSession()
        uiState = LoginUiState(serverUrl = store.serverUrl)
        screen = Screen.Login
    }
}
