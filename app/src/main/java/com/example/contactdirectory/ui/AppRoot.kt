package com.example.contactdirectory.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.contactdirectory.data.AppGraph
import com.example.contactdirectory.ui.contacts.ContactDetailScreen
import com.example.contactdirectory.ui.contacts.ContactEditScreen
import com.example.contactdirectory.ui.contacts.ContactsListScreen
import com.example.contactdirectory.ui.contacts.TrashScreen
import com.example.contactdirectory.ui.profile.ProfileScreen

@Composable
fun AppRoot() {
    val nav = remember {
        Navigator(if (AppGraph.store.isLoggedIn) Dest.Contacts else Dest.Login)
    }
    BackHandler(enabled = nav.canPop) { nav.pop() }

    when (val dest = nav.current) {
        Dest.Login -> LoginScreen(onLoggedIn = { nav.replaceAll(Dest.Contacts) })
        Dest.Contacts -> ContactsListScreen(nav)
        is Dest.Detail -> ContactDetailScreen(id = dest.id, nav = nav)
        is Dest.Edit -> ContactEditScreen(id = dest.id, nav = nav)
        Dest.Trash -> TrashScreen(nav)
        Dest.Profile -> ProfileScreen(nav)
    }
}
