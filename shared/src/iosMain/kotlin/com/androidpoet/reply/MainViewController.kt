package com.androidpoet.reply

import androidx.compose.ui.window.ComposeUIViewController
import com.androidpoet.reply.database.buildReplyDatabase
import com.androidpoet.reply.datastore.createSettingsDataStore
import com.androidpoet.reply.database.replyDatabaseBuilder
import com.androidpoet.reply.di.buildAppGraph
import platform.UIKit.UIViewController

private val appGraph by lazy { buildAppGraph(replyDatabaseBuilder().buildReplyDatabase(), createSettingsDataStore()) }

@Suppress("FunctionNaming")
fun MainViewController(): UIViewController = ComposeUIViewController {
    App(appGraph)
}
