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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class SampleDestination(
    val name: String,
    val category: String,
    val location: String,
    val difficulty: String,
    val reward: Int,
)

@Composable
fun DiscoverScreen(modifier: Modifier = Modifier) {
    val destinations = listOf(
        SampleDestination("Osmeña Peak", "Hiking", "Dalaguete, Cebu", "Moderate", 30),
        SampleDestination("Kawasan Falls", "Falls", "Badian, Cebu", "Easy", 25),
        SampleDestination("Basdaku Beach", "Beach", "Moalboal, Cebu", "Easy", 20)
    )

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

        items(destinations) { destination ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(destination.name, style = MaterialTheme.typography.titleMedium)
                    Text("Category: ${destination.category}")
                    Text("Location: ${destination.location}")
                    Text("Difficulty: ${destination.difficulty}")
                    Text("Reward: ${destination.reward} TRIPIX")
                }
            }
        }
    }
}