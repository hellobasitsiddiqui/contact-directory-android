package com.example.contactdirectory.ui.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contactdirectory.data.AppGraph
import com.example.contactdirectory.data.Contact
import com.example.contactdirectory.data.ContactRepository
import com.example.contactdirectory.ui.AppBar
import com.example.contactdirectory.ui.Dest
import com.example.contactdirectory.ui.InitialsAvatar
import com.example.contactdirectory.ui.Navigator
import com.example.contactdirectory.ui.errorMessage
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ContactsViewModel : ViewModel() {
    var search by mutableStateOf("")
        private set
    private val items = mutableStateListOf<Contact>()
    val contacts: List<Contact> get() = items

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    private var page = 0
    var hasMore by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    fun onSearch(v: String) { search = v }

    fun refresh() = load(0, replace = true)

    fun loadMore() {
        if (hasMore && !loading) load(page + 1, replace = false)
    }

    private fun load(p: Int, replace: Boolean) {
        loading = true
        error = null
        viewModelScope.launch {
            try {
                val result = ContactRepository.listContacts(search, p)
                if (replace) items.clear()
                items.addAll(result.content)
                page = result.number
                hasMore = !result.last
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 401) sessionExpired = true
                else error = errorMessage(e)
            } finally {
                loading = false
            }
        }
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            try {
                val updated = ContactRepository.setFavorite(contact.id, !contact.favorite)
                val idx = items.indexOfFirst { it.id == updated.id }
                if (idx >= 0) items[idx] = updated
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 401) sessionExpired = true
                else error = errorMessage(e)
            }
        }
    }
}

@Composable
fun ContactsListScreen(nav: Navigator, vm: ContactsViewModel = viewModel()) {
    LaunchedEffect(vm.sessionExpired) {
        if (vm.sessionExpired) {
            AppGraph.store.clearSession()
            nav.replaceAll(Dest.Login)
        }
    }
    // Re-fetch when we return to this screen (e.g. after add/edit/delete).
    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize()) {
        AppBar(title = "Contacts") {
            TextButton(onClick = { nav.push(Dest.Trash) }) { Text("Trash") }
            TextButton(onClick = { nav.push(Dest.Profile) }) { Text("Profile") }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = vm.search,
                onValueChange = vm::onSearch,
                label = { Text("Search") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.refresh() }),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.padding(4.dp))
            Button(onClick = { nav.push(Dest.Edit(null)) }) { Text("+ New") }
        }

        vm.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        if (vm.contacts.isEmpty() && !vm.loading) {
            Text(
                "No contacts yet. Tap “+ New” to add one.",
                modifier = Modifier.padding(24.dp),
            )
        }

        LazyColumn(Modifier.weight(1f)) {
            items(vm.contacts, key = { it.id }) { c ->
                ContactRow(
                    contact = c,
                    onClick = { nav.push(Dest.Detail(c.id)) },
                    onToggleFavorite = { vm.toggleFavorite(c) },
                )
                Divider()
            }
            item {
                if (vm.loading) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) { CircularProgressIndicator() }
                } else if (vm.hasMore) {
                    TextButton(
                        onClick = { vm.loadMore() },
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                    ) { Text("Load more") }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialsAvatar(contact.initials)
        Column(
            Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(contact.fullName.ifBlank { contact.email }, style = MaterialTheme.typography.titleMedium)
            val sub = listOfNotNull(contact.company?.ifBlank { null }, contact.email).firstOrNull()
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = onToggleFavorite) {
            Text(if (contact.favorite) "★" else "☆", style = MaterialTheme.typography.titleLarge)
        }
    }
}
