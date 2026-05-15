package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    var events by remember { mutableStateOf<List<EventModel>>(emptyList()) }
    var myParticipants by remember { mutableStateOf<List<EventParticipant>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var creating by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var stakeAmount by remember { mutableStateOf("") }
    var routeStartName by remember { mutableStateOf("") }
    var routeDestinationName by remember { mutableStateOf("") }
    var routeStartLatitude by remember { mutableStateOf("") }
    var routeStartLongitude by remember { mutableStateOf("") }
    var routeDestinationLatitude by remember { mutableStateOf("") }
    var routeDestinationLongitude by remember { mutableStateOf("") }

    val accessToken = sessionManager.getAccessToken().orEmpty()
    val userId = sessionManager.getUserId().orEmpty()

    fun loadEvents() {
        loading = true
        thread {
            val eventResult = repository.fetchEvents(accessToken)
            val participantResult = repository.fetchMyEventParticipation(
                accessToken = accessToken,
                userId = userId
            )
            events = eventResult
            myParticipants = participantResult
            loading = false
            if (eventResult.isEmpty()) {
                message = "No events found."
            }
        }
    }

    fun participantFor(eventId: String): EventParticipant? {
        return myParticipants.firstOrNull { it.eventId == eventId }
    }

    LaunchedEffect(Unit) {
        loadEvents()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Events",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Create Event",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = { difficulty = it },
                        label = { Text("Difficulty") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = expirationDate,
                        onValueChange = { expirationDate = it },
                        label = { Text("Expiration Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time (HH:MM:SS)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time (HH:MM:SS)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = stakeAmount,
                        onValueChange = { stakeAmount = it },
                        label = { Text("Stake Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = routeStartName,
                        onValueChange = { routeStartName = it },
                        label = { Text("Route Start Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = routeDestinationName,
                        onValueChange = { routeDestinationName = it },
                        label = { Text("Route Destination Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = routeStartLatitude,
                        onValueChange = { routeStartLatitude = it },
                        label = { Text("Route Start Latitude") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = routeStartLongitude,
                        onValueChange = { routeStartLongitude = it },
                        label = { Text("Route Start Longitude") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = routeDestinationLatitude,
                        onValueChange = { routeDestinationLatitude = it },
                        label = { Text("Route Destination Latitude") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = routeDestinationLongitude,
                        onValueChange = { routeDestinationLongitude = it },
                        label = { Text("Route Destination Longitude") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                message = "Event title is required."
                                return@Button
                            }

                            creating = true
                            message = ""

                            thread {
                                val result = repository.createEvent(
                                    accessToken = accessToken,
                                    createdBy = userId,
                                    title = title,
                                    category = category,
                                    difficulty = difficulty,
                                    description = description,
                                    startDate = startDate,
                                    expirationDate = expirationDate,
                                    startTime = startTime,
                                    endTime = endTime,
                                    capacity = capacity.toIntOrNull() ?: 0,
                                    stakeAmount = stakeAmount.toDoubleOrNull() ?: 0.0,
                                    routeStartName = routeStartName,
                                    routeDestinationName = routeDestinationName,
                                    routeStartLatitude = routeStartLatitude.toDoubleOrNull(),
                                    routeStartLongitude = routeStartLongitude.toDoubleOrNull(),
                                    routeDestinationLatitude = routeDestinationLatitude.toDoubleOrNull(),
                                    routeDestinationLongitude = routeDestinationLongitude.toDoubleOrNull()
                                )

                                message = result.message
                                creating = false

                                if (result.success) {
                                    title = ""
                                    category = ""
                                    difficulty = ""
                                    description = ""
                                    startDate = ""
                                    expirationDate = ""
                                    startTime = ""
                                    endTime = ""
                                    capacity = ""
                                    stakeAmount = ""
                                    routeStartName = ""
                                    routeDestinationName = ""
                                    routeStartLatitude = ""
                                    routeStartLongitude = ""
                                    routeDestinationLatitude = ""
                                    routeDestinationLongitude = ""
                                    loadEvents()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !creating
                    ) {
                        Text(if (creating) "Creating..." else "Create Event")
                    }

                    if (message.isNotBlank()) {
                        Text(message)
                    }
                }
            }
        }

        if (loading) {
            item {
                Text("Loading events...")
            }
        } else {
            items(events) { event ->
                val participant = participantFor(event.id)
                val joined = participant?.joined == true
                val verifiedStart = participant?.verifiedStart == true
                val completed = participant?.completed == true
                val rewardClaimed = participant?.rewardClaimed == true

                val possibleReward = if (event.capacity > 0) {
                    event.remainingRewardPool / event.capacity
                } else {
                    0.0
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Category: ${event.category}")
                        Text("Difficulty: ${event.difficulty}")
                        Text("Description: ${event.description}")
                        Text("Start Date: ${event.startDate}")
                        Text("Expiration Date: ${event.expirationDate}")
                        Text("Start Time: ${event.startTime}")
                        Text("End Time: ${event.endTime}")
                        Text("Capacity: ${event.capacity}")
                        Text("Stake: ${event.stakeAmount} TRIPIX")
                        Text("Burn: ${event.burnAmount} TRIPIX")
                        Text("Reward Pool: ${event.rewardPool} TRIPIX")
                        Text("Remaining Reward Pool: ${event.remainingRewardPool} TRIPIX")
                        Text("Estimated Reward Per Finisher: $possibleReward TRIPIX")
                        Text("Route Start: ${event.routeStartName}")
                        Text("Route Destination: ${event.routeDestinationName}")
                        Text("Status: ${event.status}")

                        Button(
                            onClick = {
                                thread {
                                    val result = repository.joinEvent(
                                        accessToken = accessToken,
                                        eventId = event.id,
                                        userId = userId
                                    )
                                    message = result.message
                                    if (result.success) {
                                        loadEvents()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !joined
                        ) {
                            Text(if (joined) "Joined" else "Join Event")
                        }

                        Button(
                            onClick = {
                                thread {
                                    val result = repository.verifyEventStart(
                                        accessToken = accessToken,
                                        eventId = event.id,
                                        userId = userId
                                    )
                                    message = result.message
                                    if (result.success) {
                                        loadEvents()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = joined && !verifiedStart
                        ) {
                            Text(if (verifiedStart) "Start Verified" else "Verify Start")
                        }

                        Button(
                            onClick = {
                                thread {
                                    val result = repository.completeEvent(
                                        accessToken = accessToken,
                                        eventId = event.id,
                                        userId = userId
                                    )
                                    message = result.message
                                    if (result.success) {
                                        loadEvents()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = verifiedStart && !completed
                        ) {
                            Text(if (completed) "Completed" else "Complete Event")
                        }

                        Button(
                            onClick = {
                                thread {
                                    val result = repository.claimEventReward(
                                        accessToken = accessToken,
                                        eventId = event.id,
                                        userId = userId,
                                        rewardAmount = possibleReward
                                    )
                                    message = result.message
                                    if (result.success) {
                                        loadEvents()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = completed && !rewardClaimed
                        ) {
                            Text(if (rewardClaimed) "Reward Claimed" else "Claim Reward")
                        }
                    }
                }
            }
        }
    }
}