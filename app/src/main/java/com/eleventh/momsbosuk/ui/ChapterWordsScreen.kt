package com.eleventh.momsbosuk.ui

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleventh.momsbosuk.R
import com.eleventh.momsbosuk.data.loadChapterFromRaw
import com.eleventh.momsbosuk.data.loadChapterFromRawSafe
import com.eleventh.momsbosuk.ui.components.WordRow
import androidx.compose.material3.HorizontalDivider


@Composable
fun ChapterWordsScreen() {
    val ctx = LocalContext.current
    // 선택 전: null → 챕터 목록, 선택 후: 상세
    var selectedChapter by remember { mutableStateOf<Int?>(null) }

    if (selectedChapter == null) {
        ChapterListScreen(
            chapters = remember { availableWordChapters(ctx, max = 30) },
            onOpen = { selectedChapter = it }
        )
    } else {
        ChapterDetailScreen(
            chapter = selectedChapter!!,
            onBack = { selectedChapter = null }
        )
    }
}

/* ---------- A. 챕터 목록 ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterListScreen(
    chapters: List<Int>,
    onOpen: (Int) -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("n교시 단어") }) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(chapters) { ch ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(ch) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${ch} 교시", fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onOpen(ch) },
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

/* ---------- B. 챕터 상세(단어 리스트) ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterDetailScreen(
    chapter: Int,
    onBack: () -> Unit
) {


    var showMeaning by remember { mutableStateOf(false) }
    val expandedMap = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(showMeaning) {
        if (!showMeaning) expandedMap.clear()
    }


    val ctx = LocalContext.current
    val resId = remember(chapter) { resIdForChapter(chapter) }
    val result = remember(resId) { loadChapterFromRawSafe(ctx, resId) }

    //  글자 크기 상태 (단어 리스트에 적용)
    var wordFontSize by remember { mutableStateOf(26) }   // 기본 26sp
    val minSize = 16
    val maxSize = 48
    val step = 2

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${chapter} 교시") },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onBack) { Text("뒤로") }

                        TextButton(onClick = { showMeaning = !showMeaning }) {
                            Text(if (showMeaning) "뜻 숨기기" else "뜻 보기")
                        }
                    }
                },
                actions = {

                    //  - 버튼
                    TextButton(
                        onClick = { if (wordFontSize > minSize) wordFontSize -= step },
                        enabled = wordFontSize > minSize
                    ) { Text("–", fontSize = 22.sp) }

                    //  + 버튼
                    TextButton(
                        onClick = { if (wordFontSize < maxSize) wordFontSize += step },
                        enabled = wordFontSize < maxSize
                    ) { Text("+", fontSize = 22.sp) }
                }
            )
        }
    ) { inner ->
        result.fold(
            onSuccess = { payload ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(payload.items, key = { it.id }) { item ->
                        val isExpanded = expandedMap[item.id] == true

                        WordRow(
                            item = item,
                            expanded = isExpanded,
                            onToggle = { expandedMap[item.id] = !isExpanded },
                            wordFontSize = wordFontSize,
                            showMeaning = showMeaning
                        )
                    }
                }
            },
            onFailure = { err ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentAlignment = Alignment.Center
                ) {
                    Text("로딩 실패: ${err.message ?: "알 수 없는 오류"}",
                        color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

/* ---------- 유틸 ---------- */
private fun availableWordChapters(ctx: Context, max: Int): List<Int> {
    val res = ctx.resources
    val pkg = ctx.packageName
    // res/raw/words_ch{n}.json 존재하는 것만 리스트업
    return (1..max).filter { n ->
        res.getIdentifier("words_ch$n", "raw", pkg) != 0
    }
}

@RawRes
private fun resIdForChapter(ch: Int): Int = when (ch) {
    1 -> R.raw.words_ch1
    2 -> R.raw.words_ch2
    else -> R.raw.words_ch1
}
