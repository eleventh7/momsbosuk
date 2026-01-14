package com.eleventh.momsbosuk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.border
import com.eleventh.momsbosuk.BuildConfig



@Composable
fun SplashScreen(
    onSuccess: () -> Unit = {}
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var successTriggered by remember { mutableStateOf(false) }

    // 4자리 맞으면 살짝 딜레이 후 메인으로
    if (successTriggered) {
        LaunchedEffect(true) {
            if (!BuildConfig.DEBUG) kotlinx.coroutines.delay(350)
            onSuccess()
        }
    }
    

    // 따뜻한 배경
    val bg = Brush.verticalGradient(
        listOf(Color(0xFFFFF7E0), Color(0xFFFFDDA6))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),               // 키보드 올라올 때도 안전
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 위쪽 여백(작게)
            Spacer(Modifier.weight(0.35f))

            Image(
                painter = painterResource(id = R.drawable.bosukmain),
                contentDescription = "Bosuk Gem",
                modifier = Modifier
                    .size(220.dp)
                    .padding(bottom = 24.dp)
            )

            PinInput(
                value = pin,
                length = 4,
                boxSize = 46.dp,
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() }.take(4)
                    pin = filtered
                    isError = false

                    if (filtered.length == 4) {
                        if (filtered == "5999") {
                            successTriggered = true
                        } else {
                            isError = true
                            pin = ""
                        }
                    }
                },
                isError = isError
            )

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(
                visible = !successTriggered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = if (isError) "잘못된 비밀번호입니다." else "Preparing your gem...",
                    color = if (isError) Color(0xFFB00020) else Color(0xFF7A6248),
                    fontSize = 15.sp
                )
            }

            // 아래쪽 여백(크게) → 전체 덩어리가 위로 올라감
            Spacer(Modifier.weight(0.65f))
        }

    }
}

/**
 * 네모 4칸에 ● 표시되는 PIN 입력 UI
 */
@Composable
private fun PinInput(
    value: String,
    length: Int,
    boxSize: Dp,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 숨겨진 입력 필드(실제 포커스는 여기), 박스 UI는 decoration으로 그림
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        textStyle = TextStyle(color = Color.Transparent), // 실제 텍스트는 안 보이게
        modifier = Modifier
            .width(boxSize * length + 12.dp * (length - 1))
            .focusRequester(focusRequester)
            .clickable { focusRequester.requestFocus() },
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(length) { index ->
                    val filled = index < value.length
                    val borderColor = when {
                        isError -> Color(0xFFB00020)
                        filled   -> Color(0xFFB8955E) // 골드 포인트
                        else     -> Color(0xFFCFB789) // 연한 베이지 라인
                    }
                    Box(
                        modifier = Modifier
                            .size(boxSize)
                            .shadow(1.dp, RoundedCornerShape(10.dp), clip = false)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFFBEA)) // 박스 안 배경(아주 옅은 크림)
                            .border(
                                width = 1.2.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filled) "•" else "",
                            fontSize = 24.sp,
                            color = Color(0xFF4E3E22),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )

    // 화면 아무데나 탭하면 포커스 되도록
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
