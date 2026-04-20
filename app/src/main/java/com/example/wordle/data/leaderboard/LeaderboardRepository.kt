package com.example.wordle.data.leaderboard

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class LeaderboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun fetchTopByMaxStreak(limit: Int = 50): List<LeaderboardEntry> {
        val snapshot = firestore.collection("leaderboard_maxStreak")
            .orderBy("maxStreak", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return snapshot.documents.mapIndexed { index, doc ->
            LeaderboardEntry(
                rank = index + 1,
                username = doc.getString("username") ?: "Unknown",
                maxStreak = (doc.getLong("maxStreak") ?: 0L).toInt(),
                gamesPlayed = (doc.getLong("gamesPlayed") ?: 0L).toInt(),
                wins = (doc.getLong("wins") ?: 0L).toInt()
            )
        }
    }
}
