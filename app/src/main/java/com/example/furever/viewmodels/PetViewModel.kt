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
import kotlinx.coroutines.flow.stateIn

class PetViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ── Estado base ───────────────────────────────────────────────────────────
    private val _pets = MutableStateFlow<List<PetPost>>(emptyList())
    val pets: StateFlow<List<PetPost>> = _pets

    // ── Filtros ───────────────────────────────────────────────────────────────
    private val _searchQuery     = MutableStateFlow("")
    private val _speciesFilter   = MutableStateFlow("Todas")
    private val _genderFilter    = MutableStateFlow("Todos")
    private val _sizeFilter      = MutableStateFlow("Todos")
    private val _ageGroupFilter  = MutableStateFlow("Todos")

    // ── Lista filtrada reactiva ───────────────────────────────────────────────
    val filteredPets: StateFlow<List<PetPost>> = combine(
        _pets,
        _searchQuery,
        _speciesFilter,
        _genderFilter,
        _sizeFilter
    ) { pets, query, species, gender, size ->
        pets.filter { pet ->
            val matchesSearch  = query.isEmpty() ||
                    pet.name.contains(query, ignoreCase = true) ||
                    pet.city.contains(query, ignoreCase = true) ||
                    pet.breed.contains(query, ignoreCase = true)
            val matchesSpecies = species == "Todas" || pet.species == species
            val matchesGender  = gender  == "Todos" || pet.gender  == gender
            val matchesSize    = size    == "Todos" || pet.size    == size
            matchesSearch && matchesSpecies && matchesGender && matchesSize
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // combine solo acepta hasta 5 parámetros, así que el filtro de edad
    // lo aplicamos observando _ageGroupFilter por separado
    private val _ageGroupFilterState = MutableStateFlow("Todos")

    init {
        fetchPets()
    }

    // ── Funciones de filtro ───────────────────────────────────────────────────
    fun onSearchQueryChanged(query: String)   { _searchQuery.value    = query   }
    fun onSpeciesFilterChanged(species: String) { _speciesFilter.value = species }
    fun onGenderFilterChanged(gender: String)   { _genderFilter.value  = gender  }
    fun onSizeFilterChanged(size: String)       { _sizeFilter.value    = size    }

    // Para el filtro de edad usamos filteredPets como base en la UI,
    // pero lo resolvemos dentro del mismo combine extendido:
    fun onAgeGroupFilterChanged(ageGroup: String) {
        _ageGroupFilter.value = ageGroup
    }

    // ── Lista filtrada con los 5 filtros (versión completa) ───────────────────
    val allFilteredPets: StateFlow<List<PetPost>> = combine(
        _pets,
        _searchQuery,
        _speciesFilter,
        _genderFilter,
        combine(_sizeFilter, _ageGroupFilter) { size, age -> Pair(size, age) }
    ) { pets, query, species, gender, (size, age) ->
        pets.filter { pet ->
            val matchesSearch  = query.isEmpty() ||
                    pet.name.contains(query, ignoreCase = true)  ||
                    pet.city.contains(query, ignoreCase = true)  ||
                    pet.breed.contains(query, ignoreCase = true)
            val matchesSpecies = species == "Todas" || pet.species  == species
            val matchesGender  = gender  == "Todos" || pet.gender   == gender
            val matchesSize    = size    == "Todos" || pet.size     == size
            val matchesAge     = age     == "Todos" || pet.ageGroup == age
            matchesSearch && matchesSpecies && matchesGender && matchesSize && matchesAge
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                        val imageUrl = resultData?.get("secure_url") as? String ?: ""
                        savePet(pet, petRef, usermail, imageUrl)
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
        petRef.set(
            pet.copy(
                id        = petRef.id,
                ownerId   = usermail,
                imageUrl  = imageUrl,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // ── Adoptar ───────────────────────────────────────────────────────────────
    fun adoptPet(petId: String) {
        db.collection("pets").document(petId)
            .update("adoptedStatus", "Adoptado")
    }
}