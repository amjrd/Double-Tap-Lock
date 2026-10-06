package com.example

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TapLockViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TapLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val openGuide = intent?.getBooleanExtra("OPEN_ACCESSIBILITY_GUIDE", false) ?: false
        if (openGuide) {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (_: Exception) { }
        }
    }
}
