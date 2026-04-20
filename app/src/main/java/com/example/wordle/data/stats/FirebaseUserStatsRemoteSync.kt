package com.example.wordle.data.stats

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseUserStatsRemoteSync(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserStatsRemoteSync {

    override suspend fun syncStatsToFirestore(stats: UserStats) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        // 1) Private user stats document (owner-only reads/writes)
        val userData = mapOf(
            "gamesPlayed" to stats.gamesPlayed,
            "wins" to stats.gamesWon,
            "currentStreak" to stats.currentStreak,
            "maxStreak" to stats.maxStreak
        )

        firestore.collection("users").document(uid).set(userData, SetOptions.merge()).await()

        // 2) Public leaderboard entry (client-writable; not cheat-proof without a trusted backend)
        val leaderboardData = mapOf(
            "uid" to uid,
            "username" to (user.displayName ?: "Unknown"),
            "gamesPlayed" to stats.gamesPlayed,
            "wins" to stats.gamesWon,
            "currentStreak" to stats.currentStreak,
            "maxStreak" to stats.maxStreak,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("leaderboard_maxStreak")
            .document(uid)
            .set(leaderboardData, SetOptions.merge())
            .await()
    }
}
