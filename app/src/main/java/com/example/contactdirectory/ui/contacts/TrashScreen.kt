package com.example.contactdirectory.ui.contacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun TrashScreen(nav: Navigator) {
    val scope = rememberCoroutineScope()
    val items = remember { mutableStateListOf<Contact>() }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun handle(e: Throwable) {
        if (e is HttpException && e.code() == 401) {
            AppGraph.store.clearSession(); nav.replaceAll(Dest.Login)
        } else {
            error = errorMessage(e)
        }
    }

    suspend fun reload() {
        loading = true; error = null
        try {
            val page = ContactRepository.listTrash(0)
            items.clear(); items.addAll(page.content)
        } catch (e: Exception) {
            handle(e)
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(Modifier.fillMaxSize()) {
        AppBar(title = "Trash", onBack = { nav.pop() })
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp)) }
        if (loading) {
            CircularProgressIndicator(Modifier.padding(24.dp))
        } else if (items.isEmpty()) {
            Text("Trash is empty.", Modifier.padding(24.dp))
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            c.fullName.ifBlank { c.email },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(onClick = {
                            scope.launch {
                                try { ContactRepository.restore(c.id); items.remove(c) }
                                catch (e: Exception) { handle(e) }
                            }
                        }) { Text("Restore") }
                        Spacer(Modifier.padding(4.dp))
                        Button(onClick = {
                            scope.launch {
                                try { ContactRepository.purge(c.id); items.remove(c) }
                                catch (e: Exception) { handle(e) }
                            }
                        }) { Text("Delete") }
                    }
                    Divider()
                }
            }
        }
    }
}
