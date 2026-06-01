package com.example.furever.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.furever.R
import com.example.furever.auth.AuthViewModel
import com.example.furever.viewmodels.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit
) {
    val pets by petViewModel.filteredPets.collectAsStateWithLifecycle()
    var searchText by remember { mutableStateOf("") }

    val categories = listOf(
        "Todas" to R.string.search_all,
        "Perro" to R.string.search_dog,
        "Gato" to R.string.search_cat,
        "Otro" to R.string.search_other
    )
    var selectedCategory by remember { mutableStateOf("Todas") }

    LaunchedEffect(Unit) { petViewModel.fetchPets() }

    // Función interna para obtener el ID del recurso según el valor de la base de datos
    fun getPetTranslation(value: String): Int? {
        return when (value) {
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
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF5C4033))) {
                TopAppBar(
                    title = { Text("FurEver 🐾", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF5C4033),
                        titleContentColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        petViewModel.onSearchQueryChanged(it)
                    },
                    placeholder = { Text(stringResource(R.string.search_hint), color = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (key, labelRes) ->
                        val isSelected = selectedCategory == key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = key
                                petViewModel.onSpeciesFilterChanged(key)
                            },
                            label = { Text(stringResource(labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                labelColor = Color.White,
                                selectedContainerColor = Color(0xFFD7CCC8),
                                selectedLabelColor = Color(0xFF5C4033)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.LightGray,
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPet,
                containerColor = Color(0xFF5C4033),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_pet))
            }
        },
        containerColor = Color(0xFFF5F0EB)
    ) { padding ->
        if (pets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No se encontraron mascotas que coincidan 🐾",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(pets) { pet ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onNavigateToPetDetail(pet.id) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = pet.imageUrl,
                                contentDescription = pet.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        pet.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3E2723)
                                    )

                                    // Estado Traducido (Disponible/Adoptado)
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (pet.adoptedStatus == "Disponible")
                                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    ) {
                                        val statusRes = getPetTranslation(pet.adoptedStatus)
                                        Text(
                                            text = statusRes?.let { stringResource(it) } ?: pet.adoptedStatus,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (pet.adoptedStatus == "Disponible")
                                                Color(0xFF388E3C) else Color(0xFFC62828)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Especie Traducida + Raza
                                val speciesRes = getPetTranslation(pet.species)
                                Text(
                                    text = buildString {
                                        append(speciesRes?.let { stringResource(it) } ?: pet.species)
                                        if (pet.breed.isNotEmpty()) append(" · ${pet.breed}")
                                    },
                                    fontSize = 13.sp,
                                    color = Color(0xFF795548)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Chips Traducidos (Género, Edad, Tamaño)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (pet.gender.isNotEmpty()) {
                                        val res = getPetTranslation(pet.gender)
                                        PetChip(res?.let { stringResource(it) } ?: pet.gender)
                                    }
                                    if (pet.ageGroup.isNotEmpty()) {
                                        val res = getPetTranslation(pet.ageGroup)
                                        PetChip(res?.let { stringResource(it) } ?: pet.ageGroup)
                                    }
                                    if (pet.size.isNotEmpty()) {
                                        val res = getPetTranslation(pet.size)
                                        PetChip(res?.let { stringResource(it) } ?: pet.size)
                                    }
                                }

                                if (pet.city.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("📍 ${pet.city}", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PetChip(label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF5F0EB)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            color = Color(0xFF5C4033),
            fontWeight = FontWeight.Medium
        )
    }
}