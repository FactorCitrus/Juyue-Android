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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.legado.app.ui.about.AboutActivity
import io.legado.app.ui.about.CrashLogsDialog
import io.legado.app.ui.book.source.manage.BookSourceActivity
import io.legado.app.ui.config.ConfigActivity
import io.legado.app.ui.config.ConfigTag
import io.legado.app.help.config.LocalConfig

private val LockShape = RoundedCornerShape(2.dp)

@Composable
fun JuyueUnlockScreen(onUnlock: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        JuyuePanel(modifier = Modifier.padding(horizontal = 28.dp)) {
            Text(
                text = "LOCAL LOCK / 本地密码",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "解锁橘阅",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    error = false
                },
                singleLine = true,
                isError = error,
                placeholder = { Text("输入密码") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = LockShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            if (error) {
                Text(
                    text = "密码不正确",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clickable {
                        if (input == LocalConfig.password) onUnlock() else error = true
                    }
                    .background(MaterialTheme.colorScheme.primary, LockShape)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "解锁",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
fun SystemSettingsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var password by remember { mutableStateOf(LocalConfig.password) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    val privacyText = remember {
        context.assets.open("privacyPolicy.md").bufferedReader().use { it.readText() }
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
                    text = "系统",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
        item {
            JuyuePageHeader(
                kicker = "SYSTEM / 05",
                title = "系统",
                subtitle = "本地密码 · 备份同步 · 书源更新 · 诊断",
                trailing = "LOCAL",
            )
        }
        item {
            JuyuePanel(modifier = Modifier.fillMaxWidth()) {
                SystemRow(
                    title = "本地密码",
                    summary = if (password.isNullOrBlank()) "未设置" else "已开启",
                    onClick = { showPasswordDialog = true },
                )
                SystemRow(
                    title = "书源更新",
                    summary = "打开书源管理，导入或更新规则",
                    onClick = { context.startActivity(Intent(context, BookSourceActivity::class.java)) },
                )
                SystemRow(
                    title = "备份与同步",
                    summary = "WebDAV 备份、恢复与自动同步",
                    onClick = {
                        context.startActivity(
                            Intent(context, ConfigActivity::class.java)
                                .putExtra("configTag", ConfigTag.BACKUP_CONFIG)
                        )
                    },
                )
                SystemRow(
                    title = "隐私协议",
                    summary = "查看当前生效的用户协议",
                    onClick = { showPrivacy = true },
                )
                SystemRow(
                    title = "诊断日志",
                    summary = "查看崩溃与运行记录",
                    onClick = {
                        (context as? androidx.fragment.app.FragmentActivity)
                            ?.supportFragmentManager
                            ?.let { CrashLogsDialog().show(it, "CrashLogs") }
                    },
                )
                SystemRow(
                    title = "关于橘阅",
                    summary = "版本、开源许可与反馈",
                    onClick = { context.startActivity(Intent(context, AboutActivity::class.java)) },
                    last = true,
                )
            }
        }
    }
    if (showPasswordDialog) {
        PasswordDialog(
            current = password,
            onDismiss = { showPasswordDialog = false },
            onSave = {
                LocalConfig.password = it
                password = it
                showPasswordDialog = false
            },
        )
    }
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            shape = LockShape,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("用户隐私与协议") },
            text = { Text(privacyText) },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) { Text("关闭") }
            },
        )
    }
}

@Composable
private fun SystemRow(
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

@Composable
private fun PasswordDialog(
    current: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit,
) {
    var value by remember { mutableStateOf(current.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = LockShape,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("本地密码") },
        text = {
            Column {
                Text(
                    text = "留空表示关闭密码。设置后每次启动需要输入。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = LockShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(value.ifBlank { null }) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
