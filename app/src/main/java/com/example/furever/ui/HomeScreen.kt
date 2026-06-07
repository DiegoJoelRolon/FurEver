package com.example.furever.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val currentUser  by authViewModel.currentUser.collectAsStateWithLifecycle()
    val recentPets   by petViewModel.recentPets.collectAsStateWithLifecycle()
    val dogs         by petViewModel.dogs.collectAsStateWithLifecycle()
    val cats         by petViewModel.cats.collectAsStateWithLifecycle()
    val puppies      by petViewModel.puppies.collectAsStateWithLifecycle()
    val others       by petViewModel.others.collectAsStateWithLifecycle()
    val filteredPets by petViewModel.allFilteredPets.collectAsStateWithLifecycle()
    var activeSearch by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { petViewModel.fetchPets() }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF5C4033))) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Rounded.Pets,
                                contentDescription = null,
                                tint               = Color(0xFFD7CCC8),
                                modifier           = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FurEver", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor    = Color(0xFF5C4033),
                        titleContentColor = Color.White
                    )
                )
                OutlinedTextField(
                    value         = activeSearch,
                    onValueChange = {
                        activeSearch = it
                        petViewModel.onSearchQueryChanged(it)
                    },
                    placeholder = {
                        Text(
                            stringResource(R.string.search_hint),
                            color    = Color.LightGray,
                            fontSize = 13.sp
                        )
                    },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                        .height(48.dp),
                    shape       = RoundedCornerShape(12.dp),
                    singleLine  = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(18.dp)
                        )
                    },
                    colors    = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Color.White,
                        unfocusedBorderColor = Color(0x66FFFFFF),
                        cursorColor          = Color.White,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onNavigateToAddPet,
                containerColor = Color(0xFF5C4033),
                contentColor   = Color.White,
                shape          = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_pet))
            }
        },
        containerColor = Color(0xFFF5F0EB)
    ) { padding ->

        if (activeSearch.isNotEmpty()) {
            if (filteredPets.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector        = Icons.Rounded.Pets,
                            contentDescription = null,
                            tint               = Color(0xFFBCAAA4),
                            modifier           = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No se encontraron mascotas",
                            color      = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding      = PaddingValues(vertical = 16.dp)
                ) {
                    items(filteredPets) { pet ->
                        val isFav = currentUser?.favorites?.contains(pet.id) ?: false
                        PetCardVertical(
                            pet         = pet,
                            isFav       = isFav,
                            onToggleFav = { authViewModel.toggleFavorite(pet.id) },
                            onClick     = { onNavigateToPetDetail(pet.id) }
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    val nombre = currentUser?.name?.takeIf { it.isNotEmpty() } ?: "amigo"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            "Hola, $nombre",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF3E2723)
                        )
                        Text(
                            "Encontrá tu compañero ideal",
                            fontSize = 14.sp,
                            color    = Color(0xFF9E9E9E)
                        )
                    }
                }

                if (recentPets.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title       = "Recién llegados",
                            count       = recentPets.size,
                            icon        = Icons.Rounded.AutoAwesome,
                            iconTint    = Color(0xFFF57C00),
                            iconBgColor = Color(0xFFFFF3E0)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recentPets) { pet ->
                                val isFav = currentUser?.favorites?.contains(pet.id) ?: false
                                PetCardHorizontal(
                                    pet         = pet,
                                    isFav       = isFav,
                                    onToggleFav = { authViewModel.toggleFavorite(pet.id) },
                                    onClick     = { onNavigateToPetDetail(pet.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (dogs.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title       = "Perros",
                            count       = dogs.size,
                            icon        = Icons.Rounded.Pets,
                            iconTint    = Color(0xFF388E3C),
                            iconBgColor = Color(0xFFE8F5E9)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(dogs) { pet ->
                                val isFav = currentUser?.favorites?.contains(pet.id) ?: false
                                PetCardHorizontal(
                                    pet         = pet,
                                    isFav       = isFav,
                                    onToggleFav = { authViewModel.toggleFavorite(pet.id) },
                                    onClick     = { onNavigateToPetDetail(pet.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (puppies.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title       = "Cachorros",
                            count       = puppies.size,
                            icon        = Icons.Rounded.CrueltyFree,
                            iconTint    = Color(0xFF7B1FA2),
                            iconBgColor = Color(0xFFEDE7F6)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(puppies) { pet ->
                                val isFav = currentUser?.favorites?.contains(pet.id) ?: false
                                PetCardHorizontal(
                                    pet         = pet,
                                    isFav       = isFav,
                                    onToggleFav = { authViewModel.toggleFavorite(pet.id) },
                                    onClick     = { onNavigateToPetDetail(pet.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (cats.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title       = "Gatos",
                            count       = cats.size,
                            icon        = Icons.Rounded.Pets,
                            iconTint    = Color(0xFFC62828),
                            iconBgColor = Color(0xFFFFEBEE)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(cats) { pet ->
                                val isFav = currentUser?.favorites?.contains(pet.id) ?: false
                                PetCardHorizontal(
                                    pet         = pet,
                                    isFav       = isFav,
                                    onToggleFav = { authViewModel.toggleFavorite(pet.id) },
                                    onClick     = { onNavigateToPetDetail(pet.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (others.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title       = "Otros",
                            count       = others.size,
                            icon        = Icons.Rounded.Pets,
                            iconTint    = Color(0xFF5C4033),
                            iconBgColor = Color(0xFFEDE0D4)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(others) { pet ->
                                val isFav = currentUser?.favorites?.contains(pet.id) ?: false
                                PetCardHorizontal(
                                    pet         = pet,
                                    isFav       = isFav,
                                    onToggleFav = { authViewModel.toggleFavorite(pet.id) },
                                    onClick     = { onNavigateToPetDetail(pet.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (recentPets.isEmpty() && dogs.isEmpty() && cats.isEmpty() && others.isEmpty()) {
                    item {
                        Box(
                            modifier         = Modifier
                                .fillParentMaxSize()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector        = Icons.Rounded.Pets,
                                    contentDescription = null,
                                    tint               = Color(0xFFBCAAA4),
                                    modifier           = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No hay mascotas disponibles aún",
                                    color      = Color(0xFF9E9E9E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconTint,
                    modifier           = Modifier.size(18.dp)
                )
            }
            Text(
                title,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF3E2723)
            )
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFEDE0D4)
        ) {
            Text(
                "$count",
                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                fontSize   = 11.sp,
                color      = Color(0xFF5C4033),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PetCardHorizontal(
    pet: PetPost,
    isFav: Boolean,
    onToggleFav: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier.width(160.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Box(modifier = Modifier.clickable { onClick() }) {
            Column {
                AsyncImage(
                    model              = pet.imageUrl,
                    contentDescription = pet.name,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale       = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        pet.name,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        color      = Color(0xFF3E2723),
                        maxLines   = 1
                    )
                    if (pet.breed.isNotEmpty()) {
                        Text(
                            pet.breed,
                            fontSize = 11.sp,
                            color    = Color(0xFF795548),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (pet.ageGroup.isNotEmpty()) MiniChip(pet.ageGroup, Icons.Rounded.Cake)
                        if (pet.size.isNotEmpty())     MiniChip(pet.size,     Icons.Rounded.Straighten)
                    }
                    if (pet.city.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint               = Color(0xFF9E9E9E),
                                modifier           = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(pet.city, fontSize = 10.sp, color = Color(0xFF9E9E9E), maxLines = 1)
                        }
                    }
                }
            }

            // ✅ IconButton con zIndex — siempre por encima del clickable del Box
            IconButton(
                onClick  = onToggleFav,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(36.dp)
                    // sin zIndex
                    .clip(CircleShape)
                    .background(Color(0xCCFFFFFF))
            ) {
                Icon(
                    imageVector        = if (isFav) Icons.Filled.Favorite
                    else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint               = if (isFav) Color(0xFFC62828) else Color(0xFF9E9E9E),
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PetCardVertical(
    pet: PetPost,
    isFav: Boolean,
    onToggleFav: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Box(modifier = Modifier.clickable { onClick() }) {
            Column {
                Box {
                    AsyncImage(
                        model              = pet.imageUrl,
                        contentDescription = pet.name,
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale       = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (pet.adoptedStatus == "Disponible")
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            pet.adoptedStatus,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color      = if (pet.adoptedStatus == "Disponible")
                                Color(0xFF388E3C) else Color(0xFFC62828)
                        )
                    }
                }
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        pet.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF3E2723)
                    )
                    Text(
                        buildString {
                            append(pet.species)
                            if (pet.breed.isNotEmpty()) append(" · ${pet.breed}")
                        },
                        fontSize = 13.sp,
                        color    = Color(0xFF795548)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (pet.gender.isNotEmpty())   MiniChip(pet.gender,   Icons.Rounded.Person)
                        if (pet.ageGroup.isNotEmpty()) MiniChip(pet.ageGroup, Icons.Rounded.Cake)
                        if (pet.size.isNotEmpty())     MiniChip(pet.size,     Icons.Rounded.Straighten)
                    }
                    if (pet.city.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint               = Color(0xFF9E9E9E),
                                modifier           = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(pet.city, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                        }
                    }
                }
            }

            // ✅ IconButton con zIndex — siempre por encima del clickable del Box
            IconButton(
                onClick  = onToggleFav,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(36.dp)
                    // sin zIndex
                    .clip(CircleShape)
                    .background(Color(0xCCFFFFFF))
            ) {
                Icon(
                    imageVector        = if (isFav) Icons.Filled.Favorite
                    else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint               = if (isFav) Color(0xFFC62828) else Color(0xFF9E9E9E),
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniChip(label: String, icon: ImageVector? = null) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF5F0EB)) {
        Row(
            modifier              = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = Color(0xFF5C4033),
                    modifier           = Modifier.size(10.dp)
                )
            }
            Text(
                label,
                fontSize   = 10.sp,
                color      = Color(0xFF5C4033),
                fontWeight = FontWeight.Medium
            )
        }
    }
}