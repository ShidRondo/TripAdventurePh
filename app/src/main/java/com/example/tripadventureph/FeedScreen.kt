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

data class SampleFeedPost(
    val author: String,
    val caption: String,
    val destination: String,
    val likes: Int,
    val comments: Int,
)

@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    val posts = listOf(
        SampleFeedPost("TravelQuest User", "First adventure post placeholder", "Osmeña Peak", 0, 0),
        SampleFeedPost("TravelQuest User", "Event post placeholder", "Kawasan Falls", 0, 0)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Activity Feed",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        items(posts) { post ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(post.author, style = MaterialTheme.typography.titleMedium)
                    Text(post.destination, style = MaterialTheme.typography.bodyMedium)
                    Text(post.caption, modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = "Likes: ${post.likes}   Comments: ${post.comments}",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}