package com.example.furever.ui

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.furever.models.PetPost
import com.example.furever.viewmodels.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPetScreen(petViewModel: PetViewModel, onPostSuccess: () -> Unit) {

    val context = LocalContext.current

    var name        by remember { mutableStateOf("") }
    var breed       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedSpecies  by remember { mutableStateOf("") }
    var selectedGender   by remember { mutableStateOf("") }
    var selectedSize     by remember { mutableStateOf("") }
    var selectedAgeGroup by remember { mutableStateOf("") }

    // Foto
    var imageUri        by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri  by remember { mutableStateOf<Uri?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var uploadedImageUrl by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imageUri = it } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) imageUri = cameraImageUri }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val isFormValid = name.isNotBlank()
            && selectedSpecies.isNotBlank()
            && selectedGender.isNotBlank()
            && selectedSize.isNotBlank()
            && selectedAgeGroup.isNotBlank()

    // Dialog fuente de foto
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text("Foto de la mascota", fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            showPhotoDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                        border = BorderStroke(1.5.dp, Color(0xFF5C4033))
                    ) { Text("Elegir de la galería") }

                    OutlinedButton(
                        onClick = {
                            showPhotoDialog = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                        border = BorderStroke(1.5.dp, Color(0xFF5C4033))
                    ) { Text("Sacar una foto") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Cancelar", color = Color(0xFF9E9E9E))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicar Mascota", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Foto ──────────────────────────────────────────────────────
            SectionTitle("Foto de la mascota")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDE0D4))
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Foto de la mascota",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Botón de cambiar foto encima
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xCC5C4033))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Cambiar foto", color = Color.White, fontSize = 12.sp)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tocá para agregar una foto",
                            color = Color(0xFF9E9E9E),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ── Datos básicos ─────────────────────────────────────────────
            SectionTitle("Datos básicos")

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nombre *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = fieldColors()
            )
            OutlinedTextField(
                value = breed, onValueChange = { breed = it },
                label = { Text("Raza (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = fieldColors()
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(12.dp), maxLines = 4, colors = fieldColors()
            )

            // ── Especie ───────────────────────────────────────────────────
            SectionTitle("Especie *")
            ChipGroup(
                options = listOf("Perro", "Gato", "Otro"),
                selected = selectedSpecies, onSelect = { selectedSpecies = it }
            )

            // ── Género ────────────────────────────────────────────────────
            SectionTitle("Género *")
            ChipGroup(
                options = listOf("Macho", "Hembra"),
                selected = selectedGender, onSelect = { selectedGender = it }
            )

            // ── Tamaño ────────────────────────────────────────────────────
            SectionTitle("Tamaño *")
            ChipGroup(
                options = listOf("Pequeño", "Mediano", "Grande"),
                selected = selectedSize, onSelect = { selectedSize = it }
            )

            // ── Edad ──────────────────────────────────────────────────────
            SectionTitle("Edad *")
            ChipGroup(
                options = listOf("Cachorro", "Joven", "Adulto", "Senior"),
                selected = selectedAgeGroup, onSelect = { selectedAgeGroup = it }
            )

            // ── Botón publicar ────────────────────────────────────────────
            Button(
                onClick = {
                    val post = PetPost(
                        name        = name,
                        species     = selectedSpecies,
                        breed       = breed,
                        gender      = selectedGender,
                        size        = selectedSize,
                        ageGroup    = selectedAgeGroup,
                        description = description,
                        imageUrl    = imageUri?.toString() ?: "",
                        timestamp   = System.currentTimeMillis()
                    )
                    petViewModel.uploadPet(post, imageUri)
                    onPostSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5C4033),
                    disabledContainerColor = Color(0xFFBCAAA4)
                ),
                enabled = isFormValid
            ) {
                Text("Publicar para adopción", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val file = java.io.File(context.cacheDir, "pet_photo_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", file
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF5C4033))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipGroup(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { option ->
            val isSelected = selected == option
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = {
                    Text(
                        option, fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF5C4033),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color(0xFF5C4033)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true, selected = isSelected,
                    borderColor = Color(0xFF5C4033), selectedBorderColor = Color(0xFF5C4033)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF5C4033),
    focusedLabelColor = Color(0xFF5C4033)
)