package com.androidpoet.reply.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.SearchSuggestion
import com.androidpoet.reply.data.SearchSuggestionIcon
import com.androidpoet.reply.data.SearchSuggestionStore
import com.androidpoet.reply.designsystem.component.Avatar
import com.androidpoet.reply.designsystem.component.ReplyDivider
import com.androidpoet.reply.designsystem.component.ReplyIconButton
import com.androidpoet.reply.designsystem.component.ReplyText
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_arrow_back
import com.androidpoet.reply.designsystem.resources.ic_home
import com.androidpoet.reply.designsystem.resources.ic_mic
import com.androidpoet.reply.designsystem.resources.ic_schedule
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<Email>,
    onResultClick: (Email) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ReplyTheme.colors
    val typography = ReplyTheme.typography
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = ReplyDimens.grid1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReplyIconButton(
                icon = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = "Navigate back",
                onClick = onBack,
                tint = colors.onSurfaceHigh,
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(start = ReplyDimens.grid2),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    ReplyText(text = "Search email", style = typography.subtitle1, color = colors.onSurfaceMedium)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = typography.subtitle1.copy(color = colors.onSurfaceHigh),
                    cursorBrush = SolidColor(colors.secondary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus)
                        .focusable(),
                )
            }
            ReplyIconButton(
                icon = painterResource(Res.drawable.ic_mic),
                contentDescription = "Voice search",
                onClick = {},
                tint = colors.onSurfaceHigh,
                size = 56.dp,
            )
        }
        ReplyDivider(color = colors.onSurfaceStroke)

        if (query.isBlank()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                SuggestionTitle("YESTERDAY")
                SearchSuggestionStore.YESTERDAY_SUGGESTIONS.forEach { SuggestionItem(it) }
                SuggestionTitle("THIS WEEK")
                SearchSuggestionStore.THIS_WEEK_SUGGESTIONS.forEach { SuggestionItem(it) }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                if (results.isEmpty()) {
                    item { SuggestionTitle("NO RESULTS") }
                } else {
                    item { SuggestionTitle("${results.size} RESULTS") }
                    items(results, key = { it.id }) { email -> ResultItem(email, onClick = { onResultClick(email) }) }
                }
            }
        }
    }
}

@Composable
private fun ResultItem(email: Email, onClick: () -> Unit) {
    val colors = ReplyTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ReplyDimens.grid3, vertical = ReplyDimens.grid2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(image = email.sender.avatar, contentDescription = null, size = 40.dp)
        Column(Modifier.padding(start = ReplyDimens.grid2)) {
            ReplyText(text = email.subject, style = ReplyTheme.typography.body1, color = colors.onSurfaceHigh, maxLines = 1)
            ReplyText(
                text = email.senderPreview,
                style = ReplyTheme.typography.caption,
                color = colors.onSurfaceMedium,
                maxLines = 1,
                modifier = Modifier.padding(top = ReplyDimens.grid0_5),
            )
        }
    }
}

@Composable
private fun SuggestionTitle(title: String) {
    ReplyText(
        text = title,
        style = ReplyTheme.typography.subtitle2,
        color = ReplyTheme.colors.onSurfaceHigh,
        modifier = Modifier.padding(
            start = ReplyDimens.grid3,
            top = ReplyDimens.grid2,
            end = ReplyDimens.grid3,
            bottom = ReplyDimens.grid0_5,
        ),
    )
}

@Composable
private fun SuggestionItem(suggestion: SearchSuggestion) {
    val colors = ReplyTheme.colors
    val icon = when (suggestion.icon) {
        SearchSuggestionIcon.SCHEDULE -> Res.drawable.ic_schedule
        SearchSuggestionIcon.HOME -> Res.drawable.ic_home
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = ReplyDimens.grid3, vertical = ReplyDimens.grid2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.onSurfaceMedium,
            modifier = Modifier.size(24.dp),
        )
        Column(Modifier.padding(start = ReplyDimens.grid2)) {
            ReplyText(text = suggestion.title, style = ReplyTheme.typography.body1, color = colors.onSurfaceHigh)
            ReplyText(
                text = suggestion.subtitle,
                style = ReplyTheme.typography.caption,
                color = colors.onSurfaceHigh,
                modifier = Modifier.padding(top = ReplyDimens.grid0_5),
            )
        }
    }
}
