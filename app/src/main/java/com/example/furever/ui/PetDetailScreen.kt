package com.example.furever.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.furever.models.PetPost
import com.example.furever.viewmodels.PetViewModel

@Composable
fun PetDetailScreen(
    pet: PetPost,
    petViewModel: PetViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val isAvailable = pet.adoptedStatus == "Disponible"
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Diálogo de confirmación
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    "¿Adoptás a ${pet.name}?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "Esta acción marcará a ${pet.name} como adoptado/a. " +
                            "¿Estás seguro/a de que querés continuar?",
                    color = Color(0xFF616161),
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        petViewModel.adoptPet(pet.id)
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4033)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sí, adoptar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = Color(0xFF9E9E9E))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0EB))
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = pet.imageUrl,
            contentDescription = pet.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pet.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        if (isAvailable) "Disponible" else "Adoptado",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium,
                        color = if (isAvailable) Color(0xFF388E3C) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(pet.species, fontSize = 16.sp, color = Color(0xFF795548), fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFD7CCC8))
            Spacer(modifier = Modifier.height(16.dp))

            Text("Sobre ${pet.name}", fontWeight = FontWeight.SemiBold, color = Color(0xFF5C4033))
            Spacer(modifier = Modifier.height(8.dp))
            Text(pet.description, color = Color(0xFF616161), lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Publicado por: ${pet.ownerId}", fontSize = 13.sp, color = Color(0xFFBCAAA4))

            Spacer(modifier = Modifier.height(32.dp))

            if (isAvailable) {
                Button(
                    onClick = { showConfirmDialog = true }, // abre el diálogo
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4033))
                ) {
                    Text("Adoptar esta mascota", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            } else {
                Text(
                    "Esta mascota ya encontró su hogar",
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )
            }
        }
    }
}