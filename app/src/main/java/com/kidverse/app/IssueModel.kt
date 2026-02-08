package com.kidverse.app.model

data class IssueModel(
    val title: String,      // date OR month
    val pdfUrl: String,
    val imageUrl: String?   // 👈 NEW
)
