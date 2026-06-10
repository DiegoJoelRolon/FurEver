package com.example.furever.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.furever.helpers.ImageUploadHelper
import com.example.furever.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init { checkAuthStatus() }

    private fun checkAuthStatus() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            fetchUserProfile(firebaseUser.uid)
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email o contraseña vacíos")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("El email no tiene un formato válido")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    fetchUserProfile(uid)
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Algo salió mal")
                }
            }
    }

    fun signup(
        email: String, password: String,
        name: String, lastname: String,
        phone: String, city: String,
        latitude: Double = 0.0, longitude: Double = 0.0
    ) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            _authState.value = AuthState.Error("Completá todos los campos obligatorios")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("El email no tiene un formato válido")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = User(
                        uid = uid, name = name, lastname = lastname,
                        email = email, phone = phone, city = city,
                        latitude = latitude, longitude = longitude,
                        hasCompletedOnboarding = true
                    )
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            _currentUser.value = user
                            _authState.value = AuthState.Authenticated
                        }
                        .addOnFailureListener {
                            _authState.value = AuthState.Error("Error al guardar el perfil")
                        }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Algo salió mal")
                }
            }
    }

    private fun fetchUserProfile(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                _currentUser.value = user
            }
    }

    // ── Favoritos ─────────────────────────────────────────────────────────────
    fun toggleFavorite(petId: String) {
        val uid = auth.currentUser?.uid ?: return
        val currentFavs = _currentUser.value?.favorites ?: emptyList()
        val isAlreadyFav = petId in currentFavs

        val newFavs = if (isAlreadyFav) currentFavs - petId else currentFavs + petId
        _currentUser.value = _currentUser.value?.copy(favorites = newFavs)

        db.collection("users").document(uid)
            .set(mapOf("favorites" to newFavs), com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                android.util.Log.d("FAV_DEBUG", "Firestore actualizado OK")
            }
            .addOnFailureListener {
                android.util.Log.d("FAV_DEBUG", "Firestore FALLÓ: ${it.message}")
                _currentUser.value = _currentUser.value?.copy(favorites = currentFavs)
            }
    }

    fun isFavorite(petId: String): Boolean {
        return petId in (_currentUser.value?.favorites ?: emptyList())
    }

    // ── Foto de perfil ────────────────────────────────────────────────────────
    fun updateProfileImage(uri: Uri, context: android.content.Context) {
        val uid = auth.currentUser?.uid ?: return
        _authState.value = AuthState.Loading
        ImageUploadHelper.uploadProfileImage(
            uri = uri, userId = uid,
            onSuccess = { downloadUrl ->
                db.collection("users").document(uid)
                    .update("profileImageUrl", downloadUrl)
                    .addOnSuccessListener {
                        _currentUser.value = _currentUser.value?.copy(profileImageUrl = downloadUrl)
                        _authState.value = AuthState.Authenticated
                    }
            },
            onError = { _authState.value = AuthState.Error(it) }
        )
    }

    // ── Preferencias ──────────────────────────────────────────────────────────────
    fun savePreferences(
        prefSpecies: String,
        prefSize: String,
        prefAge: String,
        prefGender: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "prefSpecies" to prefSpecies,
            "prefSize"    to prefSize,
            "prefAge"     to prefAge,
            "prefGender"  to prefGender
        )
        db.collection("users").document(uid)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                _currentUser.value = _currentUser.value?.copy(
                    prefSpecies = prefSpecies,
                    prefSize    = prefSize,
                    prefAge     = prefAge,
                    prefGender  = prefGender
                )
            }
    }

    // ── Agregar todos a favoritos ─────────────────────────────────────────────────
    fun addAllToFavorites(petIds: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        val currentFavs = _currentUser.value?.favorites ?: emptyList()
        val newFavs = (currentFavs + petIds).distinct()

        _currentUser.value = _currentUser.value?.copy(favorites = newFavs)

        // ✅ set con merge — no falla si el doc no existe
        db.collection("users").document(uid)
            .set(mapOf("favorites" to newFavs), com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener {
                _currentUser.value = _currentUser.value?.copy(favorites = currentFavs)
            }
    }
    fun signout() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
}