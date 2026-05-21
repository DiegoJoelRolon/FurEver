package com.example.furever.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.furever.auth.AuthState
import com.example.furever.auth.AuthViewModel
import com.example.furever.ui.LoginScreen
import com.example.furever.ui.HomeScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel
) {

    val navController = rememberNavController()

    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {

        when (authState) {

            is AuthState.Authenticated -> {

                navController.navigate(Routes.HOME) {

                    popUpTo(0)
                }
            }

            is AuthState.Unauthenticated -> {

                navController.navigate(Routes.LOGIN) {

                    popUpTo(0)
                }
            }

            else -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {

            LoginScreen(authViewModel)
        }

        composable(Routes.HOME) {

            HomeScreen(authViewModel)
        }
    }
}