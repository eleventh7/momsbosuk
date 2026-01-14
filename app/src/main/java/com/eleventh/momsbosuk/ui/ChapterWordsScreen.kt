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
import android.app.Activity

@Composable
fun ChapterWordsScreen() {
    val ctx = LocalContext.current
    val activity = ctx as? Activity

    // 선택 전: null → 목록, 선택 후: 상세
    var selected by rememberSaveable { mutableStateOf<CategorySpec?>(null) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var isHangulMode by rememberSaveable { mutableStateOf(false) }

    // ✅ 상세 화면이면: 시스템 뒤로 -> 목록으로
    BackHandler(enabled = selected != null) {
        selected = null
    }

    // ✅ 목록 화면이면: 시스템 뒤로 -> 종료 확인
    BackHandler(enabled = selected == null) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("앱 종료") },
            text = { Text("종료하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    activity?.finish()
                }) { Text("종료") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("취소") }
            }
        )
    }

    if (selected == null) {
        ChapterWordsListScreen(
            categories = remember { availableWordChaptersFromCollection(ctx) },
            onOpen = { selected = it }
        )
    } else {
        ChapterWordsDetailScreen(
            category = selected!!,
            isHangulMode = isHangulMode,
            onToggle = {
                //Log.d("ChapterWords", "onToggle 호출됨 (아직 기능 없음)")
                isHangulMode = !isHangulMode
            }
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

    // 뜻 보기 토글
    var showMeaning by remember { mutableStateOf(false) }
    val expandedMap = remember { mutableStateMapOf<Int, Boolean>() } // WordItem.id가 Int면 그대로 OK

    LaunchedEffect(showMeaning) {
        if (!showMeaning) expandedMap.clear()
    }

    // 글자 크기
    var wordFontSize by remember { mutableStateOf(26) }
    val minSize = 16
    val maxSize = 48
    val step = 2

    val resId = remember(category.source) {
        ctx.resources.getIdentifier(category.source, "raw", ctx.packageName)
    }
    val result = remember(resId) { loadChapterFromRawSafe(ctx, resId) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(category.title) },
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
                    TextButton(
                        onClick = { if (wordFontSize > minSize) wordFontSize -= step },
                        enabled = wordFontSize > minSize
                    ) { Text("–", fontSize = 22.sp) }

                    TextButton(
                        onClick = { if (wordFontSize < maxSize) wordFontSize += step },
                        enabled = wordFontSize < maxSize
                    ) { Text("+", fontSize = 22.sp) }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(payload.items, key = { it.id }) { item ->
                        val isExpanded = expandedMap[item.id] == true
                        if (isHangulMode) {
                            WordRowHangul(
                                item = item,
                                expanded = isExpanded,
                                onToggle = { expandedMap[item.id] = !isExpanded },
                                wordFontSize = wordFontSize,
                                showMeaning = showMeaning
                            )
                        } else {
                            WordRow(
                                item = item,
                                expanded = isExpanded,
                                onToggle = { expandedMap[item.id] = !isExpanded },
                                wordFontSize = wordFontSize,
                                showMeaning = showMeaning
                            )
                        }
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
