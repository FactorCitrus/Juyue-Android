package io.legado.app.ui.juyue

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.legado.app.constant.PreferKey
import io.legado.app.help.AppWebDav
import io.legado.app.help.storage.Backup
import io.legado.app.utils.getPrefString
import io.legado.app.utils.putPrefString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DataSettingsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var url by rememberSaveable { mutableStateOf(context.getPrefString(PreferKey.webDavUrl).orEmpty()) }
    var account by rememberSaveable {
        mutableStateOf(context.getPrefString(PreferKey.webDavAccount).orEmpty())
    }
    var password by rememberSaveable {
        mutableStateOf(context.getPrefString(PreferKey.webDavPassword).orEmpty())
    }
    var configured by remember { mutableStateOf(AppWebDav.isOk) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var backupNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var showRestore by remember { mutableStateOf(false) }

    fun saveConfig() {
        context.putPrefString(PreferKey.webDavUrl, url.trim())
        context.putPrefString(PreferKey.webDavAccount, account.trim())
        context.putPrefString(PreferKey.webDavPassword, password)
        configured = AppWebDav.isOk
        message = "已保存 WebDAV 配置"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
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
                    text = "数据",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
        item {
            JuyuePageHeader(
                kicker = "DATA / 04",
                title = "数据",
                subtitle = "WebDAV ${if (configured) "已配置" else "未配置"} · 备份与恢复",
                trailing = "SYNC",
            )
        }
        item {
            JuyuePanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "WEBDAV / 云同步",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    singleLine = true,
                    placeholder = { Text("https://dav.jianguoyun.com/dav/") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    singleLine = true,
                    placeholder = { Text("账号 / 邮箱") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    placeholder = { Text("应用密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    JuyueChip("保存配置", selected = false, onClick = { saveConfig() })
                    JuyueChip(
                        text = if (busy) "请稍候…" else "备份到 WebDAV",
                        selected = true,
                        onClick = {
                            if (busy) return@JuyueChip
                            saveConfig()
                            if (!configured) {
                                message = "请先填写 WebDAV 地址和账号"
                                return@JuyueChip
                            }
                            busy = true
                            message = "正在备份…"
                            scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        Backup.backupLocked(context, null)
                                    }
                                }.onSuccess { message = "备份完成" }
                                    .onFailure { message = "备份失败：${it.localizedMessage}" }
                                busy = false
                            }
                        },
                    )
                    JuyueChip(
                        text = "恢复",
                        selected = false,
                        onClick = {
                            if (busy) return@JuyueChip
                            busy = true
                            message = "读取云端备份…"
                            scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) { AppWebDav.getBackupNames() }
                                }.onSuccess {
                                    backupNames = it
                                    if (it.isEmpty()) {
                                        message = "云端没有备份"
                                    } else {
                                        showRestore = true
                                        message = null
                                    }
                                }.onFailure { message = "读取失败：${it.localizedMessage}" }
                                busy = false
                            }
                        },
                    )
                }
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    if (showRestore) {
        AlertDialog(
            onDismissRequest = { showRestore = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("选择备份") },
            text = {
                LazyColumn {
                    items(backupNames) { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showRestore = false
                                    busy = true
                                    message = "正在恢复…"
                                    scope.launch {
                                        runCatching {
                                            withContext(Dispatchers.IO) { AppWebDav.restoreWebDav(name) }
                                        }.onSuccess { message = "恢复完成，重启应用后生效" }
                                            .onFailure { message = "恢复失败：${it.localizedMessage}" }
                                        busy = false
                                    }
                                }
                                .padding(vertical = 10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRestore = false }) { Text("取消") }
            },
        )
    }
}
