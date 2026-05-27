package com.example.furever.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.furever.auth.AuthViewModel
import com.example.furever.viewmodels.PetViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.furever.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit
) {
    val pets by petViewModel.pets.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { petViewModel.fetchPets() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FurEver 🐾",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                    colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5C4033),
                    titleContentColor = Color.White
                )
            )
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
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
                                    Text(
                                        stringResource(R.string.available) /*pet.adoptedStatus ver como cambiar aca*/,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (pet.adoptedStatus == "Disponible")
                                            Color(0xFF388E3C) else Color(0xFFC62828)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                pet.species/*Ver como cambiar aca*/,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF795548)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                pet.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E9E9E),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}