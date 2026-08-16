package com.androidpoet.reply

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.di.AppGraph
import com.androidpoet.reply.di.LocalAppGraph
import com.androidpoet.reply.navigation.ReplyApp

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun App(
    appGraph: AppGraph,
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
) {
    var themeMode by rememberSaveable { mutableStateOf(initialThemeMode) }
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    LaunchedEffect(appGraph) { appGraph.repository.load() }
    CompositionLocalProvider(LocalAppGraph provides appGraph) {
        ReplyTheme(darkTheme = darkTheme) {
            ReplyApp(onThemeModeChange = { themeMode = it })
        }
    }
}
