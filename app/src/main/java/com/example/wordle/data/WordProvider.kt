package com.example.wordle.data

import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

class WordProvider {

    private val firestore = FirebaseFirestore.getInstance()

    private var cachedWordBank: List<String>? = null

    /**
     * Checks if the given word is in the word bank.
     * Note: This currently only works if the word bank has been loaded.
     */
    fun isValidWord(word: String): Boolean {
        return cachedWordBank?.contains(word.lowercase()) ?: true
    }

    /**
     * Returns the daily word based on the current date.
     * The word is deterministically selected from the Firestore `word_bank`.
     */
    fun getDailyWord(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("Words").document("English").get()
            .addOnSuccessListener { document ->
                val wordBank = (document["word_bank"] as? List<*>)?.filterIsInstance<String>()
                cachedWordBank = wordBank
                if (!wordBank.isNullOrEmpty()) {
                    val index = (utcEpochDay() % wordBank.size).toInt()
                    onSuccess(wordBank[index])
                } else {
                    onFailure(Exception("Word bank is empty or missing."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Fetches a random word from the Firestore collection `Words`, document `English`, array `word_bank`.
     */
    fun getRandomWord(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("Words").document("English").get()
            .addOnSuccessListener { document ->
                val wordBank = (document["word_bank"] as? List<*>)?.filterIsInstance<String>()
                cachedWordBank = wordBank
                if (!wordBank.isNullOrEmpty()) {
                    val randomWord = wordBank[Random.nextInt(wordBank.size)]
                    onSuccess(randomWord)
                } else {
                    onFailure(Exception("Word bank is empty or missing."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
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
}
