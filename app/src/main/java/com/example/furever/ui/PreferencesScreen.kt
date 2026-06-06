package com.example.furever.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.furever.models.PetPost
import com.example.furever.viewmodels.PetViewModel

// ── Modelos ───────────────────────────────────────────────────────────────────

private data class WizardOption(
    val value: String,
    val label: String,
    val subtitle: String,
    val emoji: String,
    val bgColor: Color
)

private data class WizardStep(
    val key: String,
    val question: String,
    val subtitle: String,
    val headerEmoji: String,
    val headerBgColor: Color,
    val options: List<WizardOption>
)

private val wizardSteps = listOf(
    WizardStep(
        key           = "species",
        question      = "¿Qué mascota te imaginás?",
        subtitle      = "Elegí el tipo de compañero que buscás",
        headerEmoji   = "🐾",
        headerBgColor = Color(0xFFEDE0D4),
        options       = listOf(
            WizardOption("Perro",           "Perro",       "Fiel, juguetón y activo",   "🐕", Color(0xFFE8F5E9)),
            WizardOption("Gato",            "Gato",        "Independiente y tranquilo", "🐈", Color(0xFFE8EAF6)),
            WizardOption("Otro",            "Otro",        "Conejo, hurón y más",       "🐇", Color(0xFFFFF3E0)),
            WizardOption("Sin preferencia", "Sorprendeme", "Cualquier tipo de mascota", "✨", Color(0xFFF5F0EB))
        )
    ),
    WizardStep(
        key           = "size",
        question      = "¿De qué tamaño lo imaginás?",
        subtitle      = "Pensá en el espacio que tenés en casa",
        headerEmoji   = "📏",
        headerBgColor = Color(0xFFE1F5FE),
        options       = listOf(
            WizardOption("Pequeño",         "Pequeño",        "Menos de 10kg",   "🤏", Color(0xFFE1F5FE)),
            WizardOption("Mediano",         "Mediano",        "Entre 10 y 25kg", "👐", Color(0xFFE8F5E9)),
            WizardOption("Grande",          "Grande",         "Más de 25kg",     "🦮", Color(0xFFFFEBEE)),
            WizardOption("Sin preferencia", "Sin preferencia","Sin restricción",  "✨", Color(0xFFF5F0EB))
        )
    ),
    WizardStep(
        key           = "age",
        question      = "¿Qué etapa de vida preferís?",
        subtitle      = "Cada edad tiene su encanto",
        headerEmoji   = "🎂",
        headerBgColor = Color(0xFFFFF9C4),
        options       = listOf(
            WizardOption("Cachorro",        "Cachorro",       "Menos de 1 año",   "🍼", Color(0xFFFFF9C4)),
            WizardOption("Joven",           "Joven",          "Entre 1 y 3 años", "⚡", Color(0xFFFFEBEE)),
            WizardOption("Adulto",          "Adulto",         "Entre 3 y 8 años", "🌿", Color(0xFFE8F5E9)),
            WizardOption("Senior",          "Senior",         "Más de 8 años",    "🧡", Color(0xFFFCE4EC)),
            WizardOption("Sin preferencia", "Sin preferencia","Sin restricción",   "✨", Color(0xFFF5F0EB))
        )
    ),
    WizardStep(
        key           = "gender",
        question      = "¿Tenés preferencia de género?",
        subtitle      = "Solo para ayudarte a encontrar tu match ideal",
        headerEmoji   = "💙",
        headerBgColor = Color(0xFFE3F2FD),
        options       = listOf(
            WizardOption("Macho",           "Macho",      "Masculino",   "♂", Color(0xFFE3F2FD)),
            WizardOption("Hembra",          "Hembra",     "Femenino",    "♀", Color(0xFFFCE4EC)),
            WizardOption("Sin preferencia", "Sorprendeme","Me da igual", "✨", Color(0xFFF5F0EB))
        )
    )
)

// ── Pantalla principal ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val allPets     by petViewModel.pets.collectAsStateWithLifecycle()

    var selections by remember(currentUser) {
        mutableStateOf(mapOf(
            "species" to (currentUser?.prefSpecies ?: ""),
            "size"    to (currentUser?.prefSize    ?: ""),
            "age"     to (currentUser?.prefAge     ?: ""),
            "gender"  to (currentUser?.prefGender  ?: "")
        ))
    }

    var currentStep    by remember { mutableStateOf(0) }
    var showResults    by remember { mutableStateOf(false) }
    var saved          by remember { mutableStateOf(false) }
    var favoritesAdded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        val hasPrefs = listOf(
            currentUser?.prefSpecies,
            currentUser?.prefSize,
            currentUser?.prefAge,
            currentUser?.prefGender
        ).any { !it.isNullOrEmpty() }
        if (hasPrefs) showResults = true
    }

    val matchingPets = remember(allPets, selections) {
        allPets.filter { pet ->
            if (pet.adoptedStatus != "Disponible") return@filter false
            fun matches(key: String, petValue: String): Boolean {
                val sel = selections[key] ?: return true
                return sel.isEmpty() || sel == "Sin preferencia" || petValue == sel
            }
            matches("species", pet.species)  &&
                    matches("size",    pet.size)     &&
                    matches("age",     pet.ageGroup) &&
                    matches("gender",  pet.gender)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (showResults) "Tu match perfecto" else "¿Cómo lo imaginás?",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 20.sp
                    )
                },
                actions = {
                    if (showResults) {
                        IconButton(onClick = {
                            showResults    = false
                            currentStep    = 0
                            saved          = false
                            favoritesAdded = false
                        }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Cambiar preferencias",
                                tint               = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = Color(0xFF5C4033),
                    titleContentColor      = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F0EB)
    ) { padding ->

        AnimatedContent(
            targetState    = showResults,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label          = "pref_content"
        ) { isResults ->
            if (isResults) {
                ResultsView(
                    selections          = selections,
                    matchingPets        = matchingPets,
                    saved               = saved,
                    favoritesAdded      = favoritesAdded,
                    onSave              = {
                        // ✅ llamada correcta a savePreferences en AuthViewModel
                        authViewModel.savePreferences(
                            prefSpecies = selections["species"] ?: "",
                            prefSize    = selections["size"]    ?: "",
                            prefAge     = selections["age"]     ?: "",
                            prefGender  = selections["gender"]  ?: ""
                        )
                        saved = true
                    },
                    onAddAllToFavorites = {
                        authViewModel.addAllToFavorites(matchingPets.map { it.id })
                        favoritesAdded = true
                    },
                    onReset             = {
                        selections     = mapOf("species" to "", "size" to "", "age" to "", "gender" to "")
                        showResults    = false
                        currentStep    = 0
                        saved          = false
                        favoritesAdded = false
                    },
                    padding             = padding
                )
            } else {
                WizardView(
                    steps       = wizardSteps,
                    currentStep = currentStep,
                    selections  = selections,
                    onSelect    = { key, value ->
                        selections = selections.toMutableMap().apply { put(key, value) }
                        saved = false
                    },
                    onNext      = {
                        if (currentStep < wizardSteps.size - 1) currentStep++
                        else showResults = true
                    },
                    onBack      = { if (currentStep > 0) currentStep-- },
                    padding     = padding
                )
            }
        }
    }
}

// ── Wizard ────────────────────────────────────────────────────────────────────

@Composable
private fun WizardView(
    steps: List<WizardStep>,
    currentStep: Int,
    selections: Map<String, String>,
    onSelect: (String, String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    padding: PaddingValues
) {
    val step         = steps[currentStep]
    val selected     = selections[step.key] ?: ""
    val hasSelection = selected.isNotEmpty()
    val isLast       = currentStep == steps.size - 1
    val progress     = (currentStep + 1).toFloat() / steps.size.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color      = Color(0xFF5C4033),
            trackColor = Color(0xFFD7CCC8)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Paso ${currentStep + 1} de ${steps.size}",
            fontSize = 11.sp,
            color    = Color(0xFF9E9E9E)
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState    = currentStep,
            transitionSpec = {
                if (targetState > initialState)
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                else
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            },
            label = "wizard_header"
        ) { idx ->
            val s = steps[idx]
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier         = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(s.headerBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.headerEmoji, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    s.question,
                    fontSize   = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF3E2723),
                    textAlign  = TextAlign.Center,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    s.subtitle,
                    fontSize  = 12.sp,
                    color     = Color(0xFF9E9E9E),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            step.options.forEach { option ->
                val isSelected = selected == option.value
                val isSinPref  = option.value == "Sin preferencia"

                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    onClick   = { onSelect(step.key, option.value) },
                    colors    = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF5C4033) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 0.dp else 1.dp
                    ),
                    border    = if (isSelected) null else BorderStroke(0.5.dp, Color(0xFFD7CCC8))
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(0x33FFFFFF) else option.bgColor
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(option.emoji, fontSize = 18.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                option.label,
                                fontSize   = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (isSelected) Color.White else Color(0xFF3E2723)
                            )
                            if (!isSinPref) {
                                Text(
                                    option.subtitle,
                                    fontSize = 11.sp,
                                    color    = if (isSelected) Color(0xCCFFFFFF) else Color(0xFF9E9E9E)
                                )
                            }
                        }
                        if (isSelected) {
                            Box(
                                modifier         = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick  = onBack,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                    border   = BorderStroke(1.5.dp, Color(0xFF5C4033))
                ) {
                    Text("← Atrás", fontWeight = FontWeight.Medium)
                }
            }
            Button(
                onClick  = onNext,
                modifier = Modifier.weight(1f).height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = if (hasSelection) Color(0xFF5C4033) else Color(0xFFD7CCC8),
                    disabledContainerColor = Color(0xFFD7CCC8)
                ),
                enabled  = hasSelection
            ) {
                Text(
                    if (isLast) "Ver resultados ✓" else "Siguiente →",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        TextButton(
            onClick  = {
                onSelect(step.key, "Sin preferencia")
                onNext()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Saltar este paso", color = Color(0xFF9E9E9E), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Resultados ────────────────────────────────────────────────────────────────

@Composable
private fun ResultsView(
    selections: Map<String, String>,
    matchingPets: List<PetPost>,
    saved: Boolean,
    favoritesAdded: Boolean,
    onSave: () -> Unit,
    onAddAllToFavorites: () -> Unit,
    onReset: () -> Unit,
    padding: PaddingValues
) {
    val activeTags = selections.values.filter {
        it.isNotEmpty() && it != "Sin preferencia"
    }

    LazyColumn(
        modifier       = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF5C4033))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text("Buscás algo así...", fontSize = 13.sp, color = Color(0xFFD7CCC8))
                Spacer(modifier = Modifier.height(10.dp))
                if (activeTags.isEmpty()) {
                    Text(
                        "Sin filtros — mostrando todas las mascotas",
                        fontSize = 13.sp,
                        color    = Color(0xFFD7CCC8)
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        activeTags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0x33FFFFFF)
                            ) {
                                Text(
                                    tag,
                                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize   = 12.sp,
                                    color      = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${matchingPets.size} mascotas encontradas",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF3E2723)
                    )
                    Text("listas para ser adoptadas", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                }
                OutlinedButton(
                    onClick        = onReset,
                    shape          = RoundedCornerShape(12.dp),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                    border         = BorderStroke(1.dp, Color(0xFF5C4033)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Cambiar", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (matchingPets.isEmpty()) {
            item {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No hay mascotas con esas características todavía",
                        fontSize  = 14.sp,
                        color     = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onReset) {
                        Text("Probar con otros filtros", color = Color(0xFF5C4033))
                    }
                }
            }
        } else {
            items(matchingPets) { pet ->
                ResultPetCard(pet = pet)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (matchingPets.isNotEmpty()) {
                    Button(
                        onClick  = onAddAllToFavorites,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = if (favoritesAdded) Color(0xFF5C4033) else Color(0xFFC62828)
                        )
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = if (favoritesAdded) Icons.Filled.Favorite
                                else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier           = Modifier.size(18.dp)
                            )
                            Text(
                                if (favoritesAdded) "✓ Agregados a favoritos"
                                else "Guardar los ${matchingPets.size} en favoritos",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp
                            )
                        }
                    }
                }

                TextButton(
                    onClick  = onReset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver a configurar", color = Color(0xFF9E9E9E), fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Card de resultado ─────────────────────────────────────────────────────────

@Composable
private fun ResultPetCard(pet: PetPost) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model              = pet.imageUrl,
                contentDescription = pet.name,
                modifier           = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale       = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF3E2723))
                Text(
                    buildString {
                        append(pet.species)
                        if (pet.breed.isNotEmpty()) append(" · ${pet.breed}")
                    },
                    fontSize = 12.sp,
                    color    = Color(0xFF795548)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (pet.ageGroup.isNotEmpty()) ResultChip(pet.ageGroup)
                    if (pet.size.isNotEmpty())     ResultChip(pet.size)
                    if (pet.gender.isNotEmpty())   ResultChip(pet.gender)
                }
                if (pet.city.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("📍 ${pet.city}", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                }
            }
        }
    }
}

@Composable
private fun ResultChip(label: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF5F0EB)) {
        Text(
            label,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize   = 10.sp,
            color      = Color(0xFF5C4033),
            fontWeight = FontWeight.Medium
        )
    }
}