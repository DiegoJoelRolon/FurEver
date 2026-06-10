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
import java.io.File
import com.example.furever.models.AdoptionRequest
import com.example.furever.notifications.NotificationHelper
import com.google.firebase.firestore.DocumentChange

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

    private fun uriToFile(context: Context, uri: Uri): File {val tempFile = File(
        context.cacheDir,
        "upload_${System.currentTimeMillis()}.jpg"
    )

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e("PetViewModel", "Error al copiar URI a File: ${e.message}")
            throw e // Re-lanzamos para que el try-catch de uploadPet lo capture
        }

        return tempFile
    }

    // ── Notificaciones ────────────────────────────────────────────────────────────
    private var requestsListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun startListeningForNotifications(context: Context, ownerEmail: String) {
        requestsListener?.remove()

        Log.d("NOTIF", "Iniciando listener para: $ownerEmail")
        requestsListener = db.collection("adoptionRequests")
            .whereEqualTo("ownerId", ownerEmail)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("NOTIF", "Error en listener: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("NOTIF", "Cambios detectados: ${snapshot.documentChanges.size}")
                    for (dc in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val request = dc.document.toObject(AdoptionRequest::class.java)
                            NotificationHelper.showNotification(
                                context,
                                "¡Nueva solicitud!",
                                "${request.requesterName} quiere adoptar a ${request.petName}"
                            )
                        }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        requestsListener?.remove()
    }

//funciones de POstro
    fun uploadPet(context: Context, pet: PetPost, imageUri: Uri? = null) {
        ensureCloudinaryInitialized(context)
        val uid      = auth.currentUser?.uid ?: return
        val usermail = auth.currentUser?.email ?: ""
        val petRef   = db.collection("pets").document()


        // Primero buscar el teléfono del dueño
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val ownerPhone = doc.getString("phone") ?: ""
                if (imageUri != null) {
                    try {
                        val file = uriToFile(context, imageUri)
                        MediaManager.get().upload(file.absolutePath)
                            .unsigned("ml_default")
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String?) {}
                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                                    val imageUrl = resultData?.get("secure_url") as? String ?: ""
                                    savePet(pet, petRef, usermail, ownerPhone, imageUrl)
                                }
                                override fun onError(requestId: String?, error: ErrorInfo?) {
                                    Log.e("Cloudinary", "Error: ${error?.description}")
                                    savePet(pet, petRef, usermail, ownerPhone, "")
                                }
                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                            }).dispatch()
                    }
                    catch (e: Exception) {
                        Log.e("UploadPet", "Error al procesar archivo: ${e.message}")
                        savePet(pet, petRef, usermail, ownerPhone, "")
                    }

                } else {
                    savePet(pet, petRef, usermail, ownerPhone, "")
                }
            }
    }

    fun deletePet(petId: String) {
        db.collection("pets").document(petId).delete()
    }

    private fun savePet(
        pet: PetPost,
        petRef: com.google.firebase.firestore.DocumentReference,
        usermail: String,
        ownerPhone: String,
        imageUrl: String
    ) {
        petRef.set(pet.copy(
            id         = petRef.id,
            ownerId    = usermail,
            ownerPhone = ownerPhone,
            imageUrl   = imageUrl,
            images     = if (imageUrl.isNotEmpty()) listOf(imageUrl) else emptyList(),
            timestamp  = System.currentTimeMillis()
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

    // ── Solicitudes de adopción ───────────────────────────────────────────────────

    private val _pendingRequests = MutableStateFlow<List<AdoptionRequest>>(emptyList())
    val pendingRequests: StateFlow<List<AdoptionRequest>> = _pendingRequests

    // Escuchar solicitudes recibidas (donde soy el dueño)
    fun fetchPendingRequests() {
        val myEmail = auth.currentUser?.email ?: return
        db.collection("adoptionRequests")
            .whereEqualTo("ownerId", myEmail)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                _pendingRequests.value =
                    snapshot?.toObjects(AdoptionRequest::class.java) ?: emptyList()
            }
    }

    // Enviar solicitud de adopción
    fun sendAdoptionRequest(
        pet: PetPost,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val requesterEmail = auth.currentUser?.email ?: return
        val requesterUid   = auth.currentUser?.uid   ?: return

        // Verificar que no exista ya una solicitud pendiente
        db.collection("adoptionRequests")
            .whereEqualTo("petId",      pet.id)
            .whereEqualTo("requesterId", requesterEmail)
            .whereEqualTo("status",     "pending")
            .get()
            .addOnSuccessListener { existing ->
                if (!existing.isEmpty) {
                    onError("Ya enviaste una solicitud para esta mascota")
                    return@addOnSuccessListener
                }

                // Obtener nombre del solicitante
                db.collection("users").document(requesterUid).get()
                    .addOnSuccessListener { doc ->
                        val name = "${doc.getString("name") ?: ""} ${doc.getString("lastname") ?: ""}".trim()
                        val ref  = db.collection("adoptionRequests").document()
                        val request = AdoptionRequest(
                            id            = ref.id,
                            petId         = pet.id,
                            petName       = pet.name,
                            petImageUrl   = pet.imageUrl,
                            requesterId   = requesterEmail,
                            requesterName = name.ifEmpty { requesterEmail },
                            ownerId       = pet.ownerId,
                            status        = "pending",
                            timestamp     = System.currentTimeMillis()
                        )
                        ref.set(request)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onError(it.message ?: "Error") }
                    }
            }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    // Aceptar solicitud → adopta la mascota
    fun acceptRequest(request: AdoptionRequest, onSuccess: () -> Unit = {}) {
        val batch = db.batch()

        // Actualizar estado de la solicitud
        val reqRef = db.collection("adoptionRequests").document(request.id)
        batch.update(reqRef, "status", "accepted")

        // Marcar mascota como adoptada
        val petRef = db.collection("pets").document(request.petId)
        batch.update(petRef, mapOf(
            "adoptedStatus" to "Adoptado",
            "adopterEmail"  to request.requesterId
        ))

        batch.commit().addOnSuccessListener { onSuccess() }
    }

    // Rechazar solicitud
    fun rejectRequest(request: AdoptionRequest, onSuccess: () -> Unit = {}) {
        db.collection("adoptionRequests").document(request.id)
            .update("status", "rejected")
            .addOnSuccessListener { onSuccess() }
    }

    // Verificar si ya envié una solicitud para esta mascota
    private val _myRequestStatus = MutableStateFlow<String>("")
    val myRequestStatus: StateFlow<String> = _myRequestStatus

    fun checkMyRequestStatus(petId: String) {
        val myEmail = auth.currentUser?.email ?: return
        db.collection("adoptionRequests")
            .whereEqualTo("petId",       petId)
            .whereEqualTo("requesterId", myEmail)
            .addSnapshotListener { snapshot, _ ->
                val request = snapshot?.toObjects(AdoptionRequest::class.java)?.firstOrNull()
                _myRequestStatus.value = request?.status ?: ""
            }
    }
}