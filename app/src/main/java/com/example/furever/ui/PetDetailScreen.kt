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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.furever.models.PetPost
import com.example.furever.viewmodels.PetViewModel
import com.example.furever.R
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
                    stringResource(R.string.adopt_modal,pet.name),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    fontSize = 18.sp
                )
            },
            text = {
                Text(stringResource(R.string.adopt_modal_message,pet.name)
                    ,
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
                    Text(stringResource(R.string.adopt_modal_confirm), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFF9E9E9E))
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
                        if (isAvailable) stringResource(R.string.available) else stringResource(R.string.adopted),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium,
                        color = if (isAvailable) Color(0xFF388E3C) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Especie + raza
            Text(
                buildString {
                    append(pet.species)
                    if (pet.breed.isNotEmpty()) append(" · ${pet.breed}")
                },
                fontSize = 16.sp,
                color = Color(0xFF795548),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chips de atributos
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (pet.gender.isNotEmpty())   DetailChip(label = stringResource(R.string.pet_gender),  value =getTranslation(pet.gender))
                if (pet.ageGroup.isNotEmpty()) DetailChip(label = stringResource(R.string.pet_age),    value = getTranslation(pet.ageGroup))
                if (pet.size.isNotEmpty())     DetailChip(label = stringResource(R.string.pet_size),  value = getTranslation(pet.size))
            }

            if (pet.city.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("📍 ${pet.city}", fontSize = 13.sp, color = Color(0xFF9E9E9E))
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFD7CCC8))
            Spacer(modifier = Modifier.height(16.dp))
            Text(getTranslation(pet.species), fontSize = 16.sp, color = Color(0xFF795548), fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFD7CCC8))
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.about,pet.name), fontWeight = FontWeight.SemiBold, color = Color(0xFF5C4033))
            Spacer(modifier = Modifier.height(8.dp))
            Text(pet.description, color = Color(0xFF616161), lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.published_by,pet.ownerId), fontSize = 13.sp, color = Color(0xFFBCAAA4))

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
                    Text(stringResource(R.string.adopt_button), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            } else {
                Text(
                    stringResource(R.string.adopt_button_disabled),
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DetailChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEDE0D4)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = Color(0xFF9E9E9E))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5C4033))
        }
    }
}

@Composable
fun getTranslation(value: String): String {
    val resId = when (value.trim()) {
        "Perro" -> R.string.search_dog
        "Gato" -> R.string.search_cat
        "Otro" -> R.string.search_other
        "Macho" -> R.string.male_option
        "Hembra" -> R.string.female_option
        "Pequeño" -> R.string.little_size_option
        "Mediano" -> R.string.medium_size_option
        "Grande" -> R.string.big_size_option
        "Cachorro" -> R.string.puppy_age_option
        "Joven" -> R.string.young_age_option
        "Adulto" -> R.string.adult_age_option
        "Senior" -> R.string.senior_age_option
        "Disponible" -> R.string.available
        "Adoptado" -> R.string.adopted
        else -> null
    }
    return resId?.let { stringResource(it) } ?: value
}