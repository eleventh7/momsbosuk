package com.eleventh.momsbosuk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.eleventh.momsbosuk.ui.theme.MomsBosukTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MomsBosukTheme {
                val prefs = getSharedPreferences("momsbosuk", MODE_PRIVATE)
                var authorized by rememberSaveable { mutableStateOf(prefs.getBoolean("authorized", false)) }

                if (!authorized) {
                    SplashScreen(onSuccess = {
                        prefs.edit().putBoolean("authorized", true).apply()
                        authorized = true
                    })
                } else {
                    // ✅ 인증 성공 후, 앱 진입 전 구글 TTS 체크
                    CheckGoogleTtsAndRun(
                        onExitApp = {
                            prefs.edit().putBoolean("authorized", false).apply()
                            finishAffinity()
                        }
                    ) {
                        MomsBosukApp(
                            onExitApp = {
                                prefs.edit().putBoolean("authorized", false).apply()
                                finishAffinity()
                            }
                        )
                    }
                }

            }
        }
    }
}
@Composable
fun CheckGoogleTtsAndRun(onExitApp: () -> Unit, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val googleTtsPackage = "com.google.android.tts"

    // 설치 여부 확인
    val isInstalled = remember {
        try {
            ctx.packageManager.getPackageInfo(googleTtsPackage, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    if (!isInstalled) {
        // 설치 안 되어 있으면 스토어 이동 후 종료
        LaunchedEffect(Unit) {
            android.widget.Toast.makeText(ctx
                , "싱할라어 음성을 위해 구글 TTS 설치가 필요합니다."
                , android.widget.Toast.LENGTH_LONG
            ).show()

            val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$googleTtsPackage"))
            val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$googleTtsPackage"))

            try { ctx.startActivity(market) } catch (_: Exception) { ctx.startActivity(web) }

            onExitApp()
        }
    } else {
        // 설치 되어 있으면 앱 콘텐츠 표시
        content()
    }
}
