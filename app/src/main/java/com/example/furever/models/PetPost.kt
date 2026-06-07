package com.example.furever.models
data class PetPost(
    val id: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val gender: String = "",
    val size: String = "",
    val ageGroup: String = "",
    val description: String = "",
    val imageUrl: String = "",      // ← mantenerlo para compatibilidad con posts viejos
    val images: List<String> = emptyList(), // ← lista de fotos nueva
    val ownerId: String = "",
    val ownerPhone: String = "",
    val timestamp: Long = 0L,
    val adoptedStatus: String = "Disponible",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val adopterEmail: String = "",
    val adopterPhone: String = ""
)