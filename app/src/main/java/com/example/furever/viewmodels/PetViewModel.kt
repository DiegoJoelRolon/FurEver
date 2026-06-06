package com.example.furever.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.furever.models.PetPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PetViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ── Estado base ───────────────────────────────────────────────────────────
    private val _pets = MutableStateFlow<List<PetPost>>(emptyList())
    val pets: StateFlow<List<PetPost>> = _pets

    // ── Filtros ───────────────────────────────────────────────────────────────
    private val _searchQuery    = MutableStateFlow("")
    private val _speciesFilter  = MutableStateFlow("Todas")
    private val _genderFilter   = MutableStateFlow("Todos")
    private val _sizeFilter     = MutableStateFlow("Todos")
    private val _ageGroupFilter = MutableStateFlow("Todos")

    init { fetchPets() }

    // ── Funciones de filtro ───────────────────────────────────────────────────
    fun onSearchQueryChanged(query: String)       { _searchQuery.value   = query   }
    fun onSpeciesFilterChanged(species: String)   { _speciesFilter.value = species }
    fun onGenderFilterChanged(gender: String)     { _genderFilter.value  = gender  }
    fun onSizeFilterChanged(size: String)         { _sizeFilter.value    = size    }
    fun onAgeGroupFilterChanged(ageGroup: String) { _ageGroupFilter.value = ageGroup }

    fun clearAllFilters() {
        _searchQuery.value    = ""
        _speciesFilter.value  = "Todas"
        _genderFilter.value   = "Todos"
        _sizeFilter.value     = "Todos"
        _ageGroupFilter.value = "Todos"
    }

    // ── Lista filtrada completa (buscador + todos los filtros) ────────────────
    val allFilteredPets: StateFlow<List<PetPost>> = combine(
        _pets,
        _searchQuery,
        _speciesFilter,
        _genderFilter,
        combine(_sizeFilter, _ageGroupFilter) { size, age -> Pair(size, age) }
    ) { pets, query, species, gender, (size, age) ->
        pets.filter { pet ->
            val matchesSearch  = query.isEmpty() ||
                    pet.name.contains(query, ignoreCase = true)
            val matchesSpecies = species == "Todas" || pet.species  == species
            val matchesGender  = gender  == "Todos" || pet.gender   == gender
            val matchesSize    = size    == "Todos" || pet.size     == size
            val matchesAge     = age     == "Todos" || pet.ageGroup == age
            matchesSearch && matchesSpecies && matchesGender && matchesSize && matchesAge
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Conteo de resultados para la pantalla de filtros ─────────────────────
    val filteredCount: StateFlow<Int> = allFilteredPets
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Secciones para el home ────────────────────────────────────────────────
    // Solo mascotas disponibles
    val availablePets: StateFlow<List<PetPost>> = _pets
        .map { it.filter { p -> p.adoptedStatus == "Disponible" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Perros disponibles
    val dogs: StateFlow<List<PetPost>> = _pets
        .map { it.filter { p -> p.species == "Perro" && p.adoptedStatus == "Disponible" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gatos disponibles
    val cats: StateFlow<List<PetPost>> = _pets
        .map { it.filter { p -> p.species == "Gato" && p.adoptedStatus == "Disponible" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Otros disponibles
    val others: StateFlow<List<PetPost>> = _pets
        .map { it.filter { p -> p.species == "Otro" && p.adoptedStatus == "Disponible" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cachorros disponibles
    val puppies: StateFlow<List<PetPost>> = _pets
        .map { it.filter { p -> p.ageGroup == "Cachorro" && p.adoptedStatus == "Disponible" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recién llegados (últimos 10 disponibles)
    val recentPets: StateFlow<List<PetPost>> = _pets
        .map { it.filter { p -> p.adoptedStatus == "Disponible" }.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Firestore ─────────────────────────────────────────────────────────────
    fun fetchPets() {
        db.collection("pets")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener
                _pets.value = snapshot?.toObjects(PetPost::class.java) ?: emptyList()
            }
    }

    // ── Cloudinary ────────────────────────────────────────────────────────────
    private fun ensureCloudinaryInitialized(context: Context) {
        try {
            MediaManager.get()
        } catch (e: IllegalStateException) {
            MediaManager.init(context, mapOf("cloud_name" to "dyadmxw0i", "secure" to true))
        }
    }

    fun uploadPet(context: Context, pet: PetPost, imageUri: Uri? = null) {
        ensureCloudinaryInitialized(context)
        val usermail = auth.currentUser?.email ?: "Error"
        val petRef   = db.collection("pets").document()

        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .unsigned("ml_default")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        savePet(pet, petRef, usermail, resultData?.get("secure_url") as? String ?: "")
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("Cloudinary", "Error: ${error?.description}")
                        savePet(pet, petRef, usermail, "")
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            savePet(pet, petRef, usermail, "")
        }
    }

    private fun savePet(
        pet: PetPost,
        petRef: com.google.firebase.firestore.DocumentReference,
        usermail: String,
        imageUrl: String
    ) {
        petRef.set(pet.copy(
            id        = petRef.id,
            ownerId   = usermail,
            imageUrl  = imageUrl,
            images    = if (imageUrl.isNotEmpty()) listOf(imageUrl) else emptyList(),
            timestamp = System.currentTimeMillis()
        ))
    }

    // ── Adoptar ───────────────────────────────────────────────────────────────
    fun adoptPet(petId: String) {
        val adopterEmail = auth.currentUser?.email ?: ""
        val adopterUid   = auth.currentUser?.uid   ?: ""

        db.collection("users").document(adopterUid).get()
            .addOnSuccessListener { doc ->
                val phone = doc.getString("phone") ?: ""
                db.collection("pets").document(petId).update(mapOf(
                    "adoptedStatus" to "Adoptado",
                    "adopterEmail"  to adopterEmail,
                    "adopterPhone"  to phone
                ))
            }
            .addOnFailureListener {
                db.collection("pets").document(petId).update(mapOf(
                    "adoptedStatus" to "Adoptado",
                    "adopterEmail"  to adopterEmail,
                    "adopterPhone"  to ""
                ))
            }
    }

    // ── Editar ────────────────────────────────────────────────────────────────
    fun updatePet(
        context: Context,
        petId: String,
        updatedFields: Map<String, Any>,
        newImageUri: Uri? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (newImageUri != null) {
            ensureCloudinaryInitialized(context)
            MediaManager.get().upload(newImageUri)
                .unsigned("ml_default")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String ?: ""
                        db.collection("pets").document(petId)
                            .update(updatedFields.toMutableMap().apply { put("imageUrl", imageUrl) })
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onError(it.message ?: "Error") }
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        onError(error?.description ?: "Error Cloudinary")
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            db.collection("pets").document(petId)
                .update(updatedFields)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it.message ?: "Error") }
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    fun deletePet(petId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        db.collection("pets").document(petId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }
}