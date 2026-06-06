package com.example.furever.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    pet: PetPost,
    petViewModel: PetViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val currentUser       by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isFav              = currentUser?.favorites?.contains(pet.id) ?: false
    val isAvailable        = pet.adoptedStatus == "Disponible"
    var showConfirmDialog  by remember { mutableStateOf(false) }
    val context            = LocalContext.current
    val hasLocation        = pet.latitude != 0.0 && pet.longitude != 0.0

    // Galería simulada con la única foto disponible
    val photos = pet.images.ifEmpty {
        listOf(pet.imageUrl).filter { it.isNotEmpty() }
    }
    var selectedPhoto by remember { mutableStateOf(0) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape            = RoundedCornerShape(20.dp),
            containerColor   = Color.White,
            title = {
                Text(
                    stringResource(R.string.adopt_modal, pet.name),
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF3E2723),
                    fontSize   = 18.sp
                )
            },
            text = {
                Text(
                    stringResource(R.string.adopt_modal_message, pet.name),
                    color      = Color(0xFF616161),
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
                    shape  = RoundedCornerShape(10.dp)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet.name, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.toggleFavorite(pet.id) }) {
                        Icon(
                            imageVector        = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint               = if (isFav) Color(0xFFC62828) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = Color(0xFF5C4033),
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor     = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F0EB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Foto principal ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model              = photos.getOrNull(selectedPhoto) ?: pet.imageUrl,
                    contentDescription = pet.name,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
                // Gradiente inferior para legibilidad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000))
                            )
                        )
                )
                // Badge estado sobre la foto
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        if (isAvailable) stringResource(R.string.available) else stringResource(R.string.adopted),
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium,
                        fontSize   = 12.sp,
                        color      = if (isAvailable) Color(0xFF388E3C) else Color(0xFFC62828)
                    )
                }
                // Nombre + especie sobre el gradiente
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        pet.name,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        buildString {
                            append(getTranslation(pet.species))
                            if (pet.breed.isNotEmpty()) append(" · ${pet.breed}")
                        },
                        fontSize = 14.sp,
                        color    = Color(0xCCFFFFFF)
                    )
                }
            }

            // ── Galería miniaturas ────────────────────────────────────────────
            if (photos.size > 1) {
                LazyRow(
                    modifier       = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(photos) { index, url ->
                        AsyncImage(
                            model              = url,
                            contentDescription = null,
                            modifier           = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selectedPhoto == index) 2.dp else 0.dp,
                                    color = if (selectedPhoto == index) Color(0xFF5C4033) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPhoto = index },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Chips de características ──────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    if (pet.gender.isNotEmpty())
                        InfoChip(
                            icon  = Icons.Rounded.Person,
                            label = stringResource(R.string.pet_gender),
                            value = getTranslation(pet.gender),
                            color = Color(0xFFE3F2FD),
                            iconColor = Color(0xFF1565C0),
                            modifier = Modifier.weight(1f)
                        )
                    if (pet.ageGroup.isNotEmpty())
                        InfoChip(
                            icon  = Icons.Rounded.Cake,
                            label = stringResource(R.string.pet_age),
                            value = getTranslation(pet.ageGroup),
                            color = Color(0xFFFFF9C4),
                            iconColor = Color(0xFFF57F17),
                            modifier = Modifier.weight(1f)
                        )
                    if (pet.size.isNotEmpty())
                        InfoChip(
                            icon  = Icons.Rounded.Straighten,
                            label = stringResource(R.string.pet_size),
                            value = getTranslation(pet.size),
                            color = Color(0xFFE8F5E9),
                            iconColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                }

                // ── Ubicación ─────────────────────────────────────────────────
                if (pet.city.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEDE0D4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint               = Color(0xFF5C4033),
                                modifier           = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                pet.city,
                                fontSize   = 14.sp,
                                color      = Color(0xFF5C4033),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Sección Sobre ─────────────────────────────────────────────
                SectionCard(title = "Sobre ${pet.name}", icon = Icons.Rounded.Pets) {
                    Text(
                        pet.description.ifEmpty { "Sin descripción disponible." },
                        color      = Color(0xFF616161),
                        lineHeight = 22.sp,
                        fontSize   = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Person,
                            contentDescription = null,
                            tint               = Color(0xFFBCAAA4),
                            modifier           = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.published_by, pet.ownerId),
                            fontSize = 12.sp,
                            color    = Color(0xFFBCAAA4)
                        )
                    }
                }

                // ── Google Maps ───────────────────────────────────────────────
                if (hasLocation) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionCard(title = "Ubicación", icon = Icons.Rounded.LocationOn) {
                        OutlinedButton(
                            onClick = {
                                val uri    = Uri.parse("geo:${pet.latitude},${pet.longitude}?q=${pet.latitude},${pet.longitude}(${pet.name})")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                            border   = BorderStroke(1.5.dp, Color(0xFF5C4033))
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ver en Google Maps", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // ── Adoptante ─────────────────────────────────────────────────
                if (!isAvailable && pet.adopterEmail.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionCard(title = "Datos del adoptante", icon = Icons.Rounded.Favorite) {
                        Text(
                            "Esta mascota ya encontró su hogar ❤️",
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFF3E2723),
                            fontSize   = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFD7CCC8))
                        Spacer(modifier = Modifier.height(12.dp))
                        ContactRow(icon = Icons.Default.Email, label = "Email", value = pet.adopterEmail)
                        if (pet.adopterPhone.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            ContactRow(icon = Icons.Default.Phone, label = "Teléfono", value = pet.adopterPhone)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Botón adoptar ─────────────────────────────────────────────
                if (isAvailable) {
                    Button(
                        onClick  = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4033))
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Favorite,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.adopt_button),
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEDE0D4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = Color(0xFF5C4033),
                        modifier           = Modifier.size(16.dp)
                    )
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF3E2723))
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = color,
        modifier = modifier
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconColor,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = Color(0xFF9E9E9E))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3E2723))
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(shape = CircleShape, color = Color(0xFFEDE0D4), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = Color(0xFF5C4033), modifier = Modifier.size(18.dp))
            }
        }
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E))
            Text(value, fontSize = 14.sp, color = Color(0xFF3E2723), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun getTranslation(value: String): String {
    val resId = when (value.trim()) {
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
    return resId?.let { stringResource(it) } ?: value
}