package com.androidpoet.reply.di

import com.androidpoet.reply.data.AccountStore
import com.androidpoet.reply.data.EmailStore
import com.androidpoet.reply.feature.compose.ComposeViewModel
import com.androidpoet.reply.feature.email.EmailViewModel
import com.androidpoet.reply.feature.home.HomeViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

/**
 * Compile-time dependency graph (Metro). Everything is in-memory, so the graph has no external
 * inputs; the stores are `@SingleIn(AppScope)` and shared across screens.
 */
@DependencyGraph(AppScope::class)
interface AppGraph {
    val emailStore: EmailStore
    val accountStore: AccountStore

    val homeViewModelFactory: HomeViewModel.Factory
    val emailViewModelFactory: EmailViewModel.Factory
    val composeViewModelFactory: ComposeViewModel.Factory
}

fun buildAppGraph(): AppGraph = createGraph<AppGraph>()
