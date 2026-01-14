package com.eleventh.momsbosuk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eleventh.momsbosuk.ui.theme.MomsBosukTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable

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
                    MomsBosukApp()
                }

            }
        }
    }
}
