package com.example.wordle.data.stats

import android.content.Context

class SharedPreferencesStatsRepository(
    context: Context
) : StatsRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): UserStats {
        val gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0)
        val gamesWon = prefs.getInt(KEY_GAMES_WON, 0)
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val maxStreak = prefs.getInt(KEY_MAX_STREAK, 0)
        val dist = List(6) { idx -> prefs.getInt("${KEY_GUESS_DIST_PREFIX}${idx + 1}", 0) }

        return UserStats(
            gamesPlayed = gamesPlayed,
            gamesWon = gamesWon,
            guessDistribution = dist,
            currentStreak = currentStreak,
            maxStreak = maxStreak
        )
    }

    override fun save(stats: UserStats) {
        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, stats.gamesPlayed)
            .putInt(KEY_GAMES_WON, stats.gamesWon)
            .putInt(KEY_CURRENT_STREAK, stats.currentStreak)
            .putInt(KEY_MAX_STREAK, stats.maxStreak)
            .also { editor ->
                stats.guessDistribution.forEachIndexed { i, v ->
                    editor.putInt("${KEY_GUESS_DIST_PREFIX}${i + 1}", v)
                }
            }
            .apply()
    }

    override fun reset() {
        prefs.edit().clear().apply()
    }

    private companion object {
        private const val PREFS_NAME = "wordle_user_stats"
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_GAMES_WON = "games_won"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_MAX_STREAK = "max_streak"
        private const val KEY_GUESS_DIST_PREFIX = "guess_dist_"
    }
}

