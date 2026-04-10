package com.example.wordle.data.auth

data class UserSession(
    val uid: String,
    val email: String,
    val username: String? = null
)
