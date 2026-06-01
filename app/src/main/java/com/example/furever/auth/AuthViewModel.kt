package com.example.furever.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.furever.helpers.ImageUploadHelper
import com.example.furever.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    // Perfil del usuario actual en memoria
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            fetchUserProfile(firebaseUser.uid)  // carga el perfil si ya tenía sesión
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email o contraseña vacíos")
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

    // signup ahora recibe los datos del perfil extendido
    fun signup(
        email: String,
        password: String,
        name: String,
        lastname: String,
        phone: String,
        city: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            _authState.value = AuthState.Error("Completá todos los campos obligatorios")
            return
        }
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Guardar perfil en Firestore
                    val user = User(
                        uid = uid,
                        name = name,
                        lastname = lastname,
                        email = email,
                        phone = phone,
                        city = city,
                        latitude = latitude,
                        longitude = longitude
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
                _currentUser.value = doc.toObject(User::class.java)
            }
    }

    fun updateProfileImage(uri: Uri, context: android.content.Context) {
        val uid = auth.currentUser?.uid ?: return

        _authState.value = AuthState.Loading

        ImageUploadHelper.uploadProfileImage(
            uri = uri,
            userId = uid,
            onSuccess = { downloadUrl ->
                // Actualizar URL en Firestore
                db.collection("users").document(uid)
                    .update("profileImageUrl", downloadUrl)
                    .addOnSuccessListener {
                        // Actualizar el estado local también
                        _currentUser.value = _currentUser.value?.copy(
                            profileImageUrl = downloadUrl
                        )
                        _authState.value = AuthState.Authenticated
                    }
            },
            onError = { error ->
                _authState.value = AuthState.Error(error)
            }
        )
    }
    fun signout() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
}