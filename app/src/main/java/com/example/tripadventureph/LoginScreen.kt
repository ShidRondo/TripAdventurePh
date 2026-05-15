package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

@Composable
fun LoginScreen(
    onLoginSuccess: (Boolean) -> Unit,
    onGoToSignup: () -> Unit,
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        loading = true
                        message = ""

                        thread {
                            val result = repository.login(email, password)
                            loading = false
                            message = result.message

                            if (
                                result.success &&
                                result.accessToken != null &&
                                result.userId != null &&
                                result.email != null
                            ) {
                                sessionManager.saveSession(
                                    accessToken = result.accessToken,
                                    userId = result.userId,
                                    email = result.email,
                                    profileComplete = result.profileComplete
                                )
                                onLoginSuccess(result.profileComplete)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    Text(if (loading) "Logging in..." else "Login")
                }

                OutlinedButton(
                    onClick = onGoToSignup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go to Sign Up")
                }

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}