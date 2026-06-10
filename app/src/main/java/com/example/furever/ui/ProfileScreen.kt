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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.furever.R
import com.example.furever.auth.AuthViewModel
import com.example.furever.models.PetPost
import com.example.furever.utils.LanguageManager
import com.example.furever.viewmodels.PetViewModel
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.rounded.Pets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel,
    onNavigateToPetDetail: (String) -> Unit,
    onNavigateToRequests: () -> Unit,   // ← nuevo
    onSignOut: () -> Unit
) {
    val context = LocalContext.current

    // ── Datos del usuario ─────────────────────────────────────────────────
    val currentUserProfile by authViewModel.currentUser.collectAsStateWithLifecycle()
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val email = firebaseUser?.email ?: "Usuario"
    val displayName = when {
        !currentUserProfile?.name.isNullOrEmpty() ->
            "${currentUserProfile?.name} ${currentUserProfile?.lastname ?: ""}".trim()
        else -> email
    }
    val initials = when {
        !currentUserProfile?.name.isNullOrEmpty() ->
            "${currentUserProfile?.name?.first()}${currentUserProfile?.lastname?.firstOrNull() ?: ""}".uppercase()
        else -> email.take(2).uppercase()
    }

    // ── Mascotas ──────────────────────────────────────────────────────────
    val allPets by petViewModel.pets.collectAsStateWithLifecycle()
    val myPets = allPets.filter { it.ownerId == email }
    val disponibles = myPets.count { it.adoptedStatus == "Disponible" }
    val adoptadas = myPets.count { it.adoptedStatus != "Disponible" }

    // ── Idioma ────────────────────────────────────────────────────────────
    var expanded by remember { mutableStateOf(false) }
    var selectedCode by remember { mutableStateOf(LanguageManager.getCurrentLanguageCode()) }
    val selectedLabel = LanguageManager.supportedLanguages.entries
        .firstOrNull { it.value == selectedCode }?.key ?: selectedCode

    // ── Foto de perfil ────────────────────────────────────────────────────
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageOptions by remember { mutableStateOf(false) }

    // ── Edición de mascota ────────────────────────────────────────────────
    var petToEdit by remember { mutableStateOf<PetPost?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { authViewModel.updateProfileImage(it, context) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { authViewModel.updateProfileImage(it, context) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // ── Dialog: foto de perfil ─────────────────────────────────────────────
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    "Foto de perfil",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            showImageOptions = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                        border = BorderStroke(1.5.dp, Color(0xFF5C4033))
                    ) { Text(stringResource(R.string.choose_from_gallery)) }

                    OutlinedButton(
                        onClick = {
                            showImageOptions = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                        border = BorderStroke(1.5.dp, Color(0xFF5C4033))
                    ) { Text(stringResource(R.string.take_photo)) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFF9E9E9E))
                }
            }
        )
    }

    // ── Dialog: editar mascota ─────────────────────────────────────────────
    // IMPORTANTE: debe estar DENTRO del árbol de composición, antes o después
    // del Scaffold, pero siempre dentro del scope del @Composable principal.
    petToEdit?.let { pet ->
        EditPetDialog(
            pet = pet,
            onDismiss = { petToEdit = null },
            onSave = { fields, imageUri ->
                petViewModel.updatePet(
                    context       = context,
                    petId         = pet.id,
                    updatedFields = fields,
                    newImageUri   = imageUri,
                    onSuccess     = { petToEdit = null },
                    onError       = { petToEdit = null }
                )
            }
        )
    }

    // ── UI principal ──────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.perfil),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5C4033),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F0EB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF5C4033))
                        .padding(top = 24.dp, bottom = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD7CCC8))
                            .clickable { showImageOptions = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val profileUrl = currentUserProfile?.profileImageUrl
                        if (!profileUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = profileUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                initials,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5C4033)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3E2723)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar foto",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (displayName != email) {
                        Text(email, color = Color(0xFFD7CCC8), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!currentUserProfile?.city.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFD7CCC8),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                currentUserProfile?.city ?: "",
                                color = Color(0xFFD7CCC8),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        pluralStringResource(R.plurals.published_pets, myPets.size, myPets.size),
                        color = Color(0xFFD7CCC8),
                        fontSize = 13.sp
                    )
                }
            }

            // ── Stats ─────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-16).dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = stringResource(R.string.plural_available),
                        value = disponibles.toString(),
                        color = Color(0xFF388E3C),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = stringResource(R.string.plural_adopted),
                        value = adoptadas.toString(),
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = stringResource(R.string.total),
                        value = myPets.size.toString(),
                        color = Color(0xFF5C4033),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Selector de idioma ────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFF5C4033),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.settings_language),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF5C4033)
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5C4033),
                                unfocusedBorderColor = Color(0xFF9E9E9E),
                                focusedLabelColor = Color(0xFF5C4033)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            LanguageManager.supportedLanguages.forEach { (label, code) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedCode = code
                                        expanded = false
                                        LanguageManager.setLanguage(code)
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Solicitudes recibidas ─────────────────────────────────────────────
            item {
                val pendingRequests by petViewModel.pendingRequests.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) { petViewModel.fetchPendingRequests() }

                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape     = RoundedCornerShape(14.dp),
                    onClick   = onNavigateToRequests,
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (pendingRequests.isNotEmpty()) Color(0xFFFFEBEE)
                                    else Color(0xFFEDE0D4)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Pets,
                                contentDescription = null,
                                tint               = if (pendingRequests.isNotEmpty())
                                    Color(0xFFC62828) else Color(0xFF5C4033),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.adoption_requests),
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp,
                                color      = Color(0xFF3E2723)
                            )
                            Text(
                                if (pendingRequests.isNotEmpty())
                                    pluralStringResource(R.plurals.pending_requests, pendingRequests.size,pendingRequests.size)
                                else
                                    stringResource(R.string.no_pending_requests),
                                fontSize = 12.sp,
                                color    = if (pendingRequests.isNotEmpty())
                                    Color(0xFFC62828) else Color(0xFF9E9E9E)
                            )
                        }
                        if (pendingRequests.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFC62828)
                            ) {
                                Text(
                                    "${pendingRequests.size}",
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize   = 12.sp,
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(
                            imageVector        = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint               = Color(0xFFBCAAA4),
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Título mis publicaciones ───────────────────────────────────
            item {
                Text(
                    stringResource(R.string.my_posts),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color(0xFF3E2723),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ── Estado vacío ──────────────────────────────────────────────
            if (myPets.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🐾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.empty_posts),
                            color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // ── Cards de mascotas ─────────────────────────────────────────
            items(myPets) { pet ->
                MyPetCard(
                    pet      = pet,
                    onClick  = { onNavigateToPetDetail(pet.id) },
                    onEdit   = { petToEdit = it },
                    onDelete = { petViewModel.deletePet(it.id) }
                )
            }

            // ── Cerrar sesión ─────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { authViewModel.signout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                    border = BorderStroke(1.5.dp, Color(0xFFC62828))
                ) {
                    Text(stringResource(R.string.sign_out), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── StatCard ──────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E))
        }
    }
}

// ── MyPetCard ─────────────────────────────────────────────────────────────────

@Composable
private fun MyPetCard(
    pet: PetPost,
    onClick: () -> Unit,
    onEdit: (PetPost) -> Unit,
    onDelete: (PetPost) -> Unit
) {
    val isAvailable = pet.adoptedStatus == "Disponible"
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // ── Confirmación de eliminación ───────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    stringResource(R.string.delete_confirmation_first),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
            },
            text = {
                Text(
                    stringResource(R.string.delete_confirmation_second,pet.name),
                    color = Color(0xFF5C4033)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(pet)          // ← llama directo, sin lambda extra
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.delete_button), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFF9E9E9E))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pet.imageUrl,
                contentDescription = pet.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pet.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF3E2723)
                )
                Text(getTranslation(pet.species), fontSize = 13.sp, color = Color(0xFF795548))
                if (!pet.city.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(pet.city, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        if (isAvailable) stringResource(R.string.available)
                        else stringResource(R.string.adopted),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isAvailable) Color(0xFF388E3C) else Color(0xFFC62828)
                    )
                }
            }

            // ── Menú ─────────────────────────────────────────────────────
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = Color(0xFF9E9E9E)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = Color.White
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit), color = Color(0xFF3E2723)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF5C4033),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onEdit(pet)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_button), color = Color(0xFFC62828)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showDeleteConfirm = true
                        }
                    )
                }
            }
        }
    }
}

// ── EditPetDialog ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetDialog(
    pet: PetPost,
    onDismiss: () -> Unit,
    onSave: (updatedFields: Map<String, Any>, newImageUri: Uri?) -> Unit
) {
    var name           by remember { mutableStateOf(pet.name) }
    var description    by remember { mutableStateOf(pet.description) }
    var city           by remember { mutableStateOf(pet.city ?: "") }
    var status         by remember { mutableStateOf(pet.adoptedStatus) }
    var newImageUri    by remember { mutableStateOf<Uri?>(null) }
    var statusExpanded by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { newImageUri = it } }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                stringResource(R.string.edit_pet, pet.name),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Cambiar foto
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                    border = BorderStroke(1.5.dp, Color(0xFF5C4033))
                ) {
                    Icon(
                        imageVector = if (newImageUri != null) Icons.Default.Edit else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (newImageUri != null)  stringResource(R.string.selected_photo) else stringResource(R.string.change_photo))
                }

                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.pet_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5C4033),
                        focusedLabelColor  = Color(0xFF5C4033)
                    )
                )

                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.pet_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5C4033),
                        focusedLabelColor  = Color(0xFF5C4033)
                    )
                )

                // Ciudad
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(stringResource(R.string.city)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5C4033),
                        focusedLabelColor  = Color(0xFF5C4033)
                    )
                )

                // Estado
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5C4033),
                            focusedLabelColor  = Color(0xFF5C4033)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        listOf("Disponible", "Adoptado").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    status = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fields = mapOf<String, Any>(
                        "name"          to name.trim(),
                        "description"   to description.trim(),
                        "city"          to city.trim(),
                        "adoptedStatus" to status
                    )
                    onSave(fields, newImageUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4033)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color(0xFF9E9E9E))
            }
        }
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun createCameraUri(context: Context): Uri {
    val file = java.io.File(
        context.cacheDir,
        "profile_photo_${System.currentTimeMillis()}.jpg"
    )
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}