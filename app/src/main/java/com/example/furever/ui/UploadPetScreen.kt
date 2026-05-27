package com.example.furever.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furever.models.PetPost
import com.example.furever.viewmodels.PetViewModel
import com.example.furever.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPetScreen(petViewModel: PetViewModel, onPostSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val listaMascotas = listOf(
        "https://cdn.britannica.com/16/234216-050-C66F8665/beagle-hound-dog.jpg",
        "https://images.unsplash.com/photo-1557408938-0f220f49bca1?fm=jpg&q=60&w=3000",
        "https://corazondezonasur.com.ar/wp-content/uploads/2026/05/691514867_1517968549732005_6104570804858794001_n.jpg"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.upload_pet_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5C4033),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = Color(0xFF5C4033),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                stringResource(R.string.pet_data_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF5C4033),
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.pet_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5C4033),
                    focusedLabelColor = Color(0xFF5C4033)
                )
            )

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text(stringResource(R.string.pet_species)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5C4033),
                    focusedLabelColor = Color(0xFF5C4033)
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.pet_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5C4033),
                    focusedLabelColor = Color(0xFF5C4033)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val imageUrl = listaMascotas.random()
                    val post = PetPost(
                        name = name,
                        species = species,
                        description = description,
                        imageUrl = imageUrl,
                        timestamp = System.currentTimeMillis()
                    )
                    petViewModel.uploadPet(post)
                    onPostSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5C4033)
                ),
                enabled = name.isNotBlank() && species.isNotBlank()
            ) {
                Text(
                    stringResource(R.string.upload_button),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}