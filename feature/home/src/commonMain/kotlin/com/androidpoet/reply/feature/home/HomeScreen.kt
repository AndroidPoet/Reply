package com.androidpoet.reply.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.designsystem.component.MenuBottomSheet
import com.androidpoet.reply.designsystem.component.MenuSheetItem
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_archive
import com.androidpoet.reply.designsystem.resources.ic_delete
import com.androidpoet.reply.designsystem.resources.ic_forward
import com.androidpoet.reply.designsystem.resources.ic_reply
import com.androidpoet.reply.designsystem.resources.ic_reply_all
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection

/**
 * `HomeFragment`: the mailbox list. Cards open the email; long-press shows the email action
 * sheet; a rightward swipe stars/unstars.
 *
 * [scrollConnection] lets the app shell hide the bottom app bar on scroll (`hideOnScroll`).
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEmailClick: (email: Email, cardBoundsInRoot: Rect, topLeftCornerPx: Float) -> Unit,
    onReply: (Email) -> Unit,
    modifier: Modifier = Modifier,
    scrollConnection: NestedScrollConnection? = null,
    listState: LazyListState = rememberLazyListState(),
) {
    val emails by viewModel.emails.collectAsStateWithLifecycle()
    var menuEmail by remember { mutableStateOf<Email?>(null) }
    val layoutDirection = LocalLayoutDirection.current
    val statusBars = WindowInsets.statusBars.asPaddingValues()
    val navBars = WindowInsets.navigationBars.asPaddingValues()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(ReplyTheme.colors.background)
            .then(if (scrollConnection != null) Modifier.nestedScroll(scrollConnection) else Modifier),
        contentPadding = PaddingValues(
            start = statusBars.calculateStartPadding(layoutDirection),
            end = statusBars.calculateEndPadding(layoutDirection),
            top = statusBars.calculateTopPadding() + ReplyDimens.grid0_25,
            bottom = navBars.calculateBottomPadding() + ReplyDimens.bottomAppBarHeight,
        ),
    ) {
        items(emails, key = { it.id }) { email ->
            EmailListItem(
                email = email,
                onClick = onEmailClick,
                onLongClick = { menuEmail = it },
                onStarChanged = { e, starred -> viewModel.setStarred(e, starred) },
            )
        }
    }

    menuEmail?.let { email ->
        MenuBottomSheet(
            items = listOf(
                MenuSheetItem("forward", "Forward", Res.drawable.ic_forward),
                MenuSheetItem("reply", "Reply", Res.drawable.ic_reply),
                MenuSheetItem("reply_all", "Reply all", Res.drawable.ic_reply_all),
                MenuSheetItem("archive", "Archive", Res.drawable.ic_archive),
                MenuSheetItem("delete", "Delete", Res.drawable.ic_delete),
            ),
            onDismiss = { menuEmail = null },
            onItemClick = { item ->
                when (item.id) {
                    "reply", "reply_all", "forward" -> onReply(email)
                    "archive" -> viewModel.archive(email)
                    "delete" -> viewModel.delete(email)
                }
                menuEmail = null
            },
        )
    }
}
