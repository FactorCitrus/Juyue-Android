package io.legado.app.ui.juyue

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookChapter
import io.legado.app.help.book.BookHelp
import io.legado.app.help.book.isLocal
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.model.webBook.WebBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun JuyueReaderScreen(
    book: Book,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    var chapterIndex by rememberSaveable(book.bookUrl) {
        mutableIntStateOf(book.durChapterIndex.coerceAtLeast(0))
    }
    var chapters by remember(book.bookUrl) { mutableStateOf<List<BookChapter>>(emptyList()) }
    var chapter by remember(book.bookUrl) { mutableStateOf<BookChapter?>(null) }
    var content by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showCatalog by remember { mutableStateOf(false) }
    var textSize by remember { mutableIntStateOf(ReadBookConfig.durConfig.textSize) }
    val scroll = rememberScrollState()

    LaunchedEffect(book.bookUrl) {
        chapters = withContext(Dispatchers.IO) {
            appDb.bookChapterDao.getChapterList(book.bookUrl)
        }
    }

    LaunchedEffect(book.bookUrl, chapterIndex) {
        loading = true
        error = null
        content = ""
        val target = withContext(Dispatchers.IO) {
            appDb.bookChapterDao.getChapter(book.bookUrl, chapterIndex)
        }
        chapter = target
        if (target == null) {
            error = "没有找到第 ${chapterIndex + 1} 章"
            loading = false
            return@LaunchedEffect
        }
        runCatching {
            withContext(Dispatchers.IO) {
                if (book.isLocal) {
                    BookHelp.getContent(book, target).orEmpty()
                } else {
                    val source = appDb.bookSourceDao.getBookSource(book.origin)
                        ?: throw IllegalStateException("未找到书源：${book.origin}")
                    WebBook.getContentAwait(source, book, target)
                }
            }
        }.onSuccess { content = it }
            .onFailure { error = it.localizedMessage ?: it.toString() }
        loading = false
        withContext(Dispatchers.IO) {
            val updated = book.copy(
                durChapterIndex = chapterIndex,
                durChapterTitle = target.title,
                durChapterTime = System.currentTimeMillis(),
            )
            appDb.bookDao.update(updated)
        }
        scroll.scrollTo(0)
    }

    fun moveChapter(delta: Int) {
        val next = (chapterIndex + delta).coerceIn(0, (chapters.size - 1).coerceAtLeast(0))
        if (next != chapterIndex) chapterIndex = next
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures { showMenu = !showMenu }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .statusBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(top = 24.dp, bottom = 120.dp),
        ) {
            Text(
                text = chapter?.title ?: book.name,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = book.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 14.dp)
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            when {
                loading -> Text(
                    text = "正在加载正文…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                error != null -> Text(
                    text = error.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                else -> {
                    val paragraphs = content
                        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                        .replace(Regex("<[^>]+>"), "")
                        .split('\n')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    paragraphs.forEach { paragraph ->
                        Text(
                            text = "　　$paragraph",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = textSize.sp,
                                lineHeight = (textSize * 1.75f).sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }
        }

        if (showMenu) {
            ReaderTopBar(
                title = book.name,
                chapterTitle = chapter?.title.orEmpty(),
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            ReaderBottomBar(
                index = chapterIndex,
                total = chapters.size,
                textSize = textSize,
                onPrev = { moveChapter(-1) },
                onNext = { moveChapter(1) },
                onCatalog = { showCatalog = true },
                onTextSize = { delta ->
                    val next = (textSize + delta).coerceIn(12, 34)
                    textSize = next
                    ReadBookConfig.durConfig.textSize = next
                    ReadBookConfig.save()
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    if (showCatalog) {
        CatalogDialog(
            chapters = chapters,
            current = chapterIndex,
            onSelect = {
                chapterIndex = it
                showCatalog = false
                showMenu = false
            },
            onDismiss = { showCatalog = false },
        )
    }
}

@Composable
private fun ReaderTopBar(
    title: String,
    chapterTitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onBack),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ReaderBottomBar(
    index: Int,
    total: Int,
    textSize: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onCatalog: () -> Unit,
    onTextSize: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = "第 ${index + 1} / ${total.coerceAtLeast(index + 1)} 章",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReaderAction("上一章", onPrev)
            ReaderAction("下一章", onNext)
            ReaderAction("目录", onCatalog, icon = true)
            Spacer(Modifier.weight(1f))
            ReaderAction("A-", { onTextSize(-1) })
            Text(
                text = "$textSize",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ReaderAction("A+", { onTextSize(1) })
        }
    }
}

@Composable
private fun ReaderAction(
    text: String,
    onClick: () -> Unit,
    icon: Boolean = false,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon) {
            Icon(
                Icons.Outlined.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.size(3.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CatalogDialog(
    chapters: List<BookChapter>,
    current: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("目录 · ${chapters.size} 章") },
        text = {
            LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                itemsIndexed(chapters) { index, item ->
                    val active = index == current
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(vertical = 9.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
    )
}
