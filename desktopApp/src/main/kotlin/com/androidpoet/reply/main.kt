package com.androidpoet.reply

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.androidpoet.reply.di.buildAppGraph

fun main() {
    val appGraph = buildAppGraph()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Reply",
            state = rememberWindowState(
                position = WindowPosition.Aligned(androidx.compose.ui.Alignment.Center),
                width = 412.dp,
                height = 892.dp,
            ),
        ) {
            App(appGraph)
        }
    }
}
