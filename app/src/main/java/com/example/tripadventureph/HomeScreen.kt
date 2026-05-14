package com.example.travelquest

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    sessionManager: SessionManager,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("TravelQuest Home")
            Text("Logged in as: ${sessionManager.getEmail().orEmpty()}")
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}