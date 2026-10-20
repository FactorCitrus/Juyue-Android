package io.legado.app.ui.juyue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings

private val PanelShape = RoundedCornerShape(2.dp)

@Composable
fun JuyuePanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, PanelShape)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, PanelShape)
            .padding(14.dp),
        content = content,
    )
}

@Composable
fun JuyueRule(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(24.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        Box(
            Modifier
                .size(4.dp)
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
fun JuyuePageHeader(
    kicker: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    JuyuePanel(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = kicker,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        JuyueRule(Modifier.padding(top = 8.dp))
    }
}

@Composable
fun JuyueEmptyState(
    text: String,
    modifier: Modifier = Modifier,
) {
    JuyuePanel(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun JuyueChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = Modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                shape
            )
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 4.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun JuyueBottomBar(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(27.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.70f)
                .height(56.dp)
                .shadow(10.dp, shape)
                .background(MaterialTheme.colorScheme.surface, shape)
                .border(0.5.dp, MaterialTheme.colorScheme.outline, shape),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JuyueBottomItem(0, "书架", selected, onSelect)
            JuyueBottomItem(1, "分类", selected, onSelect)
            JuyueBottomItem(2, "设置", selected, onSelect)
        }
    }
}

@Composable
private fun RowScope.JuyueBottomItem(
    index: Int,
    label: String,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val active = selected == index
    val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onSelect(index) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = when (index) {
                0 -> Icons.AutoMirrored.Outlined.MenuBook
                1 -> Icons.Outlined.GridView
                else -> Icons.Outlined.Settings
            },
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

