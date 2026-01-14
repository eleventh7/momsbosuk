package com.eleventh.momsbosuk.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleventh.momsbosuk.data.WordItem

@Composable
fun WordRow(
    item: WordItem,
    expanded: Boolean,              // ✅ 부모가 관리
    onToggle: () -> Unit,           // ✅ 부모가 토글
    modifier: Modifier = Modifier,
    wordFontSize: Int = 26,
    showMeaning: Boolean = false
) {
    val context = LocalContext.current

    // ✅ 실제 표시 여부
    val showDetail = showMeaning || expanded

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                // 뜻 보기 모드에서는 개별 토글 막기(혼란 방지)
                if (!showMeaning) onToggle()
            }
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.sinhala,
                fontSize = wordFontSize.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            if (showDetail) {
                IconButton(
                    onClick = {
                        val keyword = when {
                            item.sinhala.isNotBlank() -> item.sinhala
                            item.ipa.isNotBlank() -> item.ipa
                            else -> item.meaning
                        }
                        val query = Uri.encode(keyword)
                        val url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=si&q=$query"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "발음 듣기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showDetail) {
            Spacer(Modifier.height(6.dp))

            if (item.ipa.isNotBlank()) {
                Text(
                    text = "[${item.ipa}]",
                    fontSize = (wordFontSize - 5).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = item.meaning,
                fontSize = (wordFontSize - 5).sp
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
