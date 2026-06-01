package com.example.furever.models

data class PetPost(
    val id: String = "",
    val name: String = "",
    val species: String = "",       // "Perro", "Gato", "Otro"
    val breed: String = "",         // raza específica
    val gender: String = "",        // "Macho", "Hembra"
    val size: String = "",          // "Pequeño", "Mediano", "Grande"
    val ageGroup: String = "",      // "Cachorro", "Joven", "Adulto", "Senior"
    val description: String = "",
    val imageUrl: String = "",
    val ownerId: String = "",
    val timestamp: Long = 0L,
    val adoptedStatus: String = "Disponible",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)