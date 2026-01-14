package com.eleventh.momsbosuk.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ChapterSentencesScreen(onExitApp: () -> Unit) {
    // 나중에 sentences_ch{n}.json 스키마 정하면 여기서 동일 방식으로 로드
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("문장 탭 준비 중")
    }
}
