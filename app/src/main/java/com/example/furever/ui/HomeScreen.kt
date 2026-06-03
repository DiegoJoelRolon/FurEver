package com.example.furever.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.furever.models.PetPost
import com.example.furever.viewmodels.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit
) {
    val pets by petViewModel.allFilteredPets.collectAsStateWithLifecycle()
    var searchText       by remember { mutableStateOf("") }
    var showFilterPanel  by remember { mutableStateOf(false) }

    var selectedSpecies  by remember { mutableStateOf("Todas") }
    var selectedGender   by remember { mutableStateOf("Todos") }
    var selectedSize     by remember { mutableStateOf("Todos") }
    var selectedAgeGroup by remember { mutableStateOf("Todos") }

    val currentUser by authViewModel.currentUser.collectAsState()
    var petToDelete by remember { mutableStateOf<PetPost?>(null) }

    if (petToDelete != null) {
        AlertDialog(
            onDismissRequest = { petToDelete = null },
            title = { Text(stringResource(R.string.delete_confirmation_first)) },
            text = { Text(stringResource(R.string.delete_confirmation_second,petToDelete?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        petToDelete?.let { petViewModel.deletePet(it.id) }
                        petToDelete = null
                    }
                ) { Text(stringResource(R.string.delete_button), color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { petToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
    // Cuenta cuántos filtros secundarios están activos (género, tamaño, edad)
    val activeFilterCount = listOf(selectedGender, selectedSize, selectedAgeGroup)
        .count { it != "Todos" }

    LaunchedEffect(Unit) { petViewModel.fetchPets() }

    fun getPetTranslation(value: String): Int? = when (value) {
        "Perro"      -> R.string.search_dog
        "Gato"       -> R.string.search_cat
        "Otro"       -> R.string.search_other
        "Macho"      -> R.string.male_option
        "Hembra"     -> R.string.female_option
        "Pequeño"    -> R.string.little_size_option
        "Mediano"    -> R.string.medium_size_option
        "Grande"     -> R.string.big_size_option
        "Cachorro"   -> R.string.puppy_age_option
        "Joven"      -> R.string.young_age_option
        "Adulto"     -> R.string.adult_age_option
        "Senior"     -> R.string.senior_age_option
        "Disponible" -> R.string.available
        "Adoptado"   -> R.string.adopted
        else         -> null
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF5C4033))) {

                TopAppBar(
                    title = {
                        Text("FurEver 🐾", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF5C4033),
                        titleContentColor = Color.White
                    )
                )

                // ── Fila: buscador + botón filtros + chips especie ────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Buscador
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            petViewModel.onSearchQueryChanged(it)
                        },
                        placeholder = {
                            Text(stringResource(R.string.search_hint), color = Color.LightGray, fontSize = 13.sp)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Color.White,
                            unfocusedBorderColor = Color(0x66FFFFFF),
                            cursorColor          = Color.White,
                            focusedTextColor     = Color.White,
                            unfocusedTextColor   = Color.White
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )

                    // Botón filtros con badge
                    Box {
                        IconButton(
                            onClick = { showFilterPanel = !showFilterPanel },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (showFilterPanel || activeFilterCount > 0)
                                        Color(0xFFD7CCC8)
                                    else
                                        Color(0x33FFFFFF)
                                )
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Filtros",
                                tint = if (showFilterPanel || activeFilterCount > 0)
                                    Color(0xFF5C4033)
                                else
                                    Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Badge con cantidad de filtros activos
                        if (activeFilterCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFC62828))
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    activeFilterCount.toString(),
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ── Chips de especie — siempre visibles ───────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todas" to stringResource(R.string.search_all),
                        "Perro" to stringResource(R.string.search_dog),
                        "Gato"  to stringResource(R.string.search_cat),
                        "Otro"  to stringResource(R.string.search_other)
                    ).forEach { (key, label) ->
                        val isSelected = selectedSpecies == key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSpecies = key
                                petViewModel.onSpeciesFilterChanged(key)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor         = Color.Transparent,
                                labelColor             = Color.White,
                                selectedContainerColor = Color(0xFFD7CCC8),
                                selectedLabelColor     = Color(0xFF5C4033)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled             = true,
                                selected            = isSelected,
                                borderColor         = Color(0x66FFFFFF),
                                selectedBorderColor = Color.Transparent,
                                borderWidth         = 1.dp
                            )
                        )
                    }
                }

                // ── Panel colapsable: género, tamaño, edad ────────────────
                AnimatedVisibility(
                    visible = showFilterPanel,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4A3228))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Género
                        FilterSection(
                            title = stringResource(R.string.pet_gender),
                            options = listOf(
                                "Todos"  to "Todos",
                                "Macho"  to stringResource(R.string.male_option),
                                "Hembra" to stringResource(R.string.female_option)
                            ),
                            selected = selectedGender,
                            onSelect = {
                                selectedGender = it
                                petViewModel.onGenderFilterChanged(it)
                            }
                        )

                        // Tamaño
                        FilterSection(
                            title = stringResource(R.string.pet_size),
                            options = listOf(
                                "Todos"   to "Todos",
                                "Pequeño" to stringResource(R.string.little_size_option),
                                "Mediano" to stringResource(R.string.medium_size_option),
                                "Grande"  to stringResource(R.string.big_size_option)
                            ),
                            selected = selectedSize,
                            onSelect = {
                                selectedSize = it
                                petViewModel.onSizeFilterChanged(it)
                            }
                        )

                        // Edad
                        FilterSection(
                            title = stringResource(R.string.pet_age),
                            options = listOf(
                                "Todos"    to "Todos",
                                "Cachorro" to stringResource(R.string.puppy_age_option),
                                "Joven"    to stringResource(R.string.young_age_option),
                                "Adulto"   to stringResource(R.string.adult_age_option),
                                "Senior"   to stringResource(R.string.senior_age_option)
                            ),
                            selected = selectedAgeGroup,
                            onSelect = {
                                selectedAgeGroup = it
                                petViewModel.onAgeGroupFilterChanged(it)
                            }
                        )

                        // Botón limpiar filtros
                        if (activeFilterCount > 0) {
                            TextButton(
                                onClick = {
                                    selectedGender   = "Todos"
                                    selectedSize     = "Todos"
                                    selectedAgeGroup = "Todos"
                                    petViewModel.onGenderFilterChanged("Todos")
                                    petViewModel.onSizeFilterChanged("Todos")
                                    petViewModel.onAgeGroupFilterChanged("Todos")
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    "Limpiar filtros",
                                    color = Color(0xFFD7CCC8),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
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
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐾", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No se encontraron mascotas",
                        color = Color(0xFF9E9E9E),
                        fontWeight = FontWeight.Medium
                    )
                    if (activeFilterCount > 0 || searchText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            searchText       = ""
                            selectedSpecies  = "Todas"
                            selectedGender   = "Todos"
                            selectedSize     = "Todos"
                            selectedAgeGroup = "Todos"
                            petViewModel.onSearchQueryChanged("")
                            petViewModel.onSpeciesFilterChanged("Todas")
                            petViewModel.onGenderFilterChanged("Todos")
                            petViewModel.onSizeFilterChanged("Todos")
                            petViewModel.onAgeGroupFilterChanged("Todos")
                        }) {
                            Text("Limpiar búsqueda", color = Color(0xFF5C4033))
                        }
                    }
                }
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
                    val isOwner = currentUser?.email == pet.ownerId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = { onNavigateToPetDetail(pet.id) },onLongClick = {if (isOwner) petToDelete = pet }),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOwner)
                                Color(0xFFFFF8E1) // Distinto color si es dueño de la mascota
                            else
                                Color.White
                        ),
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
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (pet.adoptedStatus == "Disponible")
                                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    ) {
                                        val statusRes = getPetTranslation(pet.adoptedStatus)
                                        Text(
                                            text = statusRes?.let { stringResource(it) }
                                                ?: pet.adoptedStatus,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp, vertical = 4.dp
                                            ),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (pet.adoptedStatus == "Disponible")
                                                Color(0xFF388E3C) else Color(0xFFC62828)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                val speciesRes = getPetTranslation(pet.species)
                                Text(
                                    buildString {
                                        append(speciesRes?.let { stringResource(it) } ?: pet.species)
                                        if (pet.breed.isNotEmpty()) append(" · ${pet.breed}")
                                    },
                                    fontSize = 13.sp,
                                    color = Color(0xFF795548)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (pet.gender.isNotEmpty()) {
                                        PetChip(
                                            getPetTranslation(pet.gender)
                                                ?.let { stringResource(it) } ?: pet.gender
                                        )
                                    }
                                    if (pet.ageGroup.isNotEmpty()) {
                                        PetChip(
                                            getPetTranslation(pet.ageGroup)
                                                ?.let { stringResource(it) } ?: pet.ageGroup
                                        )
                                    }
                                    if (pet.size.isNotEmpty()) {
                                        PetChip(
                                            getPetTranslation(pet.size)
                                                ?.let { stringResource(it) } ?: pet.size
                                        )
                                    }
                                }

                                if (pet.city.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "📍 ${pet.city}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            fontSize = 11.sp,
            color = Color(0xFFD7CCC8),
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { (key, label) ->
                val isSelected = selected == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(key) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor         = Color.Transparent,
                        labelColor             = Color(0xFFD7CCC8),
                        selectedContainerColor = Color(0xFFD7CCC8),
                        selectedLabelColor     = Color(0xFF5C4033)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled             = true,
                        selected            = isSelected,
                        borderColor         = Color(0x55D7CCC8),
                        selectedBorderColor = Color.Transparent,
                        borderWidth         = 1.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun PetChip(label: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF5F0EB)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            color = Color(0xFF5C4033),
            fontWeight = FontWeight.Medium
        )
    }
}