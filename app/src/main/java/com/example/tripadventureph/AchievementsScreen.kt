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

data class AchievementItem(
    val title: String,
    val description: String,
    val progressText: String,
    val unlocked: Boolean
)

@Composable
fun AchievementsScreen(
    modifier: Modifier = Modifier
) {
    val achievements = listOf(
        AchievementItem(
            title = "Hike Master",
            description = "Complete major hiking destinations.",
            progressText = "Progress: 0 / 5 hikes",
            unlocked = false
        ),
        AchievementItem(
            title = "Waterfall Expertise",
            description = "Visit and verify waterfall destinations.",
            progressText = "Progress: 0 / 5 falls",
            unlocked = false
        ),
        AchievementItem(
            title = "Beach Explorer",
            description = "Check in at beach destinations.",
            progressText = "Progress: 0 / 5 beaches",
            unlocked = false
        ),
        AchievementItem(
            title = "Island Specialist",
            description = "Complete island-related travel goals.",
            progressText = "Progress: 0 / 3 islands",
            unlocked = false
        )
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = achievement.progressText,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = if (achievement.unlocked) "Status: Unlocked" else "Status: Locked",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}