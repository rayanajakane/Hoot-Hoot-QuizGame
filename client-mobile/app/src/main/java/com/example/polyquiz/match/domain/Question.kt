package com.example.polyquiz.match.domain

import java.util.Date

data class Question(
    val id: String,
    val type: String,
    var text: String,
    val points: Int,
    val choices: List<Choice>? = null,
    val estimatedParameters: EstimatedParameters? = null,
    val answer: String? = null,
    val lastModification: String?,
    val pictureUrl: String,
    val creatorName: String,
)
