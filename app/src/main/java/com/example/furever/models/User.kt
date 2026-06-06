package com.example.furever.models

data class User(
    val uid: String = "",
    val name: String = "",
    val lastname: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val profileImageUrl: String = "",
    val favorites: List<String> = emptyList(),      // IDs de mascotas favoritas
    val hasCompletedOnboarding: Boolean = false,    // si ya hizo el onboarding
    val prefSpecies: String = "",                   // preferencia especie
    val prefSize: String = "",                      // preferencia tamaño
    val prefAge: String = "",                       // preferencia edad
    val prefGender: String = ""                     // preferencia género
)