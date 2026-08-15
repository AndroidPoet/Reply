package com.androidpoet.reply

import androidx.compose.ui.window.ComposeUIViewController
import com.androidpoet.reply.di.buildAppGraph
import platform.UIKit.UIViewController

private val appGraph by lazy { buildAppGraph() }

fun MainViewController(): UIViewController = ComposeUIViewController {
    App(appGraph)
}
