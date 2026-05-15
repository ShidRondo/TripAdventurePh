package com.example.tripadventureph

import androidx.compose.foundation.layout.Arrangement
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

@Composable
fun WalletScreen(modifier: Modifier = Modifier) {
    val walletItems = listOf(
        "In-App Balance: 1200 TRIPIX",
        "On-Chain Balance: --",
        "TRIPIX Mint: configured later",
        "Network: Devnet",
        "Withdrawal: coming soon",
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