package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckInScreen(modifier: Modifier = Modifier) {
    var selectedDestination by remember { mutableStateOf("") }
    var gpsStatus by remember { mutableStateOf("GPS not verified") }
    var photoStatus by remember { mutableStateOf("No photo uploaded") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Check-In", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = selectedDestination,
            onValueChange = { selectedDestination = it },
            label = { Text("Destination Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("GPS Status: $gpsStatus")
                Text("Photo Status: $photoStatus")
            }
        }

        Button(
            onClick = { gpsStatus = "GPS verified (placeholder)" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify GPS")
        }

        Button(
            onClick = { photoStatus = "Photo attached (placeholder)" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload / Capture Photo")
        }

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Check-In")
        }
    }
}