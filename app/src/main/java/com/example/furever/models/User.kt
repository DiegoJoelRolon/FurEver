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
    val profileImageUrl: String = ""
)