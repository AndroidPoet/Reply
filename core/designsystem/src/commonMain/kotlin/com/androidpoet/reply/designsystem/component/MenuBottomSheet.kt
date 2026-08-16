@file:OptIn(ExperimentalMaterial3Api::class)

package com.androidpoet.reply.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated
import org.jetbrains.compose.resources.painterResource

@Composable
fun MenuBottomSheet(
    items: List<MenuSheetItem>,
    onDismiss: () -> Unit,
    onItemClick: (MenuSheetItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = state,
        shape = RoundedCornerShape(topStart = ReplyDimens.plane16 - 4.dp, topEnd = ReplyDimens.plane16 - 4.dp),
        containerColor = colors.elevated(colors.surface, ReplyDimens.plane16),
        contentColor = colors.onSurfaceHigh,
        scrimColor = colors.scrim,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Column(Modifier.padding(vertical = ReplyDimens.grid2)) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onItemClick(item) }
                        .padding(horizontal = ReplyDimens.grid2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.icon != null) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = null,
                            tint = colors.onSurfaceMedium,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(ReplyDimens.grid4))
                    }
                    ReplyText(
                        text = item.title,
                        style = ReplyTheme.typography.subtitle2,
                        color = colors.onSurfaceHigh,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
