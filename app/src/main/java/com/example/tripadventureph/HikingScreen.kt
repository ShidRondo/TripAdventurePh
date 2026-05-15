package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

data class HikeCheckpoint(
    val name: String,
    val reached: Boolean
)

@Composable
fun HikingScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    var trails by remember { mutableStateOf<List<Trail>>(emptyList()) }
    var trailheads by remember { mutableStateOf<List<Trailhead>>(emptyList()) }

    var loadingTrails by remember { mutableStateOf(true) }
    var loadingTrailheads by remember { mutableStateOf(false) }

    var selectedTrailId by remember { mutableStateOf("") }
    var selectedTrailCode by remember { mutableStateOf("") }
    var trailName by remember { mutableStateOf("") }
    var trailArea by remember { mutableStateOf("") }
    var selectedTrailheadName by remember { mutableStateOf("") }

    var trailheadVerified by remember { mutableStateOf(false) }
    var hikingStarted by remember { mutableStateOf(false) }
    var hikeCompleted by remember { mutableStateOf(false) }

    var rewardMessage by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Waiting to begin.") }

    val checkpoints = remember {
        mutableStateListOf(
            HikeCheckpoint("Trailhead", false),
            HikeCheckpoint("Checkpoint 1", false),
            HikeCheckpoint("Checkpoint 2", false),
            HikeCheckpoint("Target Reached", false)
        )
    }

    val accessToken = sessionManager.getAccessToken().orEmpty()
    val userId = sessionManager.getUserId().orEmpty()

    LaunchedEffect(Unit) {
        thread {
            trails = repository.fetchTrails(accessToken)
            loadingTrails = false
        }
    }

    val reachedCount = checkpoints.count { it.reached }
    val progressFraction =
        (reachedCount.toFloat() / checkpoints.size.toFloat()).coerceIn(0f, 1f)

    fun resetHikeSession() {
        for (i in checkpoints.indices) {
            checkpoints[i] = checkpoints[i].copy(reached = false)
        }
        trailheadVerified = false
        hikingStarted = false
        hikeCompleted = false
        rewardMessage = ""
        statusMessage = "Hike session reset."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Hiking Mode",
            style = MaterialTheme.typography.headlineSmall
        )

        if (loadingTrails) {
            Text("Loading trails...")
        } else {
            Text(
                text = "Available Trails",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trails) { trail ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = trail.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Code: ${trail.code.ifBlank { "-" }}")
                            Text("Area: ${trail.area.ifBlank { "-" }}")
                            Text("Next Trail ID: ${trail.nextTrailId ?: "-"}")

                            Button(
                                onClick = {
                                    selectedTrailId = trail.id
                                    selectedTrailCode = trail.code
                                    trailName = trail.name
                                    trailArea = trail.area
                                    selectedTrailheadName = ""
                                    resetHikeSession()
                                    loadingTrailheads = true
                                    statusMessage = "Trail selected."

                                    thread {
                                        trailheads = repository.fetchTrailheads(
                                            accessToken = accessToken,
                                            trailId = trail.id
                                        )
                                        loadingTrailheads = false
                                    }
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Select Trail")
                            }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = trailName,
            onValueChange = { trailName = it },
            label = { Text("Trail Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = trailArea,
            onValueChange = { trailArea = it },
            label = { Text("Trail Area") },
            modifier = Modifier.fillMaxWidth()
        )

        if (selectedTrailId.isNotBlank()) {
            Text(
                text = "Trailheads",
                style = MaterialTheme.typography.titleMedium
            )

            if (loadingTrailheads) {
                Text("Loading trailheads...")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trailheads) { trailhead ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = trailhead.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Location: ${trailhead.location.ifBlank { "-" }}")
                                Text("Lat: ${trailhead.lat ?: 0.0}")
                                Text("Lng: ${trailhead.lng ?: 0.0}")

                                Button(
                                    onClick = {
                                        selectedTrailheadName = trailhead.name
                                        statusMessage = "Trailhead selected."
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Select Trailhead")
                                }
                            }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = selectedTrailheadName,
            onValueChange = { selectedTrailheadName = it },
            label = { Text("Selected Trailhead") },
            modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Trailhead Verified: ${if (trailheadVerified) "Yes" else "No"}")
                Text("Hiking Started: ${if (hikingStarted) "Yes" else "No"}")
                Text("Hike Completed: ${if (hikeCompleted) "Yes" else "No"}")
                Text("Status: $statusMessage")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hiking Progress",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Progress: ${(progressFraction * 100).toInt()}%")
            }
        }

        Button(
            onClick = {
                if (selectedTrailId.isBlank()) {
                    statusMessage = "Select a trail first."
                } else if (selectedTrailheadName.isBlank()) {
                    statusMessage = "Select a trailhead first."
                } else {
                    thread {
                        val result = repository.createHikeSession(
                            accessToken = accessToken,
                            userId = userId,
                            trailId = selectedTrailId
                        )
                        statusMessage = result.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Hike Session")
        }

        Button(
            onClick = {
                if (selectedTrailId.isBlank()) {
                    statusMessage = "Select a trail first."
                } else if (selectedTrailheadName.isBlank()) {
                    statusMessage = "Select a trailhead first."
                } else {
                    thread {
                        val result = repository.verifyTrailheadMatch(
                            accessToken = accessToken,
                            userId = userId,
                            trailId = selectedTrailId
                        )
                        if (result.success) {
                            trailheadVerified = true
                            checkpoints[0] = checkpoints[0].copy(reached = true)
                        }
                        statusMessage = result.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !trailheadVerified
        ) {
            Text(if (trailheadVerified) "Trailhead Verified" else "Verify Trailhead")
        }

        Button(
            onClick = {
                if (selectedTrailId.isBlank()) {
                    statusMessage = "Select a trail first."
                } else if (!trailheadVerified) {
                    statusMessage = "Verify the trailhead first."
                } else {
                    thread {
                        val result = repository.startHikeSession(
                            accessToken = accessToken,
                            userId = userId,
                            trailId = selectedTrailId
                        )
                        if (result.success) {
                            hikingStarted = true
                        }
                        statusMessage = result.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !hikingStarted
        ) {
            Text(if (hikingStarted) "Hiking Started" else "Start Hiking Session")
        }

        Button(
            onClick = {
                if (!hikingStarted) {
                    statusMessage = "Start the hiking session first."
                } else {
                    val nextCheckpointIndex = checkpoints.indexOfFirst { !it.reached }
                    if (nextCheckpointIndex != -1) {
                        checkpoints[nextCheckpointIndex] =
                            checkpoints[nextCheckpointIndex].copy(reached = true)
                        statusMessage = "${checkpoints[nextCheckpointIndex].name} reached."
                    } else {
                        statusMessage = "All checkpoints already reached."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hikingStarted && !hikeCompleted
        ) {
            Text("Reach Next Checkpoint")
        }

        Button(
            onClick = {
                if (selectedTrailId.isBlank()) {
                    statusMessage = "Select a trail first."
                } else if (!hikingStarted) {
                    statusMessage = "Start the hiking session first."
                } else {
                    for (i in checkpoints.indices) {
                        checkpoints[i] = checkpoints[i].copy(reached = true)
                    }

                    thread {
                        val result = repository.completeHikeSession(
                            accessToken = accessToken,
                            userId = userId,
                            trailId = selectedTrailId,
                            totalEarned = 50.0,
                            nextTrailReady = true
                        )
                        if (result.success) {
                            hikeCompleted = true
                            rewardMessage = "Reward earned: 50 TRIPIX"
                        }
                        statusMessage = result.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hikingStarted && !hikeCompleted
        ) {
            Text("Complete Hike")
        }

        Button(
            onClick = { resetHikeSession() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Hiking Session")
        }

        if (rewardMessage.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = rewardMessage,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Text(
            text = "Checkpoint List",
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(checkpoints) { checkpoint ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = checkpoint.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (checkpoint.reached) "Reached" else "Pending"
                        )
                    }
                }
            }
        }
    }
}