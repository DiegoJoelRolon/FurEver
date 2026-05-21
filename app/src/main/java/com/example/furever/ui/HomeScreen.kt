package com.example.furever.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.furever.auth.AuthViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel
) {

    Column(
        modifier = Modifier.fillMaxSize(),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Usuario Logueado")

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                authViewModel.signout()
            }
        ) {
            Text("Cerrar sesión")
        }
    }
}