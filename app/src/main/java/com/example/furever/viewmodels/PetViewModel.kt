package com.example.furever.viewmodels

import androidx.lifecycle.ViewModel
import com.example.furever.models.PetPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class PetViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _pets = MutableStateFlow<List<PetPost>>(emptyList())
    val pets: StateFlow<List<PetPost>> = _pets

    init {
        fetchPets()
    }

    //Busqueda de Mascotas
    fun fetchPets() {
        db.collection("pets")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener
                val petList = snapshot?.toObjects(PetPost::class.java) ?: emptyList()
                _pets.value = petList
            }
    }

    fun uploadPet(pet: PetPost) {
        val usermail = auth.currentUser?.email ?: "Anónimo"
        val petRef = db.collection("pets").document()

        val newPet = PetPost(
            id = petRef.id,
            name = pet.name,
            species = pet.species,
            description = pet.description,
            imageUrl = pet.imageUrl,
            ownerId = usermail,
            timestamp = System.currentTimeMillis()
        )
        petRef.set(newPet)

    }

}