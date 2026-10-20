package io.legado.app.ui.juyue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class JuyueMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JuyueTheme {
                JuyueApp()
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun JuyueApp() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            JuyueBottomBar(selected = selected, onSelect = { selected = it })
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .statusBarsPadding()
        ) {
            when (selected) {
                0 -> BookshelfScreen()
                1 -> CategoryScreen()
                else -> SettingsScreen()
            }
        }
    }
}
