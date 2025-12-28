package com.example.talesandtruths

data class ReadingHistoryItem(
    val storyId: String = "",
    val storyTitle: String = "",
    val category: String = "",
    val readAt: Long = 0L,
    val readDurationSec: Int = 0,
    val wordsRead: Int = 0
)
