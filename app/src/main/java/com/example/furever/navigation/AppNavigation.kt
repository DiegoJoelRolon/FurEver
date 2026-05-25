package com.example.furever.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.furever.auth.AuthState
import com.example.furever.auth.AuthViewModel
import com.example.furever.ui.LoginScreen
import com.example.furever.ui.HomeScreen
import com.example.furever.ui.PetDetailScreen
import com.example.furever.ui.UploadPetScreen
import com.example.furever.viewmodels.PetViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel)
{

    val navController = rememberNavController()

    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val petViewModel: PetViewModel = viewModel()

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

    NavHost(navController = navController,startDestination = Routes.LOGIN)
    {

        composable(Routes.LOGIN) {

            LoginScreen(authViewModel)
        }

        composable(Routes.HOME) {

            HomeScreen(
                authViewModel = authViewModel,
                petViewModel = petViewModel,
                onNavigateToAddPet = { navController.navigate(Routes.UPLOAD_PET) },
                onNavigateToPetDetail = { petId ->
                    navController.navigate("pet_detail/$petId")
                }
            )
        }

        composable(Routes.UPLOAD_PET){
            UploadPetScreen(petViewModel) {
                navController.popBackStack()
            }
        }

        composable(route = Routes.PET_DETAIL, arguments = listOf(navArgument("petId"){type =NavType.StringType}))
        {
            backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            val pets by petViewModel.pets.collectAsStateWithLifecycle()
            val pet = pets.find { it.id == petId }
            pet?.let{
                PetDetailScreen(it)
            }?: run{
                Text("Pet not found")
            }

        }
    }
}
