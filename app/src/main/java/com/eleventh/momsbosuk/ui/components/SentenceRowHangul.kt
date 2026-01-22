package com.eleventh.momsbosuk.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.speech.tts.TextToSpeech
import com.eleventh.momsbosuk.data.WordItem

@Composable
fun SentenceRowHangul(
    item: WordItem,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: Int = 22,
    showMeaning: Boolean = false,
    tts: TextToSpeech,
    ttsReady: Boolean = false
) {
    val showDetail = showMeaning || expanded

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { if (!showMeaning) onToggle() }
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {

            // 1) 싱할라 문장 + 발음 버튼
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.meaning,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = if (showDetail) 10 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (showDetail) {
                    IconButton(
                        onClick = {
                            if (ttsReady) {
                                tts.speak(
                                    " ${item.sinhala} ",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "SENT_${item.id}"
                                )
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "문장 발음 듣기",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showDetail) {
                if (item.ipa.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "[${item.ipa}]",
                        fontSize = (fontSize - 4).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.sinhala,
                    fontSize = (fontSize - 2).sp
                )
            }
        }
    }
}
