package io.legado.app.ui.juyue

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import io.legado.app.data.entities.Book

class JuyueMainActivity : FragmentActivity() {

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
    var detailBook by remember { mutableStateOf<Book?>(null) }
    detailBook?.let { book ->
        BookDetailScreen(book = book, onBack = { detailBook = null })
        return
    }
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f)
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
                .drawBehind {
                    val step = 28.dp.toPx()
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 0.5.dp.toPx())
                        x += step
                    }
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                        y += step
                    }
                }
                .padding(padding)
                .statusBarsPadding()
        ) {
            when (selected) {
                0 -> BookshelfScreen(onBookClick = { detailBook = it })
                1 -> CategoryScreen()
                else -> SettingsScreen()
            }
        }
    }
}
