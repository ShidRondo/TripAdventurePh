package com.example.tripadventureph

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Signup : AppRoute("signup")
    data object CompleteProfile : AppRoute("complete_profile")
    data object MainShell : AppRoute("main_shell")
}

@Composable
fun TravelQuestApp(
    repository: AuthRepository,
    sessionManager: SessionManager,
) {
    val navController = rememberNavController()

    val startDestination = if (sessionManager.isLoggedIn()) {
        if (sessionManager.isProfileComplete()) AppRoute.MainShell.route
        else AppRoute.CompleteProfile.route
    } else {
        AppRoute.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = { profileComplete ->
                    if (profileComplete) {
                        navController.navigate(AppRoute.MainShell.route) {
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
                    navController.navigate(AppRoute.MainShell.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(AppRoute.MainShell.route) {
            MainShellScreen(
                repository = repository,
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