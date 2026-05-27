package com.example.furever.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
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
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.furever.R
import com.example.furever.auth.AuthViewModel
import com.example.furever.models.PetPost
import com.example.furever.utils.LanguageManager
import com.example.furever.viewmodels.PetViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel,
    onNavigateToPetDetail: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val email      = currentUser?.email ?: "Usuario"
    val initials   = email.take(2).uppercase()

    val allPets by petViewModel.pets.collectAsStateWithLifecycle()
    val myPets = allPets.filter { it.ownerId == email }

    // Estado del dropdown
    var expanded       by remember { mutableStateOf(false) }
    var selectedCode   by remember { mutableStateOf(LanguageManager.getCurrentLanguageCode()) }
    val selectedLabel  = LanguageManager.supportedLanguages.entries
        .firstOrNull { it.value == selectedCode }?.key ?: selectedCode

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
                        .padding(top = 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD7CCC8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initials,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C4033)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(email, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        pluralStringResource(R.plurals.published_pets, myPets.size, myPets.size),
                        color = Color(0xFFD7CCC8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // ── Stats ─────────────────────────────────────────────────────
            item {
                val disponibles = myPets.count { it.adoptedStatus == "Disponible" }
                val adoptadas   = myPets.count { it.adoptedStatus != "Disponible" }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-16).dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label    = stringResource(R.string.plural_available),
                        value    = disponibles.toString(),
                        color    = Color(0xFF388E3C),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label    = stringResource(R.string.plural_adopted),
                        value    = adoptadas.toString(),
                        color    = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label    = stringResource(R.string.total),
                        value    = myPets.size.toString(),
                        color    = Color(0xFF5C4033),
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
                                focusedBorderColor   = Color(0xFF5C4033),
                                unfocusedBorderColor = Color(0xFF9E9E9E),
                                focusedLabelColor    = Color(0xFF5C4033)
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
                                        // La Activity se recrea sola con el nuevo idioma ✓
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
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
                    pet     = pet,
                    onClick = { onNavigateToPetDetail(pet.id) }
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
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFC62828))
                ) {
                    Text(stringResource(R.string.sign_out), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Componentes privados ─────────────────────────────────────────────────────

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier             = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E))
        }
    }
}

@Composable
private fun MyPetCard(pet: PetPost, onClick: () -> Unit) {
    val isAvailable = pet.adoptedStatus == "Disponible"
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(14.dp),
        onClick   = onClick,
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model            = pet.imageUrl,
                contentDescription = pet.name,
                modifier         = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)),
                contentScale     = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF3E2723))
                Text(pet.species, fontSize = 13.sp, color = Color(0xFF795548))
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        if (isAvailable) stringResource(R.string.available)
                        else             stringResource(R.string.adopted),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isAvailable) Color(0xFF388E3C) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}