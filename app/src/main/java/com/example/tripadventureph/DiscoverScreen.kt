package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

@Composable
fun DiscoverScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    var destinations by remember { mutableStateOf<List<Destination>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        thread {
            val accessToken = sessionManager.getAccessToken().orEmpty()
            val result = repository.fetchDestinations(accessToken)
            destinations = result
            loading = false
            if (result.isEmpty()) {
                message = "No destinations found."
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Discover Destinations",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        if (loading) {
            item {
                Text("Loading destinations...")
            }
        } else {
            if (message.isNotBlank()) {
                item {
                    Text(message)
                }
            }

            items(destinations) { destination ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = destination.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Category: ${destination.category}")
                        Text("Location: ${destination.location}")
                        Text("Difficulty: ${destination.difficulty}")
                        Text("Reward: ${destination.rewardPoints} TRIPIX")

                        if (destination.description.isNotBlank()) {
                            Text(
                                text = destination.description,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}