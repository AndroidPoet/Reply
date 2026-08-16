package com.androidpoet.reply

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidpoet.reply.data.ThemeMode
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.di.AppGraph
import com.androidpoet.reply.di.LocalAppGraph
import com.androidpoet.reply.navigation.ReplyApp
import kotlinx.coroutines.launch

@Composable
fun App(
    appGraph: AppGraph,
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
) {
    val scope = rememberCoroutineScope()
    val themeMode by appGraph.settings.themeMode.collectAsStateWithLifecycle(initialValue = initialThemeMode)
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    LaunchedEffect(appGraph) { appGraph.repository.load() }
    CompositionLocalProvider(LocalAppGraph provides appGraph) {
        ReplyTheme(darkTheme = darkTheme) {
            ReplyApp(onThemeModeChange = { mode -> scope.launch { appGraph.settings.setThemeMode(mode) } })
        }
    }
}
