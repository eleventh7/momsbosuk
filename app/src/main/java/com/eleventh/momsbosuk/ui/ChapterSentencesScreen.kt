package com.eleventh.momsbosuk.ui

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleventh.momsbosuk.R
import com.eleventh.momsbosuk.data.loadChapterFromRawSafe
import com.eleventh.momsbosuk.ui.components.SentencePanel
import org.json.JSONObject
import java.util.Locale

@Composable
fun ChapterSentencesScreen(onExitApp: () -> Unit) {
    val ctx = LocalContext.current
    val categories = remember { availableSentenceChaptersFromCollection(ctx) }

    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val selected: CategorySpec? = remember(selectedId, categories) {
        selectedId?.let { id -> categories.firstOrNull { it.id == id } }
    }

    BackHandler {
        if (selectedId != null) {
            selectedId = null
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("앱 종료") },
            text = { Text("종료하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onExitApp()
                }) { Text("종료") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("취소") }
            }
        )
    }

    if (selectedId == null) {
        ChapterSentencesListScreen(
            categories = categories,
            onOpen = { selectedId = it.id }
        )
    } else {
        ChapterSentencesDetailScreen(category = selected!!)
    }
}

/* ------------------ A. 목록 ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterSentencesListScreen(
    categories: List<CategorySpec>,
    onOpen: (CategorySpec) -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("n교시 문장") }) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(cat) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(cat.title, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Button(onClick = { onOpen(cat) }) {
                        Text("보기", fontSize = 14.sp)
                    }
                }

            }
        }
    }
}

/* ------------------ B. 상세 ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterSentencesDetailScreen(
    category: CategorySpec
) {
    val ctx = LocalContext.current

    var showMeaning by remember { mutableStateOf(false) }
    val expandedMap = remember { mutableStateMapOf<Int, Boolean>() }
    var isShuffled by remember { mutableStateOf(false) }

    var fontSize by remember { mutableStateOf(22) }
    val minSize = 16
    val maxSize = 40
    val step = 2

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val r = tts?.setLanguage(Locale("si", "LK"))
                ttsReady = r != TextToSpeech.LANG_MISSING_DATA &&
                        r != TextToSpeech.LANG_NOT_SUPPORTED
                if (ttsReady) tts?.setSpeechRate(0.7f)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(showMeaning) {
        if (!showMeaning) expandedMap.clear()
    }

    val resId = remember(category.source) {
        ctx.resources.getIdentifier(category.source, "raw", ctx.packageName)
    }
    val result = remember(resId) { loadChapterFromRawSafe(ctx, resId) }

    val displayItems = remember(result, isShuffled) {
        result.getOrNull()?.items?.let {
            if (isShuffled) it.shuffled() else it
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(category.title) },
                navigationIcon = {
                    TextButton(onClick = { showMeaning = !showMeaning }) {
                        Text(if (showMeaning) "뜻 숨기기" else "뜻 보기")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { if (fontSize > minSize) fontSize -= step },
                        enabled = fontSize > minSize
                    ) { Icon(Icons.Filled.Remove, contentDescription = "-") }

                    IconButton(
                        onClick = { if (fontSize < maxSize) fontSize += step },
                        enabled = fontSize < maxSize
                    ) { Icon(Icons.Filled.Add, contentDescription = "+") }

                    IconButton(onClick = { isShuffled = !isShuffled }) {
                        Icon(Icons.Filled.Shuffle, contentDescription = "셔플")
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)   // ✅ 이게 핵심
        ) {
            items(displayItems, key = { it.id }) { item ->
                val expanded = expandedMap[item.id] == true

                tts?.let { ttsNonNull ->
                    SentencePanel(
                        item = item,
                        expanded = expanded,
                        onToggle = { expandedMap[item.id] = !expanded },
                        fontSize = fontSize,
                        showMeaning = showMeaning,
                        tts = ttsNonNull,
                        ttsReady = ttsReady
                    )
                }
            }
        }
    }
}

/* ------------------ util ------------------ */
private fun availableSentenceChaptersFromCollection(ctx: Context): List<CategorySpec> {
    val jsonText = ctx.resources
        .openRawResource(R.raw.sentence_collection)
        .bufferedReader()
        .use { it.readText() }

    val root = JSONObject(jsonText)
    val arr = root.getJSONArray("categories")

    val res = ctx.resources
    val pkg = ctx.packageName

    val out = ArrayList<CategorySpec>(arr.length())
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val source = o.getString("source")
        val exists = res.getIdentifier(source, "raw", pkg) != 0
        if (exists) {
            out.add(
                CategorySpec(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    source = source
                )
            )
        }
    }
    return out
}
