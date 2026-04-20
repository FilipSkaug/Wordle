package com.example.wordle.data.stats

/**
 * Pushes local [UserStats] to the signed-in user's Firestore profile (`users/{uid}`).
 */
fun interface UserStatsRemoteSync {
    suspend fun syncStatsToFirestore(stats: UserStats)
}
