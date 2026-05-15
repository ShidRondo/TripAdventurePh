package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

@Composable
fun CheckInScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    var destinations by remember { mutableStateOf<List<Destination>>(emptyList()) }
    var selectedDestinationName by remember { mutableStateOf("") }
    var selectedDestinationId by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        thread {
            val accessToken = sessionManager.getAccessToken().orEmpty()
            destinations = repository.fetchDestinations(accessToken)
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Check-In",
            style = MaterialTheme.typography.headlineSmall
        )

        if (loading) {
            Text("Loading destinations...")
        } else {
            destinations.forEach { destination ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = destination.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("${destination.location} • ${destination.category}")

                        Button(
                            onClick = {
                                selectedDestinationName = destination.name
                                selectedDestinationId = destination.id
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Select")
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = selectedDestinationName,
            onValueChange = {},
            label = { Text("Selected Destination") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        Button(
            onClick = {
                val accessToken = sessionManager.getAccessToken().orEmpty()
                val userId = sessionManager.getUserId().orEmpty()

                if (selectedDestinationId.isBlank()) {
                    message = "Please select a destination first."
                    return@Button
                }

                submitting = true
                message = ""

                thread {
                    val result = repository.submitBasicCheckIn(
                        accessToken = accessToken,
                        userId = userId,
                        destinationId = selectedDestinationId
                    )
                    message = result.message
                    submitting = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !submitting
        ) {
            Text(if (submitting) "Submitting..." else "Submit Check-In")
        }

        if (message.isNotBlank()) {
            Text(message)
        }
    }
}