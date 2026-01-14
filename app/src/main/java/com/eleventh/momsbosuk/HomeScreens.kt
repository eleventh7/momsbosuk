package com.eleventh.momsbosuk

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eleventh.momsbosuk.ui.WordsCollectionScreen
import com.eleventh.momsbosuk.ui.ChapterWordsScreen
import com.eleventh.momsbosuk.ui.ChapterSentencesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomsBosukApp(
    onExitApp: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.COLLECTIONS) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Moms Bosuk") }) }
    ) { inner ->
        Column(Modifier.padding(inner)) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                MainTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) }
                    )
                }
            }
            when (selectedTab) {
                MainTab.COLLECTIONS      -> WordsCollectionScreen(onExitApp = onExitApp)
                MainTab.PERIOD_WORDS     -> ChapterWordsScreen(onExitApp = onExitApp)
                MainTab.PERIOD_SENTENCES -> ChapterSentencesScreen(onExitApp = onExitApp)
            }
        }
    }
}

enum class MainTab(val label: String) {
    COLLECTIONS("단어 모음"),
    PERIOD_WORDS("n교시 단어"),
    PERIOD_SENTENCES("n교시 문장")
}
