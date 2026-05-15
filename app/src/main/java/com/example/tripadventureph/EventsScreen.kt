package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class EventItem(
    val title: String,
    val category: String,
    val difficulty: String,
    val stakeAmount: String,
    val rewardPool: String,
    val joined: Boolean = false
)

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var stakeAmount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val events = remember {
        mutableStateListOf(
            EventItem(
                title = "Cebu Mountain Challenge",
                category = "Hiking",
                difficulty = "Moderate",
                stakeAmount = "100",
                rewardPool = "90"
            )
        )
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
                        value = stakeAmount,
                        onValueChange = { stakeAmount = it },
                        label = { Text("Stake Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                message = "Event title is required."
                            } else {
                                val stake = stakeAmount.toDoubleOrNull() ?: 0.0
                                val rewardPool = (stake * 0.90).toString()

                                events.add(
                                    0,
                                    EventItem(
                                        title = title,
                                        category = category,
                                        difficulty = difficulty,
                                        stakeAmount = stakeAmount,
                                        rewardPool = rewardPool
                                    )
                                )

                                title = ""
                                category = ""
                                difficulty = ""
                                stakeAmount = ""
                                message = "Event created."
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Event")
                    }

                    if (message.isNotBlank()) {
                        Text(message)
                    }
                }
            }
        }

        itemsIndexed(events) { index, event ->
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
                    Text("Stake: ${event.stakeAmount} TRIPIX")
                    Text("Reward Pool: ${event.rewardPool} TRIPIX")
                    Text("Burn: 10%")
                    Text("Remaining for finishers: 90%")

                    Button(
                        onClick = {
                            events[index] = event.copy(joined = true)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !event.joined
                    ) {
                        Text(if (event.joined) "Joined" else "Join Event")
                    }
                }
            }
        }
    }
}