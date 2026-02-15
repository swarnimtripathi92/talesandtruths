package com.kidverse.app.model

data class IssueModel(
    val issueId: String,
    val title: String,      // date OR month
    //val pdfUrl: String,
    val pdfUrl: String?,
    val type: String,
    val imageUrl: String?   // 👈 NEW
)