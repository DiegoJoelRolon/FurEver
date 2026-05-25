package com.example.furever.models

data class PetPost (
    val id: String = "",
    val name: String = "",
    val species: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ownerId: String = "",
    val timestamp: Long = 0L
)
