package com.example.furever.ui

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

    timestamp = System.currentTimeMillis()


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        OutlinedTextField(value = species, onValueChange = { species = it }, label = { Text("Raza/Especie") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL de la Imagen") })
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