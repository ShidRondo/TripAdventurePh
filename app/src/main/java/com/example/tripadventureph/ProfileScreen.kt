package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    sessionManager: SessionManager,
    onLogout: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    var fullName by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phoneLocalNumber by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var municipality by remember { mutableStateOf("") }
    var barangay by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var walletAddress by remember { mutableStateOf("") }

    val accessToken = sessionManager.getAccessToken().orEmpty()
    val userId = sessionManager.getUserId().orEmpty()
    val email = sessionManager.getEmail().orEmpty()

    LaunchedEffect(Unit) {
        thread {
            val profile = repository.fetchProfile(accessToken, userId)
            if (profile != null) {
                fullName = profile.fullName
                displayName = profile.displayName
                bio = profile.bio
                phoneLocalNumber = profile.phoneLocalNumber
                country = profile.country
                region = profile.region
                municipality = profile.municipality
                barangay = profile.barangay
                zipCode = profile.zipCode
                walletAddress = profile.walletAddress
            }
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)

        if (loading) {
            Text("Loading profile...")
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Email: $email")
                    Text("Posts: 0")
                    Text("Places: 0")
                    Text("Badges: 0")
                }
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
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
                value = phoneLocalNumber,
                onValueChange = { phoneLocalNumber = it },
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
                    saving = true
                    message = ""

                    thread {
                        val result = repository.updateProfile(
                            accessToken = accessToken,
                            userId = userId,
                            fullName = fullName,
                            displayName = displayName,
                            bio = bio,
                            phoneLocalNumber = phoneLocalNumber,
                            country = country,
                            region = region,
                            municipality = municipality,
                            barangay = barangay,
                            zipCode = zipCode,
                            walletAddress = walletAddress
                        )
                        message = result.message
                        saving = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving
            ) {
                Text(if (saving) "Saving..." else "Save Profile")
            }

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            if (message.isNotBlank()) {
                Text(message)
            }
        }
    }
}