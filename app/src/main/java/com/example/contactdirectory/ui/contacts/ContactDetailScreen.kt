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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.contactdirectory.data.AppGraph
import com.example.contactdirectory.data.Contact
import com.example.contactdirectory.data.ContactRepository
import com.example.contactdirectory.ui.AppBar
import com.example.contactdirectory.ui.Dest
import com.example.contactdirectory.ui.Navigator
import com.example.contactdirectory.ui.errorMessage
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ContactDetailScreen(id: Long, nav: Navigator) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var contact by remember { mutableStateOf<Contact?>(null) }
    var loading by remember { mutableStateOf(true) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun handle(e: Throwable) {
        if (e is HttpException && e.code() == 401) {
            AppGraph.store.clearSession(); nav.replaceAll(Dest.Login)
        } else {
            error = errorMessage(e)
        }
    }

    LaunchedEffect(id) {
        loading = true
        try {
            contact = ContactRepository.getContact(id)
        } catch (e: Exception) {
            handle(e)
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppBar(title = "Contact", onBack = { nav.pop() }) {
            if (contact != null) TextButton(onClick = { nav.push(Dest.Edit(id)) }) { Text("Edit") }
        }

        when {
            loading -> CircularProgressIndicator(Modifier.padding(24.dp))
            contact == null -> Text(error ?: "Not found", Modifier.padding(24.dp))
            else -> {
                val c = contact!!
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                ) {
                    Text(
                        c.fullName.ifBlank { c.email },
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    if (c.favorite) Text("★ Favourite", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))

                    LabeledLink("Email", c.email) { uriHandler.openUri("mailto:${c.email}") }
                    c.phone?.takeIf { it.isNotBlank() }?.let { p ->
                        LabeledLink("Phone", p) { uriHandler.openUri("tel:$p") }
                    }
                    c.company?.takeIf { it.isNotBlank() }?.let { Labeled("Company", it) }
                    if (c.tags.isNotEmpty()) Labeled("Tags", c.tags.joinToString(", "))
                    c.notes?.takeIf { it.isNotBlank() }?.let { Labeled("Notes", it) }

                    error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            enabled = !working,
                            onClick = {
                                scope.launch {
                                    working = true; error = null
                                    try {
                                        contact = ContactRepository.setFavorite(id, !c.favorite)
                                    } catch (e: Exception) { handle(e) } finally { working = false }
                                }
                            },
                        ) { Text(if (c.favorite) "Unfavourite" else "Favourite") }
                        Spacer(Modifier.padding(6.dp))
                        Button(
                            enabled = !working,
                            onClick = {
                                scope.launch {
                                    working = true; error = null
                                    try {
                                        ContactRepository.deleteContact(id)
                                        nav.pop()
                                    } catch (e: Exception) { handle(e); working = false }
                                }
                            },
                        ) { Text("Delete") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Labeled(label: String, value: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LabeledLink(label: String, value: String, onClick: () -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        TextButton(onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
