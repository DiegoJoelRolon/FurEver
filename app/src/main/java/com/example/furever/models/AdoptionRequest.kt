package com.example.furever.models

data class AdoptionRequest(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val petImageUrl: String = "",
    val requesterId: String = "",      // email del solicitante
    val requesterName: String = "",    // nombre del solicitante
    val ownerId: String = "",          // email del dueño
    val status: String = "pending",   // pending / accepted / rejected
    val timestamp: Long = 0L
)