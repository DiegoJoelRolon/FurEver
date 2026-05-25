package com.example.furever.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.furever.models.PetPost
import com.example.furever.viewmodels.PetViewModel
import coil.compose.AsyncImage


@Composable
fun PetDetailScreen(pet: PetPost, petViewModel: PetViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(text = pet.name, style = MaterialTheme.typography.headlineLarge)
        AsyncImage(
            model = pet.imageUrl,
            contentDescription = pet.name,
            modifier = androidx.compose.ui.Modifier
                .height(200.dp)
        )
        Text(text = "Especie: ${pet.species}")
        Text(text = pet.description)
        Text(text = "Estado: ${if(pet.adoptedStatus.isEmpty()) "Disponible" else pet.adoptedStatus}")

        Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))

        if (pet.adoptedStatus == "Disponible") {
            Button(onClick = { petViewModel.adoptPet(pet.id) }) {
                Text("Adoptar esta mascota")
            }
        } else {
            Text("Esta mascota ya ha sido adoptada ❤️", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}