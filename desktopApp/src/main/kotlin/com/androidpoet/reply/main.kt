package com.androidpoet.reply

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.androidpoet.reply.database.buildReplyDatabase
import com.androidpoet.reply.datastore.createSettingsDataStore
import com.androidpoet.reply.database.replyDatabaseBuilder
import com.androidpoet.reply.di.buildAppGraph

fun main() {
    val appGraph = buildAppGraph(replyDatabaseBuilder().buildReplyDatabase(), createSettingsDataStore())
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Reply",
            icon = painterResource("reply_icon.png"),
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
