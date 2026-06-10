package com.example.furever.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import com.example.furever.R

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController        = rememberNavController()
    val authState            by authViewModel.authState.collectAsStateWithLifecycle()
    val currentUser          by authViewModel.currentUser.collectAsStateWithLifecycle()
    val petViewModel: PetViewModel = viewModel()
    val navBackStackEntry    by navController.currentBackStackEntryAsState()
    val currentRoute         = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Routes.HOME,
        Routes.FAVORITES,
        Routes.PREFERENCES,
        Routes.PROFILE
    )

    // Aplicar preferencias cuando carga el usuario
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (user.hasCompletedOnboarding) {
                if (user.prefSpecies.isNotEmpty() && user.prefSpecies != "Sin preferencia")
                    petViewModel.onSpeciesFilterChanged(user.prefSpecies)
                if (user.prefGender.isNotEmpty() && user.prefGender != "Sin preferencia")
                    petViewModel.onGenderFilterChanged(user.prefGender)
                if (user.prefSize.isNotEmpty() && user.prefSize != "Sin preferencia")
                    petViewModel.onSizeFilterChanged(user.prefSize)
                if (user.prefAge.isNotEmpty() && user.prefAge != "Sin preferencia")
                    petViewModel.onAgeGroupFilterChanged(user.prefAge)
            }
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated ->
                navController.navigate(Routes.HOME) { popUpTo(0) }
            is AuthState.Unauthenticated ->
                navController.navigate(Routes.LOGIN) { popUpTo(0) }
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
                    // Inicio
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick  = {
                            navController.navigate(Routes.HOME) { launchSingleTop = true }
                        },
                        icon  = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                        label = { Text(stringResource(R.string.home_button)) },
                        colors = navBarColors()
                    )

                    // Favoritos
                    NavigationBarItem(
                        selected = currentRoute == Routes.FAVORITES,
                        onClick  = {
                            navController.navigate(Routes.FAVORITES) { launchSingleTop = true }
                        },
                        icon  = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                        label = { Text(stringResource(R.string.favourites_button)) },
                        colors = navBarColors()
                    )

                    // Preferencias
                    NavigationBarItem(
                        selected = currentRoute == Routes.PREFERENCES,
                        onClick  = {
                            navController.navigate(Routes.PREFERENCES) { launchSingleTop = true }
                        },
                        icon  = {
                            Icon(
                                imageVector        = Icons.Filled.Search,
                                contentDescription = "Buscar mi match"
                            )
                        },
                        label  = { Text(stringResource(R.string.my_match_button)) },
                        colors = navBarColors()
                    )

                    // Perfil
                    NavigationBarItem(
                        selected = currentRoute == Routes.PROFILE,
                        onClick  = {
                            navController.navigate(Routes.PROFILE) { launchSingleTop = true }
                        },
                        icon  = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                        label = { Text(stringResource(R.string.profile_button)) },
                        colors = navBarColors()
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = Routes.LOGIN,
            modifier         = Modifier.padding(paddingValues)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    authViewModel        = authViewModel,
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    authViewModel     = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    authViewModel         = authViewModel,
                    petViewModel          = petViewModel,
                    onNavigateToAddPet    = { navController.navigate(Routes.UPLOAD_PET) },
                    onNavigateToPetDetail = { petId ->
                        navController.navigate("pet_detail/$petId")
                    }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    authViewModel         = authViewModel,
                    petViewModel          = petViewModel,
                    onNavigateToPetDetail = { petId ->
                        navController.navigate("pet_detail/$petId")
                    }
                )
            }
            composable(Routes.PREFERENCES) {
                PreferencesScreen(
                    authViewModel = authViewModel,
                    petViewModel  = petViewModel
                )
            }

            composable(Routes.ADOPTION_REQUESTS) {
                AdoptionRequestsScreen(petViewModel = petViewModel)
            }


            composable(Routes.PROFILE) {
                ProfileScreen(
                    authViewModel         = authViewModel,
                    petViewModel          = petViewModel,
                    onNavigateToPetDetail = { petId -> navController.navigate("pet_detail/$petId") },
                    onNavigateToRequests  = { navController.navigate(Routes.ADOPTION_REQUESTS) },  // ← nuevo
                    onSignOut             = { navController.navigate(Routes.LOGIN) { popUpTo(0) } }
                )
            }
            composable(Routes.UPLOAD_PET) {
                UploadPetScreen(petViewModel) { navController.popBackStack() }
            }
            composable(
                route     = Routes.PET_DETAIL,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                val pets  by petViewModel.pets.collectAsStateWithLifecycle()
                val pet   = pets.find { it.id == petId }
                pet?.let {
                    PetDetailScreen(
                        pet            = it,
                        petViewModel   = petViewModel,
                        authViewModel  = authViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                } ?: Text("Mascota no encontrada")
            }
        }
    }
}

// ── Helper colores nav bar ────────────────────────────────────────────────────

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = Color(0xFF5C4033),
    selectedTextColor   = Color(0xFF5C4033),
    unselectedIconColor = Color(0xFFBCAAA4),
    unselectedTextColor = Color(0xFFBCAAA4),
    indicatorColor      = Color(0xFFF5F0EB)
)