package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    var posts by remember { mutableStateOf<List<FeedPost>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    var destination by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }

    val accessToken = sessionManager.getAccessToken().orEmpty()
    val userId = sessionManager.getUserId().orEmpty()
    val authorName = sessionManager.getEmail().orEmpty()
        .substringBefore("@")
        .ifBlank { "TravelQuest User" }

    fun loadPosts() {
        loading = true
        thread {
            val result = repository.fetchPosts(accessToken)
            posts = result
            loading = false
            if (result.isEmpty()) {
                message = "No posts found."
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPosts()
    }

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

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Create Post",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destination") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Caption") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (caption.isBlank()) {
                                message = "Caption is required."
                                return@Button
                            }

                            creating = true
                            message = ""

                            thread {
                                val result = repository.createBasicPost(
                                    accessToken = accessToken,
                                    userId = userId,
                                    authorName = authorName,
                                    destination = destination,
                                    caption = caption
                                )

                                message = result.message
                                creating = false

                                if (result.success) {
                                    destination = ""
                                    caption = ""
                                    loadPosts()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !creating
                    ) {
                        Text(if (creating) "Posting..." else "Post")
                    }
                }
            }
        }

        if (loading) {
            item {
                Text("Loading posts...")
            }
        } else {
            if (message.isNotBlank()) {
                item {
                    Text(message)
                }
            }

            items(posts) { post ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (post.destination.isNotBlank()) {
                            Text(
                                text = post.destination,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text(post.caption)

                        Text(
                            text = "Likes: ${post.likesCount}   Comments: ${post.commentsCount}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (post.userId == userId) {
                            TextButton(
                                onClick = {
                                    thread {
                                        val result = repository.deleteOwnPost(
                                            accessToken = accessToken,
                                            postId = post.id,
                                            userId = userId
                                        )
                                        message = result.message
                                        if (result.success) {
                                            loadPosts()
                                        }
                                    }
                                }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}