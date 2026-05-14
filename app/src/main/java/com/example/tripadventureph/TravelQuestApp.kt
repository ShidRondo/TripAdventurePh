package com.example.tripadventureph

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Signup : AppRoute("signup")
    data object CompleteProfile : AppRoute("complete_profile")
    data object Home : AppRoute("home")
}

@Composable
fun TravelQuestApp(
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    val navController = rememberNavController()

    val startDestination = if (sessionManager.isLoggedIn()) {
        if (sessionManager.isProfileComplete()) AppRoute.Home.route
        else AppRoute.CompleteProfile.route
    } else {
        AppRoute.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = { profileComplete ->
                    if (profileComplete) {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(0)
                        }
                    } else {
                        navController.navigate(AppRoute.CompleteProfile.route) {
                            popUpTo(0)
                        }
                    }
                },
                onGoToSignup = {
                    navController.navigate(AppRoute.Signup.route)
                },
                repository = repository,
                sessionManager = sessionManager
            )
        }

        composable(AppRoute.Signup.route) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Signup.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                },
                repository = repository
            )
        }

        composable(AppRoute.CompleteProfile.route) {
            CompleteProfileScreen(
                repository = repository,
                sessionManager = sessionManager,
                onProfileComplete = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(AppRoute.Home.route) {
            HomeScreen(
                sessionManager = sessionManager,
                onLogout = {
                    sessionManager.clear()
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}