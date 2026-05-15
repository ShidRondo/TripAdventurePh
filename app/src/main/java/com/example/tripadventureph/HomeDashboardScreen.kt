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

data class DashboardStat(
    val title: String,
    val value: String
)

@Composable
fun HomeDashboardScreen(
    modifier: Modifier = Modifier
) {
    val quickStats = listOf(
        DashboardStat("In-App TRIPIX Balance", "1200"),
        DashboardStat("Visited Places", "0"),
        DashboardStat("Unlocked Badges", "0"),
        DashboardStat("Joined Events", "0")
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stat.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stat.value,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("• Explore destinations")
                    Text("• Submit a check-in")
                    Text("• Create a feed post")
                    Text("• View wallet")
                    Text("• Update profile")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Auth flow: Ready")
                    Text("Destinations: Connected")
                    Text("Check-In: Basic submission ready")
                    Text("Feed: Basic create/load/delete ready")
                    Text("Profile: Editable")
                }
            }
        }
    }
}