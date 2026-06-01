package com.example.furever.viewmodels

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.furever.models.PetPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.log


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

    //Cargar Mascota
    // En uploadPet(), el ownerId ya viene del auth.currentUser?.email
    // Asegurarse que la firma de uploadPet NO recibe ownerId desde afuera:

    fun uploadPet(pet: PetPost, imageUri: Uri? = null) {
        val usermail = auth.currentUser?.email ?: "Anónimo"
        val petRef = db.collection("pets").document()

        if (imageUri != null) {
            // Subir imagen a Firebase Storage primero
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .reference.child("pet_images/${petRef.id}.jpg")

            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        savePet(pet, petRef, usermail, downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    // Si falla la imagen, guardar sin foto
                    savePet(pet, petRef, usermail, "")
                }
        } else {
            savePet(pet, petRef, usermail, "")
        }
    }

    private fun savePet(pet: PetPost, petRef: com.google.firebase.firestore.DocumentReference, usermail: String, imageUrl: String) {
        val newPet = pet.copy(
            id       = petRef.id,
            ownerId  = usermail,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        petRef.set(newPet)
    }

    //Adoptar Mascota
    fun adoptPet(petId: String) {
        db.collection("pets").document(petId)
            .update("adoptedStatus", "Adoptado")
            .addOnSuccessListener {
                Log.d("Firestore", "Mascota adoptada correctamente")
            }
    }

}