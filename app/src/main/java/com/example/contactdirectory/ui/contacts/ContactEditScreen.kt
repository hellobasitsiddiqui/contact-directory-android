package com.example.contactdirectory.ui.contacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.contactdirectory.data.AppGraph
import com.example.contactdirectory.data.ContactRepository
import com.example.contactdirectory.data.ContactRequest
import com.example.contactdirectory.ui.AppBar
import com.example.contactdirectory.ui.Dest
import com.example.contactdirectory.ui.Navigator
import com.example.contactdirectory.ui.errorMessage
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ContactEditScreen(id: Long?, nav: Navigator) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(id != null) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var version by remember { mutableStateOf<Long?>(null) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var favorite by remember { mutableStateOf(false) }

    fun handle(e: Throwable) {
        if (e is HttpException && e.code() == 401) {
            AppGraph.store.clearSession(); nav.replaceAll(Dest.Login)
        } else {
            error = errorMessage(e)
        }
    }

    LaunchedEffect(id) {
        if (id == null) return@LaunchedEffect
        loading = true
        try {
            val c = ContactRepository.getContact(id)
            firstName = c.firstName; lastName = c.lastName; email = c.email
            phone = c.phone.orEmpty(); company = c.company.orEmpty(); notes = c.notes.orEmpty()
            tagsText = c.tags.joinToString(", "); favorite = c.favorite; version = c.version
        } catch (e: Exception) {
            handle(e)
        } finally {
            loading = false
        }
    }

    fun save() {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
            error = "First name, last name and email are required."
            return
        }
        saving = true; error = null
        scope.launch {
            try {
                val body = ContactRequest(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    email = email.trim(),
                    phone = phone.trim().ifBlank { null },
                    company = company.trim().ifBlank { null },
                    tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { null },
                    favorite = favorite,
                    notes = notes.trim().ifBlank { null },
                    version = version,
                )
                if (id == null) ContactRepository.createContact(body)
                else ContactRepository.updateContact(id, body)
                nav.pop()
            } catch (e: Exception) {
                saving = false
                handle(e)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppBar(title = if (id == null) "New contact" else "Edit contact", onBack = { nav.pop() })
        if (loading) {
            CircularProgressIndicator(Modifier.padding(24.dp))
            return@Column
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Field("First name", firstName) { firstName = it }
            Field("Last name", lastName) { lastName = it }
            Field("Email", email) { email = it }
            Field("Phone", phone) { phone = it }
            Field("Company", company) { company = it }
            Field("Tags (comma-separated)", tagsText) { tagsText = it }
            Field("Notes", notes, singleLine = false) { notes = it }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Checkbox(checked = favorite, onCheckedChange = { favorite = it })
                Text("Favourite")
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { save() },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (saving) CircularProgressIndicator(Modifier.height(20.dp)) else Text("Save")
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, singleLine: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}
