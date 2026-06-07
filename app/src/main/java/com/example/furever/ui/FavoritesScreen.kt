package com.example.furever.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.LocationOn
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.furever.auth.AuthViewModel
import com.example.furever.viewmodels.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel,
    onNavigateToPetDetail: (String) -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val allPets     by petViewModel.pets.collectAsStateWithLifecycle()

    val favoritePets = allPets.filter { pet ->
        currentUser?.favorites?.contains(pet.id) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Favoritos",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Color(0xFF5C4033),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F0EB)
    ) { padding ->

        if (favoritePets.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint               = Color(0xFFD7CCC8),
                        modifier           = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Todavía no tenés favoritos",
                        fontSize   = 16.sp,
                        color      = Color(0xFF9E9E9E),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tocá el ♡ en cualquier mascota\npara guardarla acá",
                        fontSize   = 13.sp,
                        color      = Color(0xFFBCAAA4),
                        textAlign  = TextAlign.Center,
                        lineHeight = 20.sp
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
                item {
                    Text(
                        "${favoritePets.size} mascota${if (favoritePets.size != 1) "s" else ""} guardada${if (favoritePets.size != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color    = Color(0xFF9E9E9E)
                    )
                }

                items(favoritePets) { pet ->
                    val isFav = currentUser?.favorites?.contains(pet.id) ?: false

                    // ✅ FIX: Card sin onClick
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        // ✅ FIX: click de navegación en el Row, no en la Card
                        Row(
                            modifier          = Modifier
                                .clickable { onNavigateToPetDetail(pet.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model              = pet.imageUrl,
                                contentDescription = pet.name,
                                modifier           = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale       = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    pet.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 16.sp,
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
                                if (pet.city.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector        = Icons.Rounded.LocationOn,
                                            contentDescription = null,
                                            tint               = Color(0xFF9E9E9E),
                                            modifier           = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            pet.city,
                                            fontSize = 12.sp,
                                            color    = Color(0xFF9E9E9E)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (pet.adoptedStatus == "Disponible")
                                        Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                ) {
                                    Text(
                                        pet.adoptedStatus,
                                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color      = if (pet.adoptedStatus == "Disponible")
                                            Color(0xFF388E3C) else Color(0xFFC62828)
                                    )
                                }
                            }

                            // ✅ FIX: IconButton maneja el click independientemente del Row
                            IconButton(
                                onClick  = { authViewModel.toggleFavorite(pet.id) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F0EB))
                            ) {
                                Icon(
                                    imageVector        = if (isFav) Icons.Filled.Favorite
                                    else Icons.Default.FavoriteBorder,
                                    contentDescription = "Quitar favorito",
                                    tint               = if (isFav) Color(0xFFC62828)
                                    else Color(0xFF9E9E9E),
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}