package com.kidverse.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Calendar

object ReadingTracker {

    private const val PREF_NAME = "kidverse_prefs"
    private const val KEY_LAST_READ_STORY_ID = "lastReadStoryId"
    private const val KEY_WEEKLY_READ_PREFIX = "weeklyReadStoryIds_"

    fun recordStoryRead(context: Context, storyId: String) {
        recordStorySession(
            context = context,
            storyId = storyId,
            storyTitle = "",
            category = "",
            readDurationSec = 1,
            wordsRead = 0
        )
    }

    fun recordStorySession(
        context: Context,
        storyId: String,
        storyTitle: String,
        category: String,
        readDurationSec: Int,
        wordsRead: Int
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val weekKey = getCurrentWeekKey()
        val weeklyKey = "$KEY_WEEKLY_READ_PREFIX$weekKey"
        val existing = prefs.getStringSet(weeklyKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        existing.add(storyId)

        prefs.edit()
            .putString(KEY_LAST_READ_STORY_ID, storyId)
            .putStringSet(weeklyKey, existing)
            .apply()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val payload = mapOf(
            "storyId" to storyId,
            "storyTitle" to storyTitle,
            "category" to category,
            "readAt" to System.currentTimeMillis(),
            "weekKey" to weekKey,
            "readDurationSec" to maxOf(1, readDurationSec),
            "wordsRead" to maxOf(0, wordsRead)
        )

        val sessionId = "${System.currentTimeMillis()}_$storyId"

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("readingHistory")
            .document(sessionId)
            .set(payload, SetOptions.merge())
    }

    fun getLocalWeeklyReadCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val weeklyKey = "$KEY_WEEKLY_READ_PREFIX${getCurrentWeekKey()}"
        return prefs.getStringSet(weeklyKey, emptySet())?.size ?: 0
    }

    private fun getCurrentWeekKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_W${cal.get(Calendar.WEEK_OF_YEAR)}"
    }
}
