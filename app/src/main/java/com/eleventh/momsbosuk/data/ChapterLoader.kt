package com.eleventh.momsbosuk.data

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.annotation.RawRes
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString


fun loadChapterFromRawSafe(context: Context, @RawRes resId: Int): Result<ChapterPayload> = runCatching {
    val text = context.resources.openRawResource(resId).bufferedReader(Charsets.UTF_8).use { it.readText() }

    if (text.isBlank()) {
        error("JSON is blank") // Throws an exception that will be caught by runCatching
    }

    // Explicitly specify the type for the decoder
    Json { ignoreUnknownKeys = true }.decodeFromString<ChapterPayload>(text)
}.onFailure { e ->
    when (e) {
        is Resources.NotFoundException -> Log.e("Bosuk", "raw/$resId not found", e)
        is SerializationException -> Log.e("Bosuk", "JSON parse error", e)
        // This will now catch the "JSON is blank" error as well
        else -> Log.e("Bosuk", "Unexpected error", e)
    }
}
