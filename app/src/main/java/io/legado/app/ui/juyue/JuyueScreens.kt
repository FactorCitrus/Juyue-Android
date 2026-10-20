@file:OptIn(com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi::class)

package io.legado.app.ui.juyue

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.GlideImage
import io.legado.app.R
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookGroup
import io.legado.app.ui.about.AboutActivity
import io.legado.app.ui.book.source.manage.BookSourceActivity
import io.legado.app.ui.config.ConfigActivity
import io.legado.app.ui.config.ConfigTag
import kotlin.math.min

@Composable
fun BookshelfScreen() {
    val books by remember {
        appDb.bookDao.flowByGroup(BookGroup.IdRoot)
    }.collectAsState(initial = emptyList())
    val groups by remember {
        appDb.bookGroupDao.flowSelect()
    }.collectAsState(initial = emptyList())
    val readingCount = books.count {
        it.durChapterIndex > 0 || !it.durChapterTitle.isNullOrBlank()
    }

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            BookshelfTopBar()
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            JuyuePanel(modifier = Modifier.fillMaxWidth()) {
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
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JuyueChip("全部", selected = true, onClick = {})
                groups.take(2).forEach { group ->
                    JuyueChip(group.groupName, selected = false, onClick = {})
                }
                JuyueChip("新建分组", selected = false, onClick = {})
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
                BookshelfBookCard(book = book, index = index)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(64.dp))
        }
    }
}

@Composable
private fun BookshelfTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.juyue_shelf_mark),
            contentDescription = null,
            modifier = Modifier.height(56.dp),
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
                modifier = Modifier.size(19.dp),
            )
            Icon(
                Icons.Outlined.Share,
                contentDescription = "分享",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp),
            )
            Icon(
                Icons.Outlined.GridView,
                contentDescription = "书架布局",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun BookshelfBookCard(book: Book, index: Int) {
    val shape = RoundedCornerShape(2.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            if (book.coverUrl.isNullOrBlank()) {
                Text(
                    text = book.name.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp),
                )
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
            maxLines = 1,
        )
        Text(
            text = book.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (book.author.isNotBlank() && book.author != "未知作者") {
            Text(
                text = book.author,
                style = MaterialTheme.typography.labelSmall,
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun CategoryScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            JuyuePageHeader(
                kicker = "CATEGORY / SOURCES",
                title = "分类",
                subtitle = "书源 · 分类 · 筛选 · 下拉刷新",
                trailing = "SOURCE 00",
            )
        }
        item {
            JuyueEmptyState(
                text = "当前没有发现源！",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
            Text(
                text = "END OF MODULES / 05",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
