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
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import io.legado.app.data.entities.Book
import io.legado.app.help.config.LocalConfig
import android.app.Activity

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
    val context = LocalContext.current
    var privacyAccepted by remember { mutableStateOf(LocalConfig.privacyPolicyOk) }
    if (!privacyAccepted) {
        JuyuePrivacyDialog(
            text = remember {
                context.assets.open("privacyPolicy.md").bufferedReader().use { it.readText() }
            },
            onAgree = {
                LocalConfig.privacyPolicyOk = true
                privacyAccepted = true
            },
            onRefuse = { (context as? Activity)?.finish() },
        )
        return
    }
    var unlocked by remember { mutableStateOf(LocalConfig.password.isNullOrBlank()) }
    if (!unlocked) {
        JuyueUnlockScreen(onUnlock = { unlocked = true })
        return
    }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var settingsSubPage by remember { mutableStateOf(false) }
    var detailBook by remember { mutableStateOf<Book?>(null) }
    detailBook?.let { book ->
        BookDetailScreen(book = book, onBack = { detailBook = null })
        return
    }
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!(selected == 2 && settingsSubPage)) {
                JuyueBottomBar(selected = selected, onSelect = { selected = it })
            }
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
                .offset(y = (-10).dp)
        ) {
            when (selected) {
                0 -> BookshelfScreen(onBookClick = { detailBook = it })
                1 -> CategoryScreen()
                else -> SettingsScreen(onSubPageChange = { settingsSubPage = it })
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun JuyuePrivacyDialog(
    text: String,
    onAgree: () -> Unit,
    onRefuse: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(2.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("用户隐私与协议") },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onAgree) { Text("同意") }
        },
        dismissButton = {
            TextButton(onClick = onRefuse) { Text("拒绝") }
        },
    )
}
