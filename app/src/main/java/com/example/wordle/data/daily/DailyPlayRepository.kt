package com.example.wordle.data.daily

import android.content.Context
import java.util.Calendar
import java.util.TimeZone

class DailyPlayRepository(
    context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasPlayedTodayUtc(): Boolean {
        val lastPlayed = prefs.getLong(KEY_LAST_PLAYED_UTC_EPOCH_DAY, -1L)
        return lastPlayed == utcEpochDay()
    }

    fun markPlayedTodayUtc() {
        prefs.edit()
            .putLong(KEY_LAST_PLAYED_UTC_EPOCH_DAY, utcEpochDay())
            .apply()
    }

    private fun utcEpochDay(): Long {
        val tz = TimeZone.getTimeZone("UTC")
        val cal = Calendar.getInstance(tz)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val millis = cal.timeInMillis
        return millis / 86_400_000L
    }

    private companion object {
        private const val PREFS_NAME = "wordle_daily_play"
        private const val KEY_LAST_PLAYED_UTC_EPOCH_DAY = "last_played_utc_epoch_day"
    }
}

