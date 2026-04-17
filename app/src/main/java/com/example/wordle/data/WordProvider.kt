package com.example.wordle.data

import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

class WordProvider {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Returns the daily word based on the current date.
     * The word is deterministically selected from the Firestore `word_bank`.
     */
    fun getDailyWord(language: String = "English", onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("Words").document(language).get()
            .addOnSuccessListener { document ->
                val wordBank = (document["word_bank"] as? List<*>)?.filterIsInstance<String>()
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
     * Fetches a random word from the Firestore collection `Words`, document for the specified language.
     * Defaults to "English" if no language is provided.
     */
    fun getRandomWord(language: String = "English", onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("Words").document(language).get()
            .addOnSuccessListener { document ->
                val wordBank = (document["word_bank"] as? List<*>)?.filterIsInstance<String>()
                if (!wordBank.isNullOrEmpty()) {
                    val randomWord = wordBank[Random.nextInt(wordBank.size)]
                    onSuccess(randomWord)
                } else {
                    onFailure(Exception("Word bank is empty or missing for language: $language."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Checks if the input word exists in the Firestore `valid_words` array for the specified language.
     * Defaults to "English" if no language is provided.
     */
    fun isWordValid(word: String, language: String = "English", onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
        val firstLetter = word.firstOrNull()?.lowercaseChar()
        if (firstLetter == null || !firstLetter.isLetter()) {
            onFailure(Exception("Invalid word: $word"))
            return
        }

        firestore.collection("Words").document(language).get()
            .addOnSuccessListener { document ->
                val validWordsMap = document["valid_words"] as? Map<*, *>
                val validWords = validWordsMap?.get(firstLetter.toString()) as? List<*>
                if (validWords != null) {
                    onSuccess(validWords.filterIsInstance<String>().contains(word))
                } else {
                    onFailure(Exception("Valid words list is empty or missing for letter: $firstLetter in language: $language."))
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
