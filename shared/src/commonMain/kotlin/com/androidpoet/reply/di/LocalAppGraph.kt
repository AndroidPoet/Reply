package com.androidpoet.reply.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppGraph = staticCompositionLocalOf<AppGraph> { error("AppGraph not provided") }
