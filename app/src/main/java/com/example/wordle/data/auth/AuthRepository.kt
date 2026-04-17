package com.example.wordle.data.auth

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun signup(username: String, email: String, password: String): Result<UserSession>
    fun getCurrentUser(): UserSession?
    fun logout()
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun deleteAccount(currentPassword: String): Result<Unit>
    suspend fun changeUsername(newUsername: String): Result<Unit>
}
