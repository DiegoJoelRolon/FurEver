package com.example.furever.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.furever.auth.AuthState
import com.example.furever.auth.AuthViewModel
import com.example.furever.ui.*
import com.example.furever.viewmodels.PetViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val petViewModel: PetViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(Routes.HOME, Routes.PROFILE)

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate(Routes.HOME) { popUpTo(0) }
            is AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN) { popUpTo(0) }
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 4.dp
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == Routes.HOME,
                                onClick = { navController.navigate(Routes.HOME) { launchSingleTop = true } },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Home,
                                        contentDescription = "Inicio"
                                    )
                                },
                                label = { Text("Inicio") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF5C4033),
                                    selectedTextColor = Color(0xFF5C4033),
                                    unselectedIconColor = Color(0xFFBCAAA4),
                                    unselectedTextColor = Color(0xFFBCAAA4),
                                    indicatorColor = Color(0xFFF5F0EB)
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute == Routes.PROFILE,
                                onClick = { navController.navigate(Routes.PROFILE) { launchSingleTop = true } },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Perfil"
                                    )
                                },
                                label = { Text("Perfil") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF5C4033),
                                    selectedTextColor = Color(0xFF5C4033),
                                    unselectedIconColor = Color(0xFFBCAAA4),
                                    unselectedTextColor = Color(0xFFBCAAA4),
                                    indicatorColor = Color(0xFFF5F0EB)
                                )
                            )
                        }
            }
        }
    ) { paddingValues  ->
        NavHost(navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(paddingValues)) {
            composable(Routes.LOGIN) {
                LoginScreen(authViewModel)
            }
            composable(Routes.HOME) {
                HomeScreen(
                    authViewModel = authViewModel,
                    petViewModel = petViewModel,
                    onNavigateToAddPet = { navController.navigate(Routes.UPLOAD_PET) },
                    onNavigateToPetDetail = { petId -> navController.navigate("pet_detail/$petId") }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    petViewModel = petViewModel,
                    onNavigateToPetDetail = { petId -> navController.navigate("pet_detail/$petId") },
                    onSignOut = { navController.navigate(Routes.LOGIN) { popUpTo(0) } }
                )
            }
            composable(Routes.UPLOAD_PET) {
                UploadPetScreen(petViewModel) { navController.popBackStack() }
            }
            composable(
                route = Routes.PET_DETAIL,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                val pets by petViewModel.pets.collectAsStateWithLifecycle()
                val pet = pets.find { it.id == petId }
                pet?.let { PetDetailScreen(it, petViewModel) }
                    ?: run { androidx.compose.material3.Text("Mascota no encontrada") }
            }
        }
    }
}