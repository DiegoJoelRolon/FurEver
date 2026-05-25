package com.example.furever.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.furever.auth.AuthViewModel
import com.example.furever.viewmodels.PetViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel,onNavigateToAddPet: () -> Unit
) {
    val pets by petViewModel.pets.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        petViewModel.fetchPets()
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddPet) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            items(pets) { pet ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(pet.id, style = MaterialTheme.typography.bodyMedium)
                        Text(pet.name, style = MaterialTheme.typography.headlineSmall)
                        Text(pet.species, style = MaterialTheme.typography.bodyMedium)
                        Text(pet.description, style = MaterialTheme.typography.bodyMedium)
                        Text(pet.ownerId, style = MaterialTheme.typography.bodyMedium)
                        Text(pet.timestamp.toString(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}