package com.example.furever.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import kotlin.math.log
import kotlin.text.contains


class PetViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _pets = MutableStateFlow<List<PetPost>>(emptyList())
    val pets: StateFlow<List<PetPost>> = _pets

    private val _searchQuery = MutableStateFlow("")
    private val _selectedSpecies = MutableStateFlow("Todas")

    val filteredPets: StateFlow<List<PetPost>> = combine(_pets, _searchQuery, _selectedSpecies) { pets, query, species ->
        pets.filter { pet ->
            val matchesQuery = pet.name.contains(query, ignoreCase = true) ||
                    pet.city.contains(query, ignoreCase = true)||
                    pet.breed.contains(query, ignoreCase = true)

            val matchesSpecies = if (species == "Todas") true else pet.species == species
            matchesQuery && matchesSpecies
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery }
    fun onSpeciesFilterChanged(species: String) { _selectedSpecies.value = species }

    init {
        fetchPets()
    }

    // Inicialisa Cloudinary
    private fun ensureCloudinaryInitialized(context: Context) {
        try {
            MediaManager.get()
        } catch (e: IllegalStateException) {
            val config = mapOf(
                "cloud_name" to "dyadmxw0i",
                "secure" to true
            )
            MediaManager.init(context, config)
        }
    }

    fun fetchPets() {
        db.collection("pets")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener
                val petList = snapshot?.toObjects(PetPost::class.java) ?: emptyList()
                _pets.value = petList
            }
    }

    fun uploadPet(context: Context, pet: PetPost, imageUri: Uri? = null) {
        //Activamos cloudinary
        ensureCloudinaryInitialized(context)

        val usermail = auth.currentUser?.email ?: "Error"
        val petRef = db.collection("pets").document()

        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .unsigned("ml_default")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        // Obtenemos la URL de Cloudinary
                        val imageUrl = resultData?.get("secure_url") as? String ?: ""
                        savePet(pet, petRef, usermail, imageUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("Cloudinary", "Error: ${error?.description}")
                        // Si falla la subida, guardamos sin imagen o mostramos error
                        savePet(pet, petRef, usermail, "")
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            savePet(pet, petRef, usermail, "")
        }
    }

    private fun savePet(pet: PetPost, petRef: com.google.firebase.firestore.DocumentReference, usermail: String, imageUrl: String) {
        val newPet = pet.copy(
            id       = petRef.id,
            ownerId  = usermail,
            imageUrl = imageUrl, // Aca guardamos la URL de Cloudinary
            timestamp = System.currentTimeMillis()
        )
        petRef.set(newPet)
    }

    fun adoptPet(petId: String) {
        db.collection("pets").document(petId)
            .update("adoptedStatus", "Adoptado")
    }
}