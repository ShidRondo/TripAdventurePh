package com.example.tripadventureph

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

enum class MainTab(val label: String) {
    Home("Home"),
    Feed("Feed"),
    Discover("Discover"),
    CheckIn("Check-In"),
    CheckInAdvanced("Track"),
    Wallet("Wallet"),
    Achievements("Achievements"),
    Events("Events"),
    Profile("Profile")
}

@Composable
fun MainShellScreen(
    repository: AuthRepository,
    sessionManager: SessionManager,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(MainTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            val icon = when (tab) {
                                MainTab.Home -> Icons.Default.Home
                                MainTab.Feed -> Icons.AutoMirrored.Filled.Article
                                MainTab.Discover -> Icons.Default.Explore
                                MainTab.CheckIn -> Icons.Default.CameraAlt
                                MainTab.CheckInAdvanced -> Icons.Default.Map
                                MainTab.Wallet -> Icons.Default.Wallet
                                MainTab.Achievements -> Icons.Default.MilitaryTech
                                MainTab.Events -> Icons.Default.Event
                                MainTab.Profile -> Icons.Default.AccountCircle
                            }
                            Icon(icon, contentDescription = tab.label)
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            MainTab.Home -> HomeDashboardScreen(
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.Feed -> FeedScreen(
                modifier = Modifier.padding(innerPadding),
                repository = repository,
                sessionManager = sessionManager
            )

            MainTab.Discover -> DiscoverScreen(
                modifier = Modifier.padding(innerPadding),
                repository = repository,
                sessionManager = sessionManager
            )

            MainTab.CheckIn -> CheckInScreen(
                modifier = Modifier.padding(innerPadding),
                repository = repository,
                sessionManager = sessionManager
            )

            MainTab.CheckInAdvanced -> CheckInAdvancedScreen(
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.Wallet -> WalletScreen(
                modifier = Modifier.padding(innerPadding),
                repository = repository,
                sessionManager = sessionManager
            )

            MainTab.Achievements -> AchievementsScreen(
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.Events -> EventsScreen(
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.Profile -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                repository = repository,
                sessionManager = sessionManager,
                onLogout = onLogout
            )
        }
    }
}