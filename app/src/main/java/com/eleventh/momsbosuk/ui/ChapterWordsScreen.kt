package com.eleventh.momsbosuk.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.eleventh.momsbosuk.ui.components.WordRow
import com.eleventh.momsbosuk.ui.components.WordRowHangul
import org.json.JSONObject
import androidx.activity.compose.BackHandler
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Subway
import kotlin.random.Random
import android.content.Intent
import android.speech.tts.TextToSpeech
import java.util.Locale


@Composable
fun ChapterWordsScreen(onExitApp: () -> Unit) {
    val ctx = LocalContext.current
    val categories = remember { availableWordChaptersFromCollection(ctx) }

    // 선택 전: null → 목록, 선택 후: 상세
    //var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var isHangulMode by rememberSaveable { mutableStateOf(false) }

    val selected: CategorySpec? = remember(selectedId, categories) {
        selectedId?.let { id -> categories.firstOrNull { it.id == id } }
    }

    // ✅ 시스템 뒤로: 상세면 목록으로, 목록이면 종료 다이얼로그
    BackHandler {
        if (selectedId != null) {
            showExitDialog = false
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
        ChapterWordsListScreen(
            categories = categories,
            onOpen = { selectedId = it.id }
        )
    } else {
        ChapterWordsDetailScreen(
            category = selected!!,
            isHangulMode = isHangulMode,
            onToggle = {isHangulMode = !isHangulMode }
        )
    }
}

/* ------------------ A. 목록(교시 단어) ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterWordsListScreen(
    categories: List<CategorySpec>,
    onOpen: (CategorySpec) -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("n교시 단어") }) }
    ) { inner ->
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "표시할 교시 단어가 없습니다.\n(words_collection.json / raw 파일 이름 확인)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
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
                        Button(
                            onClick = { onOpen(cat) },
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
                        ) { Text("보기", fontSize = 14.sp) }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

/* ------------------ B. 상세(단어 리스트) ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterWordsDetailScreen(
    category: CategorySpec,
    isHangulMode: Boolean,
    onToggle: () -> Unit
) {
    val ctx = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }

    // 뜻 보기 토글
    var showMeaning by remember { mutableStateOf(false) }
    val expandedMap = remember { mutableStateMapOf<Int, Boolean>() } // WordItem.id가 Int면 그대로 OK
    var isShuffled by remember { mutableStateOf(false) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val r = tts?.setLanguage(Locale("si", "LK"))
                ttsReady = (r != TextToSpeech.LANG_MISSING_DATA &&
                        r != TextToSpeech.LANG_NOT_SUPPORTED)
                if (ttsReady) tts?.setSpeechRate(0.7f)
            } else {
                ttsReady = false
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

    // 글자 크기
    var wordFontSize by remember { mutableStateOf(26) }
    val minSize = 16
    val maxSize = 48
    val step = 2

    // 1. 데이터를 로드합니다.
    val resId = remember(category.source) {
        ctx.resources.getIdentifier(category.source, "raw", ctx.packageName)
    }
    val result = remember(resId) { loadChapterFromRawSafe(ctx, resId) }

    // 2. 섞기 상태에 따라 리스트를 변환합니다.
    // remember(result, isShuffled)를 통해 데이터가 바뀌거나 셔플 버튼을 누를 때만 계산합니다.
    val displayItems = remember(result, isShuffled) {
        result.getOrNull()?.items?.let { items ->
            if (isShuffled) items.shuffled() else items
        } ?: emptyList()
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(category.title, maxLines = 1) },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            Log.d("ChapterWords", "한글로 버튼 클릭")
                            onToggle()
                        }) { Text(if (isHangulMode) "සිංහලෙන්" else "한글로"  ) }
                        TextButton(onClick = { showMeaning = !showMeaning }) {
                            Text(if (showMeaning) "뜻 숨기기" else "뜻 보기")
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
//                        TextButton(
//                            onClick = { if (wordFontSize > minSize) wordFontSize -= step },
//                            enabled = wordFontSize > minSize
//                        ) { Text("–", fontSize = 20.sp) }
//
//                        TextButton(
//                            onClick = { if (wordFontSize < maxSize) wordFontSize += step },
//                            enabled = wordFontSize < maxSize
//                        ) { Text("+", fontSize = 20.sp) }

                        IconButton(
                            onClick = { if (wordFontSize > minSize) wordFontSize -= step },
                            enabled = wordFontSize > minSize
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "-",
                                tint = MaterialTheme.colorScheme.tertiary

                            )
                        }

                        IconButton(
                            onClick = { if (wordFontSize < maxSize) wordFontSize += step },
                            enabled = wordFontSize < maxSize
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "+",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        IconButton(
                            onClick = {
                                isShuffled = !isShuffled
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "순서 섞기",
                                tint = if (isShuffled)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                }
            )
        }
    ) { inner ->
        if (resId == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "raw 파일을 찾을 수 없습니다:\n${category.source}.json",
                    color = MaterialTheme.colorScheme.error
                )
            }
            return@Scaffold
        }

        result.fold(
            onSuccess = { payload ->

                if (displayItems.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(inner),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        val installTts = {
                            val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(intent)
                        }

                        Log.d("ttsReady","ttsReady:" + ttsReady.toString())

                        items(displayItems, key = { it.id }) { item ->
                            val isExpanded = expandedMap[item.id] == true
                            tts?.let { ttsNonNull ->
                                if (isHangulMode) {
                                    WordRowHangul(
                                        item = item,
                                        expanded = isExpanded,
                                        onToggle = { expandedMap[item.id] = !isExpanded },
                                        wordFontSize = wordFontSize,
                                        showMeaning = showMeaning,
                                        tts = ttsNonNull,
                                        ttsReady = ttsReady
                                    )
                                } else {
                                    WordRow(
                                        item = item,
                                        expanded = isExpanded,
                                        onToggle = { expandedMap[item.id] = !isExpanded },
                                        wordFontSize = wordFontSize,
                                        showMeaning = showMeaning,
                                        tts = ttsNonNull,
                                        ttsReady = ttsReady
                                    )
                                }
                            }
                        }
                    }
                } else if (result.isFailure) {
                    // 실패 처리 로직...
                }


            },
            onFailure = { err ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "로딩 실패: ${err.message ?: "알 수 없는 오류"}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

/* ------------------ 유틸: words_collection.json 로드 + 존재하는 raw만 필터 ------------------ */
private fun availableWordChaptersFromCollection(ctx: Context): List<CategorySpec> {
    val jsonText = ctx.resources
        .openRawResource(R.raw.words_collection)
        .bufferedReader()
        .use { it.readText() }

    val root = JSONObject(jsonText)
    val arr = root.getJSONArray("categories")

    val res = ctx.resources
    val pkg = ctx.packageName

    val out = ArrayList<CategorySpec>(arr.length())
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val id = o.getString("id")
        val title = o.getString("title")
        val source = o.getString("source")

        // source에 해당하는 raw json이 실제 존재하는 것만 리스트업
        val exists = res.getIdentifier(source, "raw", pkg) != 0
        if (exists) out.add(CategorySpec(id = id, title = title, source = source))
    }
    return out
}
