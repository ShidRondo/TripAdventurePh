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
fun AchievementsScreen(modifier: Modifier = Modifier) {
    val achievements = listOf(
        "Hike Master - Locked",
        "Waterfall Expertise - Locked",
        "Beach Explorer - Locked",
        "Island Specialist - Locked",
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        items(achievements) { achievement ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(achievement, style = MaterialTheme.typography.titleMedium)
                    Text("Progress tracking will be connected in the next phase.")
                }
            }
        }
    }
}