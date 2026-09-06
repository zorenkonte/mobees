package com.mobees.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mobees.app.ui.navigation.MobeesApp
import com.mobees.app.ui.theme.MobeesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MobeesTheme {
                MobeesApp()
            }
        }
    }
}
