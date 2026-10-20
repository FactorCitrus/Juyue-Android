@file:OptIn(com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi::class)

package io.legado.app.ui.juyue

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.GlideImage
import io.legado.app.R
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookGroup
import io.legado.app.data.entities.BookSourcePart
import io.legado.app.ui.about.AboutActivity
import io.legado.app.ui.about.CrashLogsDialog
import io.legado.app.ui.book.explore.ExploreShowActivity
import io.legado.app.ui.book.search.SearchActivity
import io.legado.app.ui.book.source.manage.BookSourceActivity
import io.legado.app.ui.config.ConfigActivity
import io.legado.app.ui.config.ConfigTag
import io.legado.app.utils.startActivityForBook
import io.legado.app.help.AppWebDav
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BookshelfScreen(onBookClick: (Book) -> Unit = {}) {
    val context = LocalContext.current
    var compactGrid by rememberSaveable { mutableStateOf(true) }
    val books by remember {
        appDb.bookDao.flowByGroup(BookGroup.IdAll)
    }.collectAsState(initial = emptyList())
    val groups by remember {
        appDb.bookGroupDao.flowSelect()
    }.collectAsState(initial = emptyList())
    val readingCount = books.count {
        it.durChapterIndex > 0 || !it.durChapterTitle.isNullOrBlank()
    }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(if (compactGrid) 4 else 2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            BookshelfTopBar(
                onSearch = { SearchActivity.start(context, null) },
                onShare = {
                    val text = books.joinToString("\n") { "${it.name} · ${it.author}" }
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(share, "分享书架"))
                },
                onToggleGrid = { compactGrid = !compactGrid },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            JuyuePanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = androidx.compose.ui.graphics.Color(0xFFB33A2B),
                            size = Size(3.dp.toPx(), size.height),
                        )
                    }
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ARCHIVE / LIBRARY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "SYS.READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                text = "档案库",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "${books.size} BOOKS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "${groups.size} GROUPS · $readingCount IN PROGRESS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        JuyueRule(Modifier.padding(top = 8.dp))
                    }
                    Text(
                        text = books.size.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 80.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JuyueChip("全部", selected = true, onClick = {})
                groups.take(2).forEach { group ->
                    JuyueChip(group.groupName, selected = false, onClick = {})
                }
                JuyueChip(
                    text = "新建分组",
                    selected = false,
                    onClick = {},
                    icon = Icons.Outlined.CreateNewFolder,
                )
            }
        }
        if (books.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                JuyueEmptyState(
                    text = "书架还空着，先去搜索书籍或从分类里添加吧！",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            itemsIndexed(books) { index, book ->
                BookshelfBookCard(
                    book = book,
                    index = index,
                    onClick = { onBookClick(book) },
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(64.dp))
        }
    }
}

@Composable
private fun BookshelfTopBar(
    onSearch: () -> Unit,
    onShare: () -> Unit,
    onToggleGrid: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.juyue_shelf_mark),
            contentDescription = null,
            modifier = Modifier.height(64.dp),
        )
        Text(
            text = "我的书架",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "搜索",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(17.dp)
                    .clickable(onClick = onSearch),
            )
            Icon(
                Icons.Outlined.Share,
                contentDescription = "分享",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(17.dp)
                    .clickable(onClick = onShare),
            )
            Icon(
                Icons.Outlined.GridView,
                contentDescription = "书架布局",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(17.dp)
                    .clickable(onClick = onToggleGrid),
            )
        }
    }
}

@Composable
private fun BookshelfBookCard(book: Book, index: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(2.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(5.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.80f)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            if (book.coverUrl.isNullOrBlank()) {
                Column(Modifier.padding(10.dp)) {
                    Text(
                        text = "NO COVER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = book.name.take(2),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else {
                GlideImage(
                    model = book.coverUrl,
                    contentDescription = book.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            text = "ARCHIVE %02d".format(index + 1),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
            maxLines = 1,
        )
        Text(
            text = book.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (book.author.isNotBlank() && book.author != "未知作者") {
            Text(
                text = book.author,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        if (book.totalChapterNum > 0) {
            val read = min(book.durChapterIndex + 1, book.totalChapterNum)
            LinearProgressIndicator(
                progress = { read.toFloat() / book.totalChapterNum },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "$read/${book.totalChapterNum} CHAPT...",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun BookDetailScreen(
    book: Book,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onBack),
            )
            Text(
                text = "书籍详情",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            )
            Icon(
                Icons.Outlined.MoreVert,
                contentDescription = "更多",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                JuyuePanel(modifier = Modifier.fillMaxWidth()) {
                    Row {
                        Box(
                            modifier = Modifier
                                .size(width = 92.dp, height = 124.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            if (book.coverUrl.isNullOrBlank()) {
                                Column(Modifier.padding(8.dp)) {
                                    Text(
                                        "NO COVER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        book.name.take(2),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            } else {
                                GlideImage(
                                    model = book.coverUrl,
                                    contentDescription = book.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                        ) {
                            Text(
                                text = book.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = book.author.ifBlank { "未知作者" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                text = book.originName.ifBlank { book.origin.ifBlank { "本地书籍" } },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            if (book.totalChapterNum > 0) {
                                val read = min(book.durChapterIndex + 1, book.totalChapterNum)
                                LinearProgressIndicator(
                                    progress = { read.toFloat() / book.totalChapterNum },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .height(2.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.outline,
                                )
                                Text(
                                    text = "$read/${book.totalChapterNum} CHAPTERS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!book.kind.isNullOrBlank()) {
                        JuyueChip(book.kind.orEmpty(), selected = false, onClick = {})
                    }
                    if (!book.wordCount.isNullOrBlank()) {
                        JuyueChip("${book.wordCount} 字", selected = false, onClick = {})
                    }
                    JuyueChip("本地", selected = false, onClick = {})
                }
            }
            item {
                JuyuePanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "INTRO / 简介",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = book.getDisplayIntro().orEmpty().ifBlank { "暂无简介" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            item {
                JuyuePanel(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TOC / 目录",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${book.totalChapterNum} 章",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = book.durChapterTitle ?: book.latestChapterTitle ?: "暂无目录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DetailAction(
                text = "已在书架",
                primary = false,
                modifier = Modifier.weight(1f),
                onClick = {},
            )
            DetailAction(
                text = "开始阅读",
                primary = true,
                modifier = Modifier.weight(1f),
                onClick = { context.startActivityForBook(book) },
            )
        }
    }
}

@Composable
private fun DetailAction(
    text: String,
    primary: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(
                if (primary) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(2.dp),
            )
            .border(
                1.dp,
                if (primary) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(2.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun CategoryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sources by remember {
        appDb.bookSourceDao.flowEnabledExplore()
    }.collectAsState(initial = emptyList())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            JuyuePageHeader(
                kicker = "CATEGORY / SOURCES",
                title = "分类",
                subtitle = "书源 · 分类 · 筛选 · 下拉刷新",
                trailing = "SOURCE ${sources.size.toString().padStart(2, '0')}",
            )
        }
        if (sources.isEmpty()) {
            item {
                JuyueEmptyState(
                    text = "当前没有发现源！",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(sources, key = { it.bookSourceUrl }) { source ->
                CategorySourceCard(
                    source = source,
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val full = appDb.bookSourceDao.getBookSource(source.bookSourceUrl)
                            val exploreUrl = full?.exploreUrl
                            if (!exploreUrl.isNullOrBlank()) {
                                withContext(Dispatchers.Main) {
                                    context.startActivity(
                                        Intent(context, ExploreShowActivity::class.java)
                                            .putExtra("exploreName", source.bookSourceName)
                                            .putExtra("sourceUrl", source.bookSourceUrl)
                                            .putExtra("exploreUrl", exploreUrl)
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CategorySourceCard(source: BookSourcePart, onClick: () -> Unit) {
    JuyuePanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = source.bookSourceName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (source.hasExploreUrl) "DISCOVER" else "NO RULE",
                style = MaterialTheme.typography.labelSmall,
                color = if (source.hasExploreUrl) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = source.bookSourceGroup?.ifBlank { "未分组" } ?: "未分组",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val bookCount by remember {
        appDb.bookDao.flowAll()
    }.collectAsState(initial = emptyList())
    val sourceCount by remember {
        appDb.bookSourceDao.flowAll()
    }.collectAsState(initial = emptyList())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            JuyuePageHeader(
                kicker = "SETTINGS / 橘阅",
                title = "设置",
                subtitle = "本地设置 · 即时生效 · 05 MODULES · v4.7.10.4",
                trailing = "SYS.ONLINE",
            )
        }
        item {
            SettingsCard(
                index = "01",
                title = "阅读",
                description = "字号、行距、背景与净化",
                height = 112.dp,
                onClick = {
                    context.startActivity(
                        Intent(context, ConfigActivity::class.java)
                            .putExtra("configTag", ConfigTag.OTHER_CONFIG)
                    )
                },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsCard(
                    index = "02",
                    title = "外观",
                    description = "主题、图标与书架外观",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(
                            Intent(context, ConfigActivity::class.java)
                                .putExtra("configTag", ConfigTag.THEME_CONFIG)
                        )
                    },
                )
                SettingsCard(
                    index = "03",
                    title = "内容源",
                    description = "书源、OPDS、RSS 与订阅",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, BookSourceActivity::class.java))
                    },
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsCard(
                    index = "04",
                    title = "数据",
                    description = "云同步、缓存与下载",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(
                            Intent(context, ConfigActivity::class.java)
                                .putExtra("configTag", ConfigTag.BACKUP_CONFIG)
                        )
                    },
                )
                SettingsCard(
                    index = "05",
                    title = "系统",
                    description = "朗读、日志与关于",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    },
                )
            }
        }
        item {
            SettingsFooter(
                bookCount = bookCount.size,
                sourceCount = sourceCount.size,
                webDavConfigured = AppWebDav.isOk,
                onDiagnostics = {
                    if (context is FragmentActivity) {
                        CrashLogsDialog().show(context.supportFragmentManager, "CrashLogs")
                    }
                },
            )
        }
    }
}

@Composable
private fun SettingsFooter(
    bookCount: Int,
    sourceCount: Int,
    webDavConfigured: Boolean,
    onDiagnostics: () -> Unit,
) {
    JuyuePanel(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "END OF MODULES / 05",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
            Box(
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable(onClick = onDiagnostics),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "v4.7.10.4",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "书架 $bookCount 本 · 书源 $sourceCount 个 · WebDAV ${if (webDavConfigured) "已配置" else "未配置"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "诊断日志  ›",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun SettingsCard(
    index: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 112.dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Text(
            text = index,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
