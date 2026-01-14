package com.eleventh.momsbosuk.data

import kotlinx.serialization.Serializable

@Serializable
data class ChapterPayload(
    val chapter: Int,
    val title: String,
    val items: List<WordItem>
)

@Serializable
data class WordItem(
    val id: Int,
    val sinhala: String,
    val meaning: String,
    val ipa: String,
    //val written: String,
    //val spoken: String
)
