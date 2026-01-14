package com.eleventh.momsbosuk.data

import android.content.Context
import androidx.annotation.RawRes
import kotlinx.serialization.Serializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// Remove the @file:OptIn line from here



fun loadChapterFromRaw(context: Context, @RawRes resId: Int): ChapterPayload {
    val text = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
    return Json { ignoreUnknownKeys = true }.decodeFromString(text)
}