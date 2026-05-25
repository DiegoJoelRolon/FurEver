package com.example.furever.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.furever.models.PetPost
import com.example.furever.viewmodels.PetViewModel

@Composable
fun UploadPetScreen(petViewModel: PetViewModel, onPostSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var ownerId by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(0L) }

    val listaMascotas: List<String> = listOf("https://cdn.britannica.com/16/234216-050-C66F8665/beagle-hound-dog.jpg",
            "https://images.unsplash.com/photo-1557408938-0f220f49bca1?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8c3RyZWV0JTIwY2F0fGVufDB8fDB8fHww",
            "https://corazondezonasur.com.ar/wp-content/uploads/2026/05/691514867_1517968549732005_6104570804858794001_n.jpg")

    timestamp = System.currentTimeMillis()

    imageUrl = listaMascotas.random()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        OutlinedTextField(value = species, onValueChange = { species = it }, label = { Text("Raza/Especie") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
        OutlinedTextField(value = ownerId, onValueChange = { ownerId = it }, label = { Text("ID del Dueño") })


        val post = PetPost(name = name, species = species, description = description, imageUrl = imageUrl, ownerId = ownerId, timestamp = timestamp)
        Button(onClick = {
            petViewModel.uploadPet(post)
            onPostSuccess()
        }) {
            Text("Publicar para Adopción")
        }
    }
}