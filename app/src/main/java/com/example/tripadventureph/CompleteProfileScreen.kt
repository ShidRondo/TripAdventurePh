package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

@Composable
fun CompleteProfileScreen(
    repository: AuthRepository,
    sessionManager: SessionManager,
    onProfileComplete: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var municipality by remember { mutableStateOf("") }
    var barangay by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var walletAddress by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val accessToken = sessionManager.getAccessToken().orEmpty()
    val userId = sessionManager.getUserId().orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Complete Profile", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contactNumber,
            onValueChange = { contactNumber = it },
            label = { Text("Contact Number") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Region") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = municipality,
            onValueChange = { municipality = it },
            label = { Text("Municipality") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = barangay,
            onValueChange = { barangay = it },
            label = { Text("Barangay") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = zipCode,
            onValueChange = { zipCode = it },
            label = { Text("ZIP Code") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = walletAddress,
            onValueChange = { walletAddress = it },
            label = { Text("Wallet Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                loading = true
                message = ""

                thread {
                    val result = repository.completeProfile(
                        accessToken = accessToken,
                        userId = userId,
                        fullName = fullName,
                        bio = bio,
                        contactNumber = contactNumber,
                        country = country,
                        region = region,
                        municipality = municipality,
                        barangay = barangay,
                        zipCode = zipCode,
                        walletAddress = walletAddress
                    )

                    loading = false
                    message = result.message

                    if (result.success) {
                        sessionManager.saveProfileComplete(true)
                        onProfileComplete()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Saving..." else "Save and Continue")
        }

        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}