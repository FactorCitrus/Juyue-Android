package io.legado.app.ui.juyue

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.legado.app.data.appDb
import io.legado.app.data.entities.RssArticle
import io.legado.app.data.entities.RssSource
import io.legado.app.model.rss.Rss
import io.legado.app.ui.book.source.manage.BookSourceActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ContentSourceScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showRss by remember { mutableStateOf(false) }
    var importing by remember { mutableStateOf(false) }
    val sources by remember { appDb.bookSourceDao.flowAll() }.collectAsState(initial = emptyList())
    val rssSources by remember { appDb.rssSourceDao.flowAll() }.collectAsState(initial = emptyList())
    if (showRss) {
        RssSourceListScreen(onBack = { showRss = false })
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SubPageTitle(title = "内容源", onBack = onBack) }
        item {
            JuyuePageHeader(
                kicker = "SOURCES / 03",
                title = "内容源",
                subtitle = "书源 ${sources.size} · RSS ${rssSources.size}",
                trailing = "LOCAL",
            )
        }
        item {
            JuyuePanel(modifier = Modifier.fillMaxWidth()) {
                SubPageRow(
                    title = "书源管理",
                    summary = "导入、编辑、调试与更新规则",
                    onClick = { context.startActivity(Intent(context, BookSourceActivity::class.java)) },
                )
                SubPageRow(
                    title = if (importing) "正在导入…" else "导入内置书源",
                    summary = "写入 assets/defaultData/bookSources.json",
                    onClick = {
                        if (importing) return@SubPageRow
                        importing = true
                        scope.launch {
                            withContext(Dispatchers.IO) { importBundledBookSources(context) }
                            importing = false
                        }
                    },
                )
                SubPageRow(
                    title = "RSS 订阅",
                    summary = "订阅源 ${rssSources.size} 个",
                    onClick = { showRss = true },
                    last = true,
                )
            }
        }
    }
}

@Composable
private fun RssSourceListScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var opened by remember { mutableStateOf<RssSource?>(null) }
    val sources by remember { appDb.rssSourceDao.flowAll() }.collectAsState(initial = emptyList())
    opened?.let { source ->
        RssArticlesScreen(source = source, onBack = { opened = null })
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SubPageTitle(title = "RSS 订阅", onBack = onBack) }
        item {
            JuyuePageHeader(
                kicker = "RSS / SUBSCRIBE",
                title = "订阅源",
                subtitle = "共 ${sources.size} 个订阅源",
                trailing = "${sources.size}".padStart(2, '0'),
            )
        }
        if (sources.isEmpty()) {
            item { JuyueEmptyState("还没有 RSS 订阅源。", Modifier.fillMaxWidth()) }
        } else {
            items(sources) { source ->
                JuyuePanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { opened = source },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = source.sourceName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = source.sourceGroup?.ifBlank { "未分组" } ?: "未分组",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "›",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RssArticlesScreen(source: RssSource, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var opened by remember { mutableStateOf<RssArticle?>(null) }
    val articles by remember(source.sourceUrl) {
        appDb.rssArticleDao.flowByOriginSort(source.sourceUrl, source.sortUrl ?: source.sourceUrl)
    }.collectAsState(initial = emptyList())

    fun refresh() {
        if (refreshing) return
        refreshing = true
        message = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    Rss.getArticlesAwait(
                        sortName = source.sourceName,
                        sortUrl = source.sortUrl ?: source.sourceUrl,
                        rssSource = source,
                        page = 1,
                    )
                }
            }.onSuccess { (list, _) ->
                withContext(Dispatchers.IO) {
                    appDb.rssArticleDao.insert(*list.toTypedArray())
                }
                message = "已更新 ${list.size} 条"
            }.onFailure { message = it.localizedMessage ?: it.toString() }
            refreshing = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SubPageTitle(title = source.sourceName, onBack = onBack) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ARTICLE / 文章",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                JuyueChip(
                    text = if (refreshing) "刷新中…" else "刷新",
                    selected = false,
                    onClick = { refresh() },
                )
            }
        }
        if (message != null) {
            item {
                Text(
                    text = message.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (articles.isEmpty()) {
            item { JuyueEmptyState("暂无缓存文章，点刷新拉取。", Modifier.fillMaxWidth()) }
        } else {
            items(articles) { article ->
                JuyuePanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { opened = article },
                ) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = article.pubDate ?: article.link,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }

    opened?.let { article ->
        RssArticleDialog(
            source = source,
            article = article,
            onDismiss = { opened = null },
        )
    }
}

@Composable
private fun RssArticleDialog(
    source: RssSource,
    article: RssArticle,
    onDismiss: () -> Unit,
) {
    var content by remember(article.link) { mutableStateOf(article.content.orEmpty()) }
    var loading by remember(article.link) { mutableStateOf(content.isBlank()) }
    LaunchedEffect(article.link) {
        if (content.isNotBlank()) return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                Rss.getContentAwait(article, source.ruleContent.orEmpty(), source)
            }
        }.onSuccess { content = it }.onFailure { content = it.localizedMessage ?: "加载失败" }
        loading = false
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = article.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .height(420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (loading) {
                    Text("加载正文…")
                } else {
                    Text(
                        text = content
                            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                            .replace(Regex("<[^>]+>"), ""),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
    )
}

@Composable
private fun SubPageTitle(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "返回",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onBack),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun SubPageRow(
    title: String,
    summary: String,
    onClick: () -> Unit,
    last: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = summary,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, end = 16.dp),
        )
        if (!last) {
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
    }
}
