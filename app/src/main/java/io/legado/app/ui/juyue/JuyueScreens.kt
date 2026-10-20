package io.legado.app.ui.juyue

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.legado.app.ui.about.AboutActivity
import io.legado.app.ui.book.source.manage.BookSourceActivity
import io.legado.app.ui.config.ConfigActivity
import io.legado.app.ui.config.ConfigTag

@Composable
fun BookshelfScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            JuyuePageHeader(
                kicker = "ARCHIVE / LIBRARY",
                title = "我的书架",
                subtitle = "档案库 · 本地书架 · 阅读进度",
                trailing = "SYS.READY",
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JuyueChip("全部", selected = true, onClick = {})
                JuyueChip("新建分组", selected = false, onClick = {})
            }
        }
        item {
            JuyueEmptyState(
                text = "书架还空着，先去搜索书籍或从分类里添加吧！",
                modifier = Modifier.fillMaxWidth(),
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
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
