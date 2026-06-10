package com.example.contactdirectory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import retrofit2.HttpException
import java.io.IOException

/** A simple top bar (avoids the experimental Material3 TopAppBar API). */
@Composable
fun AppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                TextButton(onClick = onBack) { Text("‹ Back") }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            )
            actions()
        }
    }
}

/** Coloured circle with the contact's initials (no photo loading needed). */
@Composable
fun InitialsAvatar(initials: String, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/** Turns a thrown error into a short, user-readable message. */
fun errorMessage(e: Throwable): String = when {
    e is HttpException -> when (e.code()) {
        401 -> "Session expired — please sign in again."
        403 -> "You don't have permission to do that."
        404 -> "Not found."
        409 -> "Conflict — that already exists (e.g. a duplicate email)."
        412 -> "This contact changed elsewhere — reload and try again."
        423 -> "Account locked — too many attempts. Try later."
        else -> "Request failed (HTTP ${e.code()})."
    }
    e is IOException -> "Can't reach the server. Check the URL and that you're on the same Wi-Fi."
    else -> "Unexpected error: ${e.message}"
}
