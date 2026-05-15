package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.concurrent.thread

@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    var walletSummary by remember { mutableStateOf(WalletSummary()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    val accessToken = sessionManager.getAccessToken().orEmpty()
    val userId = sessionManager.getUserId().orEmpty()

    fun loadWallet() {
        loading = true
        message = ""

        thread {
            val result = repository.fetchWalletSummary(
                accessToken = accessToken,
                userId = userId
            )
            walletSummary = result
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loadWallet()
    }

    val walletItems = listOf(
        "In-App Available: ${walletSummary.availableBalance} TRIPIX",
        "Locked Balance: ${walletSummary.lockedBalance} TRIPIX",
        "Pending Balance: ${walletSummary.pendingBalance} TRIPIX",
        "On-Chain Balance: --",
        "TRIPIX Mint: configured later",
        "Network: Devnet",
        "Withdrawal: coming soon"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Wallet",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Button(
                onClick = { loadWallet() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text(if (loading) "Refreshing..." else "Refresh Wallet")
            }
        }

        if (message.isNotBlank()) {
            item {
                Text(message)
            }
        }

        if (loading) {
            item {
                Text("Loading wallet...")
            }
        } else {
            items(walletItems) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}