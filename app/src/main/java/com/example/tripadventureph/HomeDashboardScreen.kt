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

@Composable
fun HomeDashboardScreen(modifier: Modifier = Modifier) {
    val quickStats = listOf(
        "In-App TRIPIX Balance: 1200",
        "Visited Places: 0",
        "Unlocked Badges: 0",
        "Joined Events: 0"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "TravelQuest Dashboard",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        items(quickStats) { stat ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stat,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
                    Text("• Check in to a destination")
                    Text("• Explore places")
                    Text("• View wallet")
                    Text("• Complete profile details")
                }
            }
        }
    }
}