package com.example.wordle.data.auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("Login succeeded without a user")
            UserSession(
                uid = user.uid,
                email = user.email.orEmpty(),
                username = user.displayName
            )
        }
    }

    override suspend fun signup(
        username: String,
        email: String,
        password: String
    ): Result<UserSession> {
        return runCatching {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: error("Signup succeeded without a user")

            try {
                user.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                ).await()

                val profile = mapOf(
                    "uid" to user.uid,
                    "username" to username,
                    "email" to email,
                    "createdAt" to Timestamp.now(),
                    "gamesPlayed" to 0,
                    "wins" to 0,
                    "currentStreak" to 0,
                    "maxStreak" to 0
                )

                firestore.collection("users")
                    .document(user.uid)
                    .set(profile)
                    .await()

                UserSession(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    username = username
                )
            } catch (exception: Exception) {
                user.delete().await()
                auth.signOut()
                throw exception
            }
        }
    }

    override fun getCurrentUser(): UserSession? {
        val user = auth.currentUser ?: return null
        return UserSession(
            uid = user.uid,
            email = user.email.orEmpty(),
            username = user.displayName
        )
    }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun changePassword(newPassword: String): Result<Unit> {
        return runCatching {
            val user = auth.currentUser ?: error("No user logged in")
            user.updatePassword(newPassword).await()
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return runCatching {
            val user = auth.currentUser ?: error("No user logged in")
            val uid = user.uid

            // Delete the user's document from Firestore first
            firestore.collection("users").document(uid).delete().await()

            // Then delete the actual auth account
            user.delete().await()
        }
    }
}
