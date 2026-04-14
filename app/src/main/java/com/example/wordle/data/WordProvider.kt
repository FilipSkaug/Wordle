package com.example.wordle.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

class WordProvider {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Returns the daily word based on the current date.
     * The word is deterministically selected from the Firestore `word_bank`.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDailyWord(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("Words").document("English").get()
            .addOnSuccessListener { document ->
                val wordBank = (document["word_bank"] as? List<*>)?.filterIsInstance<String>()
                if (!wordBank.isNullOrEmpty()) {
                    val today = LocalDate.now(ZoneId.of("UTC"))
                    val index = today.toEpochDay().toInt() % wordBank.size
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
}
