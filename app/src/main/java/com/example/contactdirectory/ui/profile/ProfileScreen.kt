package com.example.contactdirectory.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.contactdirectory.data.AppGraph
import com.example.contactdirectory.data.ContactRepository
import com.example.contactdirectory.ui.AppBar
import com.example.contactdirectory.ui.Dest
import com.example.contactdirectory.ui.Navigator
import com.example.contactdirectory.ui.errorMessage
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats the server's ISO-8601 timestamp (e.g. "2026-06-11T12:54:25.160303Z")
 * as a friendly local date like "11 Jun 2026". Falls back to the raw string if
 * it can't be parsed, so a format change server-side never blanks the field.
 */
private val MEMBER_SINCE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

private fun formatMemberSince(iso: String): String =
    runCatching {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(MEMBER_SINCE_FORMAT)
    }.getOrDefault(iso)

@Composable
fun ProfileScreen(nav: Navigator) {
    val scope = rememberCoroutineScope()
    val store = AppGraph.store

    var username by remember { mutableStateOf(store.username ?: "—") }
    var role by remember { mutableStateOf(store.role ?: "—") }
    var createdAt by remember { mutableStateOf<String?>(null) }

    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }

    fun handle(e: Throwable) {
        if (e is HttpException && e.code() == 401) {
            store.clearSession(); nav.replaceAll(Dest.Login)
        } else {
            isError = true; message = errorMessage(e)
        }
    }

    LaunchedEffect(Unit) {
        try {
            val me = ContactRepository.me()
            username = me.username ?: username
            role = me.role ?: role
            createdAt = me.createdAt
        } catch (e: Exception) {
            handle(e)
        }
    }

    fun changePassword() {
        if (newPass.length < 6) { isError = true; message = "New password must be at least 6 characters."; return }
        if (newPass != confirm) { isError = true; message = "New password and confirmation don't match."; return }
        working = true; message = null
        scope.launch {
            try {
                ContactRepository.changePassword(current, newPass)
                current = ""; newPass = ""; confirm = ""
                isError = false; message = "Password updated."
            } catch (e: Exception) {
                handle(e)
            } finally {
                working = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppBar(title = "My Profile", onBack = { nav.pop() })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text(username, style = MaterialTheme.typography.headlineSmall)
            Text("Role: $role")
            createdAt?.let { Text("Member since: ${formatMemberSince(it)}") }

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(16.dp))
            Text("Change password", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            PasswordField("Current password", current) { current = it }
            PasswordField("New password", newPass) { newPass = it }
            PasswordField("Confirm new password", confirm) { confirm = it }

            message?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = { changePassword() }, enabled = !working, modifier = Modifier.fillMaxWidth()) {
                Text("Update password")
            }

            Spacer(Modifier.height(28.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        ContactRepository.logout()
                        nav.replaceAll(Dest.Login)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Log out") }
        }
    }
}

@Composable
private fun PasswordField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}
